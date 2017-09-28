package com.nucleus.scene;

import java.util.ArrayList;

import com.nucleus.camera.ViewFrustum;
import com.nucleus.common.Type;
import com.nucleus.geometry.Mesh;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.Attachement;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.renderer.RenderTarget.Target;
import com.nucleus.shader.ShaderProgram;
import com.nucleus.texturing.Image.ImageFormat;

/**
 * Implementation of RootNode, this can be used to construct simple nodetrees
 * The standard way of creating a scene is to load from file, sometimes a simple tree is needed and in those
 * cases the builder can be used.
 *
 */
public class BaseRootNode extends RootNode {

    public static class Builder {

        ViewFrustum viewFrustum;
        Type<Node> nodeType;
        ShaderProgram program;
        NodeFactory nodeFactory;
        Mesh.Builder<Mesh> builder;
        NucleusRenderer renderer;

        public Builder(NucleusRenderer renderer) {
            if (renderer == null) {
                throw new IllegalArgumentException("Renderer may not be null");
            }
            this.renderer = renderer;
        }

        public Builder setNodeFactory(NodeFactory nodeFactory) {
            this.nodeFactory = nodeFactory;
            return this;
        }

        public Builder setMeshBuilder(Mesh.Builder<Mesh> meshBuilder) {
            this.builder = meshBuilder;
            return this;
        }

        public Builder setViewFrustum(ViewFrustum viewFrustum) {
            this.viewFrustum = viewFrustum;
            return this;
        }

        /**
         * Sets the node (class) to create
         * 
         * @param classResolver Type and class for the node.
         * @return
         * @throws IllegalArgumentException If nodeclass is not instance of Node, or nodeClass can not be instantiated
         */
        public Builder setNode(Type<Node> classResolver) {
            this.nodeType = classResolver;
            return this;
        }

        public Builder setProgram(ShaderProgram program) {
            this.program = program;
            return this;
        }

        public RootNode create() throws NodeException {
            validate();
            BaseRootNode root = new BaseRootNode();
            RenderPass pass = new RenderPass(root);
            pass.setId("RenderPass");
            pass.setTarget(new RenderTarget(Target.FRAMEBUFFER, null));
            pass.setRenderState(new RenderState());
            Node created = nodeFactory.create(renderer, program, builder, nodeType, root);
            ViewFrustum vf = new ViewFrustum();
            vf.setOrthoProjection(-0.5f, 0.5f, -0.5f, 0.5f, 0, 10);
            created.setViewFrustum(vf);
            created.setId(created.getClass().getSimpleName());
            pass.addChild(created);
            root.addChild(pass);
            return root;
        }

        private void validate() {
            if (builder == null || nodeType == null || nodeFactory == null) {
                throw new IllegalArgumentException(
                        "Parameter not set: " + builder + ", " + nodeType + ", " + nodeFactory);
            }
        }

    }


    @Override
    public RootNode createInstance() {
        BaseRootNode root = new BaseRootNode();
        return root;
    }

}
