package com.nucleus.texturing;

import com.google.gson.annotations.SerializedName;
import com.nucleus.texturing.TextureParameter.Name;
import com.nucleus.texturing.TextureParameter.Parameter;
import com.nucleus.texturing.TextureParameter.Target;

/**
 * Data for one texture parameter, mainly used for parameters other than the min / mag-filter, wrap s/t
 * 
 */
public class ParameterData {

    public final static String TARGET = "target";
    public final static String NAME = "name";
    public final static String PARAM = "param";

    public ParameterData(Target target, Name name, Parameter param) {
        this.target = target;
        this.name = name;
        this.param = param;
    }

    /**
     * Texture targets for the name and param, if used.
     * Used for texture parameters other than min,mag filter, wrap s/t
     */
    @SerializedName(TARGET)
    public Target target;

    /**
     * Pname, if used
     * Used for texture parameters other than min,mag filter, wrap s/t
     */
    @SerializedName(NAME)
    public Name name;

    /**
     * Texture value, if used
     * Used for texture parameters other than min,mag filter, wrap s/t
     */
    @SerializedName(PARAM)
    public Parameter param;

}
