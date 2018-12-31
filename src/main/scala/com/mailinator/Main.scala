package com.mailinator

import cats.effect.{Effect, IO}
import cats.implicits._
import fs2.{Stream, StreamApp}
import org.http4s.HttpService
import org.http4s.rho.swagger.SwaggerSupport
import org.http4s.rho.swagger.models.Info
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object Main extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]) = ServerStream.stream[IO]
}

object ServerStream {

  import com.mailinator.repository.MailinatorRepositoryImpl
  import com.mailinator.route.{DocRoute, MailinatorRoute}
  import com.mailinator.service.MailinatorServiceImpl

  val maxItemsPerCategory = 100L

  def stream[F[_] : Effect](implicit ec: ExecutionContext): Stream[F, StreamApp.ExitCode] = {
    val staticContentRoute = new DocRoute[F]().routes()
    val swaggerMiddleware = SwaggerSupport[F].createRhoMiddleware(apiInfo = Info(
      title = SbtBuildInfo.name,
      description = Some(SbtBuildInfo.description),
      version = SbtBuildInfo.version
    ))
    val mailinatorRepository = new MailinatorRepositoryImpl(maxItemsPerCategory)
    val mailinatorService = new MailinatorServiceImpl(mailinatorRepository)
    val mailinatorModule: HttpService[F] = new MailinatorRoute[F](mailinatorService).toService(swaggerMiddleware)

    val httpService = mailinatorModule <+> staticContentRoute

    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(httpService, "/")
      .serve
  }
}

