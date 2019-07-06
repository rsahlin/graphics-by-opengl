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

// out locations
#define FRAGCOLOR_LOCATION 0

layout(std140, binding = 0) uniform pbr_matrices {
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

