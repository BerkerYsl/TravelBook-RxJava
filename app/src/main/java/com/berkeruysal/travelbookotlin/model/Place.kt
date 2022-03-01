package com.berkeruysal.travelbookotlin.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
//burada bildiğimiz table ve column'ları oluşturuyoruz.
//yaptığımız işlem CREATE TABLE işlemi
class Place (

    @ColumnInfo(name = "name")
    var name:String,

    @ColumnInfo(name = "latitude")
    var latitude:Double,   //burada kolonları oluşturduk değişken adıv e kolon adının aynı olmasına gerek yok

    @ColumnInfo(name = "longitude")
    var longitude:Double)
{
    @PrimaryKey(autoGenerate = true) var id:Int=0
}