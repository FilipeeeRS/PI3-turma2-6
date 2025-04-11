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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Spacer(modifier = modifier.height(48.dp))

        Text("Bem-vindo ao SuperID", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(64.dp))


        Button(onClick = {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black            )
        ) {
            Text("Entrar", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Não possui cadastro?", fontSize = 18.sp)

        Button(
            onClick = {
                val intent = Intent(context, CadastroActivity::class.java)
                context.startActivity(intent)

            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black            )
        ) {
            Text("Cadastre-se", fontSize = 24.sp)
        }
    }
}