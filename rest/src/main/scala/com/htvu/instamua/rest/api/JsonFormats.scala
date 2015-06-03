package com.htvu.instamua.rest.api

import spray.httpx.Json4sSupport

trait JsonFormats extends Json4sSupport {
  implicit def json4sFormats = org.json4s.DefaultFormats
}
