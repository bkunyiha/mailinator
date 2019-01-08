package com.mailinator.route

import java.util.UUID

import cats.effect.IO
import com.mailinator.EmailAddress
import com.mailinator.include.ErrorTypes._
import com.mailinator.UnitTestFixtures._
import com.mailinator.service.MailinatorService
import com.typesafe.scalalogging.LazyLogging
import org.http4s.{Method, Request, Response, Uri}
import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class MailinatorRouteTest extends FlatSpec with Matchers with MockFactory with LazyLogging {

  lazy val emailServiceMock = mock[MailinatorService]
  lazy val mailinatorRoute = new MailinatorRoute[IO](emailServiceMock).toService()

  "POST to 'mailboxes'" should "return Created" in {
    (emailServiceMock.newEmail _) expects() returns Right(Email1) once()

    val request = Request[IO](Method.POST, Uri(path = "/mailboxes"))

    val response: Option[Response[IO]] = mailinatorRoute(request).value.unsafeRunSync()

    logger.info(response.get.body.through(fs2.text.utf8Decode).compile.toList.unsafeRunSync().mkString(""))
    assert(response.get.status == org.http4s.Status.Created)
  }

  "POST to 'mailboxes/[EmailAddress]/messages'" should "return OK" in {
    (emailServiceMock.newMessage _) expects(*, *) returns Right(UUID.randomUUID()) once()

    val emailReqParamJsonString = s"""{
                                     |	"fromUser": "$EmailAddress2",
                                     |	"subject": "",
                                     |	"message": ""
                                     |}""".stripMargin.trim

    val request: IO[Request[IO]] = Request[IO](Method.POST, Uri(path = s"/mailboxes/$EmailAddress1/messages"), headers = RequestHeaders)
      .withBody(emailReqParamJsonString)

    val response: Option[Response[IO]] = mailinatorRoute(request.unsafeRunSync()).value.unsafeRunSync()

    logger.info(response.get.body.through(fs2.text.utf8Decode).compile.toList.unsafeRunSync().mkString(""))
    assert(response.get.status == org.http4s.Status.Ok)
  }

  "POST to 'mailboxes/[EmailAddress]/messages'" should "return NotFound if email does not exist" in {
    (emailServiceMock.newMessage _) expects(*, *) returns Left(EmailNotFound()) once()

    val emailReqParamJsonString = s"""{
                                     |	"fromUser": "$EmailAddress2",
                                     |	"subject": "",
                                     |	"message": ""
                                     |}""".stripMargin.trim

    val request: IO[Request[IO]] = Request[IO](Method.POST, Uri(path = s"/mailboxes/$EmailAddress1/messages"), headers = RequestHeaders)
      .withBody(emailReqParamJsonString)

    val response: Option[Response[IO]] = mailinatorRoute(request.unsafeRunSync()).value.unsafeRunSync()

    logger.info(response.get.body.through(fs2.text.utf8Decode).compile.toList.unsafeRunSync().mkString(""))
    assert(response.get.status == org.http4s.Status.NotFound)
  }

  "POST to 'mailboxes/[EmailAddress]/messages'" should "return InternalServerError for any other error" in {
    (emailServiceMock.newMessage _) expects(*, *) returns Left(Invalid("Unknown")) once()

    val emailReqParamJsonString = s"""{
                                     |	"fromUser": "$EmailAddress2",
                                     |	"subject": "",
                                     |	"message": ""
                                     |}""".stripMargin.trim

    val request: IO[Request[IO]] = Request[IO](Method.POST, Uri(path = s"/mailboxes/$EmailAddress1/messages"), headers = RequestHeaders)
      .withBody(emailReqParamJsonString)

    val response: Option[Response[IO]] = mailinatorRoute(request.unsafeRunSync()).value.unsafeRunSync()

    logger.info(response.get.body.through(fs2.text.utf8Decode).compile.toList.unsafeRunSync().mkString(""))
    assert(response.get.status == org.http4s.Status.InternalServerError)
  }

  "GET /mailboxes/pathVar[EmailAddress]/messages?page=1" should "return Ok" in {
    (emailServiceMock.getEmailMessages(_: EmailAddress, _: Int)) expects(*, *) returns Right(EmailMessagesPayload1) once()

    val request = Request[IO](uri = Uri.fromString(s"/mailboxes/$EmailAddress1/messages?page=1").right.getOrElse(sys.error("Failed.")))
    val response: Response[IO] = mailinatorRoute(request).value.unsafeRunSync().getOrElse(Response.notFound)

    assert(response.status == org.http4s.Status.Ok)
  }

  "GET /mailboxes/pathVar[EmailAddress]/messages?page=1" should "return NotFound If email does not exist" in {
    (emailServiceMock.getEmailMessages(_: EmailAddress, _: Int)) expects(*, *) returns Left(EmailNotFound()) once()

    val request = Request[IO](uri = Uri.fromString(s"/mailboxes/$EmailAddress1/messages?page=1").right.getOrElse(sys.error("Failed.")))
    val response: Response[IO] = mailinatorRoute(request).value.unsafeRunSync().getOrElse(Response.notFound)

    assert(response.status == org.http4s.Status.NotFound)
  }

  "GET /mailboxes/pathVar[EmailAddress]/messages?page=1" should "return BadRequest for invalid request" in {
    (emailServiceMock.getEmailMessages(_: EmailAddress, _: Int)) expects(*, *) returns Left(Invalid("Invalid Request")) once()

    val request = Request[IO](uri = Uri.fromString(s"/mailboxes/$EmailAddress1/messages?page=1").right.getOrElse(sys.error("Failed.")))
    val response: Response[IO] = mailinatorRoute(request).value.unsafeRunSync().getOrElse(Response.notFound)

    assert(response.status == org.http4s.Status.BadRequest)
  }

  "GET /mailboxes/pathVar[EmailAddress]/messages/messageId" should "return Ok" in {
    (emailServiceMock.getMessage(_: EmailAddress, _: UUID)) expects(*, *) returns Right(Some(EmailMessage1)) once()

    val request = Request[IO](uri = Uri.fromString(s"/mailboxes/$EmailAddress1/messages/${UUID.randomUUID()}").right.getOrElse(sys.error("Failed.")))
    val response: Response[IO] = mailinatorRoute(request).value.unsafeRunSync().getOrElse(Response.notFound)

    assert(response.status == org.http4s.Status.Ok)
  }

  "GET /mailboxes/pathVar[EmailAddress]/messages/messageId" should "return NotFound no messaged found" in {
    (emailServiceMock.getMessage(_: EmailAddress, _: UUID)) expects(*, *) returns Right(None) once()

    val request = Request[IO](uri = Uri.fromString(s"/mailboxes/$EmailAddress1/messages/${UUID.randomUUID()}").right.getOrElse(sys.error("Failed.")))
    val response: Response[IO] = mailinatorRoute(request).value.unsafeRunSync().getOrElse(Response.notFound)

    assert(response.status == org.http4s.Status.NotFound)
  }

  "GET /mailboxes/pathVar[EmailAddress]/messages/messageId" should "return NotFound if email does not exist" in {
    (emailServiceMock.getMessage(_: EmailAddress, _: UUID)) expects(*, *) returns Left(EmailNotFound()) once()

    val request = Request[IO](uri = Uri.fromString(s"/mailboxes/$EmailAddress1/messages/${UUID.randomUUID()}").right.getOrElse(sys.error("Failed.")))
    val response: Response[IO] = mailinatorRoute(request).value.unsafeRunSync().getOrElse(Response.notFound)

    assert(response.status == org.http4s.Status.NotFound)
  }

  "GET /mailboxes/pathVar[EmailAddress]/messages/messageId" should "return InternalServerError for all errors apart from email not found" in {
    (emailServiceMock.getMessage(_: EmailAddress, _: UUID)) expects(*, *) returns Left(Invalid("Invalid")) once()

    val request = Request[IO](uri = Uri.fromString(s"/mailboxes/$EmailAddress1/messages/${UUID.randomUUID()}").right.getOrElse(sys.error("Failed.")))
    val response: Response[IO] = mailinatorRoute(request).value.unsafeRunSync().getOrElse(Response.notFound)

    assert(response.status == org.http4s.Status.InternalServerError)
  }

  "DELETE /mailboxes/pathVar[EmailAddress]" should "return Ok" in {
    (emailServiceMock.deleteEmail(_: EmailAddress)) expects(*) returns Right(()) once()

    val request = Request[IO](Method.DELETE, Uri(path = s"/mailboxes/$EmailAddress1"), headers = RequestHeaders)
    val response: Option[Response[IO]] = mailinatorRoute(request).value.unsafeRunSync()

    assert(response.get.status == org.http4s.Status.Ok)
  }

  "DELETE /mailboxes/pathVar[EmailAddress]" should "return NotFound if email does not exist" in {
    (emailServiceMock.deleteEmail(_: EmailAddress)) expects(*) returns Left(EmailNotFound()) once()

    val request = Request[IO](Method.DELETE, Uri(path = s"/mailboxes/$EmailAddress1"), headers = RequestHeaders)
    val response: Option[Response[IO]] = mailinatorRoute(request).value.unsafeRunSync()

    assert(response.get.status == org.http4s.Status.NotFound)
  }

  "DELETE /mailboxes/pathVar[EmailAddress]" should "return InternalServerError all other errors" in {
    (emailServiceMock.deleteEmail(_: EmailAddress)) expects(*) returns Left(Invalid("")) once()

    val request = Request[IO](Method.DELETE, Uri(path = s"/mailboxes/$EmailAddress1"), headers = RequestHeaders)
    val response: Option[Response[IO]] = mailinatorRoute(request).value.unsafeRunSync()

    assert(response.get.status == org.http4s.Status.InternalServerError)
  }

  "DELETE /mailboxes/pathVar[EmailAddress]/messages/messageId" should "return Ok" in {
    (emailServiceMock.deleteMessage(_: EmailAddress, _: UUID)) expects(*, *) returns Right(UUID.randomUUID()) once()

    val request = Request[IO](Method.DELETE, Uri(path = s"/mailboxes/$EmailAddress1/messages/${UUID.randomUUID()}"), headers = RequestHeaders)
    val response: Option[Response[IO]] = mailinatorRoute(request).value.unsafeRunSync()

    assert(response.get.status == org.http4s.Status.Ok)
  }

  "DELETE /mailboxes/pathVar[EmailAddress]/messages/messageId" should "return NotFound if email does not exist" in {
    (emailServiceMock.deleteMessage(_: EmailAddress, _: UUID)) expects(*, *) returns Left(EmailNotFound()) once()

    val request = Request[IO](Method.DELETE, Uri(path = s"/mailboxes/$EmailAddress1/messages/${UUID.randomUUID()}"), headers = RequestHeaders)
    val response: Option[Response[IO]] = mailinatorRoute(request).value.unsafeRunSync()

    assert(response.get.status == org.http4s.Status.NotFound)
  }

  "DELETE /mailboxes/pathVar[EmailAddress]/messages/messageId" should "return InternalServerError for all other errors" in {
    (emailServiceMock.deleteMessage(_: EmailAddress, _: UUID)) expects(*, *) returns Left(Invalid("Invalid")) once()

    val request = Request[IO](Method.DELETE, Uri(path = s"/mailboxes/$EmailAddress1/messages/${UUID.randomUUID()}"), headers = RequestHeaders)
    val response: Option[Response[IO]] = mailinatorRoute(request).value.unsafeRunSync()

    assert(response.get.status == org.http4s.Status.InternalServerError)
  }
}
