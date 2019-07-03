#version 300 es
/**
 * Functions for gltf PBR model
 */
precision highp float;

//Metallic,roughnessfactor, exposure, gamma
//F0         [3] - dielectricSpecular -> basecolor for metallic = 1
//cDiff      [3]
//diffuse    [4]
uniform vec4 _PBRDATA[4];
uniform mat4 uModelMatrix[3];
// color + intensity [4]
// position [3]
uniform vec4 _LIGHT_0[2];
uniform vec3 _VIEWPOS[2];

in vec3 POSITION;
in vec3 NORMAL;
in vec4 TANGENT;
in vec3 BITANGENT;
in vec2 TEXCOORD_0;
in vec2 _TEXCOORDNORMAL;
in vec2 _TEXCOORDMR;
in vec2 _TEXCOORDOCCLUSION;

out vec4 vDiffuseColor;
out vec2 vTexCoord0;
out vec2 vTexNormal;
out vec2 vTexMR;
out vec2 vTexOccl;

out mat3 mTangentLight;
out vec4 vWorldPos;
out Light light;
out Material material;

void setLight() {
    light.eye = normalize(_VIEWPOS[0] -vWorldPos.xyz);
    light.direction = normalize(_LIGHT_0[1].xyz - vWorldPos.xyz);
    light.color = _LIGHT_0[0];
}
void setMaterial(Light light) {
    vec3 H = normalize(light.eye + light.direction);
    material.H = H;
    material.NdotH = clamp(dot(material.normal, H), 0.0, 1.0);
    material.NdotL = clamp(dot(material.normal, light.direction), 0.0, 1.0);
    material.VdotH = clamp(dot(light.eye, H), 0.0, 1.0);
    material.NdotV = abs(dot(material.normal, light.eye)) + 1e-5;
    material.LdotH = dot(light.direction, H);
}

/**
 * Calculate gl_Position and return the pos, set light, eye and tex coordinates.
 * Always call this function
 */
vec4 posLightTex() {
    vWorldPos = vec4(POSITION, 1.0) * uModelMatrix[0];
    gl_Position = vWorldPos * uModelMatrix[1] * uModelMatrix[2];
    setLight();
    setMaterial(light);
    vTexCoord0 = TEXCOORD_0;
    vTexMR = _TEXCOORDMR;
    vTexOccl = _TEXCOORDOCCLUSION;
    return vWorldPos;
}

/**
 * Calculates the position and light for pbr materials
 */
void positionLight() {
    material.normal = normalize(NORMAL * mat3(uModelMatrix[0]));
    posLightTex();
}

/**
 * Calculates the position and light, using texture normal map, for pbr materials with texture
 */
void positionLightTexNormal() {
    vec3 tangent = normalize(vec3(TANGENT) * mat3(uModelMatrix[0]));
    vec3 bitangent = normalize(vec3(BITANGENT) * mat3(uModelMatrix[0]));
    vec3 normal = normalize(vec3(NORMAL) * mat3(uModelMatrix[0]));
    mTangentLight = transpose(mat3(tangent,bitangent,normal));
    material.normal = normalize(NORMAL * mat3(uModelMatrix[0]));
    posLightTex();
    vTexNormal = _TEXCOORDNORMAL;
}

