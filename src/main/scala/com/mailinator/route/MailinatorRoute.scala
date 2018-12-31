package com.mailinator.route

import java.util.UUID

import cats.effect.Effect
import com.mailinator.EmailAddress
import com.mailinator.domain._
import com.mailinator.include.ErrorTypes._
import com.mailinator.repository.{EmailMessageData, EmailMessagesPayload}
import com.mailinator.route.RequestParams.EmailMessageReqParam
import com.mailinator.service.MailinatorService
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import org.http4s.circe._
import org.http4s.rho.RhoService
import org.http4s.rho.bits.EntityResponseGenerator
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.{EntityDecoder, EntityEncoder, Status}

class MailinatorRoute[F[_] : Effect](mailService: MailinatorService) extends RhoService[F] {

  implicit lazy val emailMessageEncoder: Encoder[EmailMessage] = deriveEncoder
  implicit lazy val emailMessageDataEncoder: Encoder[EmailMessageData] = deriveEncoder
  implicit lazy val emailMessagesPayloadEntityEncoder: EntityEncoder[F, EmailMessagesPayload] = jsonEncoderOf[F, EmailMessagesPayload]
  implicit lazy val emailMessageEntityEncoder: EntityEncoder[F, EmailMessage] = jsonEncoderOf[F, EmailMessage]

  implicit val emailMessageReqParamDecoder: EntityDecoder[F, EmailMessageReqParam] = jsonOf[F, EmailMessageReqParam]

  val swaggerEffectSupport = SwaggerSupport.apply[F]

  import swaggerEffectSupport._

  case class StatusToResponse(http4sStatus: Status) extends EntityResponseGenerator[F](http4sStatus)

  "Create a new, random email address." **
    POST / "mailboxes" |>> {
    mailService.newEmail() match {
      case Right(email) => Created(email.address)
      case Left(exists: EmailExists) => Forbidden(exists.message)
      case Left(error) => InternalServerError(error.toString)
    }
  }

  "Create a new message for a specific email address." **
    POST / "mailboxes" / pathVar[EmailAddress]("email") / "messages" ^ emailMessageReqParamDecoder |>> {
    (mailBox: EmailAddress, emailMessage: EmailMessageReqParam) => {
      mailService.newMessage(to = Email(mailBox), req = emailMessage) match {
        case Right(messageId) => StatusToResponse(Status.Ok).pure(messageId.toString)
        case Left(notFound: EmailNotFound) => StatusToResponse(Status.NotFound).pure(notFound.message)
        case Left(error) => StatusToResponse(Status.InternalServerError).pure(error.toString)
      }
    }
  }

  "Get an index of messages sent to an email address" **
    GET / "mailboxes" / pathVar[EmailAddress]("email") / "messages" +? param[Int]("page") |>> {
    (mailBox: EmailAddress, page: Int) => {
      mailService.getEmailMessages(mailBox, page) match {
        case Right(result: EmailMessagesPayload) => StatusToResponse(Status.Ok).pure(result)
        case Left(invalidRequest: Invalid) => StatusToResponse(Status.BadRequest).pure(invalidRequest.toString)
        case Left(error) => StatusToResponse(Status.NotFound).pure(error.toString)
      }
    }
  }

  "Retrieve a specific message by id." **
    GET / "mailboxes" / pathVar[EmailAddress]("email") / "messages" / pathVar[UUID]("messageId") |>> {
    (mailBox: EmailAddress, id: UUID) => {
      mailService.getMessage(emailAddress = mailBox, messageId = id) match {
        case Right(Some(message)) => StatusToResponse(Status.Ok).pure(message)
        case Right(None) => StatusToResponse(Status.NotFound).pure("")
        case Left(notFound: EmailNotFound) => StatusToResponse(Status.NotFound).pure(notFound.message)
        case Left(error) => StatusToResponse(Status.InternalServerError).pure(error.toString)
      }
    }
  }

  "Delete a specific email address and any associated messages." **
    DELETE / "mailboxes" / pathVar[EmailAddress]("email") |>> {
    (mailBox: EmailAddress) => {
      mailService.deleteEmail(emailAddress = mailBox) match {
        case Right(_) => StatusToResponse(Status.Ok).pure(mailBox)
        case Left(notFound: EmailNotFound) => StatusToResponse(Status.NotFound).pure(notFound.message)
        case Left(error) => StatusToResponse(Status.InternalServerError).pure(error.toString)
      }
    }
  }

  "Delete a specific message by id." **
    DELETE / "mailboxes" / pathVar[EmailAddress]("email") / "messages" / pathVar[UUID]("messageId") |>> {
    (mailBox: EmailAddress, id: UUID) => {
      mailService.deleteMessage(mailBox, id) match {
        case Right(id) => StatusToResponse(Status.Ok).pure(id.toString)
        case Left(notFound: EmailNotFound) => StatusToResponse(Status.NotFound).pure(notFound.message)
        case Left(error) => StatusToResponse(Status.InternalServerError).pure(error.toString)
      }
    }
  }

}
