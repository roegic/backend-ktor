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


const val GET_USER_INFO_REQUEST = "$USERS/getUserInfo"
const val GET_USER_INTERESTS_REQUEST = "$USERS/getUserInterests"
const val GET_USER_SOCIALS_REQUEST = "$USERS/get/socials"
const val GET_USER_LANGUAGES_REQUEST = "$USERS/get/languages"
const val GET_SUPPORTED_LANGUAGES_REQUEST = "$USERS/get/supportedLanguagesList"
const val GET_ALL_INTERESTS_TAGS_REQUEST = "$USERS/get/interests/tags"

@Resource(GET_USER_INFO_REQUEST)
class UserGetInfoRoute

@Resource(GET_USER_INTERESTS_REQUEST)
class GetUsernIterestsRoute

@Resource(GET_USER_SOCIALS_REQUEST)
class GetUsernSocialsRoute

@Resource(GET_USER_LANGUAGES_REQUEST)
class GetUserLanguagesRoute

@Resource(GET_SUPPORTED_LANGUAGES_REQUEST)
class GetSupportedLanguagesRoute

@Resource(GET_ALL_INTERESTS_TAGS_REQUEST)
class GetAllInterestsTagsRoute

fun Route.UserInfoGetRoutes(
    db: Repo
) {
    authenticate("jwt") {
        get<UserGetInfoRoute> {
            try {
                val userId = call.principal<User>()!!.userId
                val userInfo = db.getUserInfo(userId)

                call.respond(HttpStatusCode.OK, userInfo!!)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))
            }

        }

        get<GetUsernIterestsRoute> {
            try {
                val userId = call.principal<User>()!!.userId
                val userInterests = db.getUserInterests(userId)

                call.respond(HttpStatusCode.OK, userInterests)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))
            }
        }

        get<GetUsernSocialsRoute> {
            try {
                val userId = call.principal<User>()!!.userId
                val userSocials = db.getUserSocials(userId)

                call.respond(HttpStatusCode.OK, userSocials)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))
            }
        }

        get<GetSupportedLanguagesRoute> {
            call.respond(HttpStatusCode.OK, db.getSupportedLanguagesList())
        }

        get<GetAllInterestsTagsRoute> {
            call.respond(HttpStatusCode.OK, db.getAllInterestsTags())
        }

        get<GetUserLanguagesRoute> {
            try {
                val userId = call.principal<User>()!!.userId
                val userLanguages = db.getUserLanguages(userId)

                call.respond(HttpStatusCode.OK, userLanguages)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))
            }
        }
    }
}