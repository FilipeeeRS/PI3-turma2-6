package com.example.superid

import android.content.Context
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
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Termos de Uso – SuperID",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    """
                    1. Uso do Aplicativo
                    - Fornecer informações corretas ao realizar o cadastro.
                    - Validar o e-mail para acessar todas as funcionalidades.
                    - Manter a confidencialidade da senha mestre.

                    2. Armazenamento e Proteção de Dados
                    - Dados essenciais são armazenados com segurança.
                    - As senhas são protegidas por técnicas apropriadas.
                    - Nenhum sistema é totalmente imune a riscos.

                    3. Login sem Senha
                    - Login em sites parceiros via QR Code.
                    - O usuário confirma a autenticação através do aplicativo.

                    4. Recuperação de Senha
                    - Recuperação via e-mail cadastrado e validado.

                    5. Exclusão de Dados
                    - Exclusão de senhas e categorias a qualquer momento.
                    - Exclusão de uma categoria remove todas as senhas vinculadas.
                    - O SuperID não se responsabiliza por perdas decorrentes dessas ações.

                    6. Limitações de Responsabilidade
                    - Altos padrões de segurança e confiabilidade.
                    - Não nos responsabilizamos por falhas técnicas ou perdas.

                    7. Modificações nos Termos
                    - Os Termos podem ser modificados a qualquer momento.

                    8. Aceitação dos Termos
                    - Ao clicar em 'Aceitar', você concorda com todas as disposições.
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface
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
                            // Marca que o app já foi aberto
                            val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            with (sharedPref.edit()) {
                                putBoolean("is_first_launch", false)
                                apply()
                            }

                            // Vai para a MainActivity e finaliza as atividades Welcome e Terms
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

