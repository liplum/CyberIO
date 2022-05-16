# For Contributors
*It's hard to be a regulator, and I don't want to offend anyone.*


## For programmer
### Concepts
Cyber IO project is separated from several parts.
1. main: It's the mian project of Cyber IO. Everything in this project will be included in the mod.
2. annotations: It contains many annotations Cyber IO used. And it's used to generate codes while compiling.
3. processor: It contains some annotation processors.
4. app: It's used to start and debug game in IDE.
5. bundle: no use yet
6. story: no use yet

Cyber IO uses Gradle Kts as DSL of build scripts.

### How to build
Please follow the steps below.
1. Clone this project via Git/GitHub.
2. Synchronize the build.gradle.kts.
3. Run task `:main:jar` for generating a desktop only mod file.
4. Run task `:main:deployLocal` for generating a mod file which can work on both desktop and android.
But you need an Android SDK at first.

## For translator

WIP