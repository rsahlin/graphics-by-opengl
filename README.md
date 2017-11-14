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

To use the project in Eclipse, import as Existing Maven project, tested with Eclipse Android Oxygen

ADT needs to be installed.

pom file may display error similar to:

Plugin execution not covered by lifecycle configuration: com.simpligility.maven.plugins:android-maven-plugin:4.4.1:emma (execution: default-emma, phase: process-classes)

- To resolve this, choose quick fix 'Permanently mark goal emma in pom.xml as ignored in Eclipse build'

You may experience problem with non-existing project.properties file 

- I solved by adding an empty project.properties file in the Android project root, chosing 'Properties-Android' and selecting a valid SDK.

Maven - update project,or clean build to get rid of any trailing errors.

Gradle - to publish as maven local, perform task for each module:
graphics-by-opengl-j2se>gradle publishToMavenLocal
graphics-by-påengl-android>gradle publishToMavenLocal

