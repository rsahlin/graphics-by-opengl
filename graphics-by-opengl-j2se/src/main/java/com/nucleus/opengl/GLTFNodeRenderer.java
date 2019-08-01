package com.nucleus.opengl;

import java.util.ArrayDeque;

import com.nucleus.BackendException;
import com.nucleus.GraphicsPipeline;
import com.nucleus.profiling.FrameSampler;
import com.nucleus.renderer.NodeRenderer;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.renderer.NucleusRenderer.Matrices;
import com.nucleus.renderer.Pass;
import com.nucleus.renderer.RenderState;
import com.nucleus.scene.GLTFNode;
import com.nucleus.scene.gltf.GLTF;
import com.nucleus.scene.gltf.Mesh;
import com.nucleus.scene.gltf.Node;
import com.nucleus.scene.gltf.Primitive;
import com.nucleus.scene.gltf.Scene;
import com.nucleus.vecmath.Matrix.MatrixStack;

public class GLTFNodeRenderer implements NodeRenderer<GLTFNode> {

    transient protected FrameSampler timeKeeper = FrameSampler.getInstance();
    private Pass currentPass;
    protected float[] modelMatrix;
    protected RenderState renderState;
    protected MatrixStack modelStack = new MatrixStack(100);
    protected MatrixStack viewStack = new MatrixStack(5);
    protected MatrixStack projectionStack = new MatrixStack(5);

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
    public boolean renderNode(NucleusRenderer renderer, GLTFNode node, Pass currentPass, float[][] matrices)
            throws BackendException {
        GLTF glTF = node.getGLTF();
        Scene scene = glTF.getDefaultScene();
        if (glTF == null) {
            // Do nothing
            return false;
        }
        renderState = renderer.getRenderState();
        modelStack.push(matrices[Matrices.MODEL.index], 0);
        viewStack.push(matrices[Matrices.VIEW.index], 0);
        projectionStack.push(matrices[Matrices.PROJECTION.index], 0);

        this.currentPass = currentPass;
        // Set view matrix from previous render of this gltfNode
        // node.getSavedViewMatrix(matrices[Matrices.VIEW.index]);
        scene.setMVP(matrices);
        // This will rotate the view - ie the camera
        // matrices[Matrices.VIEW.index] = scene.getSceneTransform().concatMatrix(matrices[Matrices.VIEW.index]);
        scene.getSceneTransform().concatMatrix(matrices[Matrices.MODEL.index], 0);
        // Render the default scene.
        renderScene(renderer, glTF, scene, currentPass, matrices);

        modelStack.pop(matrices[Matrices.MODEL.index], 0);
        viewStack.pop(matrices[Matrices.VIEW.index], 0);
        projectionStack.pop(matrices[Matrices.PROJECTION.index], 0);

        return true;
    }

    protected void renderScene(NucleusRenderer renderer, GLTF glTF, Scene scene, Pass currentPass, float[][] matrices)
            throws BackendException {
        // Traverse the nodes and render each.
        Node[] sceneNodes = scene.getNodes();
        if (sceneNodes != null) {
            for (int i = 0; i < sceneNodes.length; i++) {
                renderNode(renderer, glTF, sceneNodes[i], matrices);
            }
        }
    }

    /**
     * Renders the Mesh in this node, then renders childnodes.
     * This will render the Node using depth first search
     * 
     * @param renderer
     * @param glTF
     * @param node
     * @param matrices
     */
    protected void renderNode(NucleusRenderer renderer, GLTF glTF, Node node, float[][] matrices)
            throws BackendException {
        modelStack.push(matrices[Matrices.MODEL.index], 0);
        node.concatMatrix(matrices[Matrices.MODEL.index], 0);
        renderMesh(renderer, glTF, node.getMesh(), matrices);
        renderDebugMesh(renderer, glTF, node.getMesh(), matrices);

        // Render children.
        renderNodes(renderer, glTF, node.getChildren(), matrices);
        modelStack.pop(matrices[Matrices.MODEL.index], 0);
    }

