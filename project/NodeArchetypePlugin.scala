package net.gutefrage.node

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.universal.UniversalPlugin
import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

/**
 * Packages a production ready node application.
 *
 * Requires yarn.
 *
 * @see [[https://github.com/scalacenter/scalajs-bundler/blob/master/sbt-scalajs-bundler/src/main/scala/scalajsbundler/Launcher.scala]]
 * @see [[https://github.com/scalacenter/scalajs-bundler/blob/master/sbt-scalajs-bundler/src/main/scala/scalajsbundler/sbtplugin/ScalaJSBundlerPlugin.scala]]
 * @see [[https://github.com/scalacenter/scalajs-bundler/blob/master/sbt-scalajs-bundler/src/main/scala/scalajsbundler/PackageJson.scala]]
 */
object NodeArchetypePlugin extends AutoPlugin {

  override lazy val requires: Plugins = ScalaJSBundlerPlugin && UniversalPlugin

  private val generateNodeWebpackConfig: TaskKey[Unit] =
    TaskKey[Unit]("generateNodeWebpackConfig", "generates the node.webpack.config.js")

  override def projectSettings: Seq[Setting[_]] = Seq(
    useYarn := true,
    generateNodeWebpackConfig := writeNodeWebpackConfig((crossTarget in Compile).value),
    (webpack in (Compile, fullOptJS)) := (webpack in (Compile, fullOptJS)).dependsOn(generateNodeWebpackConfig).value,
    mappings in Universal ++= {
      val webpackOutputs = (webpack in (Compile, fullOptJS)).value
      webpackOutputs.map(file => file -> file.getName)
    },
    webpackConfigFile in (Compile, fullOptJS) := Some((crossTarget in Compile).value / "node.webpack.config.js")
  )

  private def writeNodeWebpackConfig(targetDir: File): Unit = {
    val webpackConf = targetDir / "node.webpack.config.js"
    IO.write(
      webpackConf,
      """|var webpack = require('webpack');
        |
        |module.exports = require('./scalajs.webpack.config');
        |module.exports.target = 'node';
        |""".stripMargin
    )
  }
}
