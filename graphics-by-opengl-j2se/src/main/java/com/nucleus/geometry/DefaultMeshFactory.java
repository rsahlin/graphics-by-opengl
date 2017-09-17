package com.nucleus.geometry;

import java.io.IOException;

import com.nucleus.assets.AssetManager;
import com.nucleus.geometry.Mesh.Mode;
import com.nucleus.opengl.GLException;
import com.nucleus.renderer.NucleusRenderer;
import com.nucleus.scene.LayerNode;
import com.nucleus.scene.LineDrawerNode;
import com.nucleus.scene.Node;
import com.nucleus.scene.Node.NodeTypes;
import com.nucleus.scene.SwitchNode;
import com.nucleus.shader.VertexTranslateProgram;
import com.nucleus.texturing.Texture2D;
import com.nucleus.texturing.Texture2D.Shading;
import com.nucleus.texturing.TextureFactory;
import com.nucleus.texturing.TextureType;

public class DefaultMeshFactory implements MeshFactory {

    @Override
    public Mesh createMesh(NucleusRenderer renderer, Node parent) throws IOException, GLException {
        
    	switch (Node.NodeTypes.valueOf(parent.getType())) {
	    	case linedrawernode:
	            Mesh.Builder<Mesh> builder = new Mesh.Builder<>(renderer);
	            int count = ((LineDrawerNode) parent).getLineCount();
	            builder.setElementMode(Mode.LINES, count, count * 2);
	            Material m = new Material();
	            VertexTranslateProgram program = (VertexTranslateProgram) AssetManager.getInstance()
	                    .getProgram(renderer, new VertexTranslateProgram(Shading.flat));
	            m.setProgram(program);
	            Texture2D tex = TextureFactory.createTexture(TextureType.Untextured);
	            builder.setMaterial(m);
	            builder.setTexture(tex);
	            builder.setShapeBuilder(((LineDrawerNode) parent).getShapeBuilder());
	            // RectangleShapeBuilder.Configuration config = new RectangleShapeBuilder.Configuration(0.5f, 0.5f, 0f, 1,
	            // 0);
	            // builder.setShapeBuilder(new RectangleShapeBuilder(config));
	            Mesh mesh = builder.create();
	            return mesh;
	    	case switchnode:
	    	case layernode:
	    	case renderpass:
	            // No mesh for switch node, renderpass or layernode
	            return null;
	            default:
		            throw new IllegalArgumentException("Not implemented for " + parent.getType());
    	}
    }

}
