package com.example.routes

import com.example.authentication.JwtService
import com.example.data.model.*
import com.example.repository.Repo
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get

const val API_VERSION = "/v1"
const val USERS = "$API_VERSION/users"
const val REGISTER_REQUEST = "$USERS/register"
const val LOGIN_REQUEST = "$USERS/login"

@Resource(REGISTER_REQUEST)
class UserRegisterRoute

@Resource(LOGIN_REQUEST)
class UserLoginRoute


fun Route.UserRoutes(
    db: Repo,
    jwtService: JwtService,
    hashFunction: (String) -> String
) {

    post<UserRegisterRoute> {
        val registerRequest = try {
            call.receive<RegisterRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing Some Fields"))
            return@post
        }

        try {
            var user = User(
                -1,
                registerRequest.email,
                registerRequest.username,
                hashFunction(registerRequest.password)
            ) // -1 because we have autoincrement
            db.addUser(user)
            val id = db.findUserIdByEmail(registerRequest.email)!!
            user.userId = id
            db.addUserInfo(UserInfo(id, registerRequest.firstName, registerRequest.lastName))
            call.respond(HttpStatusCode.OK, SimpleResponse(true, jwtService.generateToken(user)))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some Problem Occurred!"))
        }
    }

    post<UserLoginRoute> {
        val loginRequest = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing Some Fields"))
            return@post
        }

        try {
            val user = db.findUserByEmail(loginRequest.email)

            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Wrong Email Id"))
            } else {
                if (user.hashPassword == hashFunction(loginRequest.password)) {
                    call.respond(HttpStatusCode.OK, SimpleResponse(true, jwtService.generateToken(user)))
                } else {
                    call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Password Incorrect!"))
                }
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some Problem Occurred!"))
        }
    }
}