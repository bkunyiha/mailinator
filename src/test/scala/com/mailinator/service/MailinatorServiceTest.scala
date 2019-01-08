package com.mailinator.service

import java.util.UUID

import com.mailinator.EmailAddress
import com.mailinator.UnitTestFixtures._
import com.mailinator.domain.Email
import com.mailinator.include.ErrorTypes._
import com.mailinator.repository.MailinatorRepository
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class MailinatorServiceTest extends FlatSpec with Matchers with MockFactory {

  val mailModel = mock[MailinatorRepository]
  val mailService = new MailinatorServiceImpl(mailModel)

  "Creaating a new random email" should "Succeed" in {
    (mailModel.createEmail _) expects (*) returns (Right(()))
    (mailModel.getEmail _) expects (*) returns (None)

    val serviceResult: Either[EmailError, Email] = mailService.newEmail()

    assert(serviceResult.isRight)
  }

  "creating new email" should " fail if email already exists" in {
    (mailModel.createEmail _) expects (*) returns Left(EmailExists())
    (mailModel.getEmail _) expects (*) returns (None)

    val serviceResult: Either[EmailError, Email] = mailService.newEmail()

    assert(serviceResult.isLeft)
  }

  "Creating a new message" should "Succeed" in {
    (mailModel.newMessage(_: EmailAddress, _: EmailAddress, _: String, _: String)) expects(*, *, *, *) returns Right(UUID.randomUUID())

    val serviceResult: Either[EmailError, UUID] = mailService.newMessage(to = Email1, req = EmailMessageParams1)

    assert(serviceResult.isRight)
  }

  "Creating a new message" should "fail if email does not exist" in {
    (mailModel.newMessage(_: EmailAddress, _: EmailAddress, _: String, _: String)) expects(*, *, *, *) returns Left(EmailNotFound())

    val serviceResult: Either[EmailError, UUID] = mailService.newMessage(to = Email1, req = EmailMessageParams1)

    assert(serviceResult.isLeft)
  }

  "Getting multiple messages" should "Succeed" in {
    (mailModel.getMessages(_: EmailAddress, _: Int)) expects(*, *) returns Right(EmailMessagesPayload1)

    val serviceResult = mailService.getEmailMessages(emailAddress = EmailAddress1, page = 1)
    assert(serviceResult.isRight)
  }

  "Getting multiple messages" should "fail if page number is negative or equal to zero" in {
    val serviceResult = mailService.getEmailMessages(emailAddress = EmailAddress1, page = 0)
    assert(serviceResult.isLeft)
  }

  "Getting multiple messages" should "fail if email does not exist" in {
    (mailModel.getMessages(_: EmailAddress, _: Int)) expects(*, *) returns Left(EmailNotFound())

    val serviceResult = mailService.getEmailMessages(emailAddress = EmailAddress1, page = 1)
    assert(serviceResult.isLeft)
  }

  "Getting a single message" should "Succeed" in {
    (mailModel.getMessage(_: EmailAddress, _: UUID)) expects(*, *) returns Right(Some(EmailMessage1))

    val serviceResult = mailService.getMessage(emailAddress = EmailAddress1, messageId = MessageID1)
    assert(serviceResult.isRight)
  }

  "Getting a single message" should " fail if email does not exist" in {
    (mailModel.getMessage(_: EmailAddress, _: UUID)) expects(*, *) returns Left(EmailNotFound())

    val serviceResult = mailService.getMessage(emailAddress = EmailAddress1, messageId = MessageID1)
    assert(serviceResult.isLeft)
  }

  "Deleting the email" should "Succeed" in {
    (mailModel.deleteEmail(_: EmailAddress)) expects (*) returns Right(())

    val serviceResult = mailService.deleteEmail(emailAddress = EmailAddress1)
    assert(serviceResult.isRight)
  }

  "Deleting the email" should "fail if email does not exist" in {
    (mailModel.deleteEmail(_: EmailAddress)) expects (*) returns Left(EmailNotFound())

    val serviceResult = mailService.deleteEmail(emailAddress = EmailAddress1)
    assert(serviceResult.isLeft)
  }

  "Deleting a message" should "Succeed" in {
    (mailModel.deleteMessageById(_: EmailAddress, _: UUID)) expects(*, *) returns Right(MessageID1)

    val serviceResult = mailService.deleteMessage(emailAddress = EmailAddress1, id = MessageID1)
    assert(serviceResult.isRight)
  }

  "Deleting a message" should "fail if email does not exist" in {
    (mailModel.deleteMessageById(_: EmailAddress, _: UUID)) expects(*, *) returns Left(EmailNotFound())

    val serviceResult = mailService.deleteMessage(emailAddress = EmailAddress1, id = MessageID1)
    assert(serviceResult.isLeft)
  }

  "Generating a random email" should "succeed if email does not exist" in {

    (mailModel.getEmail(_: EmailAddress)) expects (*) returns None

    val serviceResult = mailService.generateEmailUsername()
    assert(serviceResult.length > 5)
  }
}

