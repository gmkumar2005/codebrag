  package com.softwaremill.codebrag.dao.events

  import com.softwaremill.codebrag.dao.sql.SQLDatabase
  import org.joda.time.{DateTime,DateTimeConstants,DateTimeZone}
  import org.joda.time.format.DateTimeFormatter
  import org.joda.time.format.DateTimeFormat
  import com.softwaremill.codebrag.common.StatisticEvent
  import org.bson.types.ObjectId
  import com.softwaremill.codebrag.domain.reactions.{LikeEvent, CommentAddedEvent, CommitReviewedEvent}
  import scala.collection.mutable.LinkedHashMap
 // import scala.collection.mutable.LinkedList
   import scala.collection.mutable.LinkedList
  import Joda._
  import scala.collection.immutable.SortedSet
  //dateTimes.sorted

  object Joda {
    implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
  }

  class SQLEventDAO(database: SQLDatabase) extends EventDAO {
    import database.driver.simple._
    import database._

    def storeEvent(event: StatisticEvent) {
      db.withTransaction { implicit session =>
        events += SQLEvent(new ObjectId(), event.timestamp, event.eventType, event.userId)
      }
    }

    def countEvents(start: DateTime, end: DateTime, eventType: String) = db.withTransaction { implicit session =>
      val q = events.filter(e => e.date >= start && e.date <= end && e.eventType === eventType).length
      Query(q).first()
    }

    def countActiveUsers(start: DateTime, end: DateTime) = db.withTransaction { implicit session =>
      events
      .filter(e => e.date >= start && e.date <= end && e.eventType =!= NewUserRegistered.EventType)
      .map(_.originatingUserId)
      .list().toSet.size
    }

    def weeklyStatsByEvent(today: DateTime) = db.withTransaction{ implicit session =>
      val weekBounds = weekRange(today)  
      var allEventsFor10Weeks =  events
      .filter(e => e.date >= weekBounds._2.minusWeeks(10) && e.date <= weekBounds._2)        
      .map(p => p)
      .list()

      //TODO 10 weeks data is fetched into memory should find a way to move the grouping logic to database
      val tempData = allEventsFor10Weeks
      .groupBy(weekNames)
      .map{ case( (k1,k2) , v) => (k1,k2,v.size)}.toList
      .sortBy(x => (x._1,x._2))
      .groupBy{ case (k1,k2,v) => k1}    
     //sample result in tempData Set(CommentAdded, LikeAdded, CommitReviewed)

      val mappedtempData = tempData.map{case (k,v) => (k,v.map(_._2),v.map(_._3))}
      // sample data in mappedtempData
      // List((CommentAdded,List(2014-12-22T00:00:00.000+05:30, 2015-01-12T00:00:00.000+05:30, 2015-01-19T00:00:00.000+05:30),List(1, 4, 5)), (LikeAdded,List(2015-01-12T00:00:00.000+05:30, 2015-01-19T00:00:00.000+05:30),List(4, 8)), (CommitReviewed,List(2015-01-12T00:00:00.000+05:30, 2015-01-19T00:00:00.000+05:30),List(3, 3))) 
      var distinctweeks = mappedtempData.map(_._2).flatten.to[SortedSet].toList.sorted
  	   // sample data in  distinctweeks.map(weekNames)
      // TreeSet(12/01, 19/01, 22/12) 

    
      val namesandcountwithmissingvalues = mappedtempData.map(addmissingweek(_,distinctweeks)).toList
  	  // sample data in namesandcountwithmissingvalues
      // List(List(1, 4, 5), List(4, 8, 0), List(3, 3, 0)) 
      
     
     val finalresult = Map("series"-> tempData.keys.toList,"labels"->distinctweeks.map(convertoweekname).toList,"data"-> namesandcountwithmissingvalues)
     println (" finalresult  " + finalresult)
     finalresult

   }

   private def convertoweekname( v : DateTime) : String =  {
     val dtf = DateTimeFormat.forPattern("dd/MM"); 
     dtf.print(v)
   } 
   private def addmissingweek (e : (String,List[DateTime],List[Int]), distinctweeks: List[DateTime]  ) : List[(Int)] = { 
    val weeknames = e._2
    val counts = e._3
    var updatedweeknames = e._2.zip(counts)
    for (name <- distinctweeks if !weeknames.contains(name) ) {
      // cout is zero for the missing week, that means no activity
      updatedweeknames = updatedweeknames:+(name,0) }     
     updatedweeknames = updatedweeknames.sorted
      val sordtenames = updatedweeknames.map{case (k,v) => (k,v)}
      println("  sordtenames " + sordtenames)
      updatedweeknames.map{case (k,v) => (v)}
    }
    
    def isEqualOrAfter( start : DateTime , end : DateTime , target: DateTime) : Boolean = {
      !target.isBefore(start.withTimeAtStartOfDay().toDateTime(DateTimeZone.UTC)) && !target.isAfter(end.withTime(23, 59, 59, 999).toDateTime(DateTimeZone.UTC))
    }  
    def weekNames(event : SQLEvent) : (String,DateTime) = {
      val today = new DateTime()
      val dateRange = weekRange(today)
     
      if(isEqualOrAfter(dateRange._1,dateRange._2,event.date))
      {      
        (event.eventType,dateRange._1) 
      }
      else if (isEqualOrAfter(dateRange._1.minusWeeks(1),dateRange._2.minusWeeks(1),event.date))
      {      
        (event.eventType,dateRange._1.minusWeeks(1)) 
      }
      else if  (isEqualOrAfter(dateRange._1.minusWeeks(2),dateRange._2.minusWeeks(2),event.date))
      {      
        (event.eventType,dateRange._1.minusWeeks(2) ) 
      }
      else if  (isEqualOrAfter(dateRange._1.minusWeeks(3),dateRange._2.minusWeeks(3),event.date))
      {      
        (event.eventType,dateRange._1.minusWeeks(3))  
      }
      else if  (isEqualOrAfter(dateRange._1.minusWeeks(4),dateRange._2.minusWeeks(4),event.date))
      {      
        (event.eventType,dateRange._1.minusWeeks(4))   
      }
      else if  (isEqualOrAfter(dateRange._1.minusWeeks(5),dateRange._2.minusWeeks(5),event.date))
      {      
        (event.eventType,dateRange._1.minusWeeks(5))
      }
      else if  (isEqualOrAfter(dateRange._1.minusWeeks(6),dateRange._2.minusWeeks(6),event.date))
      {      
        (event.eventType,dateRange._1.minusWeeks(6)) 
      }
      else if  (isEqualOrAfter(dateRange._1.minusWeeks(7),dateRange._2.minusWeeks(7),event.date))
      {      
        (event.eventType, dateRange._1.minusWeeks(7)) 
      }
      else if  (isEqualOrAfter(dateRange._1.minusWeeks(8),dateRange._2.minusWeeks(8),event.date))
      {      
        (event.eventType, dateRange._1.minusWeeks(8))  
      }
      else if  (isEqualOrAfter(dateRange._1.minusWeeks(9),dateRange._2.minusWeeks(9),event.date))
      {      
        (event.eventType, dateRange._1.minusWeeks(9))  
      }
      else if  (isEqualOrAfter(dateRange._1.minusWeeks(10),dateRange._2.minusWeeks(10),event.date))
      {      
        (event.eventType, dateRange._1.minusWeeks(10))  
      }
      else{
        // unknown time should not be here
        (event.eventType , dateRange._1.plusDays(100))
      }
      
    }

    private def weekRange(today: DateTime)= {
  	   val startOfWeek = today.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay() 
  	   val endOfWeek = today.withDayOfWeek(DateTimeConstants.SUNDAY).withTime(23, 59, 59, 999)
  	(startOfWeek,endOfWeek)
  }

  case class SQLEvent(id: ObjectId, date: DateTime, eventType: String, originatingUserId: ObjectId)

  private class Events(tag: Tag) extends Table[SQLEvent](tag, "events") {
    def id                = column[ObjectId]("id", O.PrimaryKey)
    def date              = column[DateTime]("event_date")
    def eventType         = column[String]("event_type")
    def originatingUserId = column[ObjectId]("originating_user_id")

    def * = (id, date, eventType, originatingUserId) <> (SQLEvent.tupled, SQLEvent.unapply)
  }
  private val events = TableQuery[Events]

}
