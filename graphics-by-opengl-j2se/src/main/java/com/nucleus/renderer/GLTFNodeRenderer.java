package com.nucleus.renderer;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;

import com.nucleus.assets.AssetManager;
import com.nucleus.common.Environment;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NucleusRenderer.Matrices;
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
import com.nucleus.scene.gltf.Primitive.Mode;
import com.nucleus.scene.gltf.Scene;
import com.nucleus.shader.GLTFShaderProgram;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.shader.ShaderVariable;

public class GLTFNodeRenderer implements NodeRenderer<GLTFNode> {

    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    private Pass currentPass;
    protected ArrayDeque<float[]> modelMatrixStack = new ArrayDeque<float[]>(100);
    protected ArrayDeque<float[]> viewMatrixStack = new ArrayDeque<float[]>(5);
    protected ArrayDeque<float[]> projectionMatrixStack = new ArrayDeque<float[]>(5);
    protected float[] modelMatrix;
    protected int currentProgram = -1;
    protected RenderState renderState;
    protected Cullface cullFace;
    protected Mode forceMode = null;

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
    public void forceRenderMode(com.nucleus.opengl.GLESWrapper.Mode mode) {
        forceMode = mode != null ? Mode.getMode(mode.mode) : null;
    }

    @Override
    public boolean renderNode(NucleusRenderer renderer, GLTFNode node, Pass currentPass, float[][] matrices)
            throws GLException {
        GLTF glTF = node.getGLTF();
        Scene scene = glTF.getDefaultScene();
        if (glTF == null) {
            // Do nothing
            return false;
        }
        forceMode = Configuration.getInstance().getGLTFMode();
        renderState = renderer.getRenderState();
        pushMatrix(viewMatrixStack, matrices[Matrices.VIEW.index]);
        pushMatrix(projectionMatrixStack, matrices[Matrices.PROJECTION.index]);
        pushMatrix(modelMatrixStack, matrices[Matrices.MODEL.index]);

        currentProgram = -1;
        this.currentPass = currentPass;
        GLES20Wrapper gles = renderer.getGLES();
        // Set view matrix from previous render of this gltfNode
        // node.getSavedViewMatrix(matrices[Matrices.VIEW.index]);
        scene.setMVP(matrices);
        // This will rotate the view - ie the camera
        // matrices[Matrices.VIEW.index] = scene.getSceneTransform().concatMatrix(matrices[Matrices.VIEW.index]);
        matrices[Matrices.MODEL.index] = scene.getSceneTransform().concatMatrix(matrices[Matrices.MODEL.index]);
        // Render the default scene.
        renderScene(gles, glTF, scene, currentPass, matrices);

        matrices[Matrices.MODEL.index] = popMatrix(modelMatrixStack);
        matrices[Matrices.VIEW.index] = popMatrix(viewMatrixStack);
        matrices[Matrices.PROJECTION.index] = popMatrix(projectionMatrixStack);

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

    /**
     * Renders the Mesh in this node, then renders childnodes.
     * This will render the Node using depth first search
     * 
     * @param gles
     * @param glTF
     * @param node
     * @param matrices
     */
    protected void renderNode(GLES20Wrapper gles, GLTF glTF, Node node, float[][] matrices)
            throws GLException {
        pushMatrix(modelMatrixStack, matrices[Matrices.MODEL.index]);
        matrices[Matrices.MODEL.index] = node.concatMatrix(matrices[Matrices.MODEL.index]);
        renderMesh(gles, glTF, node.getMesh(), matrices);
        renderDebugMesh(gles, glTF, node.getMesh(), matrices);

        // Render children.
        renderNodes(gles, glTF, node.getChildren(), matrices);
        matrices[Matrices.MODEL.index] = popMatrix(modelMatrixStack);
    }

    /**
     * Renders the mesh using the specified MVP matrices, this will render each primitive.
     * 
     * @param gles
     * @param glTF
     * @param mesh
     * @param matrices
     * @throws GLException
     */
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
     * Used to render debug info for the Mesh - if {@value GLTF#debugTBN} is true then the TBN debug primitives are
     * drawn.
     * 
     * @param gles
     * @param glTF
     * @param mesh
     * @param matrices
     * @throws GLException
     */
    protected void renderDebugMesh(GLES20Wrapper gles, GLTF glTF, Mesh mesh, float[][] matrices) throws GLException {
        if (GLTF.debugTBN) {
            debugTBN(gles, glTF, mesh, matrices);
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
        program.updateUniforms(gles, matrices);
        Material material = primitive.getMaterial();
        if (material != null) {
            PBRMetallicRoughness pbr = material.getPbrMetallicRoughness();
            // Check for doublesided.
            if (material.isDoubleSided() && renderState.cullFace != Cullface.NONE) {
                cullFace = renderState.cullFace;
                gles.glDisable(GLES20.GL_CULL_FACE);
            }
            program.prepareTextures(gles, glTF, primitive, material);
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
            ArrayList<Attributes> attribs, ArrayList<Accessor> accessors, Mode mode) throws GLException {
        gles.glVertexAttribPointer(program, attribs, accessors);
        GLUtils.handleError(gles, "glVertexAttribPointer");
        if (indices != null) {
            // Indexed mode - use glDrawElements
            BufferView indicesView = indices.getBufferView();
            Buffer buffer = indicesView.getBuffer();
            if (buffer.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffer.getBufferName());
                GLUtils.handleError(gles, "glBindBuffer");
                gles.glDrawElements(mode.value, indices.getCount(), indices.getComponentType().value,
                        indices.getByteOffset() + indicesView.getByteOffset());
                GLUtils.handleError(gles, "glDrawElements VBO " + buffer.getBufferName());
            } else {
                gles.glDrawElements(mode.value, indices.getCount(), indices.getComponentType().value,
                        indices.getBuffer());
                GLUtils.handleError(gles, "glDrawElements");
            }
            timeKeeper.addDrawElements(indices.getCount(), vertexCount);
        } else {
            // Non indexed mode - use glDrawArrays
            gles.glDrawArrays(mode.value, 0, vertexCount);
            GLUtils.handleError(gles, "glDrawArrays VBO");
            timeKeeper.addDrawArrays(vertexCount);
        }

    }

    private void debugTBN(GLES20Wrapper gles, GLTF gltf, Mesh mesh, float[][] matrices) throws GLException {
        if (mesh != null) {
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
            debugProgram.updateUniforms(gles, matrices);
            for (Primitive p : primitives) {
                gles.disableAttribPointers();
                Accessor position = p.getAccessor(Attributes.POSITION);
                drawVertices(gles, debugProgram, null, position.getCount(),
                        p.getAttributesArray(), p.getAccessorArray(), Mode.POINTS);
                gles.glUseProgram(currentProgram);
            }
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
