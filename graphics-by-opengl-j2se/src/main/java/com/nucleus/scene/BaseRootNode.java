package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderPass;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.Target;
import com.nucleus.shader.ShaderProgram;

/**
 * Implementation of RootNode, this can be used to construct simple nodetrees
 * The standard way of creating a scene is to load from file, sometimes a simple tree is needed and in those
 * cases the builder can be used.
 *
 */
public class BaseRootNode extends RootNode {

    public static class Builder extends NodeBuilder<RootNode> {

        ViewFrustum viewFrustum;

        public Builder() {
        }

        public Builder setViewFrustum(ViewFrustum viewFrustum) {
            this.viewFrustum = viewFrustum;
            return this;
        }

        public Node createInstance(RootNode root) {
            return new BaseRootNode();
        }

        @Override
        public Builder setProgram(ShaderProgram program) {
            this.program = program;
            return this;
        }

        @Override
        public RootNode create(String id) throws NodeException {
            BaseRootNode root = new BaseRootNode();
            setRoot(root);
            // TODO the builder should handle creation of renderpass in a more generic way.
            RenderPass pass = new RenderPass();
            pass.setId("RenderPass");
            pass.setTarget(new RenderTarget(Target.FRAMEBUFFER, null));
            pass.setRenderState(new RenderState());
            pass.setPass(Pass.MAIN);
            Node created = super.create("rootnode");
            if (created instanceof RenderableNode<?>) {
                ViewFrustum vf = new ViewFrustum();
                vf.setOrthoProjection(-0.8889f, 0.8889f, -0.5f, 0.5f, 0, 10);
                ((RenderableNode<?>) created).setViewFrustum(vf);
                created.setPass(Pass.ALL);
                ArrayList<RenderPass> rp = new ArrayList<>();
                rp.add(pass);
                ((RenderableNode<?>) created).setRenderPass(rp);
            }
            created.onCreated();
            root.addChild(created);
            return root;
        }

    }

    @Override
    public Node createInstance(RootNode root) {
        return new BaseRootNode();
    }

    /**
     * TODO Move construction
     */
    @Deprecated
    public BaseRootNode() {
    }

    @Override
    public void createTransient() {
    }

    @Override
    public boolean isInside(float[] position) {
        // TODO Auto-generated method stub
        return false;
    }

}
