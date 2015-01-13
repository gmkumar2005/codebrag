package com.softwaremill.codebrag.finders.browsingcontext

import com.softwaremill.codebrag.dao.repo.UserRepoDetailsDAO
import com.softwaremill.codebrag.cache.RepositoriesCache
import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.domain.UserRepoDetails

case class UserBrowsingContext(userId: ObjectId, repoName: String, branchName: String , authorName : String ="")

object UserBrowsingContext {
  def apply(d: UserRepoDetails) = new UserBrowsingContext(d.userId, d.repoName, d.branchName)
}

class UserBrowsingContextFinder(val userRepoDetailsDao: UserRepoDetailsDAO, val repositoriesCache: RepositoriesCache) extends Logging {

  def find(userId: ObjectId, repoName: String): Option[UserBrowsingContext] = {
    if(repositoriesCache.hasRepo(repoName)) {
      val found = userRepoDetailsDao.find(userId, repoName) match {
        case Some(context) => UserBrowsingContext(context)
        case None => UserBrowsingContext(userId, repoName, repositoriesCache.getCheckedOutBranchShortName(repoName))
      }
      Some(found)
    } else {
      None
    }
  }

  def findAll(userId: ObjectId) = userRepoDetailsDao.findAll(userId).filter(rd => repositoriesCache.hasRepo(rd.repoName)).map(UserBrowsingContext.apply)

  def findUserDefaultContext(userId: ObjectId) = {
    logger.debug(s"Searching for default browsing context for user $userId")
    
    val masterBranch = "master"
    val developBranch = "develop"
    
    val context = userRepoDetailsDao.findDefault(userId)
      .filter(ctx => repositoriesCache.hasRepo(ctx.repoName))
      .map(ctx => {
        val repo = repositoriesCache.getRepo(ctx.repoName)
        val branches = repo.getShortBranchNames
        val branchName = if (branches.contains(ctx.branchName)) ctx.branchName 
          else if (branches.contains(masterBranch)) masterBranch
          else if (branches.contains(developBranch)) developBranch
          else branches.head
                
        UserBrowsingContext(
            userId,
            ctx.repoName,
            branchName
        )
      })
      .getOrElse(findSystemDefaultContext(userId))
    
    logger.debug(s"User context is $context")
    context
  }

  private def findSystemDefaultContext(userId: ObjectId): UserBrowsingContext = {
    logger.debug(s"Default context not found, getting system default")
    val repoName = repositoriesCache.repoNames.head
    val branchName = repositoriesCache.getRepo(repoName).getCheckedOutBranchShortName
    logger.debug(s"Building system default context from $repoName and $branchName")
    UserBrowsingContext(userId, repoName, branchName)
  }
}
