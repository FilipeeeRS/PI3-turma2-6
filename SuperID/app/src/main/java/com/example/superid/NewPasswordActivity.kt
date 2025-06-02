@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.superid

// Importações necessárias para funcionalidades
import android.content.Intent
import android.os.Bundle
import android.util.Base64
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

// Classe principal da tela de adicionar nova senha
class NewPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verifica se o usuário está logado
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // se user for == null, mostra aviso e redireciona para tela de login se não estiver autenticado
            Toast.makeText(this, "Você precisa estar logado para acessar essa função.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        // se estiver logado, segue normalmente


        // Define o conteúdo da tela com Compose
        setContent {
            SuperIDTheme {
                NewPasswordScreen(
                    onBack = { finish() }, // Função para voltar
                    showError = false // Inicia sem erro
                )
            }
        }
    }
}

// Gera um token de acesso aleatório em base64 para a senha nova
fun gerarAccessToken(): String {
    val random = SecureRandom()
    val bytes = ByteArray(192) // Gera 192 bytes
    random.nextBytes(bytes) // Preenche o array com bytes aleatórios
    return Base64.encodeToString(bytes, Base64.NO_WRAP) // Retorna o token em base64
}

// Criptografa a senha com AES
fun criptografarSenha(senha: String): String {
    val chave = "criptografia2025" // Chave fixa de 16 bytes (requerido pelo AES) para criptografia
    val secretKey = SecretKeySpec(chave.toByteArray(), "AES") // Cria a chave AES a partir da string chave
    val cipher = Cipher.getInstance("AES") // Instância do algoritmo AES
    cipher.init(Cipher.ENCRYPT_MODE, secretKey) // Define modo de criptografia como encriptar e a chave
    val encrypted = cipher.doFinal(senha.toByteArray()) // Criptografa os bytes da senha e armazena em encrypted
    return Base64.encodeToString(encrypted, Base64.NO_WRAP) // Retorna a senha criptografada em base64 para ser salva no firestore
}

