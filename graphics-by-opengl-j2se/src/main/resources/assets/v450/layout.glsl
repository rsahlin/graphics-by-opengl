/**
 * Layouts shared by shader stages - define current stage by, eg:
 * #define VERTEX_SHADER
 * before including this file. see 'layout_vert.h'
 *
 * This file depends on knowing structs used in shaders - this means
 * that common_structs is included BEFORE this file.
 */

// uniform locations
#define MATRIX_LOCATION_UNIFORM 0
#define SAMPLER_LOCATION_UNIFORM 1

#define TRANSLATE_LOCATION 0
#define VERTEX_LOCATION 1
#define COLOR_LOCATION 2
#define TEXCOORD0_LOCATION 3

#define FRAGCOLOR_LOCATION 0

layout(binding = MATRIX_LOCATION_UNIFORM) uniform uniform_matrices {
    mat4 uModelMatrix[3];
};
layout(binding = SAMPLER_LOCATION_UNIFORM) uniform lowp sampler2D uTexture;

#ifdef VERTEX_SHADER

layout(location = TRANSLATE_LOCATION) in vec3 aTranslate;
layout(location = VERTEX_LOCATION) in vec3 aVertex;
layout(location = COLOR_LOCATION) in vec4 aColor;
layout(location = TEXCOORD0_LOCATION) in vec2 aTexCoord;

layout(location = COLOR_LOCATION) out vec4 color;
layout(location = TEXCOORD0_LOCATION) out vec2 vTexCoord;
layout(location = FRAGCOLOR_LOCATION) out vec4 fragColor;

#endif

#ifdef FRAGMENT_SHADER

layout(location = COLOR_LOCATION) in vec4 color;
layout(location = TEXCOORD0_LOCATION) in vec2 vTexCoord;
layout(location = FRAGCOLOR_LOCATION) out vec4 fragColor;


#endif

