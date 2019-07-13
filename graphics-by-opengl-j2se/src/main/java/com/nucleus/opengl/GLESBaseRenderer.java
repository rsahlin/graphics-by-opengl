package com.nucleus.opengl;

import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import com.nucleus.Backend;
import com.nucleus.Backend.DrawMode;
import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.Pipeline;
import com.nucleus.SimpleLogger;
import com.nucleus.assets.Assets;
import com.nucleus.common.Constants;
import com.nucleus.geometry.AttributeBuffer;
import com.nucleus.geometry.AttributeUpdater.BufferIndex;
import com.nucleus.geometry.ElementBuffer;
import com.nucleus.geometry.Mesh;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.opengl.GLESWrapper.GLES_EXTENSION_TOKENS;
import com.nucleus.opengl.assets.GLAssetManager;
import com.nucleus.opengl.shader.ShadowPass1Program;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.BaseRenderer;
import com.nucleus.renderer.BufferFactory;
import com.nucleus.renderer.Configuration;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderPass;
import com.nucleus.renderer.RenderState;
import com.nucleus.renderer.RenderState.ClearFunc;
import com.nucleus.renderer.RenderState.Cullface;
import com.nucleus.renderer.RenderState.DepthFunc;
import com.nucleus.renderer.RenderTarget;
import com.nucleus.renderer.RenderTarget.Attachement;
import com.nucleus.renderer.RenderTarget.AttachementData;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.State;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.RootNode;
import com.nucleus.scene.gltf.Accessor;
import com.nucleus.scene.gltf.BufferView;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Image;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Primitive.Attributes;
import com.nucleus.scene.gltf.Texture;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.texturing.BufferImage;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Format;
import com.nucleus.texturing.TextureParameter;
import com.nucleus.texturing.TextureType;
import com.nucleus.vecmath.Matrix;

/**
 * Platform agnostic renderer, this handles pure render related methods.
 * It uses a GL wrapper to access GL functions, the caller should not need to know the specifics of OpenGLES.
 * The goal of this class is to have a low level renderer that can be used to draw objects on screen without having
 * to access OpenGLES methods directly.
 * This class does not create thread to drive rendering, that shall be done separately.
 * 
 */
public class GLESBaseRenderer extends BaseRenderer {

    protected GLES20Wrapper gles;

    /**
     * Creates a new GLES based renderer
     * 
     * @param backend The render backend wrapper, must be instance of GLESWrapper
     * @throws IllegalArgumentException If gles is null or not instance of GLESWrapper
     */
    public GLESBaseRenderer(Backend backend) {
        super(backend);
        if (!(backend instanceof GLESWrapper)) {
            throw new IllegalArgumentException(INVALID_WRAPPER_ERROR + " : " + backend);
        }
        gles = (GLES20Wrapper) backend;
        gles.createInfo();
        bufferFactory = new GLESBufferFactory(gles);
        assetManager = new GLAssetManager(gles);
        for (int i = 0; i < matrices.length; i++) {
            matrices[i] = Matrix.setIdentity(Matrix.createMatrix(), 0);
        }
    }

