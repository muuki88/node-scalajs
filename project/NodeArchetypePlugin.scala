package net.gutefrage.node

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.MappingsHelper
import com.typesafe.sbt.packager.universal.UniversalPlugin
import org.scalajs.core.tools.io.{FileVirtualJSFile, VirtualJSFile}
import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.{ScalaJSPluginInternal, Stage}

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scalajsbundler.util.JS

/**
 * Packages a production ready node application.
 *
 * @see [[https://github.com/scalacenter/scalajs-bundler/blob/master/sbt-scalajs-bundler/src/main/scala/scalajsbundler/Launcher.scala]]
 * @see [[https://github.com/scalacenter/scalajs-bundler/blob/master/sbt-scalajs-bundler/src/main/scala/scalajsbundler/sbtplugin/ScalaJSBundlerPlugin.scala]]
 */
object NodeArchetypePlugin extends AutoPlugin {

  // FIXME this is a very rough version. Needs some cleanup and structuring, but works.

  val stage = Stage.FullOpt
  val stageTask = ScalaJSPluginInternal.stageKeys(stage)

  override lazy val requires = ScalaJSBundlerPlugin && UniversalPlugin

  override def projectSettings: Seq[Setting[_]] = Seq(
    scalaJSLauncher in packageBin := {
      val main = (mainClass in Compile in scalaJSLauncher in stageTask).value.getOrElse(sys.error("No main class detected"))
      val launcher = writeLauncher(
        (crossTarget in npmUpdate).value,
        (stageTask in Compile).value,
        stage,
        main
      )
      Attributed[VirtualJSFile](FileVirtualJSFile(launcher))(
        AttributeMap.empty.put(name.key, main)
      )
    },
    mappings in Universal += {
      val launcher = (scalaJSLauncher in packageBin).value
      file(launcher.data.path) -> launcher.data.name
    },
    mappings in Universal ++= {
      // depend on this task
      (webpack in Compile in fullOptJS).value
      val targetDir = (npmUpdate in Compile).value

      // get the webpack entry setting
      val entries = (webpackEntries in Compile in fullOptJS).value
      entries.map {
        case (entry, _) => (targetDir / s"$entry.js") -> s"$entry.js"
      }
    },
    mappings in Universal ++= MappingsHelper.directory((npmUpdate in Compile).value / "node_modules")

  )

  private def writeLauncher(
    targetDir: File,
    sjsOutput: Attributed[File],
    sjsStage: Stage,
    mainClass: String
  ): File = {
    val launcherContent = {
      // This differs from the default scalajs launcher
      val module = JS.ref("require")(JS.str(s"./${sjsOutput.data.getName}"))
      callEntryPoint(mainClass, module)
    }

    val stagePart =
      sjsStage match {
        case Stage.FastOpt => "fastopt"
        case Stage.FullOpt => "opt"
      }

    val launcherFile = targetDir / s"$stagePart-launcher.js"
    IO.write(launcherFile, launcherContent.show)
    launcherFile
  }

  /**
   * @param mainClass Main class name
   * @param module Module exporting the entry point
   * @return A JavaScript program that calls the main method of the main class
   */
  def callEntryPoint(mainClass: String, module: JS): JS = {
    val mainClassRef =
      mainClass
        .split('.')
        .foldLeft(module)((tree, part) => tree.bracket(part))
    mainClassRef.apply().dot("main").apply()
  }
}