// Função para salvar a nova senha no firestore
fun salvarNovaSenha(
    categoria: String,
    email: String,
    senha: String,
    descricao: String,
    nomeConta: String,
    url: String?,
    context: android.content.Context,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit // Função para tratar erros
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid // Pega o ID do usuário logado
    val db = FirebaseFirestore.getInstance() // Instância do banco

    if (userId == null) {
        // Retorna erro se o usuário não estiver autenticado
        onError(Exception("Usuário não autenticado"))
        return
    }

    // Criptografa a senha e gera token
    val senhaCriptografada = criptografarSenha(senha)
    val accessToken = gerarAccessToken()

    // Mapa com os dados que serão salvos
    val senhaMap = hashMapOf(
        "nomeConta" to nomeConta,
        "category" to categoria,
        "login" to email,
        "password" to senhaCriptografada,
        "description" to descricao,
        "accessToken" to accessToken,
        "uid" to userId
    )

    // Se for uma senha de site e a URL estiver preenchida, adiciona no mapa
    if (categoria == "Sites Web" && !url.isNullOrBlank()) {
        senhaMap["urlSite"] = url.trim()
    }

    // Define o caminho do documento no firestore
    // Caminho: users/{userId}/categorias/{categoria}/senhas/{senhaId}
    val senhaRef = db.collection("users")
        .document(userId)
        .collection("categorias")
        .document(categoria)
        .collection("senhas")
        .document() // Cria um novo ID automaticamente

    // Salva no Firestore
    senhaRef.set(senhaMap)
        .addOnSuccessListener {
            Toast.makeText(context, "Senha salva com sucesso!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            onError(e)
        }
}

@Composable
fun NewPasswordScreen(
    onBack: () -> Unit = {},  // Função chamada quando o usuário aperta para voltar
    showError: Boolean = false // Define se a mensagem de erro será exibida
) {
    val context = LocalContext.current // Obtém o contexto da tela atual para exibir mensagens

    // Variáveis para armazenar os dados da senha que são digitados pelo usuário
    var nomeConta by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Selecione uma categoria") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var erroURL by remember { mutableStateOf(false) } // Indica se a URL já existe

    var expanded by remember { mutableStateOf(false) } // Indica se o dropdown está aberto ou fechado
    var categoryList by remember {
        // Lista de categorias pré-definidas, não podem ser deletadas nem editadas
        mutableStateOf(
            listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico")
        )
    }

    // Carrega as categorias do Firestore quando a tela é iniciada para atualizar a lista
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid // Pega o ID do usuário logado
        if (uid != null) { // Se o usuário estiver autenticado, carrega as categorias
            FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("categories") // Caminho do firestore
                .addSnapshotListener { snapshots, e -> // Add listener para pegar as alterações
                    if (e != null) { // Se houver erro, mostra mensagem e retorna
                        Toast.makeText(context, "Erro ao carregar categorias", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    // Atualiza a lista de categorias com as do Firestore e as pré-definidas
                    val dynamicCategories = snapshots?.documents?.mapNotNull { it.getString("nome") } ?: emptyList()
                    categoryList = listOf("Sites Web", "Aplicativos", "Teclados de Acesso Físico") + dynamicCategories
                }
        }
    }

    val colorScheme = MaterialTheme.colorScheme // Obtém o esquema de cores atual

    Scaffold( // Barra de topo
        topBar = {
            TopAppBar(
                title = {
                    // Título do app e com ícone de cadeado na mesma linha
                    Row(
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
                            tint = colorScheme.onPrimary
                        )
                    }
                },
                navigationIcon = {
                    // Botão para voltar
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colorScheme.onPrimary
                        )
                    }
                },

                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = colorScheme.primary
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize() // Ocupar toda a tela
                    .padding(paddingValues) // Espaçamento interno
                    .padding(horizontal = 24.dp, vertical = 16.dp), // Espaçamento interno
                horizontalAlignment = Alignment.CenterHorizontally // Alinhamento horizontal
            ) {

                val colorScheme = MaterialTheme.colorScheme

                Spacer(modifier = Modifier.height(24.dp))

                // Título da tela
                Text(
                    text = "Adicionar nova senha",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Campo: Nome da conta
                TextField(
                    value = nomeConta,
                    onValueChange = { nomeConta = it },
                    label = { Text("Nome da senha", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown das categorias
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    TextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria", fontSize = 18.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    // Lista de categorias para escolher no dropdown
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categoryList.forEach { selectionOption ->
                            DropdownMenuItem( // Botão para escolher a categoria
                                text = { Text(selectionOption) },
                                onClick = {
                                    categoria = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Campo de URL só aparece se a categoria for 'Sites Web'
                if (categoria == "Sites Web") {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = url,
                        onValueChange = {
                            url = it
                            erroURL = false // Limpa erro quando o usuário digita
                        },
                        label = { Text("URL do site", fontSize = 18.sp) },
                        isError = erroURL,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    // Mensagem de erro se a URL já existir no firestore
                    if (erroURL) {
                        Text(
                            text = "Você já tem uma senha cadastrada para esse site.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de email (opcional)
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (opcional)", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de senha (com ocultação do texto)
                TextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = { Text("Senha", fontSize = 18.sp) },
                    visualTransformation = PasswordVisualTransformation(), // Oculta a senha digitada pelo usuário
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de descrição (opcional)
                TextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição (opcional)", fontSize = 18.sp) },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )

                // Mostra erro se o nome da conta já existir
                if (showError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ERRO: nome já existe.",
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botão de salvar senha
                Button(
                    onClick = {
                        if (nomeConta.isNotEmpty() && categoria.isNotEmpty() && senha.isNotEmpty()) { // Verifica se todos os campos foram preenchidos
                            val uid = FirebaseAuth.getInstance().currentUser?.uid

                            if (categoria == "Sites Web" && url.isBlank()) { // Verifica se a URL foi preenchida na categiria 'Sites Web'
                                Toast.makeText(context, "Informe a URL do site.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Verifica se a senha já existe no firestore
                            if (uid != null && categoria == "Sites Web") {
                                FirebaseFirestore.getInstance()
                                    .collection("users").document(uid)
                                    .collection("passwords")
                                    .whereEqualTo("urlSite", url.trim())
                                    .get()
                                    .addOnSuccessListener { docs ->
                                        if (!docs.isEmpty) { // Se existir, mostra erro
                                            erroURL = true
                                        } else { // Se não existir, chama função salvarNovaSenha
                                            salvarNovaSenha(categoria = categoria,email = email,senha = senha,descricao = descricao,nomeConta = nomeConta,
                                                url = if (categoria == "Sites Web") url else null, context = context, onSuccess = { onBack() },
                                                onError = {
                                                    Toast.makeText(context, "Erro ao salvar senha!", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                            } else { // Se não for 'Sites Web', chama função salvarNovaSenha normalmente
                                salvarNovaSenha(categoria = categoria, email = email, senha = senha, descricao = descricao, nomeConta = nomeConta,
                                    url = if (categoria == "Sites Web") url else null, context = context, onSuccess = { onBack() },
                                    onError = {
                                        Toast.makeText(context, "Erro ao salvar senha!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        } else { // Se algum campo não foi preenchido, mostra mensagem
                            Toast.makeText(context, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth(0.85f) // Tamanho
                        .height(56.dp), // Altura
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary), // Cor
                    shape = RoundedCornerShape(16.dp), // Arredondamento
                ) {
                    Text( // Texto do botão
                        "Adicionar",
                        fontSize = 20.sp,
                        color = colorScheme.onPrimary
                    )
                }
            }
        }

    )
}