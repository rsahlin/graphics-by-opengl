package com.nucleus.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import com.nucleus.SimpleLogger;
import com.nucleus.common.StringUtils;
import com.nucleus.renderer.RendererInfo;
import com.nucleus.shader.ShaderVariable;
import com.nucleus.shader.ShaderVariable.InterfaceBlock;
import com.nucleus.shader.ShaderVariable.VariableType;

public abstract class GLESWrapper {

    public class ProgramInfo {
        private int program;
        private int[] activeVariables;
        private int[] maxNameLength;

        public ProgramInfo(int program, int[] activeVariables, int[] maxNameLength) {
            this.program = program;
            this.activeVariables = new int[activeVariables.length];
            this.maxNameLength = new int[maxNameLength.length];
            System.arraycopy(activeVariables, 0, this.activeVariables, 0, activeVariables.length);
            System.arraycopy(maxNameLength, 0, this.maxNameLength, 0, maxNameLength.length);
        }

        public int getActiveVariables(VariableType type) {
            return type.index < activeVariables.length ? activeVariables[type.index] : 0;
        }

        public int getMaxNameLength(VariableType type) {
            return type.index < maxNameLength.length ? maxNameLength[type.index] : 0;
        }

        public int getProgram() {
            return program;
        }

    }

    /**
     * Implementation of GL/GLES on the platform.
     *
     */
    public enum Platform {
        GLES(),
        GL();
    }

    private final static String[] GLES3_VERTEX_REPLACEMENTS = new String[] { "attribute", "in", "varying", "out" };
    private final static String[] GLES3_FRAGMENT_REPLACEMENTS = new String[] { "varying", "in" };

    protected RendererInfo rendererInfo;
    protected final Platform platform;
    /**
     * Must be set by implementing classes
     */
    protected final Renderers renderVersion;

    /**
     * The supported renderers
     * 
     * @author Richard Sahlin
     *
     */
    public enum Renderers {
        GLES20(2, 0),
        GLES30(3, 0),
        GLES31(3, 1),
        GLES32(3, 2);
        public final int major;
        public final int minor;

        private Renderers(int major, int minor) {
            this.major = major;
            this.minor = minor;
        };
    }

    protected GLESWrapper(Platform platform, Renderers renderVersion) {
        this.platform = platform;
        this.renderVersion = renderVersion;
        SimpleLogger.d(getClass(), "Created GLES wrapper " + renderVersion + " for platform " + platform);
    }

    public abstract class GL10 {

        public static final int GL_POINT_SMOOTH = 0x0B10;
        public static final int GL_LINE_SMOOTH = 0x0B20;
        public static final int GL_SCISSOR_TEST = 0x0C11;
        public static final int GL_COLOR_MATERIAL = 0x0B57;
        public static final int GL_NORMALIZE = 0x0BA1;
        public static final int GL_RESCALE_NORMAL = 0x803A;
        public static final int GL_POLYGON_OFFSET_FILL = 0x8037;
        public static final int GL_VERTEX_ARRAY = 0x8074;
        public static final int GL_NORMAL_ARRAY = 0x8075;
        public static final int GL_COLOR_ARRAY = 0x8076;
        public static final int GL_TEXTURE_COORD_ARRAY = 0x8078;
        public static final int GL_MULTISAMPLE = 0x809D;
        public static final int GL_SAMPLE_ALPHA_TO_COVERAGE = 0x809E;
        public static final int GL_SAMPLE_ALPHA_TO_ONE = 0x809F;
        public static final int GL_SAMPLE_COVERAGE = 0x80A0;

    }

    /**
     * TODO Rename to GL
     */
    public abstract class GLES20 {

