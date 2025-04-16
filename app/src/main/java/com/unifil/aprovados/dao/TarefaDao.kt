package com.unifil.aprovados.dao

import androidx.room.*
import com.unifil.aprovados.model.Tarefa

@Dao
interface TarefaDao {

    @Insert
    suspend fun inserir(tarefa: Tarefa)

    @Update
    suspend fun atualizar(tarefa: Tarefa)

    @Delete
    suspend fun deletar(tarefa: Tarefa)

    @Query("SELECT * FROM tarefas ORDER BY id DESC")
    suspend fun listarTodas(): List<Tarefa>

    @Query("SELECT * FROM tarefas WHERE etapa = :etapa")
    suspend fun listarPorEtapa(etapa: String): List<Tarefa>
}
