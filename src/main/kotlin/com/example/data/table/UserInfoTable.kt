package com.example.data.table

import org.jetbrains.exposed.sql.Table

object UserInfoTable: Table() {
    val userId = reference("userId", UserTable.id)
    val firstName = varchar("firstName", 256)
    val lastName = varchar("lastName", 256)
    val age = integer("age").nullable()
    val bio = text("bio").nullable()
    val country = varchar("country", 256).nullable()
    val city = varchar("city", 256).nullable()
    val occupation = varchar("occupation", 256).nullable()
    val photo = binary("photo").nullable()
    val interests = text("interests").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(userId)
}

