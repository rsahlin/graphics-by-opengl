package com.nucleus.renderer;

import java.util.ArrayDeque;

import com.nucleus.common.Environment;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.scene.GLTFNode;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Buffer;
import com.nucleus.scene.gltf.BufferView;
import com.nucleus.scene.gltf.Camera;
import com.nucleus.scene.gltf.Camera.Perspective;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Material;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.scene.gltf.Node;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.scene.gltf.Scene;
import com.nucleus.scene.gltf.Texture;
import com.nucleus.shader.GLTFShaderProgram;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.TextureUtils;
import com.nucleus.vecmath.Matrix;

public class GLTFNodeRenderer implements NodeRenderer<GLTFNode> {

    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    private Pass currentPass;
    protected ArrayDeque<float[]> modelMatrixStack = new ArrayDeque<float[]>(10);
    protected ArrayDeque<float[]> viewMatrixStack = new ArrayDeque<float[]>(10);
    protected ArrayDeque<float[]> projectionMatrixStack = new ArrayDeque<float[]>(10);
    protected float[] modelMatrix;

    /**
     * Internal method to handle matrix stack, push a matrix on the stack
     * 
     * @param stack The stack to push onto
     * @param matrix
     */
    protected void pushMatrix(ArrayDeque<float[]> stack, float[] matrix) {
        stack.push(matrix);
    }

    /**
     * Internal method to handle matrix stack - pops the latest matrix off the stack
     * 
     * @param stack The stack to pop from
     * @return The poped matrix
     */
    protected float[] popMatrix(ArrayDeque<float[]> stack) {
        return stack.pop();
    }

    @Override
    public boolean renderNode(NucleusRenderer renderer, GLTFNode node, Pass currentPass, float[][] matrices)
            throws GLException {
        this.currentPass = currentPass;
        // pushMatrix(viewMatrixStack, matrices[Matrices.VIEW.index]);
        // pushMatrix(projectionMatrixStack, matrices[Matrices.PROJECTION.index]);

        GLES20Wrapper gles = renderer.getGLES();
        GLTF glTF = node.getGLTF();
        Scene scene = glTF.getDefaultScene();
        if (!scene.isCameraDefined()) {
            // Setup a default projection if none is specified in model - this is to get the right axes and winding.
            Perspective p = new Perspective(1.5f, 0.66f, 10000, 1);
            matrices[Matrices.PROJECTION.index] = p.calculateMatrix();
        }
        // Render the default scene.
        renderScene(gles, glTF, scene, currentPass, matrices);
        // matrices[Matrices.VIEW.index] = popMatrix(viewMatrixStack);
        // matrices[Matrices.PROJECTION.index] = popMatrix(projectionMatrixStack);
        return true;
    }

    protected void renderScene(GLES20Wrapper gles, GLTF glTF, Scene scene, Pass currentPass, float[][] matrices)
            throws GLException {
        // Traverse the nodes and render each.
        Node[] sceneNodes = scene.getNodes();
        if (sceneNodes != null) {
            for (int i = 0; i < sceneNodes.length; i++) {
                renderNode(gles, glTF, sceneNodes[i], matrices);
            }
        }
    }

    protected void setCamera(Node cameraNode, float[][] matrices) {
        Camera camera = cameraNode.getCamera();
        if (camera != null) {
            matrices[Matrices.VIEW.index] = cameraNode.getMatrix();
            Matrix.copy(camera.getProjectionMatrix(), 0, matrices[Matrices.PROJECTION.index], 0);
        }

    }

    /**
     * Renders the node and then childnodes by calling {@link #renderNodes(GLTF, Node[])}
     * This will render the Node using depth first search
     * 
     * @param gles
     * @param glTF
     * @param node
     * @param matrices
     */
    protected void renderNode(GLES20Wrapper gles, GLTF glTF, Node node, float[][] matrices)
            throws GLException {
        // Check for camera
        setCamera(node, matrices);
        pushMatrix(modelMatrixStack, matrices[Matrices.MODEL.index]);
        float[] nodeMatrix = node.concatMatrix(matrices[Matrices.MODEL.index]);
        matrices[Matrices.MODEL.index] = nodeMatrix;
        renderMesh(gles, glTF, node.getMesh(), matrices);
        // Render children.
        renderNodes(gles, glTF, node.getChildren(), matrices);
        matrices[Matrices.MODEL.index] = popMatrix(modelMatrixStack);
    }

