# graphics-by-opengl
A Java based API for graphics using OpenGLES.
Core functions for graphics rendering in a platform agnostic way using OpenGLES.
This API is intended as a low level API, using it requires OpenGL knowledge.
Currently the implementation is tied to OpenGLES, this is in order to have an API that is very well suited for how OpenGLES works.
Contains JOGL and Android implementations.

Using this API makes it possible to develop on J2SE using JOGL (or any other OpenGL ES Java API) without the need to continously using specific target devices.
This can greatly reduce development times since starting and debugging a J2SE application is much quicker than for instance deploying on Android.

