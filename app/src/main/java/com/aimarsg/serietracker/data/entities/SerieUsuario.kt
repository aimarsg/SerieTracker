package com.aimarsg.serietracker.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.time.DateTimeException
import java.util.Date

@Immutable
@Entity(tableName = "SerieUsuario")
data class SerieUsuario(
    //@PrimaryKey
    //val id: Int = 0,

    //datos de la serie como tal
    @PrimaryKey
    val titulo: String,
    val numTemps: Int,
    val epTemp: String,

    //datos del usuario
    val siguiendo: Boolean,
    val recordatorio: LocalDate,
    val epActual: Int,
    val tempActual: Int,
    val valoracion: Float
)