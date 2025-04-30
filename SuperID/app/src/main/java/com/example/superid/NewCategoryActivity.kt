@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.superid

import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme

class NewCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperIDTheme {
                NewCategoryScreen(
                    onBack = { finish() },
                    onAdd = {
                        // lógica para salvar a categoria
                    },
                    showError = false
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewCategoryScreenPreview() {
    SuperIDTheme {
        NewCategoryScreen(showError = true)
    }
}

@Composable
fun NewCategoryScreen(
    onBack: () -> Unit = {},
    onAdd: () -> Unit = {},
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