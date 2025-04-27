package com.example.data.table

import org.jetbrains.exposed.sql.Table

object InterestsTagsTable: Table() {
    val id = integer("id").autoIncrement()
    val tag = varchar("tag", 256)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}