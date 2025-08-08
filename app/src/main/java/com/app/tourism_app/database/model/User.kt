package com.app.tourism_app.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @ColumnInfo(name = "username")
    var userName : String,
    @ColumnInfo(name = "password")
    var password : String
){
    @PrimaryKey(autoGenerate = true)
    var id : Int? = null
}