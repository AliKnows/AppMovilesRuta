package com.ggs.qosqoruta.features.auth.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_table")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val role: String = "PASAJERO",
    val company: String? = null,
    val routeName: String? = null
)
