package com

import java.time.{Clock, ZoneId}

package object mailinator {

  type EmailAddress = String

  implicit val systemClock = Clock.system(ZoneId.of("America/Los_Angeles"))
  val RecordsPerPage = 10
  val DefaultPage = 1
  val EmailDomain = "mailinator.com"
  val EmailNameLength = 12
}