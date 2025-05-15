@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.superid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SuperID",
                            style = MaterialTheme.typography.titleLarge,
                            color = colorScheme.onPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            modifier = Modifier.padding(start = 8.dp),
                            tint = colorScheme.onPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colorScheme.onPrimary
                        )
                    }
                },

                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = colorScheme.primary
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Adicionar nova senha",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Wrapper com fundo claro e bordas arredondadas para os TextFields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = nomeConta,
                        onValueChange = { nomeConta = it },
                        label = { Text("Nome da senha", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        label = { Text("Categoria", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Digite o email", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        ),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = senha,
                        onValueChange = { senha = it },
                        label = { Text("Senha", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        ),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Descrição", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        ),
                    )
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ERRO: nome já existe.",
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onAdd,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        "Adicionar",
                        fontSize = 20.sp,
                        color = colorScheme.onPrimary
                    )
                }
            }
        }
    )
}
