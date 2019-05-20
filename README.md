#graphics-by-opengl
PBR based renderer in Java using OpenGLES.

A forward renderer using node hierarchy to render meshes.
Limits number of passes in order to reach high fps in 4-8K resolution and on mobile devices.
The renderer is written in pure Java and uses OpenGL ES 3.X - this means it can run on _any_ os / platform that has an OpenGL ES implementation.

Support for glTF is being improved, currently basic model import is working with textures, normal maps and pbr metallicroughenss textures.
BRDF implementation supports one directional light.
Support for sRGB textures, gamma correction and (hdr) exposure.

List of feature that will be implemented or improved - in no particular order
* Support for pbr occlusion map
* DRACO support, may need to port DRACO decoder to Java?
* Model animations and skinning (Morph targets and joint hierarchy)
* Shadow pass
* Environment maps
* Improved lightsource implementation using luminous intensity and attenuation (instead of made up scale-factor) 

see #gltf-viewer for an app that displays gltf models.

Currently there are 3 abstractions for the render backend along with window/surface support.
- JOGL
- LWJGL
- Android

As this is a multi platform project I use Eclipse and import as Maven project.

Core functions for graphics rendering in a platform agnostic way using OpenGLES.
This API is intended as a low level API, using it requires OpenGL knowledge.
It is intentionally close to OpenGLES, for instance attribute/vertex data classes are using it's memory model. 
Although it is possible to extend the functionality to cover for instance Direct3D it is not a design consideration.
Currently includes simple methods for user input/mmi, these functions may be moved in the future. 
Uses the simple 'vecmath' library for some matrix and vector functions.

Using #graphics-by-opengl makes it possible to develop on J2SE using JOGL (or any other OpenGL ES Java API) without the need to continously using specific target devices.
This can greatly reduce development times since starting and debugging a J2SE application is much quicker than for instance deploying on Android.

Code style and formatting:
Follow the Google Java guidelines:
https://google.github.io/styleguide/javaguide.html

https://github.com/rsahlin/graphics-by-opengl/blob/develop/images/gltfrenderdemo05.png

Eclipse:
Use customformatter.xml
Open preferences-general-workspace

Make sure Text file encoding is UTF-8
New text file delimiter - Unix


ECLIPSE 
----------------------------------------------------------------------
Prerequisites:
- Maven
- Eclipse
- M2Eclipse plugin https://www.eclipse.org/m2e/
- Andmore maven plugin from Eclipse Marketplace (https://projects.eclipse.org/projects/tools.andmore)
Make sure that you are using Android Andmore and m2e plugins (not the old DDMS/ADT from 'The Android Open Source Project')
Check by opening 'Help' - 'Install new software' - 'What is already installed?' 
Uninstall software from 'The Android Opensource Project' and fetch Andmore from Eclipse marketplace.
- JDK 1.7 or 1.8 (Not 1.9) - note that compiler level shall be 7 for Android builds to work.
Check with 'javac -version' 

For Android:
- Android standalone SDK for windows:
https://developer.android.com/studio/index.html#downloads - scroll down to 'Get just the command line tools'
- android-maven-plugin : Follow instructions at: http://simpligility.github.io/android-maven-plugin/
- Local maven installation of Android platform SDK to be used, defaults to 24.
To install execute the following, where $ANDROID_HOME is your android sdk folder (use %ANDROID_HOME% on Windows):
Execute the following in the base directory for graphics-by-opengl:
mvn install:install-file -Dfile="%ANDROID_HOME%/platforms/android-25/android.jar" -DgroupId="com.google.android" -DartifactId=android -Dversion="25" -Dpackaging=jar

The Android SDK level is set as property 'android.sdk'

Project structure:

graphics-by-opengl-j2se contains all APIs and implementation that is not platform specific
- the majority of functionality and code should be here.

graphics-by-opengl-jogl
JOGAMP based implementation - will run on Java platform that has support for JOGAMP (win/linux/macos)

graphics-by-opengl-android
Android implementation
- Sometimes m2e will not recognise aar packaging as Android projects when importing into Eclipse.
If graphics-by-opengl is missing Android dependenciesm which shows up by not finding any Java library classes.
Change packaging in the graphics-by-opengl\pom.xml to apk (from aar) before importing and then switch back once it builds.
Select - Maven - update project,or clean build to get rid of any trailing errors.

To use the project in Eclipse, import as Existing Maven project

MAVEN

Build in root folder using:
mvn clean install -DskipTests
This will publish to local maven so that project can be imported.

GRADLE

----------------------------------------------------------------------

Gradle - to publish as maven local, perform task for each module:
graphics-by-opengl-j2se>gradle publishToMavenLocal
graphics-by-opengl-android>gradle publishToMavenLocal