    protected void renderMesh(GLES20Wrapper gles, GLTF glTF, Mesh mesh, float[][] matrices) throws GLException {
        if (mesh != null) {
            Primitive[] primitives = mesh.getPrimitives();
            if (primitives != null) {
                for (Primitive p : primitives) {
                    renderPrimitive(gles, glTF, p, matrices);
                }
            }
        }
    }

    /**
     * Renders the primitive
     * 
     * @param gles
     * @param glTF
     * @param program
     * @param primitive
     * @param matrices
     * @throws GLException
     */
    protected void renderPrimitive(GLES20Wrapper gles, GLTF glTF, Primitive primitive, float[][] matrices)
            throws GLException {
        GLTFShaderProgram program = (GLTFShaderProgram) getProgram(gles, primitive, currentPass);
        gles.glUseProgram(program.getProgram());
        GLUtils.handleError(gles, "glUseProgram " + program.getProgram());
        // TODO - is this the best place for this check - remember, this should only be done in debug cases.
        if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
            program.validateProgram(gles);
        }
        program.updateUniforms(gles, matrices);
        Material material = primitive.getMaterial();
        if (material != null) {
            Texture texture = glTF.getTexture(material.getPbrMetallicRoughness());
            if (texture != null) {
                TextureUtils.prepareTexture(gles, texture, glTF.getTexCoord(material.getPbrMetallicRoughness()));
            }
        }
        Accessor indices = glTF.getAccessor(primitive.getIndicesIndex());
        program.updatePrimitiveUniforms(gles, primitive);
        if (indices != null) {
            // Indexed mode - use glDrawElements
            BufferView indicesView = indices.getBufferView();
            Buffer buffer = indicesView.getBuffer();
            gles.glVertexAttribPointer(glTF, program, primitive);
            if (buffer.getBufferName() > 0) {
                gles.glBindBuffer(indicesView.getTarget().value, buffer.getBufferName());
                gles.glDrawElements(primitive.glMode, indices.getCount(), indices.getComponentType().value,
                        indices.getByteOffset() + indicesView.getByteOffset());
            } else {
                gles.glDrawElements(primitive.glMode, indices.getCount(), indices.getComponentType().value,
                        indicesView.getBuffer().getBuffer()
                                .position(indices.getByteOffset() + indicesView.getByteOffset()));
            }
            timeKeeper.addDrawElements(indices.getCount(), primitive.getAccessor(Attributes.POSITION).getCount());
        } else {
            // Non indexed mode - use glDrawArrays
            throw new IllegalArgumentException("Not implemented yet");
        }
    }

    /**
     * Renders an array of nodes - each node will be rendered by calling {@link #renderNode(GLTF, Node)}
     * This means rendering will be depth first.
     * 
     * @param gles
     * @param glTF
     * @param children
     * @param matrices
     */
    protected void renderNodes(GLES20Wrapper gles, GLTF glTF, Node[] children,
            float[][] matrices) throws GLException {
        if (children != null && children.length > 0) {
            for (Node n : children) {
                renderNode(gles, glTF, n, matrices);
            }
        }
    }

    /**
     * 
     * @param gles
     * @param primitive
     * @param pass The currently defined pass
     * @return
     */
    protected ShaderProgram getProgram(GLES20Wrapper gles, Primitive primitive, Pass pass) {
        ShaderProgram program = primitive.getProgram();
        if (program == null) {
            throw new IllegalArgumentException("No program for primitive ");
        }
        return program.getProgram(gles, pass, program.getShading());
    }

}
