#graphics-by-opengl
A Java based API for graphics using OpenGLES.

As this is a multi platform project I use Eclipse Android Neon and import as Maven project.

Core functions for graphics rendering in a platform agnostic way using OpenGLES.
This API is intended as a low level API, using it requires OpenGL knowledge.
It is intentionally close to OpenGLES, for instance attribute/vertex data classes are using it's memory model. 
Although it is possible to extend the functionality to cover for instance Direct3D it is not a design consideration.
Currently includes simple methods for user input/mmi, these functions may be moved in the future. 
Uses the simple vecmath library for some matrix and vector functions.

Using #graphics-by-opengl makes it possible to develop on J2SE using JOGL (or any other OpenGL ES Java API) without the need to continously using specific target devices.
This can greatly reduce development times since starting and debugging a J2SE application is much quicker than for instance deploying on Android.

graphics-by-opengl-j2se contains all APIs and implementation that is not platform specific
- the majority of functionality and code should be here.

graphics-by-opengl-jogl and

graphics-by-opengl-android

contains JOGL and Android implementations.

To use the project in Eclipse oxygen, import as Existing Maven project, tested with Eclipse Android Oxygen

For graphics-by-opengl-android:

Download Android standalone SDK for windows:
https://developer.android.com/studio/index.html#downloads - scroll down to 'Get just the command line tools'

Install Andmore maven plugin from Eclipse Marketplace (https://projects.eclipse.org/projects/tools.andmore)

When prompted for Android for Maven Eclipse (m2e) click to install but exclude:
Android DDMS
Android Development Tools
Android TraceView
(They should be included with Andmore)

Maven - update project,or clean build to get rid of any trailing errors.


Gradle - to publish as maven local, perform task for each module:
graphics-by-opengl-j2se>gradle publishToMavenLocal
graphics-by-opengl-android>gradle publishToMavenLocal

