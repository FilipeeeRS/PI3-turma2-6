package com.example.superid2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.superid2.ui.theme.TelaLogin
import com.example.superid2.ui.theme.SuperID2Theme
import com.example.superid2.ui.theme.TelaInicial


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var mostrarTelaLogin by remember { mutableStateOf(false) }

            if (mostrarTelaLogin) {
                TelaLogin()
            } else {
                TelaInicial(onContinuarClick = { mostrarTelaLogin = true })
            }
            //pensar em outra forma
        }
    }
}