        public static final int GL_ACTIVE_TEXTURE = 0x84E0;
        public static final int GL_DEPTH_BUFFER_BIT = 0x00000100;
        public static final int GL_STENCIL_BUFFER_BIT = 0x00000400;
        public static final int GL_COLOR_BUFFER_BIT = 0x00004000;
        public static final int GL_FALSE = 0;
        public static final int GL_TRUE = 1;
        public static final int GL_POINTS = 0x0000;
        public static final int GL_LINES = 0x0001;
        public static final int GL_LINE_LOOP = 0x0002;
        public static final int GL_LINE_STRIP = 0x0003;
        public static final int GL_TRIANGLES = 0x0004;
        public static final int GL_TRIANGLE_STRIP = 0x0005;
        public static final int GL_TRIANGLE_FAN = 0x0006;
        public static final int GL_ZERO = 0;
        public static final int GL_ONE = 1;
        public static final int GL_SRC_COLOR = 0x0300;
        public static final int GL_ONE_MINUS_SRC_COLOR = 0x0301;
        public static final int GL_SRC_ALPHA = 0x0302;
        public static final int GL_ONE_MINUS_SRC_ALPHA = 0x0303;
        public static final int GL_DST_ALPHA = 0x0304;
        public static final int GL_ONE_MINUS_DST_ALPHA = 0x0305;
        public static final int GL_DST_COLOR = 0x0306;
        public static final int GL_ONE_MINUS_DST_COLOR = 0x0307;
        public static final int GL_SRC_ALPHA_SATURATE = 0x0308;
        public static final int GL_FUNC_ADD = 0x8006;
        public static final int GL_BLEND_EQUATION = 0x8009;
        public static final int GL_BLEND_EQUATION_RGB = 0x8009;
        public static final int GL_BLEND_EQUATION_ALPHA = 0x883D;
        public static final int GL_FUNC_SUBTRACT = 0x800A;
        public static final int GL_FUNC_REVERSE_SUBTRACT = 0x800B;
        public static final int GL_BLEND_DST_RGB = 0x80C8;
        public static final int GL_BLEND_SRC_RGB = 0x80C9;
        public static final int GL_BLEND_DST_ALPHA = 0x80CA;
        public static final int GL_BLEND_SRC_ALPHA = 0x80CB;
        public static final int GL_CONSTANT_COLOR = 0x8001;
        public static final int GL_ONE_MINUS_CONSTANT_COLOR = 0x8002;
        public static final int GL_CONSTANT_ALPHA = 0x8003;
        public static final int GL_ONE_MINUS_CONSTANT_ALPHA = 0x8004;
        public static final int GL_BLEND_COLOR = 0x8005;
        public static final int GL_ARRAY_BUFFER = 0x8892;
        public static final int GL_ELEMENT_ARRAY_BUFFER = 0x8893;
        public static final int GL_ARRAY_BUFFER_BINDING = 0x8894;
        public static final int GL_ELEMENT_ARRAY_BUFFER_BINDING = 0x8895;
        public static final int GL_STREAM_DRAW = 0x88E0;
        public static final int GL_STATIC_DRAW = 0x88E4;
        public static final int GL_DYNAMIC_DRAW = 0x88E8;
        public static final int GL_BUFFER_SIZE = 0x8764;
        public static final int GL_BUFFER_USAGE = 0x8765;
        public static final int GL_CURRENT_VERTEX_ATTRIB = 0x8626;
        public static final int GL_FRONT = 0x0404;
        public static final int GL_BACK = 0x0405;
        public static final int GL_FRONT_AND_BACK = 0x0408;
        public static final int GL_TEXTURE_2D = 0x0DE1;
        public static final int GL_CULL_FACE = 0x0B44;
        public static final int GL_BLEND = 0x0BE2;
        public static final int GL_DITHER = 0x0BD0;
        public static final int GL_STENCIL_TEST = 0x0B90;
        public static final int GL_DEPTH_TEST = 0x0B71;
        public static final int GL_SCISSOR_TEST = 0x0C11;
        public static final int GL_POLYGON_OFFSET_FILL = 0x8037;
        public static final int GL_SAMPLE_ALPHA_TO_COVERAGE = 0x809E;
        public static final int GL_SAMPLE_COVERAGE = 0x80A0;
        public static final int GL_NO_ERROR = 0;
        public static final int GL_INVALID_ENUM = 0x0500;
        public static final int GL_INVALID_VALUE = 0x0501;
        public static final int GL_INVALID_OPERATION = 0x0502;
        public static final int GL_OUT_OF_MEMORY = 0x0505;
        public static final int GL_CW = 0x0900;
        public static final int GL_CCW = 0x0901;
        public static final int GL_LINE_WIDTH = 0x0B21;
        public static final int GL_ALIASED_POINT_SIZE_RANGE = 0x846D;
        public static final int GL_ALIASED_LINE_WIDTH_RANGE = 0x846E;
        public static final int GL_CULL_FACE_MODE = 0x0B45;
        public static final int GL_FRONT_FACE = 0x0B46;
        public static final int GL_DEPTH_RANGE = 0x0B70;
        public static final int GL_DEPTH_WRITEMASK = 0x0B72;
        public static final int GL_DEPTH_CLEAR_VALUE = 0x0B73;
        public static final int GL_DEPTH_FUNC = 0x0B74;
        public static final int GL_STENCIL_CLEAR_VALUE = 0x0B91;
        public static final int GL_STENCIL_FUNC = 0x0B92;
        public static final int GL_STENCIL_FAIL = 0x0B94;
        public static final int GL_STENCIL_PASS_DEPTH_FAIL = 0x0B95;
        public static final int GL_STENCIL_PASS_DEPTH_PASS = 0x0B96;
        public static final int GL_STENCIL_REF = 0x0B97;
        public static final int GL_STENCIL_VALUE_MASK = 0x0B93;
        public static final int GL_STENCIL_WRITEMASK = 0x0B98;
        public static final int GL_STENCIL_BACK_FUNC = 0x8800;
        public static final int GL_STENCIL_BACK_FAIL = 0x8801;
        public static final int GL_STENCIL_BACK_PASS_DEPTH_FAIL = 0x8802;
        public static final int GL_STENCIL_BACK_PASS_DEPTH_PASS = 0x8803;
        public static final int GL_STENCIL_BACK_REF = 0x8CA3;
        public static final int GL_STENCIL_BACK_VALUE_MASK = 0x8CA4;
        public static final int GL_STENCIL_BACK_WRITEMASK = 0x8CA5;
        public static final int GL_VIEWPORT = 0x0BA2;
        public static final int GL_SCISSOR_BOX = 0x0C10;
        public static final int GL_COLOR_CLEAR_VALUE = 0x0C22;
        public static final int GL_COLOR_WRITEMASK = 0x0C23;
        public static final int GL_UNPACK_ALIGNMENT = 0x0CF5;
        public static final int GL_PACK_ALIGNMENT = 0x0D05;
        public static final int GL_MAX_TEXTURE_SIZE = 0x0D33;
        public static final int GL_MAX_VIEWPORT_DIMS = 0x0D3A;
        public static final int GL_SUBPIXEL_BITS = 0x0D50;
        public static final int GL_RED_BITS = 0x0D52;
        public static final int GL_GREEN_BITS = 0x0D53;
        public static final int GL_BLUE_BITS = 0x0D54;
        public static final int GL_ALPHA_BITS = 0x0D55;
        public static final int GL_DEPTH_BITS = 0x0D56;
        public static final int GL_STENCIL_BITS = 0x0D57;
        public static final int GL_POLYGON_OFFSET_UNITS = 0x2A00;
        public static final int GL_POLYGON_OFFSET_FACTOR = 0x8038;
        public static final int GL_TEXTURE_BINDING_2D = 0x8069;
        public static final int GL_SAMPLE_BUFFERS = 0x80A8;
        public static final int GL_SAMPLES = 0x80A9;
        public static final int GL_SAMPLE_COVERAGE_VALUE = 0x80AA;
        public static final int GL_SAMPLE_COVERAGE_INVERT = 0x80AB;
        public static final int GL_NUM_COMPRESSED_TEXTURE_FORMATS = 0x86A2;
        public static final int GL_COMPRESSED_TEXTURE_FORMATS = 0x86A3;
        public static final int GL_DONT_CARE = 0x1100;
        public static final int GL_FASTEST = 0x1101;
        public static final int GL_NICEST = 0x1102;
        public static final int GL_GENERATE_MIPMAP_HINT = 0x8192;
        public static final int GL_BYTE = 0x1400;
        public static final int GL_UNSIGNED_BYTE = 0x1401;
        public static final int GL_SHORT = 0x1402;
        public static final int GL_UNSIGNED_SHORT = 0x1403;
        public static final int GL_INT = 0x1404;
        public static final int GL_UNSIGNED_INT = 0x1405;
        public static final int GL_FLOAT = 0x1406;
        public static final int GL_FIXED = 0x140C;
        public static final int GL_DEPTH_COMPONENT = 0x1902;
        public static final int GL_ALPHA = 0x1906;
        public static final int GL_RGB = 0x1907;
        public static final int GL_RGBA = 0x1908;
        public static final int GL_LUMINANCE = 0x1909;
        public static final int GL_LUMINANCE_ALPHA = 0x190A;
        public static final int GL_UNSIGNED_SHORT_4_4_4_4 = 0x8033;
        public static final int GL_UNSIGNED_SHORT_5_5_5_1 = 0x8034;
        public static final int GL_UNSIGNED_SHORT_5_6_5 = 0x8363;
        public static final int GL_FRAGMENT_SHADER = 0x8B30;
        public static final int GL_VERTEX_SHADER = 0x8B31;
        public static final int GL_MAX_VERTEX_ATTRIBS = 0x8869;
        public static final int GL_MAX_VERTEX_UNIFORM_VECTORS = 0x8DFB;
        public static final int GL_MAX_VARYING_VECTORS = 0x8DFC;
        public static final int GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS = 0x8B4D;
        public static final int GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS = 0x8B4C;
        public static final int GL_MAX_TEXTURE_IMAGE_UNITS = 0x8872;
        public static final int GL_MAX_FRAGMENT_UNIFORM_VECTORS = 0x8DFD;
        public static final int GL_SHADER_TYPE = 0x8B4F;
        public static final int GL_DELETE_STATUS = 0x8B80;
        public static final int GL_LINK_STATUS = 0x8B82;
        public static final int GL_VALIDATE_STATUS = 0x8B83;
        public static final int GL_ATTACHED_SHADERS = 0x8B85;
        public static final int GL_ACTIVE_UNIFORMS = 0x8B86;
        public static final int GL_ACTIVE_UNIFORM_MAX_LENGTH = 0x8B87;
        public static final int GL_ACTIVE_ATTRIBUTES = 0x8B89;
        public static final int GL_ACTIVE_ATTRIBUTE_MAX_LENGTH = 0x8B8A;
        public static final int GL_SHADING_LANGUAGE_VERSION = 0x8B8C;
        public static final int GL_CURRENT_PROGRAM = 0x8B8D;
        public static final int GL_NEVER = 0x0200;
        public static final int GL_LESS = 0x0201;
        public static final int GL_EQUAL = 0x0202;
        public static final int GL_LEQUAL = 0x0203;
        public static final int GL_GREATER = 0x0204;
        public static final int GL_NOTEQUAL = 0x0205;
        public static final int GL_GEQUAL = 0x0206;
        public static final int GL_ALWAYS = 0x0207;
        public static final int GL_KEEP = 0x1E00;
        public static final int GL_REPLACE = 0x1E01;
        public static final int GL_INCR = 0x1E02;
        public static final int GL_DECR = 0x1E03;
        public static final int GL_INVERT = 0x150A;
        public static final int GL_INCR_WRAP = 0x8507;
        public static final int GL_DECR_WRAP = 0x8508;
        public static final int GL_VENDOR = 0x1F00;
        public static final int GL_RENDERER = 0x1F01;
        public static final int GL_VERSION = 0x1F02;
        public static final int GL_EXTENSIONS = 0x1F03;
        public static final int GL_NEAREST = 0x2600;
        public static final int GL_LINEAR = 0x2601;
        public static final int GL_NEAREST_MIPMAP_NEAREST = 0x2700;
        public static final int GL_LINEAR_MIPMAP_NEAREST = 0x2701;
        public static final int GL_NEAREST_MIPMAP_LINEAR = 0x2702;
        public static final int GL_LINEAR_MIPMAP_LINEAR = 0x2703;
        public static final int GL_TEXTURE_MAG_FILTER = 0x2800;
        public static final int GL_TEXTURE_MIN_FILTER = 0x2801;
        public static final int GL_TEXTURE_WRAP_S = 0x2802;
        public static final int GL_TEXTURE_WRAP_T = 0x2803;
        public static final int GL_TEXTURE = 0x1702;
        public static final int GL_TEXTURE_CUBE_MAP = 0x8513;
        public static final int GL_TEXTURE_BINDING_CUBE_MAP = 0x8514;
        public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515;
        public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516;
        public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517;
        public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518;
        public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519;
        public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A;
        public static final int GL_MAX_CUBE_MAP_TEXTURE_SIZE = 0x851C;
        public static final int GL_TEXTURE0 = 0x84C0;
        public static final int GL_TEXTURE1 = 0x84C1;
        public static final int GL_TEXTURE2 = 0x84C2;
        public static final int GL_TEXTURE3 = 0x84C3;
        public static final int GL_TEXTURE4 = 0x84C4;
        public static final int GL_TEXTURE5 = 0x84C5;
        public static final int GL_TEXTURE6 = 0x84C6;
        public static final int GL_TEXTURE7 = 0x84C7;
        public static final int GL_TEXTURE8 = 0x84C8;
        public static final int GL_TEXTURE9 = 0x84C9;
        public static final int GL_TEXTURE10 = 0x84CA;
        public static final int GL_TEXTURE11 = 0x84CB;
        public static final int GL_TEXTURE12 = 0x84CC;
        public static final int GL_TEXTURE13 = 0x84CD;
        public static final int GL_TEXTURE14 = 0x84CE;
        public static final int GL_TEXTURE15 = 0x84CF;
        public static final int GL_TEXTURE16 = 0x84D0;
        public static final int GL_TEXTURE17 = 0x84D1;
        public static final int GL_TEXTURE18 = 0x84D2;
        public static final int GL_TEXTURE19 = 0x84D3;
        public static final int GL_TEXTURE20 = 0x84D4;
        public static final int GL_TEXTURE21 = 0x84D5;
        public static final int GL_TEXTURE22 = 0x84D6;
        public static final int GL_TEXTURE23 = 0x84D7;
        public static final int GL_TEXTURE24 = 0x84D8;
        public static final int GL_TEXTURE25 = 0x84D9;
        public static final int GL_TEXTURE26 = 0x84DA;
        public static final int GL_TEXTURE27 = 0x84DB;
        public static final int GL_TEXTURE28 = 0x84DC;
        public static final int GL_TEXTURE29 = 0x84DD;
        public static final int GL_TEXTURE30 = 0x84DE;
        public static final int GL_TEXTURE31 = 0x84DF;
        public static final int GL_REPEAT = 0x2901;
        public static final int GL_CLAMP_TO_EDGE = 0x812F;
        public static final int GL_MIRRORED_REPEAT = 0x8370;
        public static final int GL_FLOAT_VEC2 = 0x8B50;
        public static final int GL_FLOAT_VEC3 = 0x8B51;
        public static final int GL_FLOAT_VEC4 = 0x8B52;
        public static final int GL_INT_VEC2 = 0x8B53;
        public static final int GL_INT_VEC3 = 0x8B54;
        public static final int GL_INT_VEC4 = 0x8B55;
        public static final int GL_BOOL = 0x8B56;
        public static final int GL_BOOL_VEC2 = 0x8B57;
        public static final int GL_BOOL_VEC3 = 0x8B58;
        public static final int GL_BOOL_VEC4 = 0x8B59;
        public static final int GL_FLOAT_MAT2 = 0x8B5A;
        public static final int GL_FLOAT_MAT3 = 0x8B5B;
        public static final int GL_FLOAT_MAT4 = 0x8B5C;
        public static final int GL_SAMPLER_2D = 0x8B5E;
        public static final int GL_SAMPLER_CUBE = 0x8B60;
        public static final int GL_VERTEX_ATTRIB_ARRAY_ENABLED = 0x8622;
        public static final int GL_VERTEX_ATTRIB_ARRAY_SIZE = 0x8623;
        public static final int GL_VERTEX_ATTRIB_ARRAY_STRIDE = 0x8624;
        public static final int GL_VERTEX_ATTRIB_ARRAY_TYPE = 0x8625;
        public static final int GL_VERTEX_ATTRIB_ARRAY_NORMALIZED = 0x886A;
        public static final int GL_VERTEX_ATTRIB_ARRAY_POINTER = 0x8645;
        public static final int GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = 0x889F;
        public static final int GL_IMPLEMENTATION_COLOR_READ_TYPE = 0x8B9A;
        public static final int GL_IMPLEMENTATION_COLOR_READ_FORMAT = 0x8B9B;
        public static final int GL_COMPILE_STATUS = 0x8B81;
        public static final int GL_INFO_LOG_LENGTH = 0x8B84;
        public static final int GL_SHADER_SOURCE_LENGTH = 0x8B88;
        public static final int GL_SHADER_COMPILER = 0x8DFA;
        public static final int GL_SHADER_BINARY_FORMATS = 0x8DF8;
        public static final int GL_NUM_SHADER_BINARY_FORMATS = 0x8DF9;
        public static final int GL_LOW_FLOAT = 0x8DF0;
        public static final int GL_MEDIUM_FLOAT = 0x8DF1;
        public static final int GL_HIGH_FLOAT = 0x8DF2;
        public static final int GL_LOW_INT = 0x8DF3;
        public static final int GL_MEDIUM_INT = 0x8DF4;
        public static final int GL_HIGH_INT = 0x8DF5;
        public static final int GL_FRAMEBUFFER = 0x8D40;
        public static final int GL_RENDERBUFFER = 0x8D41;
        public static final int GL_RGBA4 = 0x8056;
        public static final int GL_RGB5_A1 = 0x8057;
        public static final int GL_RGB565 = 0x8D62;
        public static final int GL_DEPTH_COMPONENT16 = 0x81A5;
        public static final int GL_STENCIL_INDEX8 = 0x8D48;
        public static final int GL_RENDERBUFFER_WIDTH = 0x8D42;
        public static final int GL_RENDERBUFFER_HEIGHT = 0x8D43;
        public static final int GL_RENDERBUFFER_INTERNAL_FORMAT = 0x8D44;
        public static final int GL_RENDERBUFFER_RED_SIZE = 0x8D50;
        public static final int GL_RENDERBUFFER_GREEN_SIZE = 0x8D51;
        public static final int GL_RENDERBUFFER_BLUE_SIZE = 0x8D52;
        public static final int GL_RENDERBUFFER_ALPHA_SIZE = 0x8D53;
        public static final int GL_RENDERBUFFER_DEPTH_SIZE = 0x8D54;
        public static final int GL_RENDERBUFFER_STENCIL_SIZE = 0x8D55;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE = 0x8CD0;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME = 0x8CD1;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL = 0x8CD2;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = 0x8CD3;
        public static final int GL_COLOR_ATTACHMENT0 = 0x8CE0;
        public static final int GL_DEPTH_ATTACHMENT = 0x8D00;
        public static final int GL_STENCIL_ATTACHMENT = 0x8D20;
        public static final int GL_NONE = 0;
        public static final int GL_FRAMEBUFFER_COMPLETE = 0x8CD5;
        public static final int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 0x8CD6;
        public static final int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 0x8CD7;
        public static final int GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS = 0x8CD9;
        public static final int GL_FRAMEBUFFER_UNSUPPORTED = 0x8CDD;
        public static final int GL_FRAMEBUFFER_BINDING = 0x8CA6;
        public static final int GL_RENDERBUFFER_BINDING = 0x8CA7;
        public static final int GL_MAX_RENDERBUFFER_SIZE = 0x84E8;
        public static final int GL_INVALID_FRAMEBUFFER_OPERATION = 0x0506;

    }

