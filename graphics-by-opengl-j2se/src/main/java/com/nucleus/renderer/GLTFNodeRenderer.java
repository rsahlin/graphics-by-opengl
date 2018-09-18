package com.nucleus.renderer;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.scene.GLTFNode;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Scene;

public class GLTFNodeRenderer implements NodeRenderer<GLTFNode>{

    @Override
    public boolean renderNode(NucleusRenderer renderer, GLTFNode node,Pass currentPass, float[][] matrices)
            throws GLException {
        GLES20Wrapper gles = renderer.getGLES();
//        ShaderProgram program = getProgram(gles, this, currentPass);
//        gles.glUseProgram(program.getProgram());
//        GLUtils.handleError(gles, "glUseProgram " + program.getProgram());
        // TODO - is this the best place for this check - remember, this should only be done in debug cases.
//        if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
//            program.validateProgram(gles);
//        }
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

}
