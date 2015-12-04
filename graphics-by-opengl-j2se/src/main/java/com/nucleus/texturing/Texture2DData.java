package com.nucleus.texturing;

import com.nucleus.io.ExternalReference;
import com.nucleus.resource.ResourceBias.RESOLUTION;
import com.nucleus.scene.NodeData;

public class Texture2DData extends NodeData {

    private RESOLUTION resolution;
    private int mipmap;
    private TexParameter[] texparameter;
    private ExternalReference externalReference;

    public RESOLUTION getResolution() {
        return resolution;
    }

    public int getMipmap() {
        return mipmap;
    }

    public TexParameter[] getTexparameter() {
        return texparameter;
    }

    public ExternalReference getExternalReference() {
        return externalReference;
    }

}
