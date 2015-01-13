package com.softwaremill.codebrag.finders.commits.toreview

import org.bson.types.ObjectId
import com.softwaremill.codebrag.common.paging.PagingCriteria
import com.softwaremill.codebrag.dao.user.UserDAO
import com.typesafe.scalalogging.slf4j.Logging
import com.softwaremill.codebrag.cache.RepositoriesCache
import com.softwaremill.codebrag.dao.finders.views.CommitListView
import com.softwaremill.codebrag.finders.commits.UserLoader
import com.softwaremill.codebrag.finders.browsingcontext.{UserBrowsingContext, UserBrowsingContextFinder}
import org.joda.time.DateTime

class ToReviewCommitsFinder(
  protected val repoCache: RepositoriesCache,
  protected val userDao: UserDAO,
  userBrowsingContextFinder: UserBrowsingContextFinder,
  toReviewCommitsFilter: ToReviewBranchCommitsFilter,
  toReviewCommitsViewBuilder: ToReviewCommitsViewBuilder) extends Logging with UserLoader {

  def find(browsingContext: UserBrowsingContext, pagingCriteria: PagingCriteria[String]): CommitListView = {
    val user = loadUser(browsingContext.userId)
    val allBranchCommits = repoCache.getBranchCommits(browsingContext.repoName, browsingContext.branchName)
    val toReviewBranchCommits = toReviewCommitsFilter.filterCommitsToReview(allBranchCommits, user, browsingContext.repoName, browsingContext.authorName)
    toReviewCommitsViewBuilder.toPageView(browsingContext.repoName, toReviewBranchCommits, pagingCriteria)
  }

  def count(browsingContext: UserBrowsingContext): Long = {
    val user = loadUser(browsingContext.userId)
    val allBranchCommits = repoCache.getBranchCommits(browsingContext.repoName, browsingContext.branchName)
    toReviewCommitsFilter.filterCommitsToReview(allBranchCommits, user, browsingContext.repoName,browsingContext.authorName).length
  }

  def countSince(date: DateTime, browsingContext: UserBrowsingContext): Long = {
    val user = loadUser(browsingContext.userId)
    val branchCommits = repoCache
      .getBranchCommits(browsingContext.repoName, browsingContext.branchName)
      .filter(bc => bc.commitDate.isAfter(date) || bc.commitDate.isEqual(date))
    toReviewCommitsFilter.filterCommitsToReview(branchCommits, user, browsingContext.repoName,browsingContext.authorName).length
  }
  
  def countForUserRepoAndBranch(userId: ObjectId): Long = {
    val userDefaultContext = userBrowsingContextFinder.findUserDefaultContext(userId)
    count(userDefaultContext)
  }

}
