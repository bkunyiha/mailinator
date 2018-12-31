package com.mailinator.repository

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.google.common.cache.{Cache, _}
import com.mailinator._
import com.mailinator.domain.{Email, EmailMessage}
import com.mailinator.include.ErrorTypes.{EmailError, EmailExists, EmailNotFound}

import scala.collection.JavaConverters._
import scala.collection.concurrent

case class EmailMessageData(id: UUID, message: EmailMessage)

case class EmailMessagesPayload(emailData: List[EmailMessageData], totalItems: Long, page: Option[Int] = Some(1))

trait MailinatorRepository {

  def createEmail(email: Email): Either[EmailError, Unit]

  def newMessage(to: EmailAddress, from: EmailAddress, subject: String, message: String): Either[EmailError, UUID]

  def getEmail(address: EmailAddress): Option[Cache[UUID, EmailMessage]]

  def getMessages(to: EmailAddress, start: Int = 0): Either[EmailError, EmailMessagesPayload]

  def getMessage(emailAddress: EmailAddress, id: UUID): Either[EmailError, Option[EmailMessage]]

  def deleteEmail(emailAddress: EmailAddress): Either[EmailError, Unit]

  def deleteMessageById(emailAddress: EmailAddress, id: UUID): Either[EmailError, UUID]

  def deleteEmailMessages(to: Email): Either[EmailError, Unit]
}

class MailinatorRepositoryImpl(maxItemsPerCategory: Long) extends MailinatorRepository {

  private val emailModel = new concurrent.TrieMap[String, Cache[UUID, EmailMessage]]

  private def createUUID(): UUID = UUID.randomUUID()

  override def createEmail(email: Email): Either[EmailError, Unit] = {
    getEmail(email.address) match {
      case Some(_: Cache[UUID, EmailMessage]) => Left(EmailExists())
      case None => {
        emailModel.put(email.address, newEmail())
        Right(())
      }
    }
  }

  override def newMessage(to: EmailAddress, from: EmailAddress, subject: String, message: String): Either[EmailError, UUID] = {
    getEmail(to) match {
      case Some(emailMessages: Cache[UUID, EmailMessage]) => {

        val messageId = createUUID()
        emailMessages.put(messageId,
          EmailMessage(toUser = to, fromUser = from, subject = subject, message = message)
        )
        Right(messageId)
      }
      case None => Left(EmailNotFound())
    }
  }

  override def getEmail(address: EmailAddress): Option[Cache[UUID, EmailMessage]] = {
    emailModel.get(address)
  }

  override def getMessages(emailSddress: EmailAddress, start: Int = 0): Either[EmailError, EmailMessagesPayload] = {
    getEmail(emailSddress) match {
      case Some(emailMessage: Cache[UUID, EmailMessage]) => {
        val uuidEmailListTotal: Seq[(UUID, EmailMessage)] = emailMessage
          .asMap()
          .asScala
          .toList
        val emailCount = uuidEmailListTotal.size
        val uuidEmailList: Seq[EmailMessageData] = uuidEmailListTotal.slice(start, start + RecordsPerPage).map {
          case (id: UUID, emailMessage: EmailMessage) => EmailMessageData(id, emailMessage)
        }

        Right(EmailMessagesPayload(emailData = uuidEmailList.toList, totalItems = emailCount.toLong))
      }
      case None => Left(EmailNotFound())
    }
  }

  override def getMessage(emailAddress: EmailAddress, id: UUID): Either[EmailError, Option[EmailMessage]] = {
    getEmail(emailAddress) match {
      case Some(emailMessage: Cache[UUID, EmailMessage]) => {
        Right(Option(emailMessage.getIfPresent(id)))
      }
      case None => Left(EmailNotFound())
    }
  }

  override def deleteEmail(emailAddress: EmailAddress): Either[EmailError, Unit] = {
    getEmail(emailAddress) match {
      case Some(emailMessages: Cache[UUID, EmailMessage]) => {
        emailMessages.invalidateAll()
        emailModel.remove(emailAddress)
        Right(())
      }
      case None => Left(EmailNotFound())
    }
  }

  override def deleteMessageById(emailAddress: EmailAddress, id: UUID): Either[EmailError, UUID] = {
    getEmail(emailAddress) match {
      case Some(emailMessage: Cache[UUID, EmailMessage]) => {
        emailMessage.invalidate(id)
        Right(id)
      }
      case None => Left(EmailNotFound())
    }
  }

  override def deleteEmailMessages(to: Email): Either[EmailError, Unit] = {
    getEmail(to.address) match {
      case Some(emailMessage: Cache[UUID, EmailMessage]) => Right(emailMessage.invalidateAll())
      case None => Left(EmailNotFound())
    }
  }

  private def newEmail(): Cache[UUID, EmailMessage] =
    CacheBuilder
      .newBuilder()
      .maximumSize(maxItemsPerCategory)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build[UUID, EmailMessage]()

}