package com.example.data.table

import org.jetbrains.exposed.sql.Table

object LanguagesTable: Table()  {
    val id = integer("id").autoIncrement()
    val language = varchar("language", 128)
    val nativeName = varchar("native_name", 128)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
