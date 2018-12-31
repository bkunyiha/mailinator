package com.mailinator.route

import com.mailinator.EmailAddress

object RequestParams {
  case class EmailMessageReqParam(fromUser: EmailAddress, subject: String, message: String)
}
