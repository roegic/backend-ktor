package com.example.routes

import com.example.data.model.*
import com.example.repository.Repo
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*


const val USERS_UPDATE = "$USERS/update"
const val UPDATE_FIRST_NAME_REQUEST = "$USERS_UPDATE/firstname"
const val UPDATE_LAST_NAME_REQUEST = "$USERS_UPDATE/lastname"
const val UPDATE_AGE_REQUEST = "$USERS_UPDATE/age"
const val UPDATE_BIO_REQUEST = "$USERS_UPDATE/bio"
const val UPDATE_OCCUPATION_REQUEST = "$USERS_UPDATE/occupation"
const val UPDATE_LANGUAGES_REQUEST = "$USERS_UPDATE/languages"
const val UPDATE_PHOTO_REQUEST = "$USERS_UPDATE/photo"
const val UPDATE_SOCIALS_REQUEST = "$USERS_UPDATE/socials"
const val UPDATE_USER_INFO_REQUEST = "$USERS_UPDATE/userInfo"
const val UPDATE_USER_INTERESTS_REQUEST = "$USERS_UPDATE/userInterests"

@Resource(UPDATE_FIRST_NAME_REQUEST)
class UpdateFirstNameRequestRoute

@Resource(UPDATE_LAST_NAME_REQUEST)
class UpdateLastNameRequestRoute

@Resource(UPDATE_AGE_REQUEST)
class UpdateAgeRequestRoute

@Resource(UPDATE_BIO_REQUEST)
class UpdateBioRequestRoute


@Resource(UPDATE_OCCUPATION_REQUEST)
class UpdateOccupationRequestRoute

@Resource(UPDATE_LANGUAGES_REQUEST)
class UpdateLanguagesRequestRoute

@Resource(UPDATE_PHOTO_REQUEST)
class UpdatePhotoRequestRoute


@Resource(UPDATE_USER_INFO_REQUEST)
class UpdateUserInfoRoute

@Resource(UPDATE_USER_INTERESTS_REQUEST)
class UpdateUserInterestsRoute

@Resource(UPDATE_SOCIALS_REQUEST)
class UpdateSocialsRequestRoute


fun Route.UserInfoUpdateRoutes(
    db: Repo,
    hashFunction: (String) -> String
) {
    authenticate("jwt") {

        post<UpdateUserInfoRoute> {
            val updateInfoRequest = try {
                call.receive<UserInfo>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing Some Fields"))
                return@post
            }

            try {
                val userId = call.principal<User>()!!.userId
                val userInfo = UserInfo(
                    userId = userId,
                    firstName = updateInfoRequest.firstName,
                    lastName = updateInfoRequest.lastName,
                    age = updateInfoRequest.age,
                    bio = updateInfoRequest.bio,
                    country = updateInfoRequest.country,
                    city = updateInfoRequest.city,
                    occupation = updateInfoRequest.occupation,
                    photo = updateInfoRequest.photo,
                    interests = updateInfoRequest.interests
                )
                db.updateUserInfo(userInfo)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "User info updated successfully"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some Problem Occurred!"))
            }
        }

        post<UpdateLanguagesRequestRoute> {
            val languages = try {
                call.receive<List<String>>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing fields"))
                return@post
            }

            try {
                val userId = call.principal<User>()!!.userId
                db.updateUserLanguages(userId, languages)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Languages changed successfully"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem occurred"))
            }
        }



        post<UpdateUserInterestsRoute> {
            val updateInterestsRequest = try {
                call.receive<List<Interest>>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing Some Fields"))
                return@post
            }

            try {
                val userId = call.principal<User>()!!.userId
                db.updateUserInterests(userId, updateInterestsRequest)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "User info updated successfully"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some Problem Occurred!"))
            }
        }

        post<UpdateSocialsRequestRoute> {
            val updateSocialsRequest = try {
                call.receive<List<Social>>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing Some Fields"))
                return@post
            }

            try {
                val userId = call.principal<User>()!!.userId
                db.updateUserSocials(userId, updateSocialsRequest)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "User info updated successfully"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some Problem Occurred!"))
            }
        }

    }
}