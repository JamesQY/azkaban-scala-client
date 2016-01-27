package com.madhukaraphatak.azkaban

import spray.json.DefaultJsonProtocol

/**
  * All the request and response models
  */
object AzkabanModels {

  final case class CreateSessionRequest(userName:String,password:String)
  final case class RunFlowRequest(projectName:String, flowName:String)
  final case class ScheduleFlowRequest(projectName:String,
                                       flowName:String,
                                       startTime:String,
                                       startDate:String,
                                       repeats:Option[String]=None,
                                       repeatUnit:Option[String] = None)

  final case class SessionResponse(status:Option[String], `session.id`:
                                  Option[String] , error:Option[String])
  final case class ExecuteFlowResponse(message:Option[String], project:String,
                                      flow:String, execid:Option[Int],
                                      error:Option[String])
  final case class FlowInfo(flowId:String)
  final case class FetchFlowsResponse(project:Option[String],
                                      projectId:Option[Int],
                                      flows:Option[Array[FlowInfo]]
                                     )
  final case class Schedule(nextExecTime:String,
                       firstSchedTime:String,
                       submitUser:String,
                       scheduleId:String,
                       period:String)
  final case class FetchScheduleResponse(schedule:Schedule)

  object ServiceJsonProtocol extends DefaultJsonProtocol {
    implicit val sesionResponseFormat = jsonFormat3(SessionResponse)
    implicit val executeFlowResponse = jsonFormat5(ExecuteFlowResponse)
    implicit val flowInfo = jsonFormat1(FlowInfo)
    implicit val fetchFlowResponse = jsonFormat3(FetchFlowsResponse)
    implicit val schedule = jsonFormat5(Schedule)
    implicit val fetchScheduleResponse = jsonFormat1(FetchScheduleResponse)
  }
}
