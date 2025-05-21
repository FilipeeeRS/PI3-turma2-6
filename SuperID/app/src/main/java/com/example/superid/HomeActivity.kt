package com.example.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.FirebaseFirestore
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog

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

fun deletarSenha(userId: String, senhaId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .document(userId)
        .collection("passwords")
        .document(senhaId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e) }
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
    var logoutDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Categorias") }
    var passwordList by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    val filteredPasswords = remember(passwordList, selectedCategory) {
        if (selectedCategory == "Todas" || selectedCategory == "Categorias") {
            passwordList
        } else {
            passwordList.filter { (_, data) ->
                data["category"] == selectedCategory
            }
        }
    }

    var categoryList by remember {
        mutableStateOf(
            mutableListOf("Todas", "Sites Web", "Aplicativos", "Teclados de Acesso Físico")
        )
    }
    val colorScheme = MaterialTheme.colorScheme

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

    fun signOut(context: Context) {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Desconexão realizada", Toast.LENGTH_SHORT).show()

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "SuperID",
                                style = MaterialTheme.typography.titleLarge,
                                color = colorScheme.onPrimary
                            )

                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Secure",
                                modifier = Modifier.padding(start = 8.dp),

                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .clickable {
                                        logoutDialog = true
                                    },

                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary
                )
            )
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
                    },
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ) {
                    Text("QR")
                }

                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, NewPasswordActivity::class.java)
                        context.startActivity(intent)
                    },
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Conta")
                }
            }
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorScheme.background)
        ) {
            Divider(thickness = 10.dp, color = colorScheme.surface)

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
                    color = colorScheme.onBackground
                )

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(selectedCategory, fontSize = 20.sp, color = colorScheme.onBackground)
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = colorScheme.onBackground
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(colorScheme.surface)
                    ) {
                        categoryList.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(category, color = colorScheme.onSurface)
                                },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }

                        Divider(color = colorScheme.outline)

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Adicionar",
                                        tint = colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Adicionar categoria", color = colorScheme.onSurface)
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
            var senhaIdParaExcluir by remember { mutableStateOf<String?>(null) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(filteredPasswords) { (id, senhaItem) ->
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
                            // Linha 1 — título + ícones
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = senhaItem["nomeConta"] as? String ?: "Sem nome",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(onClick = {
                                    visibilidadeSenha[id] = !(visibilidadeSenha[id] ?: false)
                                }) {
                                    Icon(
                                        imageVector = if (visibilidadeSenha[id] == true) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle senha"
                                    )
                                }

                                IconButton(onClick = {
                                    val intent = Intent(context, EditPasswordActivity::class.java)
                                    intent.putExtra("senhaId", id)
                                    context.startActivity(intent)
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }

                                IconButton(onClick = {
                                    senhaIdParaExcluir = id
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                                }
                            }

                            // Linha 2 — categoria
                            Text(
                                text = "categoria: ${senhaItem["category"] as? String ?: "Sem categoria"}",
                                fontSize = 14.sp
                            )

                            // Linha 3 — senha (visível ou oculta)
                            val senhaCriptografada = senhaItem["password"] as? String
                            val senhaVisivel = visibilidadeSenha[id] == true

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (senhaVisivel)
                                    descriptografarSenha(senhaCriptografada ?: "")
                                else
                                    "••••••••",
                                fontSize = 16.sp
                            )
                        }
                    }

                }
            }
            if (senhaIdParaExcluir != null) {
                AlertDialog(
                    onDismissRequest = { senhaIdParaExcluir = null },
                    title = { Text("Confirmar exclusão") },
                    text = { Text("Tem certeza que deseja remover esta senha?") },
                    confirmButton = {
                        TextButton(onClick = {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            senhaIdParaExcluir?.let { senhaId ->
                                if (userId != null) {
                                    deletarSenha(userId, senhaId,
                                        onSuccess = {
                                            Toast.makeText(context, "Senha removida", Toast.LENGTH_SHORT).show()
                                            senhaIdParaExcluir = null
                                        },
                                        onError = {
                                            Toast.makeText(context, "Erro ao remover: ${it.message}", Toast.LENGTH_SHORT).show()
                                            senhaIdParaExcluir = null
                                        }
                                    )
                                }
                            }
                        }) {
                            Text("Remover", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { senhaIdParaExcluir = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            if (logoutDialog) {
                AlertDialog(
                    onDismissRequest = { logoutDialog = false },
                    title = { Text("Confirmar Saída") },
                    text = { Text("Tem certeza que deseja sair da conta?") },
                    confirmButton = {
                        TextButton(onClick = {
                            logoutDialog = false
                            signOut(context)
                        }) {
                            Text("Sair", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { logoutDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
