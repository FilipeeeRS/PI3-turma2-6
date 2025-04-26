package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation.Companion.None
import android.provider.Settings



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
                        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

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
                                Toast.makeText(
                                    this,
                                    "Conta criada com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
                    Toast.makeText(
                        this,
                        "Erro ao criar conta: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher),
            contentDescription = "Logo"
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text("Registre-se", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(64.dp))

        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Digite o nome*") },
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Digite o email*") },
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de senha
        TextField(
            value = senha,
            onValueChange = {
                senha = it
                if (erroConfirmacao) erroConfirmacao = false
            },
            label = { Text("Digite a senha*") },
            modifier = Modifier.fillMaxWidth(0.85f),
            visualTransformation = if (senhaVisivel) None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                    Icon(
                        imageVector = if (senhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Mostrar senha",
                        tint = Color.Gray
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de confirmar senha
        TextField(
            value = confirmarSenha,
            onValueChange = {
                confirmarSenha = it
                if (erroConfirmacao) erroConfirmacao = false
            },
            label = { Text("Confirme a senha*") },
            isError = erroConfirmacao,
            modifier = Modifier.fillMaxWidth(0.85f),
            visualTransformation = if (confirmarSenhaVisivel) None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmarSenhaVisivel = !confirmarSenhaVisivel }) {
                    Icon(
                        imageVector = if (confirmarSenhaVisivel) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Mostrar confirmação de senha",
                        tint = Color.Gray
                    )
                }
            }
        )

        if (erroConfirmacao) {
            Text(
                text = "As senhas não coincidem.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (senha != confirmarSenha) {
                    erroConfirmacao = true
                } else {
                    erroConfirmacao = false
                    onCadastrarClick(nome.trim(), email.trim(), senha.trim())
                }
            },
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = "Registrar", fontSize = 24.sp)
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