    public abstract class GLES30 extends GLES20 {

        public static final int GL_READ_BUFFER = 0x0C02;
        public static final int GL_UNPACK_ROW_LENGTH = 0x0CF2;
        public static final int GL_UNPACK_SKIP_ROWS = 0x0CF3;
        public static final int GL_UNPACK_SKIP_PIXELS = 0x0CF4;
        public static final int GL_PACK_ROW_LENGTH = 0x0D02;
        public static final int GL_PACK_SKIP_ROWS = 0x0D03;
        public static final int GL_PACK_SKIP_PIXELS = 0x0D04;
        public static final int GL_COLOR = 0x1800;
        public static final int GL_DEPTH = 0x1801;
        public static final int GL_STENCIL = 0x1802;
        public static final int GL_RED = 0x1903;
        public static final int GL_RGB8 = 0x8051;
        public static final int GL_RGBA8 = 0x8058;
        public static final int GL_RGB10_A2 = 0x8059;
        public static final int GL_TEXTURE_BINDING_3D = 0x806A;
        public static final int GL_UNPACK_SKIP_IMAGES = 0x806D;
        public static final int GL_UNPACK_IMAGE_HEIGHT = 0x806E;
        public static final int GL_TEXTURE_3D = 0x806F;
        public static final int GL_TEXTURE_WRAP_R = 0x8072;
        public static final int GL_MAX_3D_TEXTURE_SIZE = 0x8073;
        public static final int GL_UNSIGNED_INT_2_10_10_10_REV = 0x8368;
        public static final int GL_MAX_ELEMENTS_VERTICES = 0x80E8;
        public static final int GL_MAX_ELEMENTS_INDICES = 0x80E9;
        public static final int GL_TEXTURE_MIN_LOD = 0x813A;
        public static final int GL_TEXTURE_MAX_LOD = 0x813B;
        public static final int GL_TEXTURE_BASE_LEVEL = 0x813C;
        public static final int GL_TEXTURE_MAX_LEVEL = 0x813D;
        public static final int GL_MIN = 0x8007;
        public static final int GL_MAX = 0x8008;
        public static final int GL_DEPTH_COMPONENT24 = 0x81A6;
        public static final int GL_MAX_TEXTURE_LOD_BIAS = 0x84FD;
        public static final int GL_TEXTURE_COMPARE_MODE = 0x884C;
        public static final int GL_TEXTURE_COMPARE_FUNC = 0x884D;
        public static final int GL_CURRENT_QUERY = 0x8865;
        public static final int GL_QUERY_RESULT = 0x8866;
        public static final int GL_QUERY_RESULT_AVAILABLE = 0x8867;
        public static final int GL_BUFFER_MAPPED = 0x88BC;
        public static final int GL_BUFFER_MAP_POINTER = 0x88BD;
        public static final int GL_STREAM_READ = 0x88E1;
        public static final int GL_STREAM_COPY = 0x88E2;
        public static final int GL_STATIC_READ = 0x88E5;
        public static final int GL_STATIC_COPY = 0x88E6;
        public static final int GL_DYNAMIC_READ = 0x88E9;
        public static final int GL_DYNAMIC_COPY = 0x88EA;
        public static final int GL_MAX_DRAW_BUFFERS = 0x8824;
        public static final int GL_DRAW_BUFFER0 = 0x8825;
        public static final int GL_DRAW_BUFFER1 = 0x8826;
        public static final int GL_DRAW_BUFFER2 = 0x8827;
        public static final int GL_DRAW_BUFFER3 = 0x8828;
        public static final int GL_DRAW_BUFFER4 = 0x8829;
        public static final int GL_DRAW_BUFFER5 = 0x882A;
        public static final int GL_DRAW_BUFFER6 = 0x882B;
        public static final int GL_DRAW_BUFFER7 = 0x882C;
        public static final int GL_DRAW_BUFFER8 = 0x882D;
        public static final int GL_DRAW_BUFFER9 = 0x882E;
        public static final int GL_DRAW_BUFFER10 = 0x882F;
        public static final int GL_DRAW_BUFFER11 = 0x8830;
        public static final int GL_DRAW_BUFFER12 = 0x8831;
        public static final int GL_DRAW_BUFFER13 = 0x8832;
        public static final int GL_DRAW_BUFFER14 = 0x8833;
        public static final int GL_DRAW_BUFFER15 = 0x8834;
        public static final int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS = 0x8B49;
        public static final int GL_MAX_VERTEX_UNIFORM_COMPONENTS = 0x8B4A;
        public static final int GL_SAMPLER_3D = 0x8B5F;
        public static final int GL_SAMPLER_2D_SHADOW = 0x8B62;
        public static final int GL_FRAGMENT_SHADER_DERIVATIVE_HINT = 0x8B8B;
        public static final int GL_PIXEL_PACK_BUFFER = 0x88EB;
        public static final int GL_PIXEL_UNPACK_BUFFER = 0x88EC;
        public static final int GL_PIXEL_PACK_BUFFER_BINDING = 0x88ED;
        public static final int GL_PIXEL_UNPACK_BUFFER_BINDING = 0x88EF;
        public static final int GL_FLOAT_MAT2x3 = 0x8B65;
        public static final int GL_FLOAT_MAT2x4 = 0x8B66;
        public static final int GL_FLOAT_MAT3x2 = 0x8B67;
        public static final int GL_FLOAT_MAT3x4 = 0x8B68;
        public static final int GL_FLOAT_MAT4x2 = 0x8B69;
        public static final int GL_FLOAT_MAT4x3 = 0x8B6A;
        public static final int GL_SRGB = 0x8C40;
        public static final int GL_SRGB8 = 0x8C41;
        public static final int GL_SRGB8_ALPHA8 = 0x8C43;
        public static final int GL_COMPARE_REF_TO_TEXTURE = 0x884E;
        public static final int GL_MAJOR_VERSION = 0x821B;
        public static final int GL_MINOR_VERSION = 0x821C;
        public static final int GL_NUM_EXTENSIONS = 0x821D;
        public static final int GL_RGBA32F = 0x8814;
        public static final int GL_RGB32F = 0x8815;
        public static final int GL_RGBA16F = 0x881A;
        public static final int GL_RGB16F = 0x881B;
        public static final int GL_VERTEX_ATTRIB_ARRAY_INTEGER = 0x88FD;
        public static final int GL_MAX_ARRAY_TEXTURE_LAYERS = 0x88FF;
        public static final int GL_MIN_PROGRAM_TEXEL_OFFSET = 0x8904;
        public static final int GL_MAX_PROGRAM_TEXEL_OFFSET = 0x8905;
        public static final int GL_MAX_VARYING_COMPONENTS = 0x8B4B;
        public static final int GL_TEXTURE_2D_ARRAY = 0x8C1A;
        public static final int GL_TEXTURE_BINDING_2D_ARRAY = 0x8C1D;
        public static final int GL_R11F_G11F_B10F = 0x8C3A;
        public static final int GL_UNSIGNED_INT_10F_11F_11F_REV = 0x8C3B;
        public static final int GL_RGB9_E5 = 0x8C3D;
        public static final int GL_UNSIGNED_INT_5_9_9_9_REV = 0x8C3E;
        public static final int GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH = 0x8C76;
        public static final int GL_TRANSFORM_FEEDBACK_BUFFER_MODE = 0x8C7F;
        public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS = 0x8C80;
        public static final int GL_TRANSFORM_FEEDBACK_VARYINGS = 0x8C83;
        public static final int GL_TRANSFORM_FEEDBACK_BUFFER_START = 0x8C84;
        public static final int GL_TRANSFORM_FEEDBACK_BUFFER_SIZE = 0x8C85;
        public static final int GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN = 0x8C88;
        public static final int GL_RASTERIZER_DISCARD = 0x8C89;
        public static final int GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS = 0x8C8A;
        public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS = 0x8C8B;
        public static final int GL_INTERLEAVED_ATTRIBS = 0x8C8C;
        public static final int GL_SEPARATE_ATTRIBS = 0x8C8D;
        public static final int GL_TRANSFORM_FEEDBACK_BUFFER = 0x8C8E;
        public static final int GL_TRANSFORM_FEEDBACK_BUFFER_BINDING = 0x8C8F;
        public static final int GL_RGBA32UI = 0x8D70;
        public static final int GL_RGB32UI = 0x8D71;
        public static final int GL_RGBA16UI = 0x8D76;
        public static final int GL_RGB16UI = 0x8D77;
        public static final int GL_RGBA8UI = 0x8D7C;
        public static final int GL_RGB8UI = 0x8D7D;
        public static final int GL_RGBA32I = 0x8D82;
        public static final int GL_RGB32I = 0x8D83;
        public static final int GL_RGBA16I = 0x8D88;
        public static final int GL_RGB16I = 0x8D89;
        public static final int GL_RGBA8I = 0x8D8E;
        public static final int GL_RGB8I = 0x8D8F;
        public static final int GL_RED_INTEGER = 0x8D94;
        public static final int GL_RGB_INTEGER = 0x8D98;
        public static final int GL_RGBA_INTEGER = 0x8D99;
        public static final int GL_SAMPLER_2D_ARRAY = 0x8DC1;
        public static final int GL_SAMPLER_2D_ARRAY_SHADOW = 0x8DC4;
        public static final int GL_SAMPLER_CUBE_SHADOW = 0x8DC5;
        public static final int GL_UNSIGNED_INT_VEC2 = 0x8DC6;
        public static final int GL_UNSIGNED_INT_VEC3 = 0x8DC7;
        public static final int GL_UNSIGNED_INT_VEC4 = 0x8DC8;
        public static final int GL_INT_SAMPLER_2D = 0x8DCA;
        public static final int GL_INT_SAMPLER_3D = 0x8DCB;
        public static final int GL_INT_SAMPLER_CUBE = 0x8DCC;
        public static final int GL_INT_SAMPLER_2D_ARRAY = 0x8DCF;
        public static final int GL_UNSIGNED_INT_SAMPLER_2D = 0x8DD2;
        public static final int GL_UNSIGNED_INT_SAMPLER_3D = 0x8DD3;
        public static final int GL_UNSIGNED_INT_SAMPLER_CUBE = 0x8DD4;
        public static final int GL_UNSIGNED_INT_SAMPLER_2D_ARRAY = 0x8DD7;
        public static final int GL_BUFFER_ACCESS_FLAGS = 0x911F;
        public static final int GL_BUFFER_MAP_LENGTH = 0x9120;
        public static final int GL_BUFFER_MAP_OFFSET = 0x9121;
        public static final int GL_DEPTH_COMPONENT32F = 0x8CAC;
        public static final int GL_DEPTH32F_STENCIL8 = 0x8CAD;
        public static final int GL_FLOAT_32_UNSIGNED_INT_24_8_REV = 0x8DAD;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING = 0x8210;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE = 0x8211;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE = 0x8212;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE = 0x8213;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE = 0x8214;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE = 0x8215;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE = 0x8216;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE = 0x8217;
        public static final int GL_FRAMEBUFFER_DEFAULT = 0x8218;
        public static final int GL_FRAMEBUFFER_UNDEFINED = 0x8219;
        public static final int GL_DEPTH_STENCIL_ATTACHMENT = 0x821A;
        public static final int GL_DEPTH_STENCIL = 0x84F9;
        public static final int GL_UNSIGNED_INT_24_8 = 0x84FA;
        public static final int GL_DEPTH24_STENCIL8 = 0x88F0;
        public static final int GL_UNSIGNED_NORMALIZED = 0x8C17;
        public static final int GL_DRAW_FRAMEBUFFER_BINDING = 0x8CA6;
        public static final int GL_READ_FRAMEBUFFER = 0x8CA8;
        public static final int GL_DRAW_FRAMEBUFFER = 0x8CA9;
        public static final int GL_READ_FRAMEBUFFER_BINDING = 0x8CAA;
        public static final int GL_RENDERBUFFER_SAMPLES = 0x8CAB;
        public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER = 0x8CD4;
        public static final int GL_MAX_COLOR_ATTACHMENTS = 0x8CDF;
        public static final int GL_COLOR_ATTACHMENT1 = 0x8CE1;
        public static final int GL_COLOR_ATTACHMENT2 = 0x8CE2;
        public static final int GL_COLOR_ATTACHMENT3 = 0x8CE3;
        public static final int GL_COLOR_ATTACHMENT4 = 0x8CE4;
        public static final int GL_COLOR_ATTACHMENT5 = 0x8CE5;
        public static final int GL_COLOR_ATTACHMENT6 = 0x8CE6;
        public static final int GL_COLOR_ATTACHMENT7 = 0x8CE7;
        public static final int GL_COLOR_ATTACHMENT8 = 0x8CE8;
        public static final int GL_COLOR_ATTACHMENT9 = 0x8CE9;
        public static final int GL_COLOR_ATTACHMENT10 = 0x8CEA;
        public static final int GL_COLOR_ATTACHMENT11 = 0x8CEB;
        public static final int GL_COLOR_ATTACHMENT12 = 0x8CEC;
        public static final int GL_COLOR_ATTACHMENT13 = 0x8CED;
        public static final int GL_COLOR_ATTACHMENT14 = 0x8CEE;
        public static final int GL_COLOR_ATTACHMENT15 = 0x8CEF;
        public static final int GL_COLOR_ATTACHMENT16 = 0x8CF0;
        public static final int GL_COLOR_ATTACHMENT17 = 0x8CF1;
        public static final int GL_COLOR_ATTACHMENT18 = 0x8CF2;
        public static final int GL_COLOR_ATTACHMENT19 = 0x8CF3;
        public static final int GL_COLOR_ATTACHMENT20 = 0x8CF4;
        public static final int GL_COLOR_ATTACHMENT21 = 0x8CF5;
        public static final int GL_COLOR_ATTACHMENT22 = 0x8CF6;
        public static final int GL_COLOR_ATTACHMENT23 = 0x8CF7;
        public static final int GL_COLOR_ATTACHMENT24 = 0x8CF8;
        public static final int GL_COLOR_ATTACHMENT25 = 0x8CF9;
        public static final int GL_COLOR_ATTACHMENT26 = 0x8CFA;
        public static final int GL_COLOR_ATTACHMENT27 = 0x8CFB;
        public static final int GL_COLOR_ATTACHMENT28 = 0x8CFC;
        public static final int GL_COLOR_ATTACHMENT29 = 0x8CFD;
        public static final int GL_COLOR_ATTACHMENT30 = 0x8CFE;
        public static final int GL_COLOR_ATTACHMENT31 = 0x8CFF;
        public static final int GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = 0x8D56;
        public static final int GL_MAX_SAMPLES = 0x8D57;
        public static final int GL_HALF_FLOAT = 0x140B;
        public static final int GL_MAP_READ_BIT = 0x0001;
        public static final int GL_MAP_WRITE_BIT = 0x0002;
        public static final int GL_MAP_INVALIDATE_RANGE_BIT = 0x0004;
        public static final int GL_MAP_INVALIDATE_BUFFER_BIT = 0x0008;
        public static final int GL_MAP_FLUSH_EXPLICIT_BIT = 0x0010;
        public static final int GL_MAP_UNSYNCHRONIZED_BIT = 0x0020;
        public static final int GL_RG = 0x8227;
        public static final int GL_RG_INTEGER = 0x8228;
        public static final int GL_R8 = 0x8229;
        public static final int GL_RG8 = 0x822B;
        public static final int GL_R16F = 0x822D;
        public static final int GL_R32F = 0x822E;
        public static final int GL_RG16F = 0x822F;
        public static final int GL_RG32F = 0x8230;
        public static final int GL_R8I = 0x8231;
        public static final int GL_R8UI = 0x8232;
        public static final int GL_R16I = 0x8233;
        public static final int GL_R16UI = 0x8234;
        public static final int GL_R32I = 0x8235;
        public static final int GL_R32UI = 0x8236;
        public static final int GL_RG8I = 0x8237;
        public static final int GL_RG8UI = 0x8238;
        public static final int GL_RG16I = 0x8239;
        public static final int GL_RG16UI = 0x823A;
        public static final int GL_RG32I = 0x823B;
        public static final int GL_RG32UI = 0x823C;
        public static final int GL_VERTEX_ARRAY_BINDING = 0x85B5;
        public static final int GL_R8_SNORM = 0x8F94;
        public static final int GL_RG8_SNORM = 0x8F95;
        public static final int GL_RGB8_SNORM = 0x8F96;
        public static final int GL_RGBA8_SNORM = 0x8F97;
        public static final int GL_SIGNED_NORMALIZED = 0x8F9C;
        public static final int GL_PRIMITIVE_RESTART_FIXED_INDEX = 0x8D69;
        public static final int GL_COPY_READ_BUFFER = 0x8F36;
        public static final int GL_COPY_WRITE_BUFFER = 0x8F37;
        public static final int GL_COPY_READ_BUFFER_BINDING = 0x8F36;
        public static final int GL_COPY_WRITE_BUFFER_BINDING = 0x8F37;
        public static final int GL_UNIFORM_BUFFER = 0x8A11;
        public static final int GL_UNIFORM_BUFFER_BINDING = 0x8A28;
        public static final int GL_UNIFORM_BUFFER_START = 0x8A29;
        public static final int GL_UNIFORM_BUFFER_SIZE = 0x8A2A;
        public static final int GL_MAX_VERTEX_UNIFORM_BLOCKS = 0x8A2B;
        public static final int GL_MAX_FRAGMENT_UNIFORM_BLOCKS = 0x8A2D;
        public static final int GL_MAX_COMBINED_UNIFORM_BLOCKS = 0x8A2E;
        public static final int GL_MAX_UNIFORM_BUFFER_BINDINGS = 0x8A2F;
        public static final int GL_MAX_UNIFORM_BLOCK_SIZE = 0x8A30;
        public static final int GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS = 0x8A31;
        public static final int GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS = 0x8A33;
        public static final int GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT = 0x8A34;
        public static final int GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH = 0x8A35;
        public static final int GL_ACTIVE_UNIFORM_BLOCKS = 0x8A36;
        public static final int GL_UNIFORM_TYPE = 0x8A37;
        public static final int GL_UNIFORM_SIZE = 0x8A38;
        public static final int GL_UNIFORM_NAME_LENGTH = 0x8A39;
        public static final int GL_UNIFORM_BLOCK_INDEX = 0x8A3A;
        public static final int GL_UNIFORM_OFFSET = 0x8A3B;
        public static final int GL_UNIFORM_ARRAY_STRIDE = 0x8A3C;
        public static final int GL_UNIFORM_MATRIX_STRIDE = 0x8A3D;
        public static final int GL_UNIFORM_IS_ROW_MAJOR = 0x8A3E;
        public static final int GL_UNIFORM_BLOCK_BINDING = 0x8A3F;
        public static final int GL_UNIFORM_BLOCK_DATA_SIZE = 0x8A40;
        public static final int GL_UNIFORM_BLOCK_NAME_LENGTH = 0x8A41;
        public static final int GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS = 0x8A42;
        public static final int GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES = 0x8A43;
        public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER = 0x8A44;
        public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER = 0x8A46;
        public static final int GL_INVALID_INDEX = 0xFFFFFFFF;
        public static final int GL_MAX_VERTEX_OUTPUT_COMPONENTS = 0x9122;
        public static final int GL_MAX_FRAGMENT_INPUT_COMPONENTS = 0x9125;
        public static final int GL_MAX_SERVER_WAIT_TIMEOUT = 0x9111;
        public static final int GL_OBJECT_TYPE = 0x9112;
        public static final int GL_SYNC_CONDITION = 0x9113;
        public static final int GL_SYNC_STATUS = 0x9114;
        public static final int GL_SYNC_FLAGS = 0x9115;
        public static final int GL_SYNC_FENCE = 0x9116;
        public static final int GL_SYNC_GPU_COMMANDS_COMPLETE = 0x9117;
        public static final int GL_UNSIGNALED = 0x9118;
        public static final int GL_SIGNALED = 0x9119;
        public static final int GL_ALREADY_SIGNALED = 0x911A;
        public static final int GL_TIMEOUT_EXPIRED = 0x911B;
        public static final int GL_CONDITION_SATISFIED = 0x911C;
        public static final int GL_WAIT_FAILED = 0x911D;
        public static final int GL_SYNC_FLUSH_COMMANDS_BIT = 0x00000001;
        public static final int GL_TIMEOUT_IGNORED = 0xFFFFFFFF;
        public static final int GL_VERTEX_ATTRIB_ARRAY_DIVISOR = 0x88FE;
        public static final int GL_ANY_SAMPLES_PASSED = 0x8C2F;
        public static final int GL_ANY_SAMPLES_PASSED_CONSERVATIVE = 0x8D6A;
        public static final int GL_SAMPLER_BINDING = 0x8919;
        public static final int GL_RGB10_A2UI = 0x906F;
        public static final int GL_TEXTURE_SWIZZLE_R = 0x8E42;
        public static final int GL_TEXTURE_SWIZZLE_G = 0x8E43;
        public static final int GL_TEXTURE_SWIZZLE_B = 0x8E44;
        public static final int GL_TEXTURE_SWIZZLE_A = 0x8E45;
        public static final int GL_GREEN = 0x1904;
        public static final int GL_BLUE = 0x1905;
        public static final int GL_INT_2_10_10_10_REV = 0x8D9F;
        public static final int GL_TRANSFORM_FEEDBACK = 0x8E22;
        public static final int GL_TRANSFORM_FEEDBACK_PAUSED = 0x8E23;
        public static final int GL_TRANSFORM_FEEDBACK_ACTIVE = 0x8E24;
        public static final int GL_TRANSFORM_FEEDBACK_BINDING = 0x8E25;
        public static final int GL_PROGRAM_BINARY_RETRIEVABLE_HINT = 0x8257;
        public static final int GL_PROGRAM_BINARY_LENGTH = 0x8741;
        public static final int GL_NUM_PROGRAM_BINARY_FORMATS = 0x87FE;
        public static final int GL_PROGRAM_BINARY_FORMATS = 0x87FF;
        public static final int GL_COMPRESSED_R11_EAC = 0x9270;
        public static final int GL_COMPRESSED_SIGNED_R11_EAC = 0x9271;
        public static final int GL_COMPRESSED_RG11_EAC = 0x9272;
        public static final int GL_COMPRESSED_SIGNED_RG11_EAC = 0x9273;
        public static final int GL_COMPRESSED_RGB8_ETC2 = 0x9274;
        public static final int GL_COMPRESSED_SRGB8_ETC2 = 0x9275;
        public static final int GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9276;
        public static final int GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9277;
        public static final int GL_COMPRESSED_RGBA8_ETC2_EAC = 0x9278;
        public static final int GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC = 0x9279;
        public static final int GL_TEXTURE_IMMUTABLE_FORMAT = 0x912F;
        public static final int GL_MAX_ELEMENT_INDEX = 0x8D6B;
        public static final int GL_NUM_SAMPLE_COUNTS = 0x9380;
        public static final int GL_TEXTURE_IMMUTABLE_LEVELS = 0x82DF;

    }

