package com.example.superid2.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.superid2.R

@Composable
fun TelaLogin(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        // Imagem
        Image(
            painter = painterResource(id = R.drawable.c8a7ae39_b091_4a6a_af43_15f8453e8b98),
            contentDescription = "Logo da aplicação",
            modifier = Modifier.size(120.dp)
        )

        // Texto "SuperID"
        Text(
            text = "SuperID",
            style = MaterialTheme.typography.h3,
            fontSize = 32.sp // Aumentado para destacar mais
        )
        Spacer(modifier = Modifier.height(50.dp))

        // Texto "Login"
        Text(
            text = "Login",
            style = MaterialTheme.typography.h4,
            fontSize = 24.sp, // Aumentado para melhorar a visibilidade
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail", fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha", fontSize = 18.sp) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Implementar oq vem dps de login!!! */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
        ) {
            Text(text = "Login", color = Color.White, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Não possui cadastro ainda?", color = Color.Gray, fontSize = 16.sp)

        Button(
            onClick = { navController.navigate("tela_cadastro") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
        ) {
            Text(text = "Cadastre-se", color = Color.White, fontSize = 20.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    TelaLogin(
        navController = TODO()
    )
}
