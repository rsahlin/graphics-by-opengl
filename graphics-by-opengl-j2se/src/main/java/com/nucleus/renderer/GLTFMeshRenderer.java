package com.nucleus.renderer;

import java.util.ArrayList;

import com.nucleus.opengl.GLES20Wrapper;
import com.nucleus.opengl.GLException;
import com.nucleus.scene.RenderableNode;
import com.nucleus.scene.gltf.RenderableMesh;
import com.nucleus.shader.ShaderProgram;

public class GLTFMeshRenderer implements MeshRenderer<RenderableMesh>{

    transient ArrayList<RenderableMesh> nodeMeshes = new ArrayList<>();
    
    @Override
    public void renderMesh(NucleusRenderer renderer, ShaderProgram program, RenderableMesh mesh, float[][] matrices)
            throws GLException {
/*        
        GLES20Wrapper gles = renderer.getGLES();
        Consumer updater = mesh.getAttributeConsumer();
        if (updater != null) {
            updater.updateAttributeData(renderer);
        }
        
        //Loop through primitives and render each.
        for (Primitive p : mesh.getPrimitives()) {
            p.getIndices();
        }
        
        com.nucleus.scene.gltf.Material material = mesh.getMaterial();

        program.updateAttributes(gles, mesh);
        program.updateUniforms(gles, matrices);
        program.prepareTextures(gles, mesh);

        material.setBlendModeSeparate(gles);

        ElementBuffer indices = mesh.getElementBuffer();

        if (indices == null) {
            gles.glDrawArrays(mesh.getMode().mode, mesh.getOffset(), mesh.getDrawCount());
            GLUtils.handleError(gles, "glDrawArrays ");
            timeKeeper.addDrawArrays(mesh.getDrawCount());
        } else {
            if (indices.getBufferName() > 0) {
                gles.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.getBufferName());
                gles.glDrawElements(mesh.getMode().mode, mesh.getDrawCount(), indices.getType().type,
                        mesh.getOffset());
                GLUtils.handleError(gles, "glDrawElements with ElementBuffer ");
            } else {
                gles.glDrawElements(mesh.getMode().mode, mesh.getDrawCount(), indices.getType().type,
                        indices.getBuffer().position(mesh.getOffset()));
                GLUtils.handleError(gles, "glDrawElements no ElementBuffer ");
            }
            AttributeBuffer vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES_STATIC);
            if (vertices == null) {
                vertices = mesh.getAttributeBuffer(BufferIndex.ATTRIBUTES);
            }
            timeKeeper.addDrawElements(vertices.getVerticeCount(), mesh.getDrawCount());
        }
*/        
    }

    @Override
    public boolean renderMeshes(NucleusRenderer renderer, ShaderProgram program, RenderableNode<RenderableMesh> node,
            float[][] matrices) throws GLException {
        
        GLES20Wrapper gles = renderer.getGLES();
        nodeMeshes.clear();
        node.getMeshes(nodeMeshes);
        if (nodeMeshes.size() > 0) {
            for (RenderableMesh mesh : nodeMeshes) {
                renderMesh(renderer, program, mesh, matrices);
            }
            return true;
        }
        return false;
    }
    
}
