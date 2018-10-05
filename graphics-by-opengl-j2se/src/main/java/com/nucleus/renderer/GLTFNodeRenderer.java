package com.nucleus.renderer;

import com.nucleus.common.Environment;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.scene.GLTFNode;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.Buffer;
import com.nucleus.scene.gltf.BufferView;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.scene.gltf.Node;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Scene;
import com.nucleus.shader.GLTFShaderProgram;
import com.nucleus.shader.ShaderProgram;

public class GLTFNodeRenderer implements NodeRenderer<GLTFNode> {

    private Pass currentPass;

    @Override
    public boolean renderNode(NucleusRenderer renderer, GLTFNode node, Pass currentPass, float[][] matrices)
            throws GLException {
        this.currentPass = currentPass;
        GLES20Wrapper gles = renderer.getGLES();
        GLTF glTF = node.getGLTF();
        int sceneIndex = glTF.getScene();
        if (sceneIndex == -1) {
            sceneIndex = 0;
        }
        Scene scene = glTF.getScene(sceneIndex);
        // Traverse the nodes and render each.
        renderNodes(gles, glTF, scene.getNodes(), matrices);
        return true;
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
        // Render this node.
        renderMesh(gles, glTF, node.getMesh(), matrices);
        // Render children.
        renderNodes(gles, glTF, node.getChildren(), matrices);
    }

    protected void renderMesh(GLES20Wrapper gles, GLTF glTF, Mesh mesh, float[][] matrices) throws GLException {
        if (mesh != null) {
            Primitive[] primitives = mesh.getPrimitives();
            if (primitives != null) {
                for (Primitive p : primitives) {
                    GLTFShaderProgram program = (GLTFShaderProgram) getProgram(gles, p, currentPass);
                    gles.glUseProgram(program.getProgram());
                    GLUtils.handleError(gles, "glUseProgram " + program.getProgram());
                    // TODO - is this the best place for this check - remember, this should only be done in debug cases.
                    if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
                        program.validateProgram(gles);
                    }
                    program.updateUniforms(gles, matrices);

                    renderPrimitive(gles, glTF, program, p);
                }
            }
        }
    }

    protected void renderPrimitive(GLES20Wrapper gles, GLTF glTF, GLTFShaderProgram program, Primitive primitive)
            throws GLException {
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
        } else {
            // Non indexed mode - use glDrawArrays
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
