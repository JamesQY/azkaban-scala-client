package com.madhukaraphatak.azkaban

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.madhukaraphatak.azkaban.AzkabanModels._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}


/**
  * Created by madhu on 27/1/16.
  */
object Test {

  def main(args: Array[String]) {

    implicit val actorSystem = ActorSystem.create()
    implicit val materializer = ActorMaterializer()
    val azkabanClient = new AzkabanClient("http://localhost:8081")
    val azkabanSession = azkabanClient.createSession(CreateSessionRequest("azkaban","azkaban"))
    implicit val azkabanContext = azkabanSession.get

    println(azkabanClient.runFlow(RunFlowRequest("test","testk"))(azkabanContext))

    //println(azkabanClient.scheduleFlow(ScheduleFlowRequest("test","testk","10,30,pm,PDT","02/29/2016"))(azkabanContext))
    //azkabanClient.getProjectIdByProjectName("test")(azkabanContext)

    //println(azkabanClient.getScheduleByFlowName("test","test1")(azkabanContext))

    //azkabanClient.unscheduleFlow(UnScheduleFlowRequest("test","test2"))(azkabanContext)
    /*al fileUploadResponse =
      azkabanClient.uploadProjectZip(UploadProjectZipRequest(
        "test1","/home/madhu/Dev/azkaban/azkaban-jobs.zip"))(azkabanContext)
    import scala.concurrent.duration._
    println(Await.result(fileUploadResponse, 3 seconds))*/

    //actorSystem.shutdown()

  }

}
