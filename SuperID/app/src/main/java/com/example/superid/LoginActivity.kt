package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.VisualTransformation.Companion.None
import androidx.compose.ui.text.style.TextDecoration
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth // Inicializa o FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        setContent {
            SuperIDTheme {
                TelaLogin { email, senha -> loginUser(email, senha) }
            }
        }
    }

    private fun loginUser(email: String, senha: String) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login bem-sucedido
                    val uid = auth.currentUser?.uid
                    Log.i("AUTH-INFO", "Usuário autenticado: $uid")
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    // Falha no login
                    Log.e("AUTH-INFO", "Falha na autenticação: ${task.exception}")
                    Toast.makeText(
                        this,
                        "Falha na autenticação: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
// Envia email para redefinir senha
fun sendPasswordReset(email: String, onResult: (String) -> Unit) {
    val auth = Firebase.auth

    // Tentativa de login com senha inválida só pra puxar o user
    auth.signInWithEmailAndPassword(email, "123")
        .addOnCompleteListener { task ->
            val exception = task.exception
            val user = auth.currentUser

            if (task.isSuccessful || exception?.message?.contains("The password is invalid") == true) {
                // Verifica se o email está verificado
                if (user != null && user.isEmailVerified) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { resetTask ->
                            if (resetTask.isSuccessful) {
                                onResult("E-mail de redefinição enviado com êxito!")
                            } else {
                                onResult("Falha ao enviar e-mail.")
                            }
                        }
                }
            } else {
                val errorMessage = when ((exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "Usuário não encontrado."
                    "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                    else -> "E-mail não verificado."
                }
                onResult(errorMessage)
            }
        }
}


// AlertDialog quando clicado "Esqueci minha senha"
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Recuperação de senha") },
        text = {
            Column {
                Text("Digite o e-mail para envio do link de redefinição de senha.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSend(email)
                onDismiss()
            }) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    SuperIDTheme {
        TelaLogin { _, _ ->}
    }
}

@Composable
fun TelaLogin(
    modifier: Modifier = Modifier,
    onLoginClick: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var dialogoEsqueceuSenha by remember { mutableStateOf(false) }
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
            shape = MaterialTheme.shapes.medium, // igual WelcomeScreen
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // igual WelcomeScreen
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
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
                    text = "Bem-vindo ao SuperID",
                    style = typography.titleLarge.copy(color = colorScheme.primary)
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
                    onValueChange = { senha = it },
                    label = { Text("Senha", style = typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = if (senhaVisivel) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                            Icon(
                                imageVector = if (senhaVisivel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
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

                TextButton(onClick = { dialogoEsqueceuSenha = true }) {
                    Text(
                        "Esqueceu sua senha?",
                        style = typography.bodySmall.copy(
                            color = colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }

                Button(
                    onClick = { onLoginClick(email.trim(), senha.trim()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text("Entrar", style = typography.titleMedium.copy(color = colorScheme.onPrimary))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Não possui conta?",
                        style = typography.bodySmall.copy(color = colorScheme.onSurfaceVariant)
                    )
                    TextButton(
                        onClick = {
                            val intent = Intent(context, CadastroActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Text(
                            "Cadastre-se agora",
                            style = typography.bodySmall.copy(
                                color = colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    }
                }
            }
        }

        if (dialogoEsqueceuSenha) {
            ForgotPasswordDialog(
                onDismiss = { dialogoEsqueceuSenha = false },
                onSend = { emailDigitado ->
                    sendPasswordReset(emailDigitado) { resultado ->
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}