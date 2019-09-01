// Uniforms
layout(binding = 0) uniform sampler2D uTexture0; //The texture 0 sampler
layout(binding = 1) uniform sampler2D uTextureNormal; //normal texture sampler
layout(binding = 2) uniform sampler2D uTextureMR; //Metallic roughness texture sampler
layout(binding = 3) uniform sampler2D uTextureOcclusion; //Occlusion texture sampler

layout(std140, binding = 4) uniform pbr_matrices {
    //Metallic,roughnessfactor, exposure, gamma
    //F0         [3] - dielectricSpecular -> basecolor for metallic = 1
    //cDiff      [3]
    //diffuse    [4]
    vec4 _PBRDATA[4];
    mat4 uModelMatrix[3];
    // color + intensity [4]
    // position [3]
    vec4 _LIGHT_0[2];
    vec3 _VIEWPOS[2];
} pbrdata;


// in locations
#define DIFFUSECOLOR_LOCATION 8
#define TEXCOORD0_LOCATION 9
#define TEXNORMAL_LOCATION 10
#define TEXMR_LOCATION 11
#define TEXOCCL_LOCATION 12
#define WORLDPOS_LOCATION 13
#define LIGHT_LOCATION 14
#define MATERIAL_LOCATION LIGHT_LOCATION + LIGHT_LOCATIONS
#define TANGENTLIGHT_LOCATION MATERIAL_LOCATION + MATERIAL_LOCATIONS

#define LAST_LOCATION_IN  TANGENTLIGHT_LOCATION + 3

// out locations
#define FRAGCOLOR_LOCATION_OUT LAST_LOCATION_IN


layout(location = 0) in vec3 POSITION;
layout(location = 1) in vec3 NORMAL;
layout(location = 2) in vec4 TANGENT;
layout(location = 3) in vec3 BITANGENT;
layout(location = 4) in vec2 TEXCOORD_0;
layout(location = 5) in vec2 _TEXCOORDNORMAL;
layout(location = 6) in vec2 _TEXCOORDMR;
layout(location = 7) in vec2 _TEXCOORDOCCLUSION;

layout(location = DIFFUSECOLOR_LOCATION) out vec4 vDiffuseColor;
layout(location = TEXCOORD0_LOCATION) out vec2 vTexCoord0;
layout(location = TEXNORMAL_LOCATION) out vec2 vTexNormal;
layout(location = TEXMR_LOCATION) out vec2 vTexMR;
layout(location = TEXOCCL_LOCATION) out vec2 vTexOccl;

layout(location = WORLDPOS_LOCATION) out vec4 vWorldPos;
layout(location = 14) out Light light;
layout(location = 17) out Material material;
layout(location = 24) out mat3 mTangentLight;

layout(location = FRAGCOLOR_LOCATION_OUT) out vec4 fragColor;

