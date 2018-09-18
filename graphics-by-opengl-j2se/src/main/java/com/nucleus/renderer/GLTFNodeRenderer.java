package com.nucleus.renderer;

import com.nucleus.common.Environment;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.scene.GLTFNode;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.RenderableMesh;
import com.nucleus.scene.gltf.Scene;
import com.nucleus.shader.ShaderProgram;

public class GLTFNodeRenderer implements NodeRenderer<GLTFNode>{

    @Override
    public boolean renderNode(NucleusRenderer renderer, GLTFNode node,Pass currentPass, float[][] matrices)
            throws GLException {
        GLES20Wrapper gles = renderer.getGLES();
        ShaderProgram program = getProgram(gles, node, currentPass);
        gles.glUseProgram(program.getProgram());
        GLUtils.handleError(gles, "glUseProgram " + program.getProgram());
        // TODO - is this the best place for this check - remember, this should only be done in debug cases.
        if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
            program.validateProgram(gles);
        }
        GLTF glTF = node.getGLTF();
        int sceneIndex = glTF.getScene();
        if (sceneIndex == -1) {
            sceneIndex = 0;
        }
        Scene scene = glTF.getScenes()[sceneIndex];
        int[] nodes = scene.getNodes();
        node.getMeshRenderer().renderMeshes(renderer, node.getProgram(), node, matrices);
        return true;
    }

    /**
     * 
     * @param node The node being rendered
     * @param pass The currently defined pass
     * @return
     */
    protected ShaderProgram getProgram(GLES20Wrapper gles, RenderableNode<RenderableMesh> node, Pass pass) {
        ShaderProgram program = node.getProgram();
        if (program == null) {
            throw new IllegalArgumentException("No program for node " + node.getId());
        }
        return program.getProgram(gles, pass, program.getShading());
    }
    
    
    
    
}
