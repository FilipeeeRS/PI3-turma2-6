@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.superid

// Importações necessárias para funcionalidades
import android.content.Intent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Tela para adicionar uma nova categoria
class NewCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verifica se o usuário está logado
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Você precisa estar logado para acessar essa função.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java)) // Substitua pelo nome real da tela de login
            finish()
            return
        }
        // Se estiver logado, continua normalmente

        setContent {
            var showError by remember { mutableStateOf(false) } // Variavel que indica se a mensagem de erro será exibida

            SuperIDTheme {
                NewCategoryScreen(
                    onBack = { finish() }, // Fecha a tela ao voltar
                    onAdd = { nomeCategoria ->
                        salvarCategoria(
                            nomeCategoria = nomeCategoria,
                            onSuccess = {
                                Toast.makeText(this, "Categoria adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                                finish()
                            },
                            onError = { // Se houver erro, mostra mensagem de erro
                                showError = true
                            }
                        )
                    },
                    showError = showError
                )
            }
        }
    }
}

fun salvarCategoria( // Função que salva a categoria no Firestore
    nomeCategoria: String,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
        onError() // Retorna se não estiver autenticado
        return
    }

    val db = FirebaseFirestore.getInstance() // Instância do banco
    val categoriaRef = db.collection("users").document(userId).collection("categories") // Caminho do firestore

    // Verifica se já existe uma categoria com o mesmo nome no banco
    categoriaRef.whereEqualTo("nome", nomeCategoria).get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) { // Se já existir, mostra erro
                onError()
            } else { // Se não existir, salva
                val novaCategoria = hashMapOf("nome" to nomeCategoria) // Cria o objeto da nova categoria
                categoriaRef.add(novaCategoria) // Salva no banco
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError() }
            }
        }
        .addOnFailureListener {
            onError()
        }
}

@Composable
fun NewCategoryScreen(
    onBack: () -> Unit = {}, // Ação de voltar
    onAdd: (String) -> Unit = {}, // Ação de adicionar
    showError: Boolean = false // Indica se a mensagem de erro será exibida
) {
    var nomeCategoria by remember { mutableStateOf("") } // Variável que armazena o nome da categoria
    val colorScheme = MaterialTheme.colorScheme // Obtém o esquema de cores atual

    Scaffold( // Barra no topo
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) { // Alinhamento do título e ícone na mesma linha
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
                navigationIcon = { // Botão para voltar
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
            Column( // Conteúdo da tela nova categoria
                modifier = Modifier
                    .fillMaxSize() // Ocupa toda a tela
                    .padding(paddingValues) // Espaçamento interno
                    .padding(horizontal = 24.dp, vertical = 16.dp), // Espaçamento interno
                horizontalAlignment = Alignment.CenterHorizontally // Alinhamento horizontal
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text( // Título da tela
                    text = "Nova categoria",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column( // Campo para inserir o nome da categoria
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField( // Campo de texto para inserir o nome da categoria
                        value = nomeCategoria,
                        onValueChange = { nomeCategoria = it }, // Atualiza a variável quando o usuário digita
                        label = { Text("Nome da categoria:", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors( // Configurações do campo
                            containerColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.primary,
                            unfocusedIndicatorColor = colorScheme.outline,
                            focusedLabelColor = colorScheme.primary,
                            unfocusedLabelColor = colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (showError) { // Mensagem de erro se a categoria já existir
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ERRO: categoria já existe.",
                        color = colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button( // Botão para adicionar a categoria com o nome digitado no banco
                    onClick = {
                        if (nomeCategoria.isNotBlank()) { // Se o nome não estiver vazio, chama a função de adicionar categoria
                            onAdd(nomeCategoria.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
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