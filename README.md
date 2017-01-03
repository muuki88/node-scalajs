# A Scalajs Node App

Bundles a scalajs cli app as a node.js app. You can create the app with

## Requirements

You need to have `node` installed.

## Run

```scala
sbt fullOptJS::webpack
node target/scala-2.12/scalajs-bundler/main/opt-launcher.js
```

## Packaging

You can package the node application with sbt-native-packager.

```scala
sbt stage
node  target/universal/stage/opt-launcher.js
```
