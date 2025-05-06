@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NewCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Você precisa estar logado para acessar essa função.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java)) // Substitua pelo nome real da tela de login
            finish()
            return
        }

        setContent {
            var showError by remember { mutableStateOf(false) }

            SuperIDTheme {
                NewCategoryScreen(
                    onBack = { finish() },
                    onAdd = { nomeCategoria ->
                        salvarCategoria(
                            nomeCategoria = nomeCategoria,
                            onSuccess = {
                                Toast.makeText(this, "Categoria adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                                finish()
                            },
                            onError = {
                                showError = true
                            }
                        )
                    },
                    showError = showError
                )
            }
        }
    }
}

fun salvarCategoria(
    nomeCategoria: String,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
        onError()
        return
    }

    val db = FirebaseFirestore.getInstance()
    val categoriaRef = db.collection("users").document(userId).collection("categories")

    // Verifica se já existe
    categoriaRef.whereEqualTo("nome", nomeCategoria).get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                // Já existe
                onError()
            } else {
                val novaCategoria = hashMapOf("nome" to nomeCategoria)
                categoriaRef.add(novaCategoria)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError() }
            }
        }
        .addOnFailureListener {
            onError()
        }
}

@Composable
fun NewCategoryScreen(
    onBack: () -> Unit = {},
    onAdd: (String) -> Unit = {},
    showError: Boolean = false
) {
    var nomeCategoria by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SuperID")
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Nova categoria", fontSize = 30.sp)
                Spacer(modifier = Modifier.height(32.dp))

                TextField(
                    value = nomeCategoria,
                    onValueChange = { nomeCategoria = it },
                    label = { Text("Nome da categoria:", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (showError) {
                    Text(
                        text = "ERRO: categoria já existe.",
                        color = Color.Red,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (nomeCategoria.isNotBlank()) {
                            onAdd(nomeCategoria.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(bottom = 50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                ) {
                    Text("Adicionar", fontSize = 24.sp)
                }
            }
        }
    )
}