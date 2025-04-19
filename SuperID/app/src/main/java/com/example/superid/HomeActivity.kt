package com.example.superid


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val uid = auth.currentUser?.uid

        setContent {
            SuperIDTheme {
                var userInfo by remember { mutableStateOf<Map<String, String>?>(null) }

                LaunchedEffect(uid) {
                    uid?.let {
                        firestore.collection("users").document(it).get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    val data = doc.data?.mapValues { it.value.toString() } ?: emptyMap()
                                    userInfo = data
                                } else {
                                    Toast.makeText(this@HomeActivity, "Usuário não encontrado no Firestore", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@HomeActivity, "Erro ao buscar dados: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                if (userInfo != null) {
                    UserInfoScreen(userInfo!!)
                } else {
                    Text("Carregando dados...")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    SuperIDTheme {
        HomeScreen()
    }
}

@Composable
fun UserInfoScreen(userData: Map<String, String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Bem-vindo(a)!", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        userData.forEach { (key, value) ->
            Text("$key: $value", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var presses by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SuperID")
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {/*TODO*/ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {/*TODO*/ }) {
                        Icon(Icons.Default.Search, contentDescription = "Busca")
                    }
                }
            )

        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {/*TODO*/ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {/*TODO*/ },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = "Senhas") },
                    label = { Text("Senhas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /*TODO*/ },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {/*TODO*/ }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Conta")
            }
        }
    ){}
}