    /**
     * Internal method to apply the rendersettings.
     * 
     * @param state
     * @throws BackendException
     */
    private void setRenderState(RenderState state) throws BackendException {
        renderState.set(state);
        int flags = state.getChangeFlag();
        if ((flags & RenderState.CHANGE_FLAG_CLEARCOLOR) != 0) {
            float[] clear = state.getClearColor();
            gles.glClearColor(clear[0], clear[1], clear[2], clear[3]);
        }
        if ((flags & RenderState.CHANGE_FLAG_CULLFACE) != 0) {
            // Set GL values.
            if (state.getCullFace() != Cullface.NONE) {
                gles.glEnable(GLES20.GL_CULL_FACE);
                gles.glCullFace(gles.getCullFace(state.getCullFace()));
            } else {
                gles.glDisable(GLES20.GL_CULL_FACE);
            }
        }
        if ((flags & RenderState.CHANGE_FLAG_DEPTH) != 0) {
            DepthFunc function = state.getDepthFunc();
            if (state.getDepthFunc() != DepthFunc.NONE) {
                gles.glEnable(GLES20.GL_DEPTH_TEST);
                gles.glDepthFunc(gles.getDepthFunc(function));
                gles.glDepthMask(true);
                gles.glClearDepthf(state.getClearDepth());
                gles.glDepthRangef(state.getDepthRangeNear(), state.getDepthRangeFar());
            } else {
                gles.glDisable(GLES20.GL_DEPTH_TEST);
                gles.glClearDepthf(state.getClearDepth());
                gles.glDepthRangef(state.getDepthRangeNear(), state.getDepthRangeFar());
            }
        }
        if ((flags & RenderState.CHANGE_FLAG_MULTISAMPLE) != 0) {
            if (GLES20Wrapper.getInfo()
                    .hasExtensionSupport(GLESWrapper.GLES_EXTENSIONS.ARB_multisample.name())) {
                if (surfaceConfig != null && surfaceConfig.getSamples() > 1 && state.isMultisampling()) {
                    gles.glEnable(GLES_EXTENSION_TOKENS.MULTISAMPLE_EXT.value);
                } else {
                    gles.glDisable(GLES_EXTENSION_TOKENS.MULTISAMPLE_EXT.value);
                }
            }
        }
        GLUtils.handleError(gles, "setRenderSettings ");
    }

    @Override
    public void endFrame() {
    }

    @Override
    public void forceRenderMode(DrawMode mode) {
        forceMode = mode != null ? mode : null;
    }

    @Override
    public void render(RenderableNode<?> node) throws BackendException {
        forceMode = Configuration.getInstance().getGLTFMode();
        Pass pass = node.getPass();
        if (pass != null && (currentPass.getFlags() & pass.getFlags()) != 0) {
            // Node has defined pass and masked with current pass
            State state = node.getState();
            if (state == null || state == State.ON || state == State.RENDER) {
                ArrayList<RenderPass> renderPasses = node.getRenderPass();
                if (renderPasses != null) {
                    for (RenderPass renderPass : renderPasses) {
                        if (renderPass.getViewFrustum() != null && renderPass.getPass() == Pass.SHADOW1) {
                            // Save light projection
                            ShadowPass1Program.getLightMatrix(matrices[Matrices.RENDERPASS_1.index]);
                            // Store shadow1 projection * lightmatrix
                            Matrix.mul4(renderPass.getViewFrustum().getMatrix(tempMatrix),
                                    matrices[Matrices.RENDERPASS_1.index],
                                    matrices[Matrices.RENDERPASS_2.index]);
                        }
                        pushPass(renderPass.getPass());
                        setRenderPass(renderPass);
                        // Render node and children
                        internalRender(node);
                        popPass();
                    }
                } else {
                    // Render node and children
                    internalRender(node);
                }
            }
        }
    }

    /**
     * Internal method to render the {@link RenderableNode} using result matrices.
     * If node is rendered it is added to list of rendered nodes in RootNode.
     * Will recursively render child nodes.
     * 
     * @param node
     * @throws BackendException
     */
    private void internalRender(RenderableNode<?> node) throws BackendException {
        float[] nodeMatrix = node.concatModelMatrix(this.modelMatrix);
        // Fetch projection just before render
        float[] projection = node.getProjection(currentPass);
        if (projection != null) {
            pushMatrix(this.projection, matrices[Matrices.PROJECTION.index]);
            matrices[Matrices.PROJECTION.index] = projection;
        }
        matrices[Matrices.MODEL.index] = nodeMatrix;
        matrices[Matrices.VIEW.index] = viewMatrix;

        NodeRenderer<RenderableNode<?>> nodeRenderer = (NodeRenderer<RenderableNode<?>>) node.getNodeRenderer();
        if (nodeRenderer != null) {
            if (nodeRenderer.renderNode(this, node, currentPass, matrices)) {
                // Add this to rendered nodes before children.
                node.getRootNode().addRenderedNode(node);
            }
        }

        this.modelMatrix = nodeMatrix;
        for (Node n : node.getChildren()) {
            if (n instanceof RenderableNode<?>) {
                pushMatrix(matrixStack, this.modelMatrix);
                render((RenderableNode<?>) n);
                this.modelMatrix = popMatrix(matrixStack);
            }
        }
        if (projection != null) {
            matrices[Matrices.PROJECTION.index] = popMatrix(this.projection);
        }
    }

