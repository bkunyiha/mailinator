package com.mailinator

import java.util.UUID

import com.mailinator.domain.{Email, EmailMessage}
import com.mailinator.repository.{EmailMessageData, EmailMessagesPayload}
import com.mailinator.route.RequestParams.EmailMessageReqParam
import org.http4s.headers.`Content-Type`
import org.http4s.{Headers, MediaType}

object UnitTestFixtures {

  val EmailAddress1 = s"email1@$EmailDomain"
  val Email1 = Email(address = EmailAddress1)
  val EmailAddress2 = s"email2@$EmailDomain"
  val Email2 = Email(address = EmailAddress2)
  val MessageID1 = UUID.randomUUID()

  val EmailMessageParams1 = EmailMessageReqParam(fromUser = EmailAddress2, subject = "", message = "")
  val RequestHeaders = Headers(List(`Content-Type`(MediaType.`application/json`)))


  val EmailMessage1 = EmailMessage(toUser=EmailAddress1,
    fromUser=EmailAddress2,
    subject="",
    message="",
    creationTime = systemClock.millis()
  )
  val EmailMessageData1 = EmailMessageData(id=MessageID1, message=EmailMessage1)
  val EmailMessagesPayload1 = EmailMessagesPayload(emailData= List(EmailMessageData1), totalItems=1)
}

