package com.example.superid2.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaInicial(onContinuarClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "SuperID",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "O que é o SuperID?",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SuperID é um aplicativo mobile para autenticação segura e armazenamento de credenciais. Desenvolvido em Kotlin no Android Studio, com backend no Google Firebase, ele permite:\n\n" +
                        "- Criar e gerenciar contas de usuário\n" +
                        "- Armazenar senhas de forma criptografada\n" +
                        "- Login sem senha via QR Code\n" +
                        "- Recuperação de senha por email\n\n" +
                        "O sistema possui duas partes: um aplicativo Android para gestão de credenciais e uma integração web para login sem senha em sites parceiros.",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Button(
            onClick= onContinuarClick,
            modifier= Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp),
            colors= ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape= RectangleShape
        ) {
            Text("OK", color = Color.White, fontSize = 20.sp)
        }
    }
}
