# graphics-by-opengl
A Java based API for graphics using OpenGLES.
Core functions for graphics rendering in a platform agnostic way using OpenGLES.
This API is intended as a low level API, using it requires OpenGL knowledge.
It is intentionally close to OpenGLES, for instance attribute/vertex data classes are using it's memory model. 
Although it is possible to extend the functionality to cover for instance Direct3D it is not a design consideration.
Currently includes simple methods for user input/mmi, these functions may be moved in the future. 
Uses the simple vecmath library for some matrix and vector functions.

Using #graphics-by-opengl makes it possible to develop on J2SE using JOGL (or any other OpenGL ES Java API) without the need to continously using specific target devices.
This can greatly reduce development times since starting and debugging a J2SE application is much quicker than for instance deploying on Android.

When importing the project use either the eclipse project files or the pom file. I use Eclipse, though a Maven base IDE _should_ work.

#graphics-by-opengl-j2se contains all APIs and implementation that is not platform specific 
- the majority of functionality and code should be here.

#graphics-by-opengl-jogl and #graphics-by-opengl-android
contains JOGL and Android implementations.


