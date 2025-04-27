package com.example.repository

import com.example.data.table.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.repository.Repo

object DatabaseFactory {

    fun init(){
        Database.connect(hikari())


        transaction {
            SchemaUtils.create(UserTable)
            SchemaUtils.create(UserInfoTable)
            SchemaUtils.create(UserSocialsTable)
            SchemaUtils.create(UserInterestsTable)
            SchemaUtils.create(UserLanguagesTable)
            SchemaUtils.create(UserFollowedProfilesTable)
            SchemaUtils.create(LanguagesTable)
            SchemaUtils.create(InterestsTagsTable)
        }
        runBlocking {
            fillInterestTags()
        }

    }



    fun hikari(): HikariDataSource {
        val jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5433/BoundBuddyDB"
        val username = System.getenv("DATABASE_USER") ?: "postgres"
        val password = System.getenv("DATABASE_PASSWORD") ?: "123"
        val driverClassName = System.getenv("JDBC_DRIVER") ?: "org.postgresql.Driver"
        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            this.driverClassName = driverClassName
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }

}