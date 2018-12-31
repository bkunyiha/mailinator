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

  "Should be able to create new email" should "Succeed" in {

    (mailModel.createEmail _ ) expects(*) returns(Right(()))
    (mailModel.getEmail _ ) expects(*) returns(None)

    val serviceResult: Either[EmailError, Email] = mailService.newEmail()

    assert(serviceResult.isRight)
  }

  "Creating a new message" should "Succeed" in {

    (mailModel.newMessage(_: EmailAddress, _: EmailAddress, _: String, _: String)) expects(*,*,*,*) returns(Right((UUID.randomUUID())))

    val serviceResult: Either[EmailError, UUID] = mailService.newMessage(to=Email1, req=EmailMessageParams1)

    assert(serviceResult.isRight)
  }
}

