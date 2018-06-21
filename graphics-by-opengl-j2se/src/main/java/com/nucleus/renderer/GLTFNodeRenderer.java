package com.nucleus.renderer;

import com.nucleus.opengl.GLException;
import com.nucleus.scene.GLTFNode;

public class GLTFNodeRenderer extends NodeRenderer<GLTFNode> {

    @Override
    public void renderNode(NucleusRenderer renderer, GLTFNode node, Pass currentPass, float[][] matrices)
            throws GLException {
        super.renderNode(renderer, node, currentPass, matrices);
    }

}
