package com.example

import com.example.authentication.JwtService
import com.example.authentication.hash
import com.example.repository.Repo
import com.example.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting() {
    install(Resources)

    val db = Repo()
    val jwtService = JwtService()
    val hashFunction = {s:String -> hash(s) }
    routing {
        UserRoutes(db,jwtService,hashFunction)
        UserInfoUpdateRoutes(db,hashFunction)
        UserInfoGetRoutes(db)
        DiscoverRecommendationsRoutes(db)
        FollowRoutes(db)

    }
}