    public abstract class GLES31 extends GLES20 {
        public static final int GL_COMPUTE_SHADER = 0x91B9;
        public static final int GL_MAX_COMPUTE_UNIFORM_BLOCKS = 0x91BB;
        public static final int GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS = 0x91BC;
        public static final int GL_MAX_COMPUTE_IMAGE_UNIFORMS = 0x91BD;
        public static final int GL_MAX_COMPUTE_SHARED_MEMORY_SIZE = 0x8262;
        public static final int GL_MAX_COMPUTE_UNIFORM_COMPONENTS = 0x8263;
        public static final int GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS = 0x8264;
        public static final int GL_MAX_COMPUTE_ATOMIC_COUNTERS = 0x8265;
        public static final int GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS = 0x8266;
        public static final int GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS = 0x90EB;
        public static final int GL_MAX_COMPUTE_WORK_GROUP_COUNT = 0x91BE;
        public static final int GL_MAX_COMPUTE_WORK_GROUP_SIZE = 0x91BF;
        public static final int GL_COMPUTE_WORK_GROUP_SIZE = 0x8267;
        public static final int GL_DISPATCH_INDIRECT_BUFFER = 0x90EE;
        public static final int GL_DISPATCH_INDIRECT_BUFFER_BINDING = 0x90EF;
        public static final int GL_COMPUTE_SHADER_BIT = 0x00000020;
        public static final int GL_DRAW_INDIRECT_BUFFER = 0x8F3F;
        public static final int GL_DRAW_INDIRECT_BUFFER_BINDING = 0x8F43;
        public static final int GL_MAX_UNIFORM_LOCATIONS = 0x826E;
        public static final int GL_FRAMEBUFFER_DEFAULT_WIDTH = 0x9310;
        public static final int GL_FRAMEBUFFER_DEFAULT_HEIGHT = 0x9311;
        public static final int GL_FRAMEBUFFER_DEFAULT_SAMPLES = 0x9313;
        public static final int GL_FRAMEBUFFER_DEFAULT_FIXED_SAMPLE_LOCATIONS = 0x9314;
        public static final int GL_MAX_FRAMEBUFFER_WIDTH = 0x9315;
        public static final int GL_MAX_FRAMEBUFFER_HEIGHT = 0x9316;
        public static final int GL_MAX_FRAMEBUFFER_SAMPLES = 0x9318;
        public static final int GL_UNIFORM = 0x92E1;
        public static final int GL_UNIFORM_BLOCK = 0x92E2;
        public static final int GL_PROGRAM_INPUT = 0x92E3;
        public static final int GL_PROGRAM_OUTPUT = 0x92E4;
        public static final int GL_BUFFER_VARIABLE = 0x92E5;
        public static final int GL_SHADER_STORAGE_BLOCK = 0x92E6;
        public static final int GL_ATOMIC_COUNTER_BUFFER = 0x92C0;
        public static final int GL_TRANSFORM_FEEDBACK_VARYING = 0x92F4;
        public static final int GL_ACTIVE_RESOURCES = 0x92F5;
        public static final int GL_MAX_NAME_LENGTH = 0x92F6;
        public static final int GL_MAX_NUM_ACTIVE_VARIABLES = 0x92F7;
        public static final int GL_NAME_LENGTH = 0x92F9;
        public static final int GL_TYPE = 0x92FA;
        public static final int GL_ARRAY_SIZE = 0x92FB;
        public static final int GL_OFFSET = 0x92FC;
        public static final int GL_BLOCK_INDEX = 0x92FD;
        public static final int GL_ARRAY_STRIDE = 0x92FE;
        public static final int GL_MATRIX_STRIDE = 0x92FF;
        public static final int GL_IS_ROW_MAJOR = 0x9300;
        public static final int GL_ATOMIC_COUNTER_BUFFER_INDEX = 0x9301;
        public static final int GL_BUFFER_BINDING = 0x9302;
        public static final int GL_BUFFER_DATA_SIZE = 0x9303;
        public static final int GL_NUM_ACTIVE_VARIABLES = 0x9304;
        public static final int GL_ACTIVE_VARIABLES = 0x9305;
        public static final int GL_REFERENCED_BY_VERTEX_SHADER = 0x9306;
        public static final int GL_REFERENCED_BY_FRAGMENT_SHADER = 0x930A;
        public static final int GL_REFERENCED_BY_COMPUTE_SHADER = 0x930B;
        public static final int GL_TOP_LEVEL_ARRAY_SIZE = 0x930C;
        public static final int GL_TOP_LEVEL_ARRAY_STRIDE = 0x930D;
        public static final int GL_LOCATION = 0x930E;
        public static final int GL_VERTEX_SHADER_BIT = 0x00000001;
        public static final int GL_FRAGMENT_SHADER_BIT = 0x00000002;
        public static final int GL_ALL_SHADER_BITS = 0xFFFFFFFF;
        public static final int GL_PROGRAM_SEPARABLE = 0x8258;
        public static final int GL_ACTIVE_PROGRAM = 0x8259;
        public static final int GL_PROGRAM_PIPELINE_BINDING = 0x825A;
        public static final int GL_ATOMIC_COUNTER_BUFFER_BINDING = 0x92C1;
        public static final int GL_ATOMIC_COUNTER_BUFFER_START = 0x92C2;
        public static final int GL_ATOMIC_COUNTER_BUFFER_SIZE = 0x92C3;
        public static final int GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS = 0x92CC;
        public static final int GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS = 0x92D0;
        public static final int GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS = 0x92D1;
        public static final int GL_MAX_VERTEX_ATOMIC_COUNTERS = 0x92D2;
        public static final int GL_MAX_FRAGMENT_ATOMIC_COUNTERS = 0x92D6;
        public static final int GL_MAX_COMBINED_ATOMIC_COUNTERS = 0x92D7;
        public static final int GL_MAX_ATOMIC_COUNTER_BUFFER_SIZE = 0x92D8;
        public static final int GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS = 0x92DC;
        public static final int GL_ACTIVE_ATOMIC_COUNTER_BUFFERS = 0x92D9;
        public static final int GL_UNSIGNED_INT_ATOMIC_COUNTER = 0x92DB;
        public static final int GL_MAX_IMAGE_UNITS = 0x8F38;
        public static final int GL_MAX_VERTEX_IMAGE_UNIFORMS = 0x90CA;
        public static final int GL_MAX_FRAGMENT_IMAGE_UNIFORMS = 0x90CE;
        public static final int GL_MAX_COMBINED_IMAGE_UNIFORMS = 0x90CF;
        public static final int GL_IMAGE_BINDING_NAME = 0x8F3A;
        public static final int GL_IMAGE_BINDING_LEVEL = 0x8F3B;
        public static final int GL_IMAGE_BINDING_LAYERED = 0x8F3C;
        public static final int GL_IMAGE_BINDING_LAYER = 0x8F3D;
        public static final int GL_IMAGE_BINDING_ACCESS = 0x8F3E;
        public static final int GL_IMAGE_BINDING_FORMAT = 0x906E;
        public static final int GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT = 0x00000001;
        public static final int GL_ELEMENT_ARRAY_BARRIER_BIT = 0x00000002;
        public static final int GL_UNIFORM_BARRIER_BIT = 0x00000004;
        public static final int GL_TEXTURE_FETCH_BARRIER_BIT = 0x00000008;
        public static final int GL_SHADER_IMAGE_ACCESS_BARRIER_BIT = 0x00000020;
        public static final int GL_COMMAND_BARRIER_BIT = 0x00000040;
        public static final int GL_PIXEL_BUFFER_BARRIER_BIT = 0x00000080;
        public static final int GL_TEXTURE_UPDATE_BARRIER_BIT = 0x00000100;
        public static final int GL_BUFFER_UPDATE_BARRIER_BIT = 0x00000200;
        public static final int GL_FRAMEBUFFER_BARRIER_BIT = 0x00000400;
        public static final int GL_TRANSFORM_FEEDBACK_BARRIER_BIT = 0x00000800;
        public static final int GL_ATOMIC_COUNTER_BARRIER_BIT = 0x00001000;
        public static final int GL_ALL_BARRIER_BITS = 0xFFFFFFFF;
        public static final int GL_IMAGE_2D = 0x904D;
        public static final int GL_IMAGE_3D = 0x904E;
        public static final int GL_IMAGE_CUBE = 0x9050;
        public static final int GL_IMAGE_2D_ARRAY = 0x9053;
        public static final int GL_INT_IMAGE_2D = 0x9058;
        public static final int GL_INT_IMAGE_3D = 0x9059;
        public static final int GL_INT_IMAGE_CUBE = 0x905B;
        public static final int GL_INT_IMAGE_2D_ARRAY = 0x905E;
        public static final int GL_UNSIGNED_INT_IMAGE_2D = 0x9063;
        public static final int GL_UNSIGNED_INT_IMAGE_3D = 0x9064;
        public static final int GL_UNSIGNED_INT_IMAGE_CUBE = 0x9066;
        public static final int GL_UNSIGNED_INT_IMAGE_2D_ARRAY = 0x9069;
        public static final int GL_IMAGE_FORMAT_COMPATIBILITY_TYPE = 0x90C7;
        public static final int GL_IMAGE_FORMAT_COMPATIBILITY_BY_SIZE = 0x90C8;
        public static final int GL_IMAGE_FORMAT_COMPATIBILITY_BY_CLASS = 0x90C9;
        public static final int GL_READ_ONLY = 0x88B8;
        public static final int GL_WRITE_ONLY = 0x88B9;
        public static final int GL_READ_WRITE = 0x88BA;
        public static final int GL_SHADER_STORAGE_BUFFER = 0x90D2;
        public static final int GL_SHADER_STORAGE_BUFFER_BINDING = 0x90D3;
        public static final int GL_SHADER_STORAGE_BUFFER_START = 0x90D4;
        public static final int GL_SHADER_STORAGE_BUFFER_SIZE = 0x90D5;
        public static final int GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS = 0x90D6;
        public static final int GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS = 0x90DA;
        public static final int GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS = 0x90DB;
        public static final int GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS = 0x90DC;
        public static final int GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS = 0x90DD;
        public static final int GL_MAX_SHADER_STORAGE_BLOCK_SIZE = 0x90DE;
        public static final int GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT = 0x90DF;
        public static final int GL_SHADER_STORAGE_BARRIER_BIT = 0x00002000;
        public static final int GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES = 0x8F39;
        public static final int GL_DEPTH_STENCIL_TEXTURE_MODE = 0x90EA;
        public static final int GL_STENCIL_INDEX = 0x1901;
        public static final int GL_MIN_PROGRAM_TEXTURE_GATHER_OFFSET = 0x8E5E;
        public static final int GL_MAX_PROGRAM_TEXTURE_GATHER_OFFSET = 0x8E5F;
        public static final int GL_SAMPLE_POSITION = 0x8E50;
        public static final int GL_SAMPLE_MASK = 0x8E51;
        public static final int GL_SAMPLE_MASK_VALUE = 0x8E52;
        public static final int GL_TEXTURE_2D_MULTISAMPLE = 0x9100;
        public static final int GL_MAX_SAMPLE_MASK_WORDS = 0x8E59;
        public static final int GL_MAX_COLOR_TEXTURE_SAMPLES = 0x910E;
        public static final int GL_MAX_DEPTH_TEXTURE_SAMPLES = 0x910F;
        public static final int GL_MAX_INTEGER_SAMPLES = 0x9110;
        public static final int GL_TEXTURE_BINDING_2D_MULTISAMPLE = 0x9104;
        public static final int GL_TEXTURE_SAMPLES = 0x9106;
        public static final int GL_TEXTURE_FIXED_SAMPLE_LOCATIONS = 0x9107;
        public static final int GL_TEXTURE_WIDTH = 0x1000;
        public static final int GL_TEXTURE_HEIGHT = 0x1001;
        public static final int GL_TEXTURE_DEPTH = 0x8071;
        public static final int GL_TEXTURE_INTERNAL_FORMAT = 0x1003;
        public static final int GL_TEXTURE_RED_SIZE = 0x805C;
        public static final int GL_TEXTURE_GREEN_SIZE = 0x805D;
        public static final int GL_TEXTURE_BLUE_SIZE = 0x805E;
        public static final int GL_TEXTURE_ALPHA_SIZE = 0x805F;
        public static final int GL_TEXTURE_DEPTH_SIZE = 0x884A;
        public static final int GL_TEXTURE_STENCIL_SIZE = 0x88F1;
        public static final int GL_TEXTURE_SHARED_SIZE = 0x8C3F;
        public static final int GL_TEXTURE_RED_TYPE = 0x8C10;
        public static final int GL_TEXTURE_GREEN_TYPE = 0x8C11;
        public static final int GL_TEXTURE_BLUE_TYPE = 0x8C12;
        public static final int GL_TEXTURE_ALPHA_TYPE = 0x8C13;
        public static final int GL_TEXTURE_DEPTH_TYPE = 0x8C16;
        public static final int GL_TEXTURE_COMPRESSED = 0x86A1;
        public static final int GL_SAMPLER_2D_MULTISAMPLE = 0x9108;
        public static final int GL_INT_SAMPLER_2D_MULTISAMPLE = 0x9109;
        public static final int GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE = 0x910A;
        public static final int GL_VERTEX_ATTRIB_BINDING = 0x82D4;
        public static final int GL_VERTEX_ATTRIB_RELATIVE_OFFSET = 0x82D5;
        public static final int GL_VERTEX_BINDING_DIVISOR = 0x82D6;
        public static final int GL_VERTEX_BINDING_OFFSET = 0x82D7;
        public static final int GL_VERTEX_BINDING_STRIDE = 0x82D8;
        public static final int GL_VERTEX_BINDING_BUFFER = 0x8F4F;
        public static final int GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET = 0x82D9;
        public static final int GL_MAX_VERTEX_ATTRIB_BINDINGS = 0x82DA;
        public static final int GL_MAX_VERTEX_ATTRIB_STRIDE = 0x82E5;
    }

