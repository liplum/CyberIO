# For Contributors

*It's hard to be a regulator, and I don't want to offend anyone.*

## For programmer

Cyber IO uses [mgpp](https://github.com/PlumyGames/mgpp) gradle plugin to build mod.

### How to build

Please follow the steps below.

1. Clone this project via Git/GitHub.
2. Synchronize the build.gradle.kts.
3. Run task `:main:jar` for generating a desktop only mod file.
4. Run task `:main:deploy` for generating a mod file which can work on both desktop and android.
   But you need an Android SDK at first.

**Full Command Line**:

```
# Linux
./gradlew :main:jar
# Windows
gradlew.bat :main:jar
```

### Concepts

Cyber IO project is separated from several parts.

1. main: It's the mian project of Cyber IO. Everything in this project is included in the mod.
2. annotations: It contains many annotations Cyber IO used. And it's used to generate codes while compiling.
3. processor: It contains some annotation processors.

Cyber IO uses Gradle Kotlin DSL to build scripts.

### Conventions

1. If a class contains a mutable collection, pool it.
2. Use typealias instead of Int, meaningless name, or long name, for embedded more information.
3. Use Kotlin style functions as much as you can. Do not use any lambda without inline function.
4. Annotate everything you write as much as possible, which provides more meta information for other programmers.
5. Use ksp to generate code. And write event subscriber (by @SubscribeEvent and @Subscribe) as near logic as you can.
6. Use mutable object in serialization instead of creating them once again.
7. Use R.xxx to share global resources.
8. Use Var to share global variables

## For translator

WIP