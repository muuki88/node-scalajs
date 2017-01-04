# A Scalajs Node App

Bundles a scalajs cli app as a node.js app. You can create the app with

## Requirements

You need to have `node` installed.

## Run

```bash
sbt fullOptJS::webpack
node target/scala-2.12/scalajs-bundler/main/opt-launcher.js
```

## Packaging

You can package the node application with sbt-native-packager.

```bash
sbt stage
node  target/universal/stage/node-js-app.js
```

Packaging everything together.

```bash
sbt universal:packageBin
```
