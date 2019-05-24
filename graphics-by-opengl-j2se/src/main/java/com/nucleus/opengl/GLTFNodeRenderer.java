package com.nucleus.opengl;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;

import com.nucleus.assets.AssetManager;
import com.nucleus.common.Environment;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.shader.GLTFShaderProgram;
import com.nucleus.opengl.shader.ShaderProgram;
import com.nucleus.opengl.shader.ShaderVariable;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.Backend.DrawMode;
import com.nucleus.renderer.Configuration;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderBackendException;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderState.Cullface;
import com.nucleus.scene.GLTFNode;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Buffer;
import com.nucleus.scene.gltf.BufferView;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Material;
import com.nucleus.scene.gltf.Material.AlphaMode;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.scene.gltf.Node;
import com.nucleus.scene.gltf.PBRMetallicRoughness;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.scene.gltf.Scene;
import com.nucleus.vecmath.Matrix.MatrixStack;

public class GLTFNodeRenderer implements NodeRenderer<GLTFNode> {

    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    private Pass currentPass;
    protected float[] modelMatrix;
    protected int currentProgram = -1;
    protected RenderState renderState;
    protected Cullface cullFace;
    protected DrawMode forceMode = null;
    protected MatrixStack modelStack = new MatrixStack(100);
    protected MatrixStack viewStack = new MatrixStack(5);
    protected MatrixStack projectionStack = new MatrixStack(5);

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
    public void forceRenderMode(DrawMode mode) {
        forceMode = mode != null ? mode : null;
    }

    @Override
    public boolean renderNode(NucleusRenderer renderer, GLTFNode node, Pass currentPass, float[][] matrices)
            throws RenderBackendException {
        GLTF glTF = node.getGLTF();
        Scene scene = glTF.getDefaultScene();
        if (glTF == null) {
            // Do nothing
            return false;
        }
        forceMode = Configuration.getInstance().getGLTFMode();
        renderState = renderer.getRenderState();
        modelStack.push(matrices[Matrices.MODEL.index], 0);
        viewStack.push(matrices[Matrices.VIEW.index], 0);
        projectionStack.push(matrices[Matrices.PROJECTION.index], 0);

        currentProgram = -1;
        this.currentPass = currentPass;
        // Set view matrix from previous render of this gltfNode
        // node.getSavedViewMatrix(matrices[Matrices.VIEW.index]);
        scene.setMVP(matrices);
        // This will rotate the view - ie the camera
        // matrices[Matrices.VIEW.index] = scene.getSceneTransform().concatMatrix(matrices[Matrices.VIEW.index]);
        scene.getSceneTransform().concatMatrix(matrices[Matrices.MODEL.index], 0);
        // Render the default scene.
        renderScene(renderer, glTF, scene, currentPass, matrices);

        modelStack.pop(matrices[Matrices.MODEL.index], 0);
        viewStack.pop(matrices[Matrices.VIEW.index], 0);
        projectionStack.pop(matrices[Matrices.PROJECTION.index], 0);

        return true;
    }

    protected void renderScene(NucleusRenderer renderer, GLTF glTF, Scene scene, Pass currentPass, float[][] matrices)
            throws RenderBackendException {
        // Traverse the nodes and render each.
        Node[] sceneNodes = scene.getNodes();
        if (sceneNodes != null) {
            for (int i = 0; i < sceneNodes.length; i++) {
                renderNode(renderer, glTF, sceneNodes[i], matrices);
            }
        }
    }

    /**
     * Renders the Mesh in this node, then renders childnodes.
     * This will render the Node using depth first search
     * 
     * @param renderer
     * @param glTF
     * @param node
     * @param matrices
     */
    protected void renderNode(NucleusRenderer renderer, GLTF glTF, Node node, float[][] matrices)
            throws RenderBackendException {
        modelStack.push(matrices[Matrices.MODEL.index], 0);
        node.concatMatrix(matrices[Matrices.MODEL.index], 0);
        renderMesh(renderer, glTF, node.getMesh(), matrices);
        renderDebugMesh(renderer, glTF, node.getMesh(), matrices);

        // Render children.
        renderNodes(renderer, glTF, node.getChildren(), matrices);
        modelStack.pop(matrices[Matrices.MODEL.index], 0);
    }

