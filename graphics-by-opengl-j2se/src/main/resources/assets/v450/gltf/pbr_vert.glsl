/**
 * Functions for gltf PBR model
 */
precision highp float;

void setLight() {
    light.eye = normalize(pbrdata._VIEWPOS[0] -vWorldPos.xyz);
    light.direction = normalize(pbrdata._LIGHT_0[1].xyz - vWorldPos.xyz);
    light.color = pbrdata._LIGHT_0[0];
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
    vWorldPos = vec4(POSITION, 1.0) * pbrdata.uModelMatrix[0];
    gl_Position = vWorldPos * pbrdata.uModelMatrix[1] * pbrdata.uModelMatrix[2];
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
    material.normal = normalize(NORMAL * mat3(pbrdata.uModelMatrix[0]));
    posLightTex();
}

/**
 * Calculates the position and light, using texture normal map, for pbr materials with texture
 */
void positionLightTexNormal() {
    vec3 tangent = normalize(vec3(TANGENT) * mat3(pbrdata.uModelMatrix[0]));
    vec3 bitangent = normalize(vec3(BITANGENT) * mat3(pbrdata.uModelMatrix[0]));
    vec3 normal = normalize(vec3(NORMAL) * mat3(pbrdata.uModelMatrix[0]));
    mTangentLight = transpose(mat3(tangent,bitangent,normal));
    material.normal = normalize(NORMAL * mat3(pbrdata.uModelMatrix[0]));
    posLightTex();
    vTexNormal = _TEXCOORDNORMAL;
}

