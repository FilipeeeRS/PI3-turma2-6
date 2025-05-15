package com.example.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(24.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "O que é o SuperID?",
                        fontSize = 30.sp,
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Image(
                        painter = painterResource(R.drawable.ic_launcher),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(60.dp)
                            .padding(start = 16.dp)
                            .shadow(8.dp, MaterialTheme.shapes.small)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "O SuperID é um aplicativo para Android que permite fazer login em sites de forma rápida e segura, sem precisar digitar senha. Basta escanear um QR Code!\n\n" +
                            "Com o SuperID, você pode:\n" +
                            "- Fazer login em sites parceiros usando QR Code, sem digitar senha\n" +
                            "- Armazenar e gerenciar suas credenciais de forma segura e criptografada\n" +
                            "- Recuperar sua senha facilmente via email\n" +
                            "- Visualizar e organizar suas contas em um só lugar\n" +
                            "- Ter uma integração com sites para login automático\n\n",
                    fontSize = 18.sp,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val intent = Intent(context, TermsActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .widthIn(min = 180.dp)
                        .height(56.dp)
                ) {
                    Text("OK", fontSize = 20.sp)
                }
            }
        }
    }
}


