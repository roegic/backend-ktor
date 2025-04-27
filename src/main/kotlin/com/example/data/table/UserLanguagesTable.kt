package com.example.data.table

import org.jetbrains.exposed.sql.Table

object UserLanguagesTable: Table() {
    val userId = reference("userId", UserTable.id)
    val languageId = reference("languageId", LanguagesTable.id)
    override val primaryKey: PrimaryKey = PrimaryKey(userId, languageId)
}
