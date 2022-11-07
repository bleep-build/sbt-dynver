package bleep.plugin.dynver

import scala.sys.process.ProcessLogger

object NoProcessLogger extends ProcessLogger {
  override def out(s: => String) = ()
  override def err(s: => String) = ()
  override def buffer[T](f: => T) = f
}
