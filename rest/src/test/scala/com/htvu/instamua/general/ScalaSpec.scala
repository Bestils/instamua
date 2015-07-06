package com.htvu.instamua.test.general

import org.specs2.mutable.Specification
import org.scalatest._
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.native.JsonMethods._

object RestResponseNomal {
  case class RestResponse(status: Option[String], errors: Option[String], data: Option[String])
}

object RestResponseTypeParameter {
  case class Data(id: Option[Int])
  case class RestResponse[T](status: Option[String], errors: Option[String], data: Option[T])
  case class RestResponseJValue(status: Option[String], errors: Option[String], data: Option[org.json4s.JValue])
}

//declare spray formater protocol
object RestResponseJsonProtocol extends spray.json.DefaultJsonProtocol {
  case class RestResponse[T](status: Option[String], errors: Option[String], data: Option[T])
  implicit def sprayTypeParameterFormat[A: spray.json.JsonFormat] = jsonFormat3(RestResponse.apply[A])
}

//Simple testClass for testing the ScalaTest Style
//We will use both ScalaTest + Spec2
class ScalaTestSpec extends FlatSpec with MustMatchers {
  implicit def json4sFormat:Formats = org.json4s.DefaultFormats

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

  "json4s" must "be able to serialize normal case class" in {
    import RestResponseNomal._
    var restResponse: RestResponse = RestResponse(Some("success"), null, Some("data"))
    val deserializedResponse = read[RestResponse](write[RestResponse](restResponse))

    deserializedResponse.status mustBe Some("success")
    deserializedResponse.data mustBe Some("data")
  }

  "json4s" must "be able to serialize type-parameter case class" in {
    import RestResponseTypeParameter._
    var restResponse: RestResponse[String] = RestResponse[String](Some("success"), None, Some("data"))
    //How to do this?
  }

  "json4s" must "be able to serialize using JValue" in {
    import RestResponseTypeParameter._
    //using JValue instead of type-parameter
    val json =
      """
        |{
        | "status": "success",
        | "data": {
        |   "id": 1
        | }
        |}
      """.stripMargin

    var restJValueResponse: RestResponseJValue = read[RestResponseJValue](json)
    restJValueResponse.data.get mustBe parse("""{"id": 1}""")
  }

  "json4s" must "be able to extract to case class" in {
    import RestResponseTypeParameter._
    val data:JValue = parse("""{"id": 1}""")
    val dataObj = data.extract[Data]

    dataObj.id mustBe Some(1)
    val jvalueData:org.json4s.JValue = Extraction.decompose(dataObj)

    val serializedData:RestResponseJValue = RestResponseJValue(Some("success"), None, Some(Extraction.decompose(data)))
  }


  "spray-json" must "be able to serialize a type-parameter case class" in {
    import RestResponseJsonProtocol._
    var restResponse: RestResponse[String] = RestResponse[String](Some("success"), None, Some("data"))

    //parse the case class to spray JsValue type
    val restResponseJson: spray.json.JsValue = restResponse.toJson
    var deserializedResponse: RestResponse[String] = restResponseJson.convertTo[RestResponse[String]]

    deserializedResponse.status mustBe Some("success")
    deserializedResponse.data mustBe Some("data")
  }
}