package com.nucleus.renderer;

import com.nucleus.common.Environment;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.opengl.GLUtils;
import com.nucleus.scene.RenderableNode;
import com.nucleus.shader.ShaderProgram;

public class DefaultNodeRenderer implements NodeRenderer<RenderableNode<Mesh>>{

    @Override
    public boolean renderNode(NucleusRenderer renderer, RenderableNode<Mesh> node, Pass currentPass, float[][] matrices)
            throws GLException {
        GLES20Wrapper gles = renderer.getGLES();
        ShaderProgram program = getProgram(gles, node, currentPass);
        gles.glUseProgram(program.getProgram());
        GLUtils.handleError(gles, "glUseProgram " + program.getProgram());
        // TODO - is this the best place for this check - remember, this should only be done in debug cases.
        if (Environment.getInstance().isProperty(com.nucleus.common.Environment.Property.DEBUG, false)) {
            program.validateProgram(gles);
        }
        node.getMeshRenderer().renderMeshes(renderer, program, node, matrices);
        return true;
    }

    /**
     * 
     * @param node The node being rendered
     * @param pass The currently defined pass
     * @return
     */
    protected ShaderProgram getProgram(GLES20Wrapper gles, RenderableNode<Mesh> node, Pass pass) {
        ShaderProgram program = node.getProgram();
        if (program == null) {
            throw new IllegalArgumentException("No program for node " + node.getId());
        }
        return program.getProgram(gles, pass, program.getShading());
    }
    
    
}
