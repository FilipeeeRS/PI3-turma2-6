package com.example.superid

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
import androidx.compose.ui.res.painterResource
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
        // Criar o usuário no Firebase Authentication
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Obter o UID do usuário criado
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val user = hashMapOf(
                            "nome" to nome,
                            "email" to email
                        )

                        // Salvar os dados do usuário no Firestore
                        firestore.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Conta criada com sucesso!",
                                    Toast.LENGTH_SHORT
                                ).show()
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
fun TelaCadastro(modifier: Modifier = Modifier, onCadastrarClick: (String, String, String) -> Unit) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher),
            contentDescription = "Logo"
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text("Registre-se")

        Spacer(modifier = Modifier.height(64.dp))

        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Digite o nome*") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Digite o email*") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Digite a senha*") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onCadastrarClick(nome.trim(), email.trim(), senha.trim())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Criar conta")
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
