package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.scalaval.Validation

import org.scalatest.BeforeAndAfterEach
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.service.user.Authenticator
import org.scalatra.auth.Scentry
import org.mockito.Mockito._
import com.softwaremill.codebrag.domain.{User, UserSettings}
import com.softwaremill.codebrag.domain.builder.UserAssembler
import com.softwaremill.codebrag.usecases.user.{IncomingSettings, ChangeUserSettingsUseCase}
import com.softwaremill.codebrag.usecases.notifications.FindUserNotifications
import com.softwaremill.codebrag.finders.commits.all.AllCommitsFinder
import com.softwaremill.codebrag.dao.finders.StatsEventsFinder
import org.joda.time.{DateTimeZone, DateTime,DateTimeConstants}
import scala.collection.mutable.LinkedHashMap
import org.mockito.Matchers
import com.softwaremill.codebrag.dao.events.EventDAO

class WeeklyStatsServletSpec extends AuthenticatableServletSpec with BeforeAndAfterEach {

  val authenticator: Authenticator = mock[Authenticator]
  var currentUser: User = _
  var fakeallCommitsFinder : AllCommitsFinder = _
  var fakestatsEventsFinder : StatsEventsFinder =_
  val weeklystatsdata : Map[String,List[Object]] =  Map("series" -> List("CommentAdded", "LikeAdded", "CommitReviewed"), "labels" -> List("22/12", "12/01", "19/01"), "data" -> List(List(1, 4, 5), List(0, 4, 8), List(0, 3, 3)))
    override def beforeEach {
    super.beforeEach
    fakeallCommitsFinder = mock[AllCommitsFinder]
    fakestatsEventsFinder = mock[StatsEventsFinder]
    addServlet(new TestableWeelkyStatsServlet(fakeAuthenticator,fakeallCommitsFinder, fakestatsEventsFinder , fakeScentry), "/*")    
    currentUser = UserAssembler.randomUser.get
    userIsAuthenticatedAs(currentUser)
  }
  
  "GET /" should "return series , labels and data in json format " in {
    // given
    when(fakestatsEventsFinder.weeklyStats).thenReturn(weeklystatsdata)
    
    get("/") {
      // then
      body should be(asJson(weeklystatsdata))
      status should be(200)
    }
  }
  
  class TestableWeelkyStatsServlet(fakeAuthenticator: Authenticator, fakeallCommitsFinder:AllCommitsFinder, fakestatsEventsFinder:StatsEventsFinder , fakeScentry: Scentry[User])
  extends WeeklyStatsServlet(fakeAuthenticator,fakeallCommitsFinder,fakestatsEventsFinder) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
