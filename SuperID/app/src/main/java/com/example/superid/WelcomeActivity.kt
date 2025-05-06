package com.example.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                WelcomeScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    SuperIDTheme {
        WelcomeScreen()
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "O que é o SuperID?",
                fontSize = 30.sp,
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = painterResource(R.drawable.ic_launcher),
                contentDescription = "Logo",
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "O SuperID é um aplicativo para Android que permite fazer login em sites de forma rápida e segura, sem precisar digitar senha. Basta escanear um QR Code!\n\n" +
                    "Com o SuperID, você pode:\n" +
                    "- Fazer login em sites parceiros usando QR Code, sem digitar senha\n" +
                    "- Armazenar e gerenciar suas credenciais de forma segura e criptografada\n" +
                    "- Recuperar sua senha facilmente via email\n" +
                    "- Visualizar e organizar suas contas em um só lugar\n" +
                    "- Ter uma integração com sites para login automático\n\n",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val intent = Intent(context, TermsActivity::class.java)
                context.startActivity(intent)
                (context as? ComponentActivity)?.finish()
            },
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        ) {
            Text("OK", fontSize = 24.sp)
        }
    }
}