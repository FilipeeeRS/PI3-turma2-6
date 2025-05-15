package com.example.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme

class TermsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperIDTheme {
                TermsScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TermsScreenPreview() {
    SuperIDTheme {
        TermsScreen()
    }
}

@Composable
fun TermsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Ao usar este aplicativo, você concorda com nossos termos de uso:",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "- Uso responsável das suas credenciais\n" +
                            "- Login sem senha via QR Code\n" +
                            "- Recuperação de senha via e-mail\n\n" +
                            "Não compartilhamos seus dados com terceiros.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            (context as? ComponentActivity)?.finish()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Text("Recusar", fontSize = 18.sp)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                            (context as? ComponentActivity)?.finish()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Text("Aceitar", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

