package com.example.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SuperIDTheme {
                HomeScreen()
            }
        }
    }
}


fun descriptografarSenha(criptografada: String): String {
    return try {
        val chave = "criptografia2025"
        val secretKey = SecretKeySpec(chave.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedBytes = cipher.doFinal(Base64.decode(criptografada, Base64.NO_WRAP))
        String(decryptedBytes)
    } catch (e: Exception) {
        "Erro"
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    SuperIDTheme {
        HomeScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Categorias") }
    var passwordList by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var categoryList by remember {
        mutableStateOf(
            mutableListOf("Todas", "Sites Web", "Aplicativos", "Teclados de Acesso Físico")
        )
    }

    // Carregar senhas e categorias do Firestore
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            // Escuta as senhas em tempo real
            Firebase.firestore.collection("users").document(uid).collection("passwords")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Toast.makeText(context, "Erro ao ouvir senhas", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    val list = snapshots?.documents?.mapNotNull { doc ->
                        doc.id to (doc.data ?: emptyMap())
                    } ?: emptyList()
                    passwordList = list
                }

            // Escuta as categorias em tempo real
            Firebase.firestore.collection("users").document(uid).collection("categories")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Toast.makeText(context, "Erro ao ouvir categorias", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    val dynamicCategories = snapshots?.documents?.mapNotNull { it.getString("nome") } ?: emptyList()
                    categoryList = (categoryList + dynamicCategories).toMutableList()
                }
        }
    }


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
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* TODO */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO */ },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, QRCodeActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    Text("QR")
                }

                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, NewPasswordActivity::class.java)
                        context.startActivity(intent)
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Conta")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Divider(thickness = 10.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Suas Contas",
                    fontSize = 20.sp,
                )

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(selectedCategory, fontSize = 20.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categoryList.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }

                        Divider()

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = "Adicionar")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Adicionar categoria")
                                }
                            },
                            onClick = {
                                expanded = false
                                val intent = Intent(context, NewCategoryActivity::class.java)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            val visibilidadeSenha = remember { mutableStateMapOf<String, Boolean>() }

            // LISTA DE SENHAS
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {

                items(passwordList) { (id, senhaItem) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8FF)),
                        elevation = CardDefaults.cardElevation(3.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = senhaItem["nomeConta"] as? String ?: "Sem nome",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Categoria: ${senhaItem["category"] as? String ?: "Sem categoria"}",
                                        fontSize = 14.sp
                                    )
                                    val descricao = senhaItem["description"] as? String
                                    if (!descricao.isNullOrEmpty()) {
                                        Text(
                                            text = descricao,
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 👁 Mostrar/ocultar senha
                            val senhaCriptografada = senhaItem["password"] as? String
                            val senhaVisivel = visibilidadeSenha[id] == true

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (senhaVisivel) descriptografarSenha(
                                        senhaCriptografada ?: ""
                                    ) else "••••••••",
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(onClick = {
                                    visibilidadeSenha[id] = !senhaVisivel
                                }) {
                                    Icon(
                                        imageVector = if (senhaVisivel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
