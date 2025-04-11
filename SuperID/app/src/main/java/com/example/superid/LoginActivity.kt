package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth


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


@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    SuperIDTheme {
        TelaLogin{ _, _ ->}
    }
}


@Composable
fun TelaLogin(modifier: Modifier = Modifier, onLoginClick: (String, String) -> Unit ) {
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

        Spacer(modifier = modifier.height(48.dp))

        Text("Acesse agora o SuperID", fontSize=30.sp)

        Spacer(modifier = modifier.height(64.dp))

        TextField(
            value = email,
            onValueChange = {email = it},
            label = {Text("Digite o email", fontSize=18.sp)},
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = modifier.height(16.dp))

        TextField(
            value = senha,
            onValueChange = {senha = it},
            label = { Text("Digite a senha", fontSize=18.sp) },
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = modifier.height(24.dp))

        Button(onClick = { onLoginClick(email.trim(), senha.trim())},
            modifier = modifier.fillMaxWidth(0.7f),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Entrar", fontSize=24.sp)
        }
    }
}