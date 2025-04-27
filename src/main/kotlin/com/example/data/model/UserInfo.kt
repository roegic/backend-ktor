package com.example.data.model

import com.example.data.table.UserInfoTable
import com.example.data.table.UserInfoTable.nullable
import com.example.data.table.UserTable

data class UserInfo(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val age: Int? = null,
    val bio: String? = null,
    val country: String? = null,
    val city: String? = null,
    val occupation: String? = null,
    val languages: String? = null,
    val photo: ByteArray? = null,
    val interests: String? = null
)
