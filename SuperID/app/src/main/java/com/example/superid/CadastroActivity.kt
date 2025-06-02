package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class CadastroActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth // Inicializa o FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializando o FirebaseAuth e o FirebaseFirestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TelaCadastro(modifier = Modifier.padding(innerPadding)) { nome, email, senha ->
                        cadastrarUsuario(nome, email, senha)
                    }
                }
            }
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Pegando o ID do dispositivo (Android ID)
                        val deviceId =
                            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

                        val user = hashMapOf(
                            "nome" to nome,
                            "email" to email,
                            "uid" to uid,
                            "deviceId" to deviceId // <--- salvando deviceId (como IMEI equivalente)
                        )

                        firestore.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                val user = auth.currentUser
                                user?.sendEmailVerification()
                                    ?.addOnCompleteListener { verifyTask ->
                                        if (verifyTask.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                "Conta criada! Verifique seu e-mail para ativá-la.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Conta criada, mas falha ao enviar e-mail de verificação.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                val intent = Intent(this, HomeActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Erro ao salvar no Firestore: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    val exception = task.exception
                    val errorMessage = when ((exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode) {
                        "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está em uso."
                        "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                        "ERROR_WEAK_PASSWORD" -> "A senha deve ter pelo menos 6 caracteres."
                        else -> "Erro ao criar conta: ${exception?.localizedMessage}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    @Composable
    fun TelaCadastro(
        modifier: Modifier = Modifier,
        onCadastrarClick: (String, String, String) -> Unit
    ) {
        var nome by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }
        var confirmarSenha by remember { mutableStateOf("") }
        var erroConfirmacao by remember { mutableStateOf(false) }
        var senhaVisivel by remember { mutableStateOf(false) }
        var confirmarSenhaVisivel by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val colorScheme = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(24.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(8.dp, MaterialTheme.shapes.small)
                    )

                    Text(
                        text = "Crie sua conta",
                        style = typography.titleLarge.copy(color = colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome", style = typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            cursorColor = colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail", style = typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            cursorColor = colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = senha,
                        onValueChange = {
                            senha = it
                            erroConfirmacao = false
                        },
                        label = { Text("Senha", style = typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                Icon(
                                    imageVector = if (senhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar senha",
                                    tint = colorScheme.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            cursorColor = colorScheme.primary
                        )
                    )

                    OutlinedTextField(
                        value = confirmarSenha,
                        onValueChange = {
                            confirmarSenha = it
                            erroConfirmacao = false
                        },
                        label = { Text("Confirme a senha", style = typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = erroConfirmacao,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (confirmarSenhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                confirmarSenhaVisivel = !confirmarSenhaVisivel
                            }) {
                                Icon(
                                    imageVector = if (confirmarSenhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar confirmação",
                                    tint = colorScheme.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            cursorColor = colorScheme.primary,
                            errorBorderColor = colorScheme.error
                        )
                    )

                    if (erroConfirmacao) {
                        Text(
                            text = "As senhas são diferentes.",
                            style = typography.bodySmall.copy(color = colorScheme.error)
                        )
                    }

                    Button(
                        onClick = {
                            if (senha != confirmarSenha) {
                                erroConfirmacao = true
                            } else {
                                onCadastrarClick(nome.trim(), email.trim(), senha.trim())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                    ) {
                        Text(
                            "Registrar",
                            style = typography.titleMedium.copy(color = colorScheme.onPrimary)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Já possui conta?",
                            style = typography.bodySmall.copy(color = colorScheme.onSurfaceVariant)
                        )

                        TextButton(onClick = {
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Text(
                                "Entrar",
                                style = typography.bodySmall.copy(
                                    color = colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        }
                    }
                }
            }
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun CadastroPreview() {
        SuperIDTheme {
            TelaCadastro { _, _, _ -> }
        }
    }
}