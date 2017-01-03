enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

name := "node-js-app"

scalaVersion := "2.12.1"

npmDependencies in Compile ++= Seq(
  "node-uuid" -> "1.4.7"
)

// custom webpack config to set target to 'node'
useYarn := true
webpackConfigFile := Some(baseDirectory.value / "webpack.node.config.js")
