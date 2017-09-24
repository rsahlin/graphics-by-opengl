package com.nucleus.renderer;

import com.google.gson.annotations.SerializedName;
import com.nucleus.opengl.GLESWrapper.GLES20;

/**
 * Represents a render target, currently only supports window framebuffer
 * Future versions will add support for changing render target.
 * This class can be serialized using GSON
 */
public class RenderTarget {

    public enum Attachement {
        COLOR(),
        DEPTH(),
        STENCIL();
    }
    
    private static final String TARGET = "target";
    private static final String NAME = "name";
    private static final String ATTACHEMENT = "attachement";
    
    public enum Target {
        FRAMEBUFFER(GLES20.GL_FRAMEBUFFER),
        RENDERBUFFER(GLES20.GL_RENDERBUFFER),
        TEXTURE(GLES20.GL_TEXTURE);

        public final int target;

        private Target(int target) {
            this.target = target;
        }

    }

    @SerializedName(TARGET)
    private Target target = Target.FRAMEBUFFER;
    @SerializedName(NAME)
    private int name = 0;
    @SerializedName(ATTACHEMENT)
    private static Attachement[] attachement = new Attachement[] {Attachement.COLOR};

    
    public Target getTarget() {
        return target;
    }
    
    public int getName() {
        return name;
    }

    public Attachement[] getAttachements() {
        return attachement;
    }
    
}
