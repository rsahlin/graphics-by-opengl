#version 300 es
#line 3
/* 
 *
 * Fragment shader for glTF asset
 * @author Richard Sahlin
 */
precision highp float;

void main() {
    BRDF brdf = getPerVertexBRDF();
    vec3[2] diffuseSpecular = calculateFresnelDiffuse(brdf);
    outputPixel(vec4(diffuseSpecular[0] * texture(uTexture0, vTexCoord0).rgb + diffuseSpecular[1], 1.0));
}
