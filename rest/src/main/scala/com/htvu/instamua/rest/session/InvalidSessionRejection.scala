package com.htvu.instamua.rest.session

import spray.routing.Rejection

case class InvalidSessionRejection(valueName: String) extends Rejection
