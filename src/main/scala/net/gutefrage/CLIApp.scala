package net.gutefrage

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
 * dependency on the uuid npm package
 */
@JSImport("uuid", JSImport.Namespace)
@js.native
object uuid extends js.Object {
  def v1(): String = js.native
  def v4(): String = js.native
}

object CLIApp extends js.JSApp {
  def main(): Unit = {
    println(s"hello world v4 ${uuid.v4()}")
    println(s"hello world v1 ${uuid.v1()}")
  }

}
