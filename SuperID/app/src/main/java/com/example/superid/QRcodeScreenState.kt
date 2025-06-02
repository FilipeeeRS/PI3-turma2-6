package com.example.superid

sealed class QRCodeScreenState {
    object Idle : QRCodeScreenState() // Estado inicial, pronto para escanear
    data class Processing(val message: String) : QRCodeScreenState() // Escaneando ou chamando função
    data class Success(val message: String) : QRCodeScreenState() // Sucesso na chamada da função
    data class Error(val message: String) : QRCodeScreenState() // Erro
}