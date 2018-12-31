package com.mailinator.service

import java.util.UUID

import com.mailinator._
import com.mailinator.domain.{Email, EmailMessage}
import com.mailinator.include.ErrorTypes.{EmailError, Invalid}
import com.mailinator.include.Util._
import com.mailinator.repository.{EmailMessagesPayload, MailinatorRepository}
import com.mailinator.route.RequestParams.EmailMessageReqParam

import scala.util.Random

trait MailinatorService {
  def newEmail(): Either[EmailError, Email]

  def newMessage(to: Email, req: EmailMessageReqParam): Either[EmailError, UUID]

  def getEmailMessages(email: EmailAddress, page: Int): Either[EmailError, EmailMessagesPayload]

  def getMessage(emailAddress: EmailAddress, messageId: UUID): Either[EmailError, Option[EmailMessage]]

  def deleteEmail(emailAddress: EmailAddress): Either[EmailError, Unit]

  def deleteMessage(emailAddress: EmailAddress, id: UUID): Either[EmailError, UUID]

  def generateEmailUsername(): EmailAddress
}

class MailinatorServiceImpl(repository: MailinatorRepository) extends MailinatorService {

  override def newEmail(): Either[EmailError, Email] = {
    val emailAddress = s"${generateEmailUsername()}@$EmailDomain"
    repository.createEmail(Email(emailAddress)).map(_ => Email(emailAddress))
  }

  override def newMessage(to: Email, req: EmailMessageReqParam): Either[EmailError, UUID] = {
    repository.newMessage(to = to.address, from = req.fromUser, subject = req.subject, message = req.message)
  }

  override def getEmailMessages(emailAddress: EmailAddress, page: Int = 1): Either[EmailError, EmailMessagesPayload] = {
    val start = if (page <= 0) {
      Left(Invalid("Invalid Page"))
    } else {
      Right((page - 1) * RecordsPerPage)
    }
    start.flatMap {
      repository.getMessages(emailAddress, _)
        .map(_.copy(page = Option(page)))
    }
  }

  override def getMessage(emailAddress: EmailAddress, messageId: UUID): Either[EmailError, Option[EmailMessage]] = {
    repository.getMessage(emailAddress = emailAddress, id = messageId): Either[EmailError, Option[EmailMessage]]
  }

  override def deleteEmail(emailAddress: EmailAddress): Either[EmailError, Unit] = {
    repository.deleteEmail(emailAddress: EmailAddress)
  }

  override def deleteMessage(emailAddress: EmailAddress, id: UUID): Either[EmailError, UUID] = {
    repository.deleteMessageById(emailAddress, id)
  }

  override def generateEmailUsername(): EmailAddress = {
    RunUntil {
      Random.alphanumeric.take(EmailNameLength).mkString("")
    } until ((emailAddress: EmailAddress) => repository.getEmail(emailAddress).isEmpty)
  }
}
