#version 300 es
/**
 * Returns a matrix with rotate, scale and translate set.
 * @return Matrix with rotate, scale and translate set
 */
precision highp float;

mat4 calculateTransformMatrix(vec3 rotate, vec3 scale, vec3 translate){
    
    mat4 modelview = mat4(1);
    float cx = cos(rotate.x);
    float sx = sin(rotate.x);
    float cy = cos(rotate.y);
    float sy = sin(rotate.y);
    //Default to left handed coordinate system.
    float cz = cos(-rotate.z);
    float sz = sin(-rotate.z);
    
    modelview[0][0] = cz * cy * scale.x;
    modelview[0][1] =  -sz * scale.x;
    modelview[0][2] =  sy * scale.x;

    modelview[1][0] = sz * scale.y;
    modelview[1][1] = cz * cx * scale.y;
    modelview[1][2] = -sx * scale.y;

    modelview[2][0] = -sy * scale.z;
    modelview[2][1] = sx * scale.z;
    modelview[2][2] = cx * cy * scale.z;

    //TODO - currently un-optimized, put translation into modelview
    mat4 tMatrix = mat4(1);
        
    tMatrix[0][3] = translate.x;
    tMatrix[1][3] = translate.y;
    tMatrix[2][3] = translate.z;
    
    return modelview * tMatrix;
}


