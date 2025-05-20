package com.example.superid

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val senhaId = intent.getStringExtra("senhaId") ?: return

        setContent {
            SuperIDTheme {
                EditPasswordScreen(senhaId) { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(
    senhaId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    var nome by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Selecione uma categoria") }
    var expanded by remember { mutableStateOf(false) }
    var categoryList by remember { mutableStateOf(listOf<String>()) }

    // Carrega campos
    LaunchedEffect(Unit) {
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("passwords").document(senhaId)
                .get()
                .addOnSuccessListener { doc ->
                    nome = doc.getString("nomeConta") ?: ""
                    val senhaCriptografada = doc.getString("password") ?: ""
                    senha = descriptografarSenha(senhaCriptografada)
                    categoria = doc.getString("category") ?: "Selecione uma categoria"
                }

            FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("categories")
                .addSnapshotListener { snapshots, _ ->
                    val dynamic = snapshots?.documents?.mapNotNull { it.getString("nome") } ?: emptyList()
                    categoryList = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") + dynamic
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SuperID", style = MaterialTheme.typography.titleLarge, color = colorScheme.onPrimary)
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(start = 8.dp), tint = colorScheme.onPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorScheme.primary)
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
                    text = "Editar senha",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Campos agrupados
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
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome da senha", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = categoria,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria", style = MaterialTheme.typography.bodyMedium) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = colorScheme.surface,
                                focusedIndicatorColor = colorScheme.primary,
                                unfocusedIndicatorColor = colorScheme.outline
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categoryList.forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection) },
                                    onClick = {
                                        categoria = selection
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

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
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (uid != null) {
                            FirebaseFirestore.getInstance()
                                .collection("users").document(uid)
                                .collection("passwords").document(senhaId)
                                .update(
                                    "nomeConta", nome,
                                    "password", criptografarSenha(senha),
                                    "category", categoria
                                ).addOnSuccessListener {
                                    Toast.makeText(context, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }.addOnFailureListener {
                                    Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text("Salvar alterações", fontSize = 20.sp, color = colorScheme.onPrimary)
                }
            }
        }
    )
}
