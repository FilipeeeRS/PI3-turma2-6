@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.util.Base64
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class NewPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Você precisa estar logado para acessar essa função.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java)) // Altere se o nome da sua tela de login for diferente
            finish()
            return
        }

        setContent {
            SuperIDTheme {
                NewPasswordScreen(
                    onBack = { finish() },
                    showError = false
                )
            }
        }
    }
}

fun gerarAccessToken(): String {
    val random = SecureRandom()
    val bytes = ByteArray(192) // 192 bytes ≈ 256 base64 chars
    random.nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

fun criptografarSenha(senha: String): String {
    val chave = "criptografia2025" // chave AES de 16 bytes
    val secretKey = SecretKeySpec(chave.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val encrypted = cipher.doFinal(senha.toByteArray())
    return Base64.encodeToString(encrypted, Base64.NO_WRAP)
}

fun salvarNovaSenha(
    categoria: String,
    email: String,
    senha: String,
    descricao: String,
    nomeConta: String,
    context: android.content.Context,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    if (userId == null) {
        onError(Exception("Usuário não autenticado"))
        return
    }

    val senhaCriptografada = criptografarSenha(senha)
    val accessToken = gerarAccessToken()

    val senhaMap = hashMapOf(
        "nomeConta" to nomeConta,
        "category" to categoria,
        "login" to email,
        "password" to senhaCriptografada,
        "description" to descricao,
        "accessToken" to accessToken
    )

    db.collection("users")
        .document(userId)
        .collection("passwords")
        .add(senhaMap)
        .addOnSuccessListener {
            Toast.makeText(context, "Senha salva com sucesso!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            onError(e)
        }
}

@Composable
fun NewPasswordScreen(
    onBack: () -> Unit = {},
    showError: Boolean = false
) {
    val context = LocalContext.current
    var nomeConta by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Selecione uma categoria") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var categoryList by remember {
        mutableStateOf(
            listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico")
        )
    }

    // Carregar categorias dinâmicas
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("categories")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Toast.makeText(context, "Erro ao carregar categorias", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    val dynamicCategories = snapshots?.documents?.mapNotNull { it.getString("nome") } ?: emptyList()
                    categoryList = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") + dynamicCategories
                }
        }
    }

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
                val colorScheme = MaterialTheme.colorScheme

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Adicionar nova senha",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                TextField(
                    value = nomeConta,
                    onValueChange = { nomeConta = it },
                    label = { Text("Nome da senha", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    TextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria", fontSize = 18.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categoryList.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    categoria = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (opcional)", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha", fontSize = 18.sp) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição (opcional)", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ERRO: nome já existe.",
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (nomeConta.isNotEmpty() && categoria.isNotEmpty() && senha.isNotEmpty()) {
                            salvarNovaSenha(
                                categoria = categoria,
                                email = email,
                                senha = senha,
                                descricao = descricao,
                                nomeConta = nomeConta,
                                context = context,
                                onSuccess = { onBack() },
                                onError = {
                                    Toast.makeText(context, "Erro ao salvar senha!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                        }
                    },
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