    /**
     * Renders the mesh using the specified MVP matrices, this will render each primitive.
     * 
     * @param renderer
     * @param glTF
     * @param mesh
     * @param matrices
     * @throws BackendException
     */
    protected void renderMesh(NucleusRenderer renderer, GLTF glTF, Mesh mesh, float[][] matrices)
            throws BackendException {
        if (mesh != null) {
            Primitive[] primitives = mesh.getPrimitives();
            if (primitives != null) {
                for (Primitive p : primitives) {
                    renderPrimitive(renderer, glTF, p, matrices);
                }
            }
        }
    }

    /**
     * Used to render debug info for the Mesh - if {@value GLTF#debugTBN} is true then the TBN debug primitives are
     * drawn.
     * 
     * @param renderer
     * @param glTF
     * @param mesh
     * @param matrices
     * @throws BackendException
     */
    protected void renderDebugMesh(NucleusRenderer renderer, GLTF glTF, Mesh mesh, float[][] matrices)
            throws BackendException {
        if (GLTF.debugTBN) {
            debugTBN(renderer, glTF, mesh, matrices);
        }
    }

    /**
     * Renders the primitive
     * 
     * @param renderer
     * @param glTF
     * @param primitive
     * @param matrices
     * @throws BackendException
     */
    protected void renderPrimitive(NucleusRenderer renderer, GLTF glTF, Primitive primitive, float[][] matrices)
            throws BackendException {
        GraphicsPipeline pipeline = getPipeline(renderer, primitive, currentPass);
        renderer.usePipeline(pipeline);
        pipeline.update(renderer, glTF, primitive, matrices);
        renderer.renderPrimitive(pipeline, glTF, primitive, matrices);
    }

    private void debugTBN(NucleusRenderer renderer, GLTF gltf, Mesh mesh, float[][] matrices)
            throws BackendException {
        if (mesh != null) {
            Primitive[] primitives = mesh.getDebugTBNPrimitives();
            if (primitives == null) {
                // primitives = mesh.createDebugTBNPrimitives(gles, mesh.getPrimitives());
            }
            // renderer.getAssets().getGraphicsPipeline(renderer,
            // mesh.getDebugTBNProgram());
            /*
             * // Set uniforms.
             * ShaderVariable var = debugProgram.getUniformByName(Attributes._EMISSIVE.name());
             * FloatBuffer uniformData = debugProgram.getUniformData();
             * if (var != null && uniformData != null) {
             * uniformData.position(var.getOffset());
             * float[] debugColors = new float[] { 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1 };
             * uniformData.put(debugColors);
             * }
             * debugProgram.uploadUniform(gles, debugProgram.getUniformData(),
             * debugProgram.getUniformByName(Attributes._EMISSIVE.name()));
             * 
             * gles.glUseProgram(debugProgram.getProgram());
             * debugProgram.setUniformMatrices(matrices);
             * debugProgram.updateUniformData(debugProgram.getUniformData());
             * debugProgram.uploadUniforms(gles);
             * for (Primitive p : primitives) {
             * gles.disableAttribPointers();
             * Accessor position = p.getAccessor(Attributes.POSITION);
             * throw new IllegalArgumentException("Not implemented");
             * // renderer.drawVertices(debugProgram, null, position.getCount(),
             * // p.getAttributesArray(), p.getAccessorArray(), DrawMode.POINTS);
             * }
             */
        }

    }

    /**
     * Renders an array of nodes - each node will be rendered by calling {@link #renderNode(GLTF, Node)}
     * This means rendering will be depth first.
     * 
     * @param renderer
     * @param glTF
     * @param children
     * @param matrices
     */
    protected void renderNodes(NucleusRenderer renderer, GLTF glTF, Node[] children,
            float[][] matrices) throws BackendException {
        if (children != null && children.length > 0) {
            for (Node n : children) {
                renderNode(renderer, glTF, n, matrices);
            }
        }
    }

    /**
     * 
     * @param renderer
     * @param primitive
     * @param pass The currently defined pass
     * @return
     */
    protected GraphicsPipeline getPipeline(NucleusRenderer renderer, Primitive primitive, Pass pass) {
        GraphicsPipeline pipeline = primitive.getPipeline();
        if (pipeline == null) {
            throw new IllegalArgumentException("No pipeline for primitive ");
        }
        return pipeline;
    }

}
