package common

object May {

  implicit class OptionMemo[A](o : Option[A]) {
    def memo[A](st: => String) = {
      if (o.isEmpty)
        println(st)
      o
    }
  }

  def state(str: => String)
  : Unit = {
    println(s"[ State ] ========== $str ==========")
  }

  def warn[A](a: => Option[A])(str: => String)
  : Option[A] = {
    if (a.isEmpty) println(s"[ Warn ] : None = $str");
    a
  }

  def maybeInfo[A](a: => A)(str: => String)
  : Option[A] = try Some(a) catch {
    case e: Throwable => println("[ maybeInfo ]" + str, e); None
  }


  def maybeWarn2[A](a: => A)(str: String = "[ maybeWarn ]")
  : Option[A] = {
    try Some(a) catch {
      case e: Throwable => println(str, e); None
    }
  }

  def maybe[A](a: => A)
  : Option[A] = {
    try Some(a) catch { case e: Throwable => None }
  }

  def using[A <: AutoCloseable,B](f: => A, needClose: Boolean = true)(g: A => B): Option[B] = {
    maybe( f ).flatMap { a =>
      try{
        Some(g(a))
      } catch { case e: Throwable =>
        println(e.toString)
        None
      } finally {
        if(needClose)
          a.close()
      }
    }
  }

  class Lazy[T]( calc0: () => T){
    private lazy val force = calc0()
    def apply(): T = force
  }

  def Timer[A](f: => A): (A, Long) = {
    val s = System.currentTimeMillis
    val r = f
    val e = System.currentTimeMillis
    println("spent(millis): " + (e - s))
    r -> (e - s)
  }
}
