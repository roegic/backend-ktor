package com.example.data.table

import com.example.data.table.UserInfoTable.nullable
import org.jetbrains.exposed.sql.Table

object UserFollowedProfilesTable : Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(UserTable.id)
    val followedUserId = integer("followed_user_id").references(UserTable.id)

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(userId, followedUserId)
    }
}