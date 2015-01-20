package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.json.JacksonJsonSupport
import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.finders.browsingcontext.UserBrowsingContext
import com.softwaremill.codebrag.finders.commits.all.AllCommitsFinder
import com.softwaremill.codebrag.dao.finders.StatsEventsFinder
import org.joda.time.{DateTimeZone, DateTime,DateTimeConstants}
import scala.collection.mutable.LinkedHashMap

class WeeklyStatsServlet(val authenticator: Authenticator, allCommitsFinder : AllCommitsFinder, statsEventsFinder: StatsEventsFinder) extends JsonServletWithAuthentication with JacksonJsonSupport {

  get("/") {
   haltIfNotAuthenticated()
   println(" statsEventsFinder.weeklyStats  " + statsEventsFinder.weeklyStats )
  	statsEventsFinder.weeklyStats      
  }

}

object WeeklyStatsServlet {
  val MappingPath = "weeklyStats"
}


