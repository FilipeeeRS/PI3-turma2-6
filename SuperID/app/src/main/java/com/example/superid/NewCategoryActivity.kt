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
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = "Nova categoria",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

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
                        value = nomeCategoria,
                        onValueChange = { nomeCategoria = it },
                        label = { Text("Nome da categoria:", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ERRO: categoria já existe.",
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