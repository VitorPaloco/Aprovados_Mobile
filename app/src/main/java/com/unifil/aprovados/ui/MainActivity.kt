package com.unifil.aprovados.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unifil.aprovados.database.AppDatabase
import com.unifil.aprovados.model.Tarefa
import com.unifil.aprovados.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.unifil.aprovados.R
import android.graphics.Color
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dao by lazy {
        AppDatabase.getDatabase(this).tarefaDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializando o binding corretamente
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val botaoPomodoro = findViewById<LinearLayout>(R.id.bottom_nav).getChildAt(2) as LinearLayout

        botaoPomodoro.setOnClickListener {
            val intent = Intent(this, Pomodoro::class.java)
            startActivity(intent)
        }


        binding.fabAdicionar.setOnClickListener {
            abrirDialogTarefa(null) // null significa nova tarefa
        }

        // Listar tarefas ao abrir a tela
        listarTarefas()
    }


    private fun listarTarefas() {
        lifecycleScope.launch {
            val tarefas = dao.listarTodas()

            val blocos = mapOf(
                "Pendente" to binding.blocoPendente,
                "Em andamento" to binding.blocoEmAndamento,
                "Concluída" to binding.blocoConcluida,
                "Atrasada" to binding.blocoAtrasada
            )

            // Limpa os blocos
            blocos.values.forEach { it.removeAllViews() }

            blocos.forEach { (status, bloco) ->
                val tarefasPorStatus = tarefas.filter { it.etapa == status }

                val tituloStatus = TextView(this@MainActivity).apply {
                    text = "$status"
                    textSize = 18f
                    setTextColor(Color.WHITE)
                    setPadding(16, 16, 16, 8)
                }
                bloco.addView(tituloStatus)

                if (tarefasPorStatus.isEmpty()) {
                    val semTarefa = TextView(this@MainActivity).apply {
                        text = "Sem tarefas \"$status\""
                        setTextColor(Color.GRAY)
                        setPadding(16, 8, 16, 16)
                    }
                    bloco.addView(semTarefa)
                } else {
                    tarefasPorStatus.forEach { tarefa ->
                        val tarefaView = layoutInflater.inflate(android.R.layout.simple_list_item_2, bloco, false)
                        val titulo = tarefaView.findViewById<TextView>(android.R.id.text1)
                        val descricao = tarefaView.findViewById<TextView>(android.R.id.text2)

                        titulo.text = tarefa.titulo
                        descricao.text = tarefa.descricao

                        titulo.setTextColor(Color.WHITE)
                        descricao.setTextColor(Color.LTGRAY)


                        tarefaView.setOnClickListener {
                            abrirDialogTarefa(tarefa)
                        }

                        tarefaView.setOnLongClickListener {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Remover Tarefa")
                                .setMessage("Deseja remover \"${tarefa.titulo}\"?")
                                .setPositiveButton("Remover") { _, _ ->
                                    lifecycleScope.launch {
                                        dao.deletar(tarefa)
                                        listarTarefas()
                                    }
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                            true
                        }

                        bloco.addView(tarefaView)
                    }
                }
            }
        }
    }



    private fun abrirDialogTarefa(tarefaExistente: Tarefa?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tarefa, null)
        val edtTitulo = dialogView.findViewById<EditText>(R.id.edtTituloDialog)
        val edtDescricao = dialogView.findViewById<EditText>(R.id.edtDescricaoDialog)
        val spinnerEtapa = dialogView.findViewById<Spinner>(R.id.spinnerEtapaDialog)

        val etapas = listOf("Pendente", "Em andamento", "Concluída", "Atrasada")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, etapas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEtapa.adapter = adapter

        if (tarefaExistente != null) {
            edtTitulo.setText(tarefaExistente.titulo)
            edtDescricao.setText(tarefaExistente.descricao)
            val pos = etapas.indexOf(tarefaExistente.etapa)
            if (pos >= 0) spinnerEtapa.setSelection(pos)
        }

        AlertDialog.Builder(this)
            .setTitle(if (tarefaExistente == null) "Nova Tarefa" else "Editar Tarefa")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val novaTarefa = Tarefa(
                    id = tarefaExistente?.id ?: 0,
                    titulo = edtTitulo.text.toString(),
                    descricao = edtDescricao.text.toString(),
                    etapa = spinnerEtapa.selectedItem.toString()
                )

                lifecycleScope.launch {
                    if (tarefaExistente == null) {
                        dao.inserir(novaTarefa)
                    } else {
                        dao.atualizar(novaTarefa)
                    }
                    listarTarefas()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

}
