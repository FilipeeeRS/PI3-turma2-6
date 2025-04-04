package com.example.superid2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.superid2.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperID2Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "tela_inicial") {
                    composable("tela_inicial") {
                        TelaInicial(onContinuarClick = { navController.navigate("tela_login") })
                    }
                    composable("tela_login") {
                        TelaLogin(navController)
                    }
                    composable("tela_cadastro") {
                        TelaCadastro()
                    }
                }
            }
        }
    }
}