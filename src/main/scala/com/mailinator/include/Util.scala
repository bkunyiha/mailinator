package com.mailinator.include

object Util {

  class RunUntil[A](body: => A) {
    def until(cond: A => Boolean): A = {
      val result = body
      if (cond(result)) result else until(cond)
    }
  }

  object RunUntil {
    def apply[A](body: => A) = new RunUntil(body)
  }

}

