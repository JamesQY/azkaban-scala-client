package com.madhukaraphatak.azkaban

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.madhukaraphatak.azkaban.AzkabanModels._
import org.slf4j.{LoggerFactory, Logger}

import scala.concurrent.Await
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * End to end test
  */
object E2ETest {

  def main(args: Array[String]) {

    val azkabanUrl = Try(args(0)).getOrElse("http://localhost:8081")
    val userName = Try(args(1)).getOrElse("azkaban")
    val password = Try(args(2)).getOrElse("azkaban")
    val project =  Try(args(3)).getOrElse("test")
    val flow = Try(args(4)).getOrElse("test1")
    val zipFilePath = Try(args(5)).getOrElse("src/main/resources/azkaban-jobs.zip")
    val logger = LoggerFactory.getLogger("E2ETest")

    implicit val actorSystem = ActorSystem.create()
    implicit val materializer = ActorMaterializer()
    val azkabanClient = new AzkabanClient(azkabanUrl)

    logger.info("setting up session with azkaban")
    //create azkaban session
    val azkabanSession = azkabanClient.createSession(
      CreateSessionRequest(userName,password)).get

    logger.info(s"setuped session. Session id is ${azkabanSession.sessionId}")

    logger.info("uploading project file")

    val fileUploadResponse = azkabanClient.uploadProjectZip(UploadProjectZipRequest(
      project,zipFilePath))(azkabanSession)

    import scala.concurrent.duration._
    Await.result(fileUploadResponse, 3 minute)

    logger.info("sucessfully uploaded project zip")

    logger.info(s"running flow $flow")

    azkabanClient.runFlow(RunFlowRequest(project,flow))(azkabanSession).get

    logger.info(s"successfully ran flow $flow")

    logger.info(s"scheduling flow $flow")

    azkabanClient.scheduleFlow(ScheduleFlowRequest(project,flow,"10,30,pm,PDT","02/29/2016"))(azkabanSession).get

    logger.info(s"unscheduling flow $flow")

    azkabanClient.unscheduleFlow(UnScheduleFlowRequest(project,flow))(azkabanSession).get

    logger.info(s"successfully unscheduled flow $flow")

    actorSystem.shutdown()

  }

}
