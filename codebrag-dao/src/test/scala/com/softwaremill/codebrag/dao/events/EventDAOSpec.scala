package com.softwaremill.codebrag.dao.events

import com.softwaremill.codebrag.test.{FlatSpecWithSQL, ClearSQLDataAfterTest}
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.reactions.{LikeEvent, CommentAddedEvent, CommitReviewedEvent}
import com.softwaremill.codebrag.common.{RealTimeClock, StatisticEvent}
import org.bson.types.ObjectId
import org.scalatest.FlatSpec

trait EventDAOSpec extends FlatSpec with ShouldMatchers {
  def eventDAO: EventDAO

  val today = RealTimeClock.nowUtc
  val yesterday = today.minusDays(1)

  val CommitReviewedEventType = CommitReviewedEvent.EventType
  val CommentAddedEventType = CommentAddedEvent.EventType
  val LikeAddedEventType = LikeEvent.EventType
  val NewUserRegisteredEventType = NewUserRegistered.EventType

  it should "count events in the given date bounds" in {
    // given
    storeEventWith(today, CommitReviewedEventType)
    storeEventWith(today, CommitReviewedEventType)
    storeEventWith(yesterday, CommitReviewedEventType)

    // when
    val count1 = eventDAO.countEvents(today, today, CommitReviewedEventType)
    val count2 = eventDAO.countEvents(today.minusMinutes(1), today.plusMinutes(1), CommitReviewedEventType)
    val count3 = eventDAO.countEvents(yesterday, today.plusMinutes(1), CommitReviewedEventType)

    // then
    count1 should be (2)
    count2 should be (2)
    count3 should be (3)
  }

  it should "count events of the given type" in {
    // given
    storeEventWith(today, LikeAddedEventType)
    storeEventWith(today, LikeAddedEventType)
    storeEventWith(yesterday, LikeAddedEventType)

    storeEventWith(today, CommentAddedEventType)
    storeEventWith(yesterday, CommentAddedEventType)

    // when
    val likesCount = eventDAO.countEvents(yesterday, today, LikeAddedEventType)
    val commentsCount = eventDAO.countEvents(yesterday, today, CommentAddedEventType)
    val reviewedCount = eventDAO.countEvents(yesterday, today, CommitReviewedEventType)

    // then
    likesCount should be (3)
    commentsCount should be (2)
    reviewedCount should be (0)
  }

  it should "get number of active users (without registered)" in {
    // given
    storeEventWith(today, NewUserRegisteredEventType)
    storeEventWith(today, LikeAddedEventType)
    storeEventWith(today, CommentAddedEventType)

    // when
    val count = eventDAO.countActiveUsers(today, today)

    // then
    count should be (2)
  }

  it should "count user as active only once" in {
    // given
    val user = new ObjectId
    storeEventWith(today, LikeAddedEventType, user)
    storeEventWith(today, CommentAddedEventType, user)

    // when
    val count = eventDAO.countActiveUsers(today, today)

    // then
    count should be (1)

  }
  it should "count events group by week" in {
    // given
    val user = new ObjectId
  storeEventWith(today, LikeAddedEventType, user) // 18/01
	storeEventWith(today, LikeAddedEventType, user) // 18/01
  storeEventWith(today, CommentAddedEventType, user)
  storeEventWith(today, CommitReviewedEventType)
	storeEventWith(yesterday, CommitReviewedEventType)	
	storeEventWith(yesterday, CommentAddedEventType)
	storeEventWith(today, LikeAddedEventType) //18/01
	storeEventWith(yesterday, LikeAddedEventType)
	storeEventWith(today, CommentAddedEventType)
  storeEventWith(today.minusDays(1), LikeAddedEventType, user) // 11/01
  storeEventWith(today.minusDays(1), LikeAddedEventType, user) // 11/01
  storeEventWith(today.minusDays(1), CommentAddedEventType, user)
  storeEventWith(today.minusDays(1), CommitReviewedEventType)
  storeEventWith(yesterday.minusDays(1), CommitReviewedEventType)  
  storeEventWith(yesterday.minusDays(1), CommentAddedEventType)
  storeEventWith(today, LikeAddedEventType) // 11/01
  storeEventWith(yesterday, LikeAddedEventType) // 11/01
  storeEventWith(today, CommentAddedEventType)	
	storeEventWith(today.minusWeeks(1), LikeAddedEventType, user)
	storeEventWith(today.minusWeeks(1), LikeAddedEventType, user)
  storeEventWith(today.minusWeeks(1), CommentAddedEventType, user)
  storeEventWith(today.minusWeeks(1), CommitReviewedEventType)
	storeEventWith(yesterday.minusWeeks(1), CommitReviewedEventType)
	storeEventWith(today.minusWeeks(1), LikeAddedEventType)
	storeEventWith(yesterday.minusWeeks(1), LikeAddedEventType)
	storeEventWith(today.minusWeeks(1), CommentAddedEventType)
	storeEventWith(yesterday.minusWeeks(1), CommentAddedEventType)
	storeEventWith(today.minusWeeks(4), CommentAddedEventType)
	
	val expectedResult : Map[String,List[Object]] =  Map("series" -> List("CommentAdded", "LikeAdded", "CommitReviewed"), "labels" -> List("22/12", "12/01", "19/01"), "data" -> List(List(1, 4, 5), List(0, 4, 8), List(0, 3, 3)))
    //when
	val thisWeekStats = eventDAO.weeklyStatsByEvent(today)
    // then
  thisWeekStats should be (expectedResult)
	
  }

  private def storeEventWith(_date: DateTime, _eventType: String, _userId: ObjectId = new ObjectId) =
    eventDAO.storeEvent(new StatisticEvent {
      def timestamp = _date
      def eventType = _eventType
      def userId = _userId
      def toEventStream = ""
    })
}

class SQLEventDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with EventDAOSpec {
  val eventDAO = new SQLEventDAO(sqlDatabase)
}
