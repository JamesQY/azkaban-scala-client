package com.madhukaraphatak.azkaban

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.madhukaraphatak.azkaban.AzkabanModels.{ScheduleFlowRequest, CreateSessionRequest}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by madhu on 27/1/16.
  */
object Test {

  def main(args: Array[String]) {

    implicit val actorSystem = ActorSystem.create()
    implicit val materializer = ActorMaterializer()
    val azkabanClient = new AzkabanClient("http://localhost:8081",actorSystem,materializer,
      scala.concurrent.ExecutionContext.Implicits.global)
    val azkabanSession = azkabanClient.createSession(CreateSessionRequest("azkaban","azkaban"))
    val azkabanContext = azkabanSession.get

    //println(azkabanClient.runFlow("test","test1")(azkabanContext).get)

    //azkabanClient.scheduleFlow(ScheduleFlowRequest("test","test1","10,30,pm,PDT","01/30/2016"))(azkabanContext)
    //azkabanClient.getProjectIdByProjectName("test")(azkabanContext)

    //println(azkabanClient.getScheduleByFlowName("test","test1")(azkabanContext))

    //azkabanClient.unscheduleFlow("test","test1")(azkabanContext)
    azkabanClient.uploadProjectZip("test","/home/madhu/Dev/azkaban/azkaban-jobs.zip")(azkabanContext)

    //actorSystem.shutdown()

  }

}
