# Frogue

An open-source FPS game made for [libGDX Jam 33](https://itch.io/jam/libgdx-jam-33/entries).

Play the game [here on itch.io](https://necrashter.itch.io/frogue).

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `android`: Android mobile platform. Needs Android SDK.
- `html`: Web platform using GWT and WebGL. Supports only Java projects.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `android:lint`: performs Android project validation.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `html:dist`: compiles GWT sources. The compiled application can be found at `html/build/dist`: you can use any HTTP server to deploy it.
- `html:superDev`: compiles GWT sources and runs the application in SuperDev mode. It will be available at [localhost:8080/html](http://localhost:8080/html). Use only during development.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

# License

The code is released under the MIT license.

2D assets:
- UI Theme by [***Raymond "Raeleus" Buckley***](https://ray3k.wordpress.com/software/skin-composer-for-libgdx/) with modifications.

3D models:
- https://opengameart.org/content/spruce-deforestation-models
- https://opengameart.org/content/frog-guy
- https://opengameart.org/content/low-poly-m4a1
- Any other models are by me for this project.
- AI-assisted texture painting

Sound effects:
- [Zombie sound](https://freesound.org/people/ohheyvoid/sounds/540818/)
- [Frog sounds](https://freesound.org/people/OneTwo_BER/sounds/474193/)
- ["Pew" sound](https://freesound.org/people/0ne_one111yt/sounds/478215/)
- ["Reloading" sound](https://freesound.org/people/TyrantTim/sounds/800861/)
- Other CC0 sounds from freesound.org

Music:
- Made by me, licensed under CC-BY-NC-SA 4.0.
