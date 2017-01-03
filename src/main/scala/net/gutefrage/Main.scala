package net.gutefrage

import scala.scalajs.js

object Main extends js.JSApp {
  def main(): Unit = {
    println(s"hello world v4 ${uuid.v4()}")
    println(s"hello world v1 ${uuid.v1()}")
  }

}
