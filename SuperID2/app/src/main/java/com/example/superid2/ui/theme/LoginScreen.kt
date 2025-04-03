package com.example.superid2.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid2.R

@Composable
fun LoginScreen() {
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

        //imagem
        Image(
            painter = painterResource(id = R.drawable.c8a7ae39_b091_4a6a_af43_15f8453e8b98),
            contentDescription = "Logo da aplicação",
            modifier = Modifier.size(120.dp)
        )

        //texto superID
        Text(text = "SuperID", style = MaterialTheme.typography.h3)
        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Login",
            style = MaterialTheme.typography.h4,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO: Implementar login */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
        ) {
            Text(text = "Login", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Não possui cadastro ainda?", color = Color.Gray)


        Button(
            onClick = { /* TODO: Implementar cadastro */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
        ) {
            Text(text = "Cadastre-se", color = Color.White)
        }
    }
}





@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen()
}
