package com.nucleus.renderer;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;
import com.nucleus.common.Constants;
import com.nucleus.opengl.GLESWrapper.GLES20;
import com.nucleus.texturing.Image.ImageFormat;

/**
 * Represents a render target, currently only supports window framebuffer
 * Future versions will add support for changing render target.
 * This class can be serialized using GSON
 */
public class RenderTarget {

    private static final String TARGET = "target";
    private static final String NAME = "name";
    private static final String ATTACHEMENTS = "attachements";

    
    
    public enum Attachement {
        COLOR(),
        DEPTH(),
        STENCIL();
    }
    
    public enum Target {
        FRAMEBUFFER(GLES20.GL_FRAMEBUFFER),
        RENDERBUFFER(GLES20.GL_RENDERBUFFER),
        TEXTURE(GLES20.GL_TEXTURE);

        public final int target;

        private Target(int target) {
            this.target = target;
        }

    }

    public static class AttachementData {
        
        private Attachement attachement;
        /**
         * The format, this depends on type of attachement
         */
        private String format;
        
        public AttachementData() {
            
        }
        public AttachementData(Attachement attachement, String format) {
            this.attachement = attachement;
            this.format = format;
        }
        
        public String getFormat() {
            return format;
        }
        
    }
    
    @SerializedName(TARGET)
    private Target target;
    /**
     * The scale of the target, compared to Window size, in x and y axis
     */
    private float[] scale;
    
    @SerializedName(ATTACHEMENTS)
    private ArrayList<AttachementData> attachements;

    transient private int name = Constants.NO_VALUE;
    transient private int[] size;
    
    public RenderTarget() {
        target = Target.FRAMEBUFFER;
    }
    
    public Target getTarget() {
        return target;
    }
    
    /**
     * Returns the target buffer name, 
     * @return
     */
    public int getName() {
        return name;
    }
    
    /**
     * Sets the name of the buffer object
     * @param name Generated buffer/texture object name
     */
    public void setName(int name) {
        this.name = name;
    }
    
    /**
     * Returns the attachement data for the attachement, or null if not set
     * @param attachement
     * @return
     */
    public AttachementData getAttachement(Attachement attachement) {
        if (attachements == null) {
            return null;
        }
        for (AttachementData ad : attachements) {
            if (ad.attachement == attachement) {
                return ad;
            }
        }
        return null;
    }
    /**
     * Returns the x and y axis scale, if set.
     * @return X and Y axis scale, or null if not set
     */
    public float[] getScale() {
        return scale;
    }

    /**
     * Calculates the size of the rendertarget, based on Window size and scale
     */
    private void calculateSize( ) {
        size = new int[] {(int) (Window.getInstance().getWidth() * scale[0]), (int) (Window.getInstance().getHeight() * scale[1])};
    }

    public int[] getSize() {
        if (size == null) {
            calculateSize();
        }
        return size;
    }
    
    
}
