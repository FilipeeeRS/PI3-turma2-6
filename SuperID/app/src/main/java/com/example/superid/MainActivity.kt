package com.example.superid

// Importações necessárias para funcionalidades
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

// Tela inicial do aplicativo com login e cadastro
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                SuperIDApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuperIDApp() {
    SuperIDTheme {
        TelaInicial()
    }
}


@Composable
fun TelaInicial(modifier: Modifier = Modifier) {
    val context = LocalContext.current // Obtém o contexto da tela atual
    val colorScheme = MaterialTheme.colorScheme // Obtém o esquema de cores atual

    Box( // Caixa que ocupa toda a tela
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(24.dp)
    ) {
        Card( // Card centralizado que contém tod0 o conteúdo da tela
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Column( // Coluna vertical com os elementos
                modifier = Modifier
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image( // Logo do app
                    painter = painterResource(R.drawable.ic_launcher),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 16.dp)
                        .shadow(6.dp, MaterialTheme.shapes.small)
                )

                Text( // Título da tela
                    text = "Bem-vindo ao SuperID",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button( // Botão para entrar
                    onClick = {
                        val intent = Intent(context, LoginActivity::class.java) // Abre a tela de login
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Entrar", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text( // Texto para se cadastrar
                    "Não possui cadastro?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button( // Botão para se cadastrar
                    onClick = {
                        val intent = Intent(context, CadastroActivity::class.java) // Abre a tela de cadastro
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Cadastre-se", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}