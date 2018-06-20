package com.nucleus.renderer;

import com.nucleus.opengl.GLException;
import com.nucleus.scene.LineDrawerNode;
import com.nucleus.scene.LineDrawerNode.LineMode;

/**
 * Renderer for linedrawer nodes
 *
 */
public class LineNodeRenderer extends NodeRenderer<LineDrawerNode> {

    @Override
    public void renderNode(NucleusRenderer renderer, LineDrawerNode node, Pass currentPass, float[][] matrices)
            throws GLException {
        if (node.getLineMode() != LineMode.POINTS) {
            renderer.getGLES().glLineWidth(node.getPointSize());
        }
        super.renderNode(renderer, node, currentPass, matrices);
    }

}
