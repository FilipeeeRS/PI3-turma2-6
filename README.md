# PI3-Turma2-6 "SuperID"
Projeto Integrador 3 / PUC CAMPINAS Engenharia de Software / Fevereiro de 2025 -> 01 de junho de 2025

Integrantes: 
- Isaac Vitor Silva Bertonha
- João Masayuki Kobata
- Filipe Ribeiro Simões
- Marcos Antônio Valério Filho
- Pedro Facine Nery
  
Orientadores:
- Prof. Mateus Dias
- Profa. Renata Arantes
- Prof. Luã Marcelo

TEMA DO PROJETO: Gerenciador de Autenticações
-------------------------------
  SuperID é um aplicativo mobile de autenticação segura e armazenamento de credenciais, desenvolvido em Kotlin no Android Studio, com backend no Google Firebase. Suas principais funcionalidades são: criar e gerenciar contas de usuário, armazenar senhas de forma criptografada, fazer login sem senha em sites através de QR Code e recuperar a senha via email. Esse sistema possui duas divisões: 
  - Aplicativo Android (para a gestão das credenciais)
  - Integração Web (API + Firebase Functions) para login sem senha em sites parceiros.

COMO CONFIGURAR O PROJETO
-------------------------------
git clone https://github.com/FilipeeeRS/PI3-turma2-6.git

COMO RODAR O PROJETO
-------------------------------
Parte Android 

Pré-requisitos:
- Android Studio (preferencialmente a versão mais recente).
- Conta no Firebase configurada com Authentication e Firestore.
- Conexão com Firebase: baixe o google-services.json e adicione na pasta app/ do projeto.

Configuração inicial:
- Clone o repositório do GitHub: git clone https://github.com/FilipeeeRS/PI3-turma2-6.git
- Abra o projeto no Android Studio.
- Sincronize o Gradle.

Execução:
- Conecte um dispositivo Android ou use um emulador.
- Execute o app (Run > Run 'app').
---------------------------------
Parte Web

Pré-requisitos:
- Node.js e npm instalados.
- Firebase CLI instalada.
  
Configuração inicial:
- Acesse a pasta das Firebase Functions.
- Instale as dependências do projeto.

Execução:
- Faça login no Firebase CLI, se necessário.
- Selecione o projeto Firebase, se ainda não estiver configurado.
- Execute as Firebase Functions localmente utilizando o emulador do Firebase ou faça o deploy para o ambiente online.
- Abra o site parceiro simulado incluído no projeto para testar o fluxo de autenticação com QR Code.


