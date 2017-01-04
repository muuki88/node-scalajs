enablePlugins(ScalaJSPlugin, NodeArchetypePlugin)

name := "node-js-app"
scalaVersion := "2.12.1"

npmDependencies in Compile ++= Seq(
  "node-uuid" -> "1.4.7"
)
