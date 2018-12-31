package com.mailinator.domain

import com.mailinator._

case class Email(address: EmailAddress, firstName: Option[String] = None, lastName: Option[String] = None)

case class EmailMessage(toUser: EmailAddress,
                        fromUser: EmailAddress,
                        subject: String,
                        message: String,
                        creationTime: Long = systemClock.millis()
                       )