    private void setupRenderTarget(RenderTarget target) throws BackendException {
        boolean init = false;
        if (target.getFramebufferName() == Constants.NO_VALUE && target.getAttachements() != null) {
            createBuffers(target);
            init = true;
        }
        switch (target.getTarget()) {
            case FRAMEBUFFER:
                bindFramebuffer(target);
                break;
            case TEXTURE:
                bindTextureFramebuffer(target);
                break;
            default:
                throw new IllegalArgumentException("Not implemented");
        }
        if (init) {
            initAttachements(target);
        }
    }

    /**
     * Initialize, ie clear to start values, the targets after the buffers are created and target bound
     * 
     * @param target
     */
    private void initAttachements(RenderTarget target) {
        int mask = 0;
        for (AttachementData ad : target.getAttachements()) {
            float[] initValue = ad.getInitValue();
            if (initValue != null) {
                switch (ad.getAttachement()) {
                    case COLOR:
                        mask |= GLES20.GL_COLOR_BUFFER_BIT;
                        gles.glClearColor(initValue[0], initValue[1], initValue[2], initValue[3]);
                        break;
                    default:
                        throw new IllegalArgumentException("Not implemented for attachement " + ad.getAttachement());
                }
            }
        }
        if (mask != 0) {
            gles.glClear(mask);
        }
    }

    /**
     * Binds the specified attachement points as framebuffer render targets
     * 
     * @param target
     */
    private void bindTextureFramebuffer(RenderTarget target) throws BackendException {
        // Loop through all attachments and setup, if not defined in rendertarget then disable
        for (Attachement a : Attachement.values()) {
            bindTextureFramebuffer(target, a);
        }
    }

    /**
     * Bind the texture as rendertarget for the specified attachement point
     * 
     * @param target
     * @param attachement
     * @throws BackendException
     */
    private void bindTextureFramebuffer(RenderTarget target, Attachement attachement) throws BackendException {
        AttachementData ad = target.getAttachement(attachement);
        if (ad == null) {
            disable(attachement);
        } else {
            Texture2D texture = ad.getTexture();
            prepareTexture(texture, Texture2D.TEXTURE_0);
            gles.bindFramebufferTexture(texture, target.getFramebufferName(), attachement);
            gles.glViewport(0, 0, texture.getWidth(), texture.getHeight());
            enable(attachement);
        }
    }

