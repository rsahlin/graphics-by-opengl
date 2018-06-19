package com.nucleus.renderer;

import com.nucleus.opengl.GLException;
import com.nucleus.scene.LineDrawerNode;
import com.nucleus.scene.LineDrawerNode.LineMode;

public class LineNodeRenderer extends NodeRenderer<LineDrawerNode> {

    public LineNodeRenderer(LineDrawerNode node) {
        this.node = node;
    }

    @Override
    public void renderNode(NucleusRenderer renderer, Pass currentPass, float[][] matrices) throws GLException {
        if (node.getLineMode() != LineMode.POINTS) {
            renderer.getGLES().glLineWidth(node.getPointSize());
        }
        super.renderNode(renderer, currentPass, matrices);
    }

    @Override
    public LineDrawerNode getNode() {
        return node;
    }

}
