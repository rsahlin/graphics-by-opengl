# graphics-by-opengl
A Java based API for graphics using OpenGLES. The goal is to have a platform agnostic API for the core functions needed when coding graphics applications, as well as using novel ways to utilize OpenGL (ES).
The API is intentionally close to OpenGLES, for instance attribute/vertex data classes are using it's memory model. 
Although it is possible to extend the functionality to cover for instance Direct3D it is not a design consideration.
Currently includes simple methods for user input/mmi, these functions may be moved in the future. 
Uses the simple vecmath library for some matrix and vector functions.
When importing the project use either the eclipse project files or the pom file. I use Eclipse, though a Maven base IDE _should_ work.