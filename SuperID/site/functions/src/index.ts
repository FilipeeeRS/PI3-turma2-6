import cors from "cors";
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as crypto from "crypto";

admin.initializeApp();
const db = admin.firestore();

const corsHandler = cors({origin: true});

export const performAuth = functions.https.onRequest((req, res) => {
  corsHandler(req, res, async () => {
    if (req.method !== "POST") {
      functions.logger.warn(
        "performAuth chamada com método não permitido:", req.method
      );
      res.status(405).json({error: "Método não permitido"});
      return;
    }

    try {
      const {apiKey, url} = req.body;

      if (!apiKey || !url) {
        functions.logger.warn(
          "performAuth: apiKey ou url ausentes.", req.body
        );
        res.status(400).json({error: "apiKey e url são obrigatórios."});
        return;
      }

      functions.logger.info(`performAuth: Validando parceiro para url: ${url}`);
      const partnerRef = db.collection("partners").doc(url);
      const partnerDoc = await partnerRef.get();

      if (!partnerDoc.exists) {
        functions.logger.warn(`performAuth: Site não cadastrado: ${url}`);
        res.status(404).json({error: "Site não cadastrado."});
        return;
      }

      const partnerData = partnerDoc.data();

      if (partnerData?.apiKey !== apiKey) {
        functions.logger.warn(`performAuth: API Key inválida para ${url}.`);
        res.status(403).json({error: "API Key inválida."});
        return;
      }

      let loginToken = crypto.randomBytes(192).toString("base64");
      loginToken = loginToken
        .replace(/\+/g, "-")
        .replace(/\//g, "_")
        .replace(/=+$/, "");

      functions.logger.info(
        `performAuth: loginToken gerado para ${url}: ${loginToken}`
      );

      await db.collection("login").doc(loginToken).set({
        site: url,
        partnerApiKey: apiKey,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        pollAttemptCount: 0,
      });

      res.status(200).json({loginToken: loginToken});
    } catch (error) {
      functions.logger.error("Erro inesperado no performAuth:", error);
      let errorMessage = "Erro interno do servidor.";
      if (error instanceof Error) {
        errorMessage = error.message;
      }
      res.status(500).json({error: errorMessage});
    }
  });
});


export const confirmLogin = functions.https
  .onCall(async (requestWrapper: any, context) => {
    functions.logger.log("Função confirmLogin INICIADA.");

    if (requestWrapper && typeof requestWrapper === "object") {
      functions.logger.log(
        "Chaves presentes no requestWrapper (nível superior):",
        Object.keys(requestWrapper)
      );
    } else {
      functions.logger.error(
        "ERRO CRÍTICO: requestWrapper (parâmetro principal)" +
                " não é um objeto ou é nulo."
      );
      throw new functions.https.HttpsError(
        "internal",
        "Estrutura de requisição interna inválida."
      );
    }

    const clientPayload = requestWrapper.data;

    if (clientPayload && typeof clientPayload === "object") {
      functions.logger.log(
        "Payload do cliente (extraído de requestWrapper.data):",
        JSON.stringify(clientPayload, null, 2)
      );
    } else {
      functions.logger.log(
        "clientPayload (requestWrapper.data) não é um objeto" +
                " ou está ausente. Valor:",
        clientPayload
      );
    }

    if (!clientPayload || typeof clientPayload !== "object") {
      functions.logger.error(
        "ERRO CRÍTICO: clientPayload (requestWrapper.data)" +
                " não é um objeto válido ou está ausente.",
        {typeOfClientPayload: typeof clientPayload}
      );
      throw new functions.https.HttpsError(
        "internal",
        "Payload do cliente (requestWrapper.data) não foi" +
                " recebido como um objeto válido."
      );
    }

    const {loginToken, userId} = clientPayload;

    functions.logger.log(
      "Valor extraído para loginToken (de clientPayload):",
      loginToken
    );
    functions.logger.log(
      "Valor extraído para userId (de clientPayload):",
      userId
    );

    if (!loginToken || !userId) {
      const loginTokenErrorMsg =
                "Tipo inválido ou ausente no clientPayload";
      const userIdErrorMsg =
                "Tipo inválido ou ausente no clientPayload";
      functions.logger.error(
        "CONDIÇÃO DE ERRO FINAL: loginToken ou userId" +
                " ausente/falsy no clientPayload!",
        {
          loginToken: typeof loginToken === "string" ?
            loginToken : loginTokenErrorMsg,
          userId: typeof userId === "string" ?
            userId : userIdErrorMsg,
        }
      );
      throw new functions.https.HttpsError(
        "invalid-argument",
        "loginToken e userId são obrigatórios no payload do cliente."
      );
    }

    functions.logger.log(
      `Processando com loginToken: '${loginToken}' e userId: '${userId}'`
    );

    const loginRef = db.collection("login").doc(loginToken);
    const loginDoc = await loginRef.get();

    if (!loginDoc.exists) {
      functions.logger.error(
        "Firestore: loginToken não encontrado no documento:",
        loginToken
      );
      throw new functions.https.HttpsError(
        "not-found",
        "loginToken inválido ou expirado."
      );
    }

    functions.logger.log(
      "Documento loginToken encontrado. Atualizando com userId:",
      userId
    );
    await loginRef.update({
      user: userId,
      loginTime: admin.firestore.FieldValue.serverTimestamp(),
    });

    functions.logger.log(
      "Documento atualizado com sucesso. Retornando success: true"
    );
    return {success: true};
  });

const MAX_POLL_ATTEMPTS = 21;
const TOKEN_LIFESPAN_MS = 1 * 60 * 1000; // 1 minuto

export const getLoginStatus = functions.https.onRequest(async (req, res) => {
  corsHandler(req, res, async () => {
    if (req.method !== "GET" && req.method !== "POST") {
      functions.logger.warn(
        "getLoginStatus chamada com método não permitido:", req.method
      );
      res.status(405).json({status: "ERROR", error: "Método não permitido."});
      return;
    }

    let loginToken: string | undefined;

    if (req.method === "GET") {
      loginToken = req.query.loginToken as string | undefined;
    } else { // POST
      loginToken = req.body.loginToken as string | undefined;
    }

    if (!loginToken) {
      functions.logger.warn(
        "getLoginStatus: loginToken ausente na requisição."
      );
      res.status(400).json({
        status: "ERROR", error: "loginToken é obrigatório.",
      });
      return;
    }

    functions.logger.info(
      `getLoginStatus: Iniciando para token: ${loginToken}`
    );

    try {
      const loginRef = db.collection("login").doc(loginToken);
      const loginDoc = await loginRef.get();

      if (!loginDoc.exists) {
        functions.logger.info(
          `getLoginStatus: Token ${loginToken} não encontrado ou já processado.`
        );
        res.status(404).json({
          status: "NOT_FOUND",
          error: "Token de login não encontrado, expirado ou já utilizado.",
        });
        return;
      }

      const docData = loginDoc.data()!;
      const createdAt = (docData.createdAt as admin.firestore.Timestamp)
        .toMillis();
      const now = Date.now();
      const pollAttemptCount = docData.pollAttemptCount || 0;

      if (now - createdAt > TOKEN_LIFESPAN_MS ||
                pollAttemptCount >= MAX_POLL_ATTEMPTS) {
        functions.logger.info(
          `getLoginStatus: Token ${loginToken} expirado/limite de ` +
                    `tentativas (${pollAttemptCount}). Deletando.`
        );
        await loginRef.delete();
        res.status(410).json({
          status: "EXPIRED_OR_MAX_ATTEMPTS",
          error: "Token de login expirou, atingiu o limite de tentativas " +
                           "ou é inválido. Solicite novo QR Code.",
        });
        return;
      }

      await loginRef.update({
        pollAttemptCount: admin.firestore.FieldValue.increment(1),
      });
      const currentPollAttempt = pollAttemptCount + 1;

      if (docData.user && typeof docData.user === "string") {
        functions.logger.info(
          `getLoginStatus: Token ${loginToken} COMPLETO. ` +
                    `Usuário: ${docData.user}. Tentativa: ${currentPollAttempt}`
        );
        res.status(200).json({
          status: "COMPLETED",
          userId: docData.user,
          site: docData.site,
        });
      } else {
        functions.logger.info(
          `getLoginStatus: Token ${loginToken} PENDENTE. ` +
                    `Tentativa: ${currentPollAttempt}`
        );
        res.status(200).json({status: "PENDING"});
      }
    } catch (error) {
      functions.logger.error(
        `getLoginStatus: Erro inesperado para token ${loginToken}:`, error
      );
      let errMsg = "Erro interno do servidor.";
      if (error instanceof Error) {
        errMsg = error.message;
      }
      res.status(500).json({status: "ERROR", error: errMsg});
    }
  });
});
