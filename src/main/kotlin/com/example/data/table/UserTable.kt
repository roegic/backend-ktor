package com.example.data.table

import com.example.data.table.UserInfoTable.autoIncrement
import com.example.data.table.UserInfoTable.uniqueIndex
import org.jetbrains.exposed.sql.Table

object UserTable:Table() {
    val id = integer("id").autoIncrement()
    val email = varchar("email",512).uniqueIndex()
    val username = varchar("username",512).uniqueIndex()
    val hashPassword = varchar("hashPassword",512)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}