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
import androidx.compose.ui.res.painterResource

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

fun deletarSenha(userId: String, categoria: String, senhaId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .document(userId)
        .collection("categorias")
        .document(categoria)
        .collection("senhas")
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
    var passwordList by remember { mutableStateOf<List<Triple<String, String, Map<String, Any>>>>(emptyList()) }
    val filteredPasswords = remember(passwordList, selectedCategory) {
        if (selectedCategory == "Todas" || selectedCategory == "Categorias") {
            passwordList
        } else {
            passwordList.filter { (_, categoryOfPassword, _) ->
                categoryOfPassword == selectedCategory
            }
        }
    }

    var categoryList by remember {
        mutableStateOf(
            mutableListOf("Todas", "Sites Web", "Aplicativos", "Teclados de Acesso Físico")
        )
    }
    val colorScheme = MaterialTheme.colorScheme
    var isEmailVerified by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnSuccessListener {
            isEmailVerified = user.isEmailVerified
        }?.addOnFailureListener {
            Toast.makeText(context, "Erro ao verificar status do e-mail.", Toast.LENGTH_SHORT).show()
        }
    }

    // Carregar senhas e categorias do Firestore
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "Usuário não logado.", Toast.LENGTH_SHORT).show()
            return@LaunchedEffect
        }

        val db = FirebaseFirestore.getInstance()
        val userCategoriesRef = db.collection("users").document(uid).collection("categories")

        // Listener para as categorias
        userCategoriesRef.addSnapshotListener { categorySnapshots, categoryE ->
            if (categoryE != null) {
                Toast.makeText(context, "Erro ao ouvir categorias: ${categoryE.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            val categoriasFixas = listOf("Todas", "Sites Web", "Aplicativos", "Teclados de Acesso Físico")
            val dynamicCategories = categorySnapshots?.documents
                ?.mapNotNull { it.getString("nome") }
                ?.filter { it !in categoriasFixas } // Filtra para não duplicar se já existir
                ?: emptyList()

            val allActiveCategories = (categoriasFixas.drop(1) + dynamicCategories).distinct() // remove "Todas" e garante unicidade
            categoryList = (categoriasFixas + dynamicCategories).toMutableList() // Atualiza a lista de categorias para a UI

            // Listener para as senhas
            val currentPasswordListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
            val newPasswordList = mutableListOf<Triple<String, String, Map<String, Any>>>()

            // Remove listeners antigos antes de adicionar novos
            currentPasswordListeners.forEach { it.remove() }
            currentPasswordListeners.clear()

            // Se não houver categorias ativas para buscar senhas, limpa a lista
            if (allActiveCategories.isEmpty()) {
                passwordList = emptyList()
                return@addSnapshotListener
            }


            // Adiciona listeners para cada categoria e coleta as senhas
            allActiveCategories.forEach { categoryName ->
                val categoryPasswordsRef = db.collection("users").document(uid)
                    .collection("categorias").document(categoryName)
                    .collection("senhas")

                val listenerRegistration = categoryPasswordsRef.addSnapshotListener { passwordSnapshots, passwordE ->
                    if (passwordE != null) {
                        Toast.makeText(context, "Erro ao ouvir senhas de '$categoryName': ${passwordE.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    // Remove senhas da categoria atual da lista temporária antes de adicionar as atualizadas
                    newPasswordList.removeAll { (_, cat, _) -> cat == categoryName }

                    passwordSnapshots?.documents?.forEach { doc ->
                        val senhaData = doc.data ?: emptyMap()
                        // Armazena o ID da senha, a CATEGORIA da senha, e os DADOS da senha
                        newPasswordList.add(Triple(doc.id, categoryName, senhaData))
                    }
                    // Atualiza a lista de senhas principal após processar todas as atualizações
                    passwordList = newPasswordList.toList()
                }
                currentPasswordListeners.add(listenerRegistration)
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
                        //Bloqueia o QRcode se a pessoa não tiver verificado seu e-mail
                        if (isEmailVerified) {
                            val intent = Intent(context, QRCodeActivity::class.java)
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(
                                context,
                                "Você precisa verificar seu e-mail antes de usar o QR Code.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    containerColor = if (isEmailVerified) colorScheme.secondary else Color.Gray,
                    contentColor = colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "QR Code",
                        tint = if (isEmailVerified) colorScheme.onPrimary else Color(0xFFBDBDBD)
                    )
                }

                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, NewPasswordActivity::class.java)
                        context.startActivity(intent)
                    },
                    containerColor = colorScheme.secondary,
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
            // Mostra aviso se a conta ainda não estiver verificada
            if (!isEmailVerified) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFCDD2))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Sua conta ainda não foi verificada. Verifique sua caixa de entrada para ativar todos os recursos.",
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(category, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
                                        if (category !in listOf("Todas", "Sites Web", "Aplicativos", "Teclados de Acesso Físico")) {
                                            IconButton(onClick = {
                                                // Remove a categoria
                                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                                if (userId != null) {
                                                    Firebase.firestore.collection("users").document(userId)
                                                        .collection("categories").whereEqualTo("nome", category)
                                                        .get()
                                                        .addOnSuccessListener { documents ->
                                                            for (document in documents) {
                                                                Firebase.firestore.collection("users").document(userId)
                                                                    .collection("categories").document(document.id)
                                                                    .delete()
                                                                Toast.makeText(context, "Categoria removida", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                        .addOnFailureListener {
                                                            Toast.makeText(context, "Erro ao remover categoria", Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color(0xFFD32F2F))
                                            }
                                        }
                                    }
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
            var senhaParaExcluir by remember { mutableStateOf<Triple<String, String, Map<String, Any>>?>(null) } // Triple (senhaId, categoriaDaSenha, senhaData)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                // filteredPasswords agora é uma lista de Triple<String, String, Map<String, Any>>
                items(filteredPasswords) { (id, categoriaDaSenha, senhaItem) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.primary),
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
                                    intent.putExtra("categoriaDaSenha", categoriaDaSenha) // Passando a categoria
                                    val urlDoSite = senhaItem["urlSite"] as? String
                                    if (!urlDoSite.isNullOrBlank()) {
                                        intent.putExtra("urlSite", urlDoSite)
                                    }
                                    context.startActivity(intent)
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }

                                IconButton(onClick = {
                                    senhaParaExcluir = Triple(id, categoriaDaSenha, senhaItem)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color(0xFFD32F2F))
                                }
                            }

                            // Categoria
                            Text(
                                text = "Categoria: ${senhaItem["category"] as? String ?: "Sem categoria"}",
                                fontSize = 14.sp
                            )

                            // Site (se houver)
                            (senhaItem["urlSite"] as? String)?.let {
                                Text(text = "Site: $it", fontSize = 14.sp)
                            }

                            // Descrição (se houver)
                            (senhaItem["description"] as? String)?.takeIf { it.isNotBlank() }?.let {
                                Text(text = "Descrição: $it", fontSize = 14.sp)
                            }

                            // Login (se houver)
                            (senhaItem["login"] as? String)?.takeIf { it.isNotBlank() }?.let {
                                Text(text = "Login: $it", fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Senha (visível ou oculta)
                            val senhaCriptografada = senhaItem["password"] as? String
                            val senhaVisivel = visibilidadeSenha[id] == true

                            Text(
                                text = if (senhaVisivel)
                                    "Senha: " + descriptografarSenha(senhaCriptografada ?: "")
                                else
                                    "Senha: ••••••••",
                                fontSize = 16.sp
                            )
                        }
                    }


                }
            }
            if (senhaParaExcluir != null) {
                AlertDialog(
                    onDismissRequest = { senhaParaExcluir = null },
                    title = { Text("Confirmar exclusão") },
                    text = { Text("Tem certeza que deseja remover esta senha?") },
                    confirmButton = {
                        TextButton(onClick = {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            senhaParaExcluir?.let { (senhaId, categoriaDaSenha, _) ->
                                if (userId != null) {
                                    deletarSenha(userId, categoriaDaSenha, senhaId,
                                        onSuccess = {
                                            Toast.makeText(context, "Senha removida", Toast.LENGTH_SHORT).show()
                                            senhaParaExcluir = null
                                        },
                                        onError = {
                                            Toast.makeText(context, "Erro ao remover: ${it.message}", Toast.LENGTH_SHORT).show()
                                            senhaParaExcluir = null
                                        }
                                    )
                                }
                            }
                        }) {
                            Text("Remover", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { senhaParaExcluir = null }) {
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