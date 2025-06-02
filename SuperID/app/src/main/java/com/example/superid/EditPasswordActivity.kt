package com.example.superid

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions




class EditPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Senha, categoria da senha e url do site carregados via Intent
        val senhaId = intent.getStringExtra("senhaId")
        val categoriaDaSenha = intent.getStringExtra("categoriaDaSenha")
        val urlDoSite = intent.getStringExtra("urlSite")

        // Verificação para garantir que temos as informações necessárias
        if (senhaId == null || categoriaDaSenha == null) {
            Toast.makeText(this, "Erro: informações da senha incompletas.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            SuperIDTheme {
                // Passa o ID da senha E a categoria da senha para a tela de edição
                EditPasswordScreen(senhaId, categoriaDaSenha, urlDoSite) { finish() }
            }
        }
    }
}

//Função para salvar as mudanças na senha
fun savePasswordChanges(context: Context, uid: String, senhaId: String,
                        nome: String, senha: String, categoriaAtual: String,
                        novaCategoria: String, url: String,
                        login: String, description: String,
                        onUrlError: () -> Unit, onComplete: () -> Unit)
{
    val db = FirebaseFirestore.getInstance()

    if (novaCategoria == "Sites Web" && url.isBlank()) {
        Toast.makeText(context, "Informe a URL do site.", Toast.LENGTH_SHORT).show()
        return
    }
    //
    val updates = mutableMapOf<String, Any>(
        "nomeConta" to nome,
        "password" to criptografarSenha(senha),
        "category" to novaCategoria
    )

    if (login.isNotBlank()) {
        updates["login"] = login.trim()
    } else {
        updates["login"] = FieldValue.delete()
    }

    if (description.isNotBlank()) {
        updates["description"] = description.trim()
    } else {
        updates["description"] = FieldValue.delete()
    }

    if (novaCategoria == "Sites Web") {
        updates["urlSite"] = url.trim()
    } else {
        // Se a categoria NÃO é "Sites Web", e a categoria anterior ERA "Sites Web", remove o campo de url so site.
        if (categoriaAtual == "Sites Web") {
            updates["urlSite"] = FieldValue.delete()
        }
    }

    // Lógica para verificar URL duplicada e atualizar a senha
    fun performUpdateOrMove() {
        if (novaCategoria != categoriaAtual) {

            // Categoria mudou: move a senha para a nova categoria
            val oldPasswordRef = db.collection("users").document(uid)
                .collection("categorias").document(categoriaAtual)
                .collection("senhas").document(senhaId)

            val newPasswordRef = db.collection("users").document(uid)
                .collection("categorias").document(novaCategoria)
                .collection("senhas").document(senhaId)

            // Remove a senha antiga
            oldPasswordRef.get().addOnSuccessListener { oldDoc ->
                if (oldDoc.exists()) {
                    newPasswordRef.set(updates, SetOptions.merge())
                        .addOnSuccessListener {
                            oldPasswordRef.delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Senha atualizada e movida com sucesso!", Toast.LENGTH_SHORT).show()
                                    onComplete()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Erro ao remover senha antiga: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Erro ao mover senha para nova categoria: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao ler senha antiga para mover: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            // Categoria não mudou: apenas atualiza a senha existente
            db.collection("users").document(uid)
                .collection("categorias").document(categoriaAtual)
                .collection("senhas").document(senhaId)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    // Verifica se a URL já existe
    if (novaCategoria == "Sites Web") {
        db.collection("users").document(uid)
            .collection("categorias").document(novaCategoria)
            .collection("senhas")
            .whereEqualTo("urlSite", url.trim())
            .get()
            .addOnSuccessListener { docs ->
                val isDuplicate = docs.documents.any { it.id != senhaId }
                if (isDuplicate) {
                    onUrlError()
                    Toast.makeText(context, "Essa URL já está cadastrada para outra senha nesta categoria.", Toast.LENGTH_LONG).show()
                } else {
                    performUpdateOrMove()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao verificar URL: ${e.message}", Toast.LENGTH_LONG).show()
            }
    } else {
        performUpdateOrMove()
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(
    senhaId: String,
    categoriaDaSenha: String,
    urlInicial: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var nome by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var categoriaSelecionada by remember { mutableStateOf("Selecione uma categoria") }
    var url by remember { mutableStateOf(urlInicial ?: "") }
    var erroURL by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var categoryList by remember { mutableStateOf(listOf<String>()) }
    var login by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Carrega campos da senha e as categorias disponíveis
    LaunchedEffect(Unit) {
        if (uid != null) {
            val db = FirebaseFirestore.getInstance()

            //Carrega os dados da senha específica
            db.collection("users").document(uid)
                .collection("categorias").document(categoriaDaSenha)
                .collection("senhas").document(senhaId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        nome = doc.getString("nomeConta") ?: ""
                        val senhaCriptografada = doc.getString("password") ?: ""
                        senha = descriptografarSenha(senhaCriptografada)
                        categoriaSelecionada = doc.getString("category") ?: "Selecione uma categoria"
                        url = doc.getString("urlSite") ?: ""
                        login = doc.getString("login") ?: ""
                        description = doc.getString("description") ?: ""
                    } else {
                        Toast.makeText(context, "Senha não encontrada.", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao carregar senha: ${e.message}", Toast.LENGTH_LONG).show()
                    onBack()
                }

            //Carrega as categorias dinâmicas para o dropdown
            db.collection("users").document(uid).collection("categories")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Toast.makeText(context, "Erro ao carregar categorias: ${e.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    val dynamic = snapshots?.documents?.mapNotNull { it.getString("nome") } ?: emptyList()
                    categoryList = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") + dynamic
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SuperID", style = MaterialTheme.typography.titleLarge, color = colorScheme.onPrimary)
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.padding(start = 8.dp), tint = colorScheme.onPrimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = colorScheme.primary)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Editar senha",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Campos agrupados
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome da senha", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = login,
                        onValueChange = { login = it },
                        label = { Text("Login", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = categoriaSelecionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria", style = MaterialTheme.typography.bodyMedium) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = colorScheme.surface,
                                focusedIndicatorColor = colorScheme.primary,
                                unfocusedIndicatorColor = colorScheme.outline
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categoryList.forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection) },
                                    onClick = {
                                        categoriaSelecionada = selection // Atualiza o estado
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (categoriaSelecionada == "Sites Web") {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = url,
                            onValueChange = {
                                url = it
                                erroURL = false // Reseta o erro ao digitar
                            },
                            label = { Text("URL do site", style = MaterialTheme.typography.bodyMedium) },
                            isError = erroURL,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = colorScheme.surface,
                                focusedIndicatorColor = colorScheme.primary,
                                unfocusedIndicatorColor = colorScheme.outline
                            )
                        )
                        if (erroURL) {
                            Text(
                                text = "Você já tem uma senha cadastrada para esse site nesta categoria.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = senha,
                        onValueChange = { senha = it },
                        label = { Text("Senha", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (uid != null) {
                            savePasswordChanges(context = context, uid = uid, senhaId = senhaId, nome = nome, senha = senha, categoriaAtual = categoriaDaSenha, novaCategoria = categoriaSelecionada, url = url, login = login, description = description, onUrlError = { erroURL = true }, onComplete = { onBack() })
                        } else {
                            Toast.makeText(context, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text("Salvar alterações", fontSize = 20.sp, color = colorScheme.onPrimary)
                }
            }
        }
    )
}

