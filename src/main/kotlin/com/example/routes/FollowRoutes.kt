package com.example.routes

import com.example.data.model.*
import com.example.repository.Repo
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.delete

const val FOLLOW_USER_REQUEST = "$USERS/follow/{followId}"
const val UNFOLLOW_USER_REQUEST = "$USERS/unfollow/{unfollowId}"
const val GET_FOLLOWERS_REQUEST = "$USERS/get/user/followers"

@Resource(FOLLOW_USER_REQUEST)
class UserFollowRoute(val followId: Int)

@Resource(UNFOLLOW_USER_REQUEST)
class UserUnFollowRoute(val unfollowId: Int)

@Resource(GET_FOLLOWERS_REQUEST)
class UserFollowerListRoute()

fun Route.FollowRoutes(
    db: Repo
) {
    authenticate("jwt") {
        post<UserFollowRoute> { route ->
            try {
                val userId = call.principal<User>()!!.userId
                val followId = route.followId

                db.followUser(userId, followId)

                call.respond(HttpStatusCode.OK, SimpleResponse(success = true, message = "Successfully followed user"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))
            }
        }

        delete<UserUnFollowRoute> { route ->
            try {
                val userId = call.principal<User>()!!.userId
                val unfollowId = route.unfollowId
                db.unfollowUser(userId, unfollowId)

                call.respond(HttpStatusCode.OK, SimpleResponse(success = true, message = "Successfully unfollowed user"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))

            }
        }

        get<UserFollowerListRoute> { route ->
            try {
                val userId = call.principal<User>()!!.userId
                val followers = db.getFollowerList(userId)

                call.respond(HttpStatusCode.OK, followers)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))

            }
        }

    }
}