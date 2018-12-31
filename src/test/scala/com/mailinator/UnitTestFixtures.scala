package com.mailinator

import com.mailinator.domain.Email

object UnitTestFixtures {

  import com.mailinator.route.RequestParams.EmailMessageReqParam
  import org.http4s.{Headers, MediaType}
  import org.http4s.headers.`Content-Type`

  val EmailAddress1 = s"email1@$EmailDomain"
  val Email1 = Email(address = EmailAddress1)
  val EmailAddress2 = s"email2@$EmailDomain"
  val Email2 = Email(address = EmailAddress2)

  val EmailMessageParams1 = EmailMessageReqParam(fromUser = EmailAddress2, subject = "", message = "")
  val RequestHeaders = Headers(List(`Content-Type`(MediaType.`application/json`)))
}

