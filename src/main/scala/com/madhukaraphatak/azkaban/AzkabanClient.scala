package com.madhukaraphatak.azkaban

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.madhukaraphatak.azkaban.AzkabanModels._
import com.madhukaraphatak.azkaban.AzkabanModels.ServiceJsonProtocol._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Main entry point of most of the functionality
  */

case class AzkabanContext(sessionId:String)

class AzkabanClient(url:String,
                    implicit val system: ActorSystem, implicit val materializer: ActorMaterializer,
                   implicit val executionContext:ExecutionContext) {

  val httpClient = Http()
  def createSession(createSessionRequest: CreateSessionRequest): Try[AzkabanContext] = {
    val requestByteString = ByteString(s"action=login&username=${createSessionRequest.userName}" +
      s"&password=${createSessionRequest.password}")
    val loginRequest = HttpRequest(
      HttpMethods.POST,
      uri = url,
      entity = HttpEntity(`application/x-www-form-urlencoded` withCharset `UTF-8`, requestByteString)
    )

    val responseFuture: Future[HttpResponse] = httpClient.singleRequest(loginRequest)
    val parseResponse = AzkabanUtil.parseResponseAs[SessionResponse](responseFuture)
    parseResponse.map { response =>
      require(!response.error.isDefined, "session creation failed with " + response.error.get)
      AzkabanContext(response.`session.id`.get)
    }
  }

  def runFlow(runFlowRequest:RunFlowRequest)(context:AzkabanContext) = {
    val requestData = s"session.id=${context.sessionId}&ajax=" +
      s"executeFlow&project=${runFlowRequest.projectName}"+
      s"&flow=${runFlowRequest.flowName}"
    val executeRequest = HttpRequest(
      HttpMethods.GET,
      uri = url + s"/executor?$requestData"
    )
    val responseFuture: Future[HttpResponse] = httpClient.singleRequest(executeRequest)
    println(AzkabanUtil.parseResponseAsJson(responseFuture))
  }

  def getProjectIdByProjectName(projectName:String)(context:AzkabanContext):Try[Int] = {
    val requestData = s"session.id=${context.sessionId}&ajax=fetchprojectflows&project=$projectName"
    val executeRequest = HttpRequest(
      HttpMethods.GET,
      uri = url + s"/manager?$requestData"
    )
    val responseFuture: Future[HttpResponse] = httpClient.singleRequest(executeRequest)
    val parseResponse = AzkabanUtil.parseResponseAs[FetchFlowsResponse](responseFuture)
    parseResponse.map { response =>
      require(response.projectId.isDefined, s"project with name $projectName not found")
      response.projectId.get
    }
  }

  def getScheduleByFlowName(projectName:String, flowName:String)(context:AzkabanContext):Try[String] = {
    val projectId = getProjectIdByProjectName(projectName)(context).get
    val requestData = s"session.id=${context.sessionId}&ajax=fetchSchedule&projectId=$projectId" +
      s"&flowId=$flowName"
    val executeRequest = HttpRequest(
      HttpMethods.GET,
      uri = url + s"/schedule?$requestData"
    )
    val responseFuture: Future[HttpResponse] = httpClient.singleRequest(executeRequest)
    val parseResponse = Try(AzkabanUtil.parseResponseAs[FetchScheduleResponse](responseFuture))
    parseResponse.map { response =>
      require(response.isSuccess, s"flow $flowName of project $projectName is not schduled")
      response.get.schedule.scheduleId
    }
  }

  def scheduleFlow(scheduleFlowRequest: ScheduleFlowRequest)(context:AzkabanContext) = {

    val projectId = getProjectIdByProjectName(scheduleFlowRequest.projectName)(context).get
    val requestData = s"session.id=${context.sessionId}&ajax=scheduleFlow&&projectName=${scheduleFlowRequest.projectName}"+
      s"&flow=${scheduleFlowRequest.flowName}&projectId=$projectId&scheduleTime=${scheduleFlowRequest.startTime}" +
      s"&scheduleDate=${scheduleFlowRequest.startDate}"
    val executeRequest = HttpRequest(
      HttpMethods.GET,
      uri = url + s"/schedule?$requestData"
    )
    val responseFuture: Future[HttpResponse] = httpClient.singleRequest(executeRequest)
    println(AzkabanUtil.parseResponseAsJson(responseFuture))
  }

  def unscheduleFlow(projectName:String, flowName:String)(context: AzkabanContext) = {
    val scheduleId = getScheduleByFlowName(projectName,flowName)(context).get
    val requestData=ByteString(s"action=removeSched&scheduleId=$scheduleId")
    val cookieHeader = RawHeader("Cookie",s"azkaban.browser.session.id=${context.sessionId}")
    val executeRequest = HttpRequest(
      HttpMethods.POST,
      headers = scala.collection.immutable.Seq(cookieHeader),
      uri = url + s"/schedule",
      entity = HttpEntity(`application/x-www-form-urlencoded` withCharset `UTF-8`, requestData)
    )
    val responseFuture: Future[HttpResponse] = httpClient.singleRequest(executeRequest)
   println(AzkabanUtil.parseResponseAsJson(responseFuture))

  }

  def uploadProjectZip(projectName:String, fileLocation:String)(context: AzkabanContext) = {

    def createEntity(file: File): Future[RequestEntity] = {
      require(file.exists())
      val formData =
        Multipart.FormData(
          Source(List(
            Multipart.FormData.BodyPart.Strict("session.id", HttpEntity(ByteString(context.sessionId))),
            Multipart.FormData.BodyPart.Strict("ajax", HttpEntity(ByteString("upload"))),
            Multipart.FormData.BodyPart.Strict("project", HttpEntity(`text/plain` withCharset `UTF-8`,
              ByteString(projectName))),
            Multipart.FormData.BodyPart(
              "file",
              HttpEntity(MediaTypes.`application/zip`, file.length(), FileIO.fromFile(file, chunkSize = 100000)), // the chunk size here is currently critical for performance
              Map("filename" -> file.getName)))))
      Marshal(formData).to[RequestEntity]
    }



    val postRequestFuture = for {
      e ← createEntity(new File(fileLocation))
    } yield {
      val executeRequest = HttpRequest(
        HttpMethods.POST,
        uri = url + s"/manager?ajax=upload",
        entity = e)

      val responseFuture: Future[HttpResponse] = httpClient.singleRequest(executeRequest)
      println(AzkabanUtil.parseResponseAsJson(responseFuture))

    }
  }


}