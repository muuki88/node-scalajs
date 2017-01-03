enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

name := "node-js-app"

scalaVersion := "2.12.1"

npmDependencies in Compile ++= Seq(
  "uuid" -> "3.0.1"
)

// custom webpack config to set target to 'node'
enableReloadWorkflow := true
webpackConfigFile := Some(baseDirectory.value / "webpack.node.config.js")