    private void disable(Attachement attachement) throws BackendException {
        switch (attachement) {
            case COLOR:
                gles.glColorMask(false, false, false, false);
                break;
            case DEPTH:
                gles.glDisable(GLES20.GL_DEPTH_TEST);
                break;
            case STENCIL:
                gles.glDisable(GLES20.GL_STENCIL_TEST);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + attachement);

        }
        GLUtils.handleError(gles, "glDisable " + attachement);
    }

    private void enable(Attachement attachement) throws BackendException {
        switch (attachement) {
            case COLOR:
                gles.glColorMask(true, true, true, true);
                break;
            case DEPTH:
                gles.glEnable(GLES20.GL_DEPTH_TEST);
                gles.glDepthFunc(GLES20.GL_ALWAYS);
                gles.glDepthMask(true);

                break;
            case STENCIL:
                gles.glEnable(GLES20.GL_STENCIL_TEST);
                break;
            default:
                throw new IllegalArgumentException("Not implemented for " + attachement);

        }
        GLUtils.handleError(gles, "glDisable " + attachement);

    }

    /**
     * Creates and initializes the buffers needed for the rendertarget
     * 
     * @param target
     */
    private void createBuffers(RenderTarget target) throws BackendException {
        ArrayList<AttachementData> attachements = target.getAttachements();
        if (attachements == null) {
            // No attachements - what does this mean?
            SimpleLogger.d(getClass(), "No attachements");
        } else {
            if (target.getFramebufferName() == Constants.NO_VALUE) {
                target.setFramebufferName(createFramebuffer());
            }
            for (AttachementData ad : target.getAttachements()) {
                switch (ad.getAttachement()) {
                    case COLOR:
                    case DEPTH:
                    case STENCIL:
                        createAttachementBuffer(target, ad);
                        break;
                    default:
                        throw new IllegalArgumentException("Not implemented");
                }
            }
        }
    }

    /**
     * Binds framebuffer to the specified target and attachement.
     * Currently only supports binding to window framebuffer (0)
     * 
     * @param target Null target or target with empty/null attachements
     */
    private void bindFramebuffer(RenderTarget target) throws BackendException {
        if (target == null || target.getAttachements() == null || target.getAttachements().size() == 0) {
            // Bind default windowbuffer
            gles.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            gles.glViewport(0, 0, window.getWidth(), window.getHeight());
            enable(Attachement.COLOR);
        } else {
            throw new IllegalArgumentException("Not implemented");
        }
    }

    /**
     * Initializes the buffer needed for the attachement
     * Currently only supports render to texture
     * 
     * @param renderTarget
     * @param attachementData
     * @throws BackendException
     */
    private void createAttachementBuffer(RenderTarget renderTarget, AttachementData attachementData)
            throws BackendException {
        switch (renderTarget.getTarget()) {
            case TEXTURE:
                attachementData.setTexture(createTexture(renderTarget, attachementData));
                break;
            default:
                throw new IllegalArgumentException("Not implemented for target:" + renderTarget.getTarget());
        }
    }

    private int createFramebuffer() {
        final int[] frameBuffer = new int[1];
        gles.glGenFramebuffers(frameBuffer);
        return frameBuffer[0];
    }

    /**
     * Creates a texture name and texture image for the attachement data
     * Will create texture image based on the scale factor in the attachement data
     * 
     * @param renderTarget
     * @param attachementData
     * @return Texture object name
     * @throws BackendException
     */
    private Texture2D createTexture(RenderTarget renderTarget, AttachementData attachementData)
            throws BackendException {
        return getAssets().createTexture(this, renderTarget, attachementData, GLES20.GL_TEXTURE_2D);
    }

    private void setRenderPass(RenderPass renderPass) throws BackendException {
        // First set state so that rendertargets can override enable/disable writing to buffers
        if (renderPass.getPass() == null || renderPass.getTarget() == null) {
            throw new IllegalArgumentException(
                    "RenderPass must contain pass and target:" + renderPass.getPass() + ", " + renderPass.getTarget());
        }
        RenderState state = renderPass.getRenderState();
        if (state != null) {
            // TODO - check diff between renderpasses and only update accordingly
            state.setChangeFlag(RenderState.CHANGE_FLAG_ALL);
            setRenderState(state);
            state.setChangeFlag(RenderState.CHANGE_FLAG_NONE);
        }
        setupRenderTarget(renderPass.getTarget());
        // Clear buffer according to settings
        int clearFunc = state.getClearFlags();
        if (clearFunc != ClearFunc.NONE.flag) {
            gles.glClear(gles.getClearMask(clearFunc));
        }
        switch (renderPass.getPass()) {
            case SHADOW2:
                // Adjust the light matrix to fit inside texture coordinates
                // The scale and translate shall be taken from current viewfrustum
                Matrix.setIdentity(matrices[Matrices.RENDERPASS_2.index], 0);
                Matrix.scaleM(matrices[Matrices.RENDERPASS_2.index], 0, 0.5f, 0.5f, 1f);
                Matrix.translate(matrices[Matrices.RENDERPASS_2.index], 0.5f, 0.5f, 0f);
                Matrix.mul4(matrices[Matrices.RENDERPASS_1.index], matrices[Matrices.RENDERPASS_2.index], tempMatrix);
                System.arraycopy(tempMatrix, 0, matrices[Matrices.RENDERPASS_1.index], 0, Matrix.MATRIX_ELEMENTS);
                break;
            default:
                // Nothing to do
        }
    }

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
    public void render(RootNode root) throws BackendException {
        long start = System.currentTimeMillis();
        List<Node> scene = root.getChildren();
        if (scene != null) {
            for (Node node : scene) {
                render((RenderableNode<?>) node);
            }
        }
        timeKeeper.addTag(FrameSampler.Samples.RENDERNODES, start, System.currentTimeMillis());
    }

    @Override
    public void uploadTextures(Texture2D texture, BufferImage[] textureImages)
            throws BackendException {
        if (texture.getName() <= 0) {
            throw new IllegalArgumentException("No texture name for texture " + texture.getId());
        }
        long start = System.currentTimeMillis();
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getName());
        boolean isMipMapParams = texture.getTexParams().isMipMapFilter();
        if ((textureImages.length > 1 && !isMipMapParams) || (texture.getLevels() > 1 && !isMipMapParams)) {
            throw new IllegalArgumentException(
                    "Multiple mipmap images but wrong min filter "
                            + texture.getTexParams().getParameters()[TextureParameter.MIN_FILTER_INDEX]);
        }
        int level = 0;
        texture.setup(textureImages[0].getWidth(), textureImages[0].getHeight());
        for (BufferImage textureImg : textureImages) {
            if (textureImg != null) {
                if (texture.getFormat() == null || texture.getType() == null) {
                    throw new IllegalArgumentException("Texture format or type is null for id " + texture.getId()
                            + " : " + texture.getFormat() + ", " + texture.getType());
                }
                gles.texImage(texture, textureImg, level);
                GLUtils.handleError(gles, "texImage2D");
                level++;
            } else {
                break;
            }
        }
        if (textureImages.length == 1 && texture.getTexParams().isMipMapFilter()
                || Configuration.getInstance().isGenerateMipMaps()) {
            gles.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            SimpleLogger.d(TextureUtils.class, "Generated mipmaps for texture " + texture.getId());
        }
        FrameSampler.getInstance().logTag(FrameSampler.Samples.UPLOAD_TEXTURE, texture.getId(), start,
                System.currentTimeMillis());
    }

    @Override
    public void uploadTextures(Image image, boolean generateMipmaps)
            throws BackendException {
        if (image.getTextureName() <= 0) {
            throw new IllegalArgumentException("No texture name for texture " + image.getUri());
        }
        long start = System.currentTimeMillis();
        gles.glBindTexture(GLES20.GL_TEXTURE_2D, image.getTextureName());
        Format format = gles.texImage(image, 0);
        GLUtils.handleError(gles, "texImage2D");
        SimpleLogger.d(TextureUtils.class,
                "Uploaded texture " + image.getUri() + " with format " + format);
        if (generateMipmaps || Configuration.getInstance().isGenerateMipMaps()) {
            gles.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
            SimpleLogger.d(TextureUtils.class, "Generated mipmaps for texture " + image.getUri());
        }
        FrameSampler.getInstance().logTag(FrameSampler.Samples.UPLOAD_TEXTURE, " " + image.getUri(), start,
                System.currentTimeMillis());
    }

    @Override
    public void prepareTexture(Texture2D texture, int unit) throws BackendException {
        if (texture != null && texture.textureType != TextureType.Untextured) {
            gles.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
            int textureID = texture.getName();
            if (textureID == Constants.NO_VALUE && texture.getExternalReference().isIdReference()) {
                // Texture has no texture object - and is id reference
                // Should only be used for dynamic textures, eg ones that depend on define in existing node
                getAssets().getIdReference(texture);
                textureID = texture.getName();
                gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
                gles.uploadTexParameters(texture.getTexParams());
                GLUtils.handleError(gles, "glBindTexture()");
            } else {
                gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
                gles.uploadTexParameters(texture.getTexParams());
                GLUtils.handleError(gles, "glBindTexture()");
            }
        }
    }

    @Override
    public void prepareTexture(Texture texture, int unit, Accessor accessor, ShaderVariable attribute,
            ShaderVariable texUniform, IntBuffer samplerUniformBuffer)
            throws BackendException {
        if (texture != null) {
            gles.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
            int textureID = texture.getImage().getTextureName();
            gles.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
            gles.uploadTexParameters(texture);
            GLUtils.handleError(gles, "glBindTexture()");
            gles.glVertexAttribPointer(accessor, attribute);
            gles.glUniform1iv(texUniform.getLocation(), texUniform.getSize(), samplerUniformBuffer);
            GLUtils.handleError(gles, "glUniform1iv - " + attribute.getLocation());
        }
    }

    @Override
    public boolean usePipeline(GraphicsPipeline pipeline) throws BackendException {
        if (currentPipeline != pipeline) {
            currentPipeline = pipeline;
            pipeline.enable(this);
            return true;
        }
        return false;
    }

    @Override
    public void renderMesh(GraphicsPipeline pipeline, Mesh mesh, float[][] matrices) throws BackendException {
        pipeline.update(this, mesh, matrices);
        int mode = gles.getDrawMode(mesh.getMode());
        ElementBuffer indices = mesh.getElementBuffer();
        if (indices == null) {
            AttributeBuffer ab = mesh.getAttributeBuffer(0);
            if (ab.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ab.getBufferName());
            }
            gles.glDrawArrays(mode, mesh.getOffset(), mesh.getDrawCount());
            GLUtils.handleError(gles, "glDrawArrays ");
            timeKeeper.addDrawArrays(mesh.getDrawCount());
        } else {
            if (indices.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
                gles.glDrawElements(mode, mesh.getDrawCount(), indices.getType().type,
                        mesh.getOffset());
                GLUtils.handleError(gles, "glDrawElements with ElementBuffer " + mesh.getMode() + ", "
                        + mesh.getDrawCount() + ", " + indices.getType() + ", " + mesh.getOffset());
            } else {
                gles.glDrawElements(mode, mesh.getDrawCount(), indices.getType().type,
                        indices.getBuffer().position(mesh.getOffset()));
                GLUtils.handleError(gles, "glDrawElements no ElementBuffer ");
            }
            AttributeBuffer vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES_STATIC);
            if (vertices == null) {
                vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES);
            }
            timeKeeper.addDrawElements(vertices.getVerticeCount(), mesh.getDrawCount());
        }
        gles.disableAttribPointers();
    }

    @Override
    public void renderPrimitive(GraphicsPipeline pipeline, GLTF glTF, Primitive primitive, float[][] matrices)
            throws BackendException {
        if (usePipeline(pipeline)) {
            // program.updateEnvironmentUniforms(gles, glTF.getDefaultScene());
        }
        if (renderState.getCullFace() != Cullface.NONE) {
            cullFace = renderState.getCullFace();
        }
        Accessor indices = primitive.getIndices();
        Accessor position = primitive.getAccessor(Attributes.POSITION);
        drawVertices(pipeline, indices, position.getCount(), primitive.getAttributesArray(),
                primitive.getAccessorArray(), forceMode == null ? primitive.getMode() : forceMode);
        // Restore cullface if changed.
        if (cullFace != null) {
            gles.glEnable(GLES20.GL_CULL_FACE);
            cullFace = null;
        }
        gles.disableAttribPointers();
    }

    @Override
    public void drawVertices(GraphicsPipeline pipeline, Accessor indices, int vertexCount,
            ArrayList<Attributes> attribs, ArrayList<Accessor> accessors, DrawMode mode) throws BackendException {
        pipeline.glVertexAttribPointer(attribs, accessors);
        int modeValue = gles.getDrawMode(mode);
        if (indices != null) {
            // Indexed mode - use glDrawElements
            BufferView indicesView = indices.getBufferView();
            com.nucleus.scene.gltf.Buffer buffer = indicesView.getBuffer();
            if (buffer.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffer.getBufferName());
                GLUtils.handleError(gles, "glBindBuffer");
                gles.glDrawElements(modeValue, indices.getCount(), indices.getComponentType().value,
                        indices.getByteOffset() + indicesView.getByteOffset());
                GLUtils.handleError(gles, "glDrawElements VBO " + buffer.getBufferName());
            } else {
                gles.glDrawElements(modeValue, indices.getCount(), indices.getComponentType().value,
                        indices.getBuffer());
                GLUtils.handleError(gles, "glDrawElements");
            }
            timeKeeper.addDrawElements(indices.getCount(), vertexCount);
        } else {
            // Non indexed mode - use glDrawArrays
            gles.glDrawArrays(modeValue, 0, vertexCount);
            GLUtils.handleError(gles, "glDrawArrays VBO");
            timeKeeper.addDrawArrays(vertexCount);
        }

    }

    @Override
    public BufferFactory getBufferFactory() {
        return bufferFactory;
    }

    @Override
    public Assets getAssets() {
        return assetManager;
    }

    @Override
    public void deletePipeline(Pipeline pipeline) {
        pipeline.destroy(this);
    }

}
