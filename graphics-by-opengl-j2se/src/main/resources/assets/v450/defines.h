// uniform locations
#define MATRIX_LOCATION_UNIFORM 0
#define SAMPLER_LOCATION_UNIFORM 1

// in locations
#define TRANSLATE_LOCATION_IN 0
#define VERTEX_LOCATION_IN 1
#define COLOR_LOCATION_IN 2

// out locations
#define COLOR_LOCATION_OUT 3
#define TEXCOORD0_LOCATION_OUT 4
#define FRAGCOLOR_LOCATION_OUT 5

layout(binding = MATRIX_LOCATION_UNIFORM) uniform uniform_matrices {
	mat4 uModelMatrix[3];
};
layout(binding = SAMPLER_LOCATION_UNIFORM) uniform lowp sampler2D uTexture;

layout(location = TRANSLATE_LOCATION_IN) in vec3 aTranslate;
layout(location = VERTEX_LOCATION_IN) in vec3 aVertex;
layout(location = COLOR_LOCATION_IN) in vec4 aColor;

layout(location = COLOR_LOCATION_OUT) out vec4 color;
layout(location = TEXCOORD0_LOCATION_OUT) out vec2 vTexCoord;
layout(location = FRAGCOLOR_LOCATION_OUT) out vec4 fragColor;
