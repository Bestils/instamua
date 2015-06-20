package com.htvu.instamua.test.general

import org.specs2.mutable.Specification
import org.scalatest._

//Simple testClass for testing the ScalaTest Style
//We will use both ScalaTest + Spec2
class ScalaTestSpec extends FlatSpec with MustMatchers {
  "list utils function" must "be correct" in {
    var list = (1 to 10) map { _ * 2 }
    list(0) mustBe(2)

    //iterate in vector/list scala
    list.zipWithIndex.foreach{
      case(x, i) => x mustBe ((i+1)*2)
    }

    //sugar sum function
    list.sum mustBe(110)

    //filter function
    list.filter((i: Int) => i%4==0).zipWithIndex.foreach{
      case(x,i) => x mustBe ((i+1)*4)
    }

    //find function
    list.find((i: Int) => i%4==0) mustBe Some(4)

    //foldLeft and reduceLeft
    list.foldLeft(0)((m: Int, n: Int) => m + n) mustBe list.sum
    list.foldRight(0)((m: Int, n: Int) => m + n) mustBe list.sum
    list.reduceLeft(_ + _) mustBe list.sum

    //partion function
    val (passed, failed) = List[Int](49, 58, 76, 82, 88, 90) partition ( _ > 60 )
    passed.length mustBe 4
    failed.length mustBe 2

    //other way to declare variable -- val is final variable which cannot be changed
    val wordList = Array[String]("scala", "akka", "play framework", "sbt", "typesafe")
    val tweet = "This is an example tweet talking about scala and sbt."

    wordList.contains("scala") mustBe true
    wordList.foldLeft(false)(_ || tweet.contains(_)) mustBe true
    wordList.exists(tweet.contains) mustBe true

    //finding min, max
    List(14, 35, -7, 46, 98).reduceLeft ( _ min _ ) mustBe List(14, 35, -7, 46, 98).min
    List(14, 35, -7, 46, 98).reduceLeft ( _ max _ ) mustBe List(14, 35, -7, 46, 98).max
  }
}