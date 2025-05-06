@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.util.Base64
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

// Gera accessToken Base64 com 256 caracteres
fun gerarAccessToken(): String {
    val random = SecureRandom()
    val bytes = ByteArray(192) // 192 bytes ≈ 256 base64 chars
    random.nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

// Criptografa a senha usando AES
fun criptografarSenha(senha: String): String {
    val chave = "criptografia2025" // chave AES de 16 bytes
    val secretKey = SecretKeySpec(chave.toByteArray(), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val encrypted = cipher.doFinal(senha.toByteArray())
    return Base64.encodeToString(encrypted, Base64.NO_WRAP)
}

// Salva senha criptografada no Firestore
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
    var categoria by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var nomeConta by remember { mutableStateOf("") }
    val context = LocalContext.current

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
                Text("Adicionar nova senha", fontSize = 30.sp)

                Spacer(modifier = Modifier.height(32.dp))

                TextField(
                    value = nomeConta,
                    onValueChange = { nomeConta = it },
                    label = { Text("Nome da senha", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoria", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

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
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição (opcional)", fontSize = 18.sp) },
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
                    onClick = {
                        if (nomeConta.isNotEmpty() && categoria.isNotEmpty() && senha.isNotEmpty()) {
                            salvarNovaSenha(
                                categoria = categoria,
                                email = email,
                                senha = senha,
                                descricao = descricao,
                                nomeConta = nomeConta,
                                context = context,
                                onSuccess = {
                                    onBack()
                                },
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
                        .padding(bottom = 50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                ) {
                    Text("Adicionar", fontSize = 24.sp)
                }
            }
        }
    )
}