package com.mailinator.route

import java.util.UUID

import cats.effect.IO
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
    (emailServiceMock.newEmail _).expects().returning(Right(Email1)) once()

    val request = Request[IO](Method.POST, Uri(path = "/mailboxes"))

    val response: Option[Response[IO]] = mailinatorRoute(request).value.unsafeRunSync()

    logger.info(response.get.body.through(fs2.text.utf8Decode).compile.toList.unsafeRunSync().mkString(""))
    assert(response.get.status == org.http4s.Status.Created)
  }

  "POST to 'mailboxes/[EmailAddress]/messages'" should "return OK" in {
    (emailServiceMock.newMessage _).expects(*, *).returning(Right(UUID.randomUUID())) once()

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

}
