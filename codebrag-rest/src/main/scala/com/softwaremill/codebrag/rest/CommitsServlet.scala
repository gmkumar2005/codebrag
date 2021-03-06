package com.softwaremill.codebrag.rest

import org.scalatra._
import com.softwaremill.codebrag.service.user.Authenticator
import json.JacksonJsonSupport

import com.softwaremill.codebrag.service.diff.DiffWithCommentsService
import com.softwaremill.codebrag.service.comments.UserReactionService
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinder
import com.softwaremill.codebrag.finders.commits.toreview.ToReviewCommitsFinder
import com.softwaremill.codebrag.finders.commits.all.AllCommitsFinder
import com.softwaremill.codebrag.usecases.reactions.{ReviewCommitUseCase, UnlikeUseCase, LikeUseCase, AddCommentUseCase}

class CommitsServlet(val authenticator: Authenticator,
                     val reviewableCommitsListFinder: ToReviewCommitsFinder,
                     val allCommitsFinder: AllCommitsFinder,
                     val reactionFinder: ReactionFinder,
                     val addCommentUseCase: AddCommentUseCase,
                     val reviewCommitUseCase: ReviewCommitUseCase,
                     val userReactionService: UserReactionService,
                     val userDao: UserDAO,
                     val diffService: DiffWithCommentsService,
                     val unlikeUseCase: UnlikeUseCase,
                     val likeUseCase: LikeUseCase)
  extends JsonServletWithAuthentication with JacksonJsonSupport with CommitsEndpoint with CommentsEndpoint with LikesEndpoint {
}

object CommitsServlet {
  val MAPPING_PATH = "commits"
}

