package com.example.data.table

import org.jetbrains.exposed.sql.Table

object UserInterestsTable: Table() {
    val id = integer("id").autoIncrement()
    val userId = reference("userId", UserTable.id)
    val interest = varchar("interest", 512)
    val interestTagId = reference("interestTagId", InterestsTagsTable.id)

    override val primaryKey: PrimaryKey = PrimaryKey(LanguagesTable.id)
}