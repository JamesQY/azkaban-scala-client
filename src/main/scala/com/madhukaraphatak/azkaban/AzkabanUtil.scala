package com.madhukaraphatak.azkaban

import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import akka.util.ByteString
import spray.json.{JsonReader, pimpString}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

/**
  * Created by madhu on 27/1/16.
  */
object AzkabanUtil {
  def parseResponseAs[T : JsonReader](response:Future[HttpResponse])
                                     (implicit materializer: ActorMaterializer,
                        executionContext:ExecutionContext):Try[T] = {
    val responseEntityFuture = for {
      response <- response
      entity <- response.entity.dataBytes.
        runFold(ByteString.empty)(_ ++ _)
    } yield {
      println(entity.utf8String)
      entity.utf8String.parseJson.convertTo[T]
    }

    Try {
      import scala.concurrent.duration._
      Await.result(responseEntityFuture, 3 seconds)
    }
  }

  def parseResponseAsJson(response:Future[HttpResponse])
                         (implicit materializer: ActorMaterializer,
                        executionContext:ExecutionContext):Try[String] = {

    val responseEntityFuture = for {
      response <- response
      entity <- response.entity.dataBytes.
        runFold(ByteString.empty)(_ ++ _)
    } yield entity.utf8String
    Try {
      import scala.concurrent.duration._
      Await.result(responseEntityFuture, 3 seconds)
    }
  }
}