    /**
     * GLES extensions
     * 
     * @author Richard Sahlin
     *
     */
    public enum GLES_EXTENSIONS {

        multisample_compatibility(),
        separate_shader_objects();

    }

    public enum GLES_EXTENSION_TOKENS {

        MULTISAMPLE_EXT(0x809D),
        SAMPLE_ALPHA_TO_ONE_EXT(0x809F);

        public final int value;

        private GLES_EXTENSION_TOKENS(int value) {
            this.value = value;
        }
    }

    /**
     * Returns the GLES shader language version for the platform implementation based on the shader source version.
     * The sourceVersion String is the version part of the "#version" source declaration, eg "300 es", "430" etc.
     * 
     * @param sourceVersion The source version string minus #version, eg "310 es" - or NULL if version not defined.
     * @param version The parsed version number, eg 100 or 0 if version not defined.
     * @return The possibly substituted source version, depending on platform implementation.
     * Mainly used to substitute "310 es" for "430" on desktop platforms/drivers that does not support GLES fully"
     */
    public abstract String getShaderVersion(String sourceVersion, int version);

    /**
     * Returns a versioned shader source as String - this is the main method that shall be used to fetch shader source.
     * If the shader source has a version string it shall be checked by the gles wrapper implementation and if needed
     * substituted for
     * a version that is suitable for the current platform.
     * For instance "#version 310 es" needs to be sutstitued for "#version 430" on desktop implementations (namely AMD
     * or Nvidia drivers that does not fully support the GLES profiles
     * 
     * @param shaderStream
     * @param type Shader type GL_VERTEX_SHADER or GL_FRAGMENT_SHADER
     * @param library True if a shader library (not main)
     * @return
     * @throws IOException
     */
    public abstract String getVersionedShaderSource(InputStream shaderStream, int type, boolean library)
            throws IOException;

