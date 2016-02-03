package com.madhukaraphatak.azkaban

import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.typesafe.config
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import spray.json.{JsonReader, pimpString}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

/**
  * Created by madhu on 27/1/16.
  */
object AzkabanUtil {
  val logger = LoggerFactory.getLogger("AzkabanUtil")
  def parseResponseAs[T : JsonReader](response:Future[HttpResponse])
                                     (implicit materializer: ActorMaterializer,
                        executionContext:ExecutionContext, config: Config):Try[T] = {
    val timeoutString = config.getString("azkaban.client.request.timeout")
    logger.info(s"timeout is $timeoutString")
    val responseEntityFuture = for {
      response <- response
      entity <- response.entity.dataBytes.
        runFold(ByteString.empty)(_ ++ _)
    } yield {
      logger.debug(entity.utf8String)
      entity.utf8String.parseJson.convertTo[T]
    }

    Try {
      import scala.concurrent.duration._
      Await.result(responseEntityFuture, Duration(timeoutString))
    }
  }

  def parseResponseAsJson(response:Future[HttpResponse])
                         (implicit materializer: ActorMaterializer,
                        executionContext:ExecutionContext,
                          azkabanContext: AzkabanContext):Try[String] = {
    val timeoutString = azkabanContext.configuration.getString("azkaban.client.request.timeout")
    val responseEntityFuture = for {
      response <- response
      entity <- response.entity.dataBytes.
        runFold(ByteString.empty)(_ ++ _)
    } yield entity.utf8String
    Try {
      import scala.concurrent.duration._
      Await.result(responseEntityFuture, Duration(timeoutString))
    }
  }

  def getDefaultConfiguration():config.Config = {
    ConfigFactory.parseString(
      """
        | { azkaban.client.request.timeout = 30 seconds }
      """.stripMargin)
  }
}
