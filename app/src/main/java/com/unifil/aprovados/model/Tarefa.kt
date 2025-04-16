package com.unifil.aprovados.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tarefas")
data class Tarefa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descricao: String,
    val etapa: String // Valores: "Pendente", "Em andamento", "Finalizada", "Atrasada"
): Serializable
