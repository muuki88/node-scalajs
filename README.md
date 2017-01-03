# A Scalajs Node App

Bundles a scalajs cli app as a node.js app. You can create the app with

## Requirements

You need to have `node` installed.

## Run

```scala
sbt fullOptJS::webpack
node target/scala-2.12/scalajs-bundler/main/opt-launcher.js
```
