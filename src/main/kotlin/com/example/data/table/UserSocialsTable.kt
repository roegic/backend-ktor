package com.example.data.table

import org.jetbrains.exposed.sql.Table

object UserSocialsTable : Table() {
    val userId = reference("userId", UserTable.id)
    val socialName = varchar("socialName", 128)
    val socialLink = varchar("socialLink", 512)

    override val primaryKey: PrimaryKey = PrimaryKey(userId, socialName)
}
