package net.gutefrage.node

import com.typesafe.sbt.SbtNativePackager.Universal
import com.typesafe.sbt.packager.Keys.executableScriptName
import com.typesafe.sbt.packager.MappingsHelper
import com.typesafe.sbt.packager.universal.UniversalPlugin
import org.scalajs.core.tools.io.{FileVirtualJSFile, VirtualJSFile}
import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scalajsbundler.util.JS

/**
 * Packages a production ready node application.
 *
 * @see [[https://github.com/scalacenter/scalajs-bundler/blob/master/sbt-scalajs-bundler/src/main/scala/scalajsbundler/Launcher.scala]]
 * @see [[https://github.com/scalacenter/scalajs-bundler/blob/master/sbt-scalajs-bundler/src/main/scala/scalajsbundler/sbtplugin/ScalaJSBundlerPlugin.scala]]
 * @see [[https://github.com/scalacenter/scalajs-bundler/blob/master/sbt-scalajs-bundler/src/main/scala/scalajsbundler/PackageJson.scala]]
 */
object NodeArchetypePlugin extends AutoPlugin {

  // FIXME this is a very rough version. Needs some cleanup and structuring, but works.

  override lazy val requires: Plugins = ScalaJSBundlerPlugin && UniversalPlugin

  override def projectSettings: Seq[Setting[_]] = Seq(
    scalaJSLauncher in packageBin := {
      val scriptName = (executableScriptName in Universal).value
      val main = (mainClass in Compile in scalaJSLauncher in fullOptJS).value
        .getOrElse(sys.error("No main class detected"))
      val launcher = writeLauncher(
        scriptName,
        (crossTarget in npmUpdate).value,
        (fullOptJS in Compile).value,
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
    mappings in Universal += {
      val optimizedJavascript = (fullOptJS in Compile).value
      optimizedJavascript.data -> optimizedJavascript.data.getName
    },
    mappings in Universal ++= MappingsHelper.directory((npmUpdate in Compile).value / "node_modules")
  )

  private def writeLauncher(
    launcherName: String,
    targetDir: File,
    sjsOutput: Attributed[File],
    mainClass: String
  ): File = {
    val launcherContent = {
      // This differs from the default scalajs launcher
      val module = JS.ref("require")(JS.str(s"./${sjsOutput.data.getName}"))
      callEntryPoint(mainClass, module)
    }

    val launcherFile = targetDir / s"$launcherName.js"
    IO.write(launcherFile, launcherContent.show)
    launcherFile
  }

  /**
   * @param mainClass Main class name
   * @param module Module exporting the entry point
   * @return A JavaScript program that calls the main method of the main class
   */
  private def callEntryPoint(mainClass: String, module: JS): JS = {
    val mainClassRef =
      mainClass
        .split('.')
        .foldLeft(module)((tree, part) => tree.bracket(part))
    mainClassRef.apply().dot("main").apply()
  }
}
