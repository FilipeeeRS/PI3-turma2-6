package com.example.superid2.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TelaInicial(onContinuarClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "SuperID", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "O que é o SuperID?", style = MaterialTheme.typography.bodyLarge)

        Text(
            text = "SuperID é um aplicativo mobile para autenticação segura e armazenamento de credenciais. Desenvolvido em Kotlin no Android Studio, com backend no Google Firebase, ele permite:\n\n" +
                    "- Criar e gerenciar contas de usuário\n" +
                    "- Armazenar senhas de forma criptografada\n" +
                    "- Login sem senha via QR Code\n" +
                    "- Recuperação de senha por email\n\n" +
                    "O sistema possui duas partes: um aplicativo Android para gestão de credenciais e uma integração web para login sem senha em sites parceiros.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))



        Button(
            onClick = onContinuarClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Ler Termos")
        }

        //o botao "ler termos" direciona pro login? acho q n, mas tava assim no figma
    }
}