    /**
     * Replaces the Shader source older OpenGL ES 2.X attribute and uniform variables to in/out naming.
     * 
     * @param source
     * @param type
     * @return
     */
    protected String replaceGLES20(String source, int type) {
        StringTokenizer st = new StringTokenizer(source, "\n");
        StringBuffer result = new StringBuffer();
        String t = "";
        String[] replacements = null;
        switch (type) {
            case GLES20.GL_VERTEX_SHADER:
                replacements = GLES3_VERTEX_REPLACEMENTS;
                break;
            case GLES20.GL_FRAGMENT_SHADER:
                replacements = GLES3_FRAGMENT_REPLACEMENTS;
                break;
            default:
                throw new IllegalArgumentException("Invalid shader type: " + type);

        }
        while (st.hasMoreTokens()) {
            t = st.nextToken().trim();
            for (int i = 0; i < replacements.length; i += 2) {
                if (t.startsWith(replacements[i])) {
                    t = replacements[i + 1] + t.substring(replacements[i].length());
                }
            }
            result.append(t + "\n");
        }
        return result.toString();
    }

    /**
     * Utility method to return the name of a shader variable, this will remove unwanted characters such as array
     * declaration or '.' field access eg 'struct.field' will become 'struct'
     * 
     * @param nameBuffer
     * @param nameLength
     * @return Name of variable, without array declaration.
     */
    public String getVariableName(byte[] nameBuffer, int nameLength) {
        String name = StringUtils.createString(nameBuffer, 0, nameLength);
        if (name.endsWith("]")) {
            int end = name.indexOf("[");
            name = name.substring(0, end);
        }
        int dot = name.indexOf(".");
        if (dot == -1) {
            return name;
        }
        if (dot == 0) {
            return name.substring(1);
        }
        return name.substring(0, dot);
    }

    /**
     * Returns the renderer info, if it has not been created before it is created and then returned.
     * 
     * @return The renderer info
     */
    public abstract RendererInfo getInfo();

    /**
     * Creates the program info for attrib /uniform / uniform block
     * 
     * @param program
     * @return
     * @throws If there is an error fetching program info
     */
    public abstract ProgramInfo getProgramInfo(int program) throws GLException;

    /**
     * Returns the uniform blocks active in the program
     * 
     * @param info
     * @return
     * @throws GLException
     */
    public abstract InterfaceBlock[] getUniformBlocks(ProgramInfo info) throws GLException;

    /**
     * Creates and returns shader variable for the program, of the specified variable type and variable index.
     * 
     * @param program
     * @param type
     * @param index Index of the active uniform, 0 to GL_ACTIVE_UNIFORMS
     * @param nameBuffer Buffer to write name of uniform
     * @return The active uniform or null if operation failed.
     * @throws If there is an error fetching info for active variable
     */
    public abstract ShaderVariable getActiveVariable(int program, VariableType type, int index, byte[] nameBuffer)
            throws GLException;

}
