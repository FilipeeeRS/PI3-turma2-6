package com.example.superid


import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid.ui.theme.SuperIDTheme


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
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {

        Image(
            painter = painterResource(R.drawable.ic_launcher),
            contentDescription = "Logo"
        )

        Text("Bem-vindo ao SuperID")

        Spacer(modifier = Modifier.height(150.dp))

        Button(onClick = { val intent = Intent(context, CadastroActivity::class.java)
            context.startActivity(intent)}, modifier = Modifier.fillMaxWidth()) {
            Text("Cadastre-se")
        }
        Spacer(modifier = Modifier.height(30.dp))

        Text("Já possui cadastro?")

        Button(onClick = {/*TODO*/}, modifier = Modifier.fillMaxWidth()) {
            Text("Entrar")
        }
    }
}