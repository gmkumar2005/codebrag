package com.softwaremill.codebrag.dao.events

import com.softwaremill.codebrag.common.StatisticEvent
import org.joda.time.DateTime
import scala.collection.mutable.LinkedHashMap

trait EventDAO {
  def storeEvent(event: StatisticEvent)
  def countEvents(start: DateTime, end: DateTime, eventType: String): Int
  def countActiveUsers(start: DateTime, end: DateTime): Int
  def weeklyStatsByEvent(today: DateTime) : Map[String,List[Object]]
}