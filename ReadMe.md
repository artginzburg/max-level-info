This is a developer-only document. For usage and screenshots, see the Modrinth page.

## Developing locally

> with Visual Studio Code

1. Run `./gradlew vscode` - to generate `launch.json`
2. Run `sh ./scripts/add_java_agent.sh` - to enable [Hotswapping](https://docs.fabricmc.net/develop/getting-started/launching-the-game#hotswapping-mixins)

## F.A.Q.

#### Q: Why don't you use data from `gradle.properties` in `fabric.mod.json`?

A: I tried in [f196777](https://github.com/artginzburg/max-level-info/commit/f1967774140b8e4d7f39b7ce5c8525b8e278ba70), but that ruined Hotswapping Mixins, so for now we're back to modifying versions in two places instead of one.
