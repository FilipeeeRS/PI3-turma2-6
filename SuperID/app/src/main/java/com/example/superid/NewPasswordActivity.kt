@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme

class NewPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperIDTheme {
                NewPasswordScreen(
                    onBack = { finish() },
                    onAdd = {
                        // adicionar a lógica para salvar no Firestore
                    },
                    showError = false
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewPasswordScreenPreview() {
    SuperIDTheme {
        NewPasswordScreen(showError = true)
    }
}

@Composable
fun NewPasswordScreen(
    onBack: () -> Unit = {},
    onAdd: () -> Unit = {},
    showError: Boolean = false
) {
    var categoria by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var nomeConta by remember { mutableStateOf("") }

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
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Busca")
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
                Text("Adicionar nova senha", fontSize = 30.sp)

                Spacer(modifier = Modifier.height(32.dp))

                TextField(
                    value = nomeConta,
                    onValueChange = { nomeConta = it },
                    label = { Text("Nome da “senha”:", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))


                TextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoria:", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Digite o email", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha:", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição:", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (showError) {
                    Text(
                        text = "ERRO: nome já existe.",
                        color = Color.Red,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onAdd,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(bottom = 50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                ) {
                    Text("adicionar", fontSize = 24.sp)
                }
            }
        }
    )
}