    /**
     * Renders the mesh using the specified MVP matrices, this will render each primitive.
     * 
     * @param renderer
     * @param glTF
     * @param mesh
     * @param matrices
     * @throws RenderBackendException
     */
    protected void renderMesh(NucleusRenderer renderer, GLTF glTF, Mesh mesh, float[][] matrices)
            throws RenderBackendException {
        if (mesh != null) {
            Primitive[] primitives = mesh.getPrimitives();
            if (primitives != null) {
                for (Primitive p : primitives) {
                    renderPrimitive(renderer, glTF, p, matrices);
                }
            }
        }
    }

    /**
     * Used to render debug info for the Mesh - if {@value GLTF#debugTBN} is true then the TBN debug primitives are
     * drawn.
     * 
     * @param renderer
     * @param glTF
     * @param mesh
     * @param matrices
     * @throws GLException
     */
    protected void renderDebugMesh(NucleusRenderer renderer, GLTF glTF, Mesh mesh, float[][] matrices)
            throws GLException {
        if (GLTF.debugTBN) {
            debugTBN(renderer, glTF, mesh, matrices);
        }
    }

    /**
     * Renders the primitive
     * 
     * @param renderer
     * @param glTF
     * @param program
     * @param primitive
     * @param matrices
     * @throws RenderBackendException
     */
    protected void renderPrimitive(NucleusRenderer renderer, GLTF glTF, Primitive primitive, float[][] matrices)
            throws RenderBackendException {
        GLES20Wrapper gles = renderer.getGLES();
        GLTFShaderProgram program = (GLTFShaderProgram) getProgram(renderer, primitive, currentPass);
        if (currentProgram != program.getProgram()) {
            currentProgram = program.getProgram();
            gles.glUseProgram(currentProgram);
            GLUtils.handleError(gles, "glUseProgram " + currentProgram);
            // TODO - is this the best place for this check - remember, this should only be done in debug cases.
            if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
                program.validateProgram(gles);
            }
            program.updateEnvironmentUniforms(gles, glTF.getDefaultScene());
        }
        // Can be optimized to update uniforms under the following conditions:
        // The program has changed OR the matrices have changed, ie another parent node.
        program.setUniformMatrices(matrices);
        program.updateUniformData(program.getUniformData());
        program.uploadUniforms(gles);
        Material material = primitive.getMaterial();
        if (material != null) {
            PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
            // Check for doublesided.
            if (material.isDoubleSided() && renderState.getCullFace() != Cullface.NONE) {
                cullFace = renderState.getCullFace();
                gles.glDisable(GLES20.GL_CULL_FACE);
            }
            program.prepareTextures(renderer, glTF, primitive, material);
            if (material.getAlphaMode() == AlphaMode.OPAQUE) {
                gles.glDisable(GLES20.GL_BLEND);
            } else {
                gles.glEnable(GLES20.GL_BLEND);
            }
        }
        Accessor indices = primitive.getIndices();
        Accessor position = primitive.getAccessor(Attributes.POSITION);
        program.updatePBRUniforms(gles, primitive);
        drawVertices(gles, program, indices, position.getCount(), primitive.getAttributesArray(),
                primitive.getAccessorArray(), forceMode == null ? primitive.getMode() : forceMode);
        // Restore cullface if changed.
        if (cullFace != null) {
            gles.glEnable(GLES20.GL_CULL_FACE);
            cullFace = null;
        }
        gles.disableAttribPointers();
    }

    /**
     * Sets attrib pointers and draws indices or arrays - uniforms must be uploaded to GL before calling this method.
     * 
     * @param gles
     * @param program
     * @param indices
     * @param vertexCount
     * @param attribs
     * @param accessors
     * @param mode
     * @throws GLException
     */
    protected final void drawVertices(GLES20Wrapper gles, ShaderProgram program, Accessor indices, int vertexCount,
            ArrayList<Attributes> attribs, ArrayList<Accessor> accessors, DrawMode mode) throws GLException {
        gles.glVertexAttribPointer(program, attribs, accessors);
        GLUtils.handleError(gles, "glVertexAttribPointer");
        int modeValue = gles.getDrawMode(mode);
        if (indices != null) {
            // Indexed mode - use glDrawElements
            BufferView indicesView = indices.getBufferView();
            Buffer buffer = indicesView.getBuffer();
            if (buffer.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffer.getBufferName());
                GLUtils.handleError(gles, "glBindBuffer");
                gles.glDrawElements(modeValue, indices.getCount(), indices.getComponentType().value,
                        indices.getByteOffset() + indicesView.getByteOffset());
                GLUtils.handleError(gles, "glDrawElements VBO " + buffer.getBufferName());
            } else {
                gles.glDrawElements(modeValue, indices.getCount(), indices.getComponentType().value,
                        indices.getBuffer());
                GLUtils.handleError(gles, "glDrawElements");
            }
            timeKeeper.addDrawElements(indices.getCount(), vertexCount);
        } else {
            // Non indexed mode - use glDrawArrays
            gles.glDrawArrays(modeValue, 0, vertexCount);
            GLUtils.handleError(gles, "glDrawArrays VBO");
            timeKeeper.addDrawArrays(vertexCount);
        }

    }

    private void debugTBN(NucleusRenderer renderer, GLTF gltf, Mesh mesh, float[][] matrices) throws GLException {
        if (mesh != null) {
            GLES20Wrapper gles = renderer.getGLES();
            Primitive[] primitives = mesh.getDebugTBNPrimitives();
            if (primitives == null) {
                primitives = mesh.createDebugTBNPrimitives(gles, mesh.getPrimitives());
            }
            ShaderProgram debugProgram = AssetManager.getInstance().getProgram(gles, mesh.getDebugTBNProgram());
            gles.glUseProgram(debugProgram.getProgram());
            // Set uniforms.
            ShaderVariable var = debugProgram.getUniformByName(Attributes._EMISSIVE.name());
            FloatBuffer uniformData = debugProgram.getUniformData();
            if (var != null && uniformData != null) {
                uniformData.position(var.getOffset());
                float[] debugColors = new float[] { 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1 };
                uniformData.put(debugColors);
            }
            debugProgram.uploadUniform(gles, debugProgram.getUniformData(),
                    debugProgram.getUniformByName(Attributes._EMISSIVE.name()));

            gles.glUseProgram(debugProgram.getProgram());
            debugProgram.setUniformMatrices(matrices);
            debugProgram.updateUniformData(debugProgram.getUniformData());
            debugProgram.uploadUniforms(gles);
            for (Primitive p : primitives) {
                gles.disableAttribPointers();
                Accessor position = p.getAccessor(Attributes.POSITION);
                drawVertices(gles, debugProgram, null, position.getCount(),
                        p.getAttributesArray(), p.getAccessorArray(), DrawMode.POINTS);
                gles.glUseProgram(currentProgram);
            }
        }

    }

    /**
     * Renders an array of nodes - each node will be rendered by calling {@link #renderNode(GLTF, Node)}
     * This means rendering will be depth first.
     * 
     * @param renderer
     * @param glTF
     * @param children
     * @param matrices
     */
    protected void renderNodes(NucleusRenderer renderer, GLTF glTF, Node[] children,
            float[][] matrices) throws RenderBackendException {
        if (children != null && children.length > 0) {
            for (Node n : children) {
                renderNode(renderer, glTF, n, matrices);
            }
        }
    }

    /**
     * 
     * @param renderer
     * @param primitive
     * @param pass The currently defined pass
     * @return
     */
    protected ShaderProgram getProgram(NucleusRenderer renderer, Primitive primitive, Pass pass) {
        ShaderProgram program = primitive.getProgram();
        if (program == null) {
            throw new IllegalArgumentException("No program for primitive ");
        }
        return program.getProgram(renderer.getGLES(), pass, program.getShading());
    }

}
