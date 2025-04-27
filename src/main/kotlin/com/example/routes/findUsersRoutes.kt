package com.example.routes

import com.example.data.model.SimpleResponse
import com.example.data.model.User
import com.example.repository.Repo
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val DISCOVER = "$API_VERSION/discover"
const val FIND_USERS_DEFAULT_REQUEST = "$DISCOVER/getDefaultRecommendation"
const val FIND_USERS_FILTERED_REQUEST = "$DISCOVER/getFilteredRecommendation"

const val GET_USER_INTERESTS_BY_ID_REQUEST = "$API_VERSION/users/get/user/{id}/interests"
const val GET_USER_SOCIALS_BY_ID_REQUEST = "$API_VERSION/users/get/user/{id}/socials"
const val GET_USER_INFO_BY_ID_REQUEST = "$API_VERSION/users/get/user/{id}/info"
const val GET_USER_LANGUAGES_BY_ID_REQUEST = "$API_VERSION/users/get/user/{id}/languages"

@Resource(GET_USER_INTERESTS_BY_ID_REQUEST)
class GetUserInterestsById(val id: Int)

@Resource(GET_USER_SOCIALS_BY_ID_REQUEST)
class GetUserSocialsById(val id: Int)

@Resource(GET_USER_INFO_BY_ID_REQUEST)
class GetUserInfoById(val id: Int)

@Resource(GET_USER_LANGUAGES_BY_ID_REQUEST)
class GetUserLanguagesById(val id: Int)

@Resource(FIND_USERS_DEFAULT_REQUEST)
class GetDefaultRecommendation

@Resource(FIND_USERS_FILTERED_REQUEST)
class GetFilteredRecommendation

fun Route.DiscoverRecommendationsRoutes(
    db: Repo
) {
    authenticate("jwt") {
        get<GetDefaultRecommendation> {
            try {
                val userId = call.principal<User>()!!.userId
                val users = db.getUsersRankedByInterests(userId)

                call.respond(HttpStatusCode.OK, users)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))
            }
        }

        get<GetFilteredRecommendation> {
            try {
                val userId = call.principal<User>()!!.userId
                val selectedInterests = call.request.queryParameters.getAll("interests")
                val minAge = call.request.queryParameters["minAge"]
                val maxAge = call.request.queryParameters["maxAge"]
                val city = call.request.queryParameters["city"]
                val country = call.request.queryParameters["country"]
                val languages = call.request.queryParameters.getAll("languages")

                val users = db.getFilteredUsers(userId, selectedInterests, minAge, maxAge, city, country, languages)


                call.respond(HttpStatusCode.OK, users)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))
            }
        }

        get<GetUserSocialsById> { request ->
            try {
                val userId = request.id
                val userSocials = db.getUserSocials(userId)
                call.respond(HttpStatusCode.OK, userSocials)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Error fetching interests"))
            }

        }

        get<GetUserInfoById> { request ->
            try {
                val userId = request.id
                val userInfo = db.getUserInfo(userId)
                call.respond(HttpStatusCode.OK, userInfo!!)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Error fetching interests"))
            }

        }

        get<GetUserInterestsById> { request ->
            try {
                val userId = request.id
                val userInterests = db.getUserInterests(userId)
                call.respond(HttpStatusCode.OK, userInterests)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Error fetching interests"))
            }
        }

        get<GetUserLanguagesById> { request ->
            try {
                val userId = request.id
                val userLanguages = db.getUserLanguages(userId)
                call.respond(HttpStatusCode.OK, userLanguages)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Error fetching interests"))
            }
        }

    }
}