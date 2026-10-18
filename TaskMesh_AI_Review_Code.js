/**
 * TaskMesh AI Review — Google Apps Script Backend (Code.gs)
 *
 * Production-ready server endpoint for Meshy AI Audio Review on Firebase Spark (Free) Plan.
 * Receives audio Base64 and dynamic answer key JSON from TaskMesh Android App,
 * verifies Firebase ID token via official Firebase Identity Toolkit API (accounts:lookup),
 * enforces user rate limits, and evaluates audio using Gemini 2.0 Flash API (API Keys in Script Properties).
 */

var FIREBASE_PROJECT_ID = "taskmesh-6242d";
var DEFAULT_FIREBASE_WEB_API_KEY = "AIzaSyC9cmhj1euAcl9MuKAWH0m0V43zwQLGbnw";
var MAX_DAILY_REVIEWS_PER_USER = 10;
var MAX_AUDIO_SIZE_BYTES = 15 * 1024 * 1024; // 15MB

/**
 * HTTP POST Request Handler
 */
function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return createJsonResponse({ error: "Invalid HTTP request: empty payload" }, 400);
    }

    var requestData = JSON.parse(e.postData.contents);

    // 1. Verify Authorization Header / Firebase ID Token
    var authHeader = e.parameter.auth || (e.postData.type === "application/json" ? requestData.idToken : null);
    if (!authHeader && e.parameter.Authorization) {
      authHeader = e.parameter.Authorization;
    }

    var idToken = null;
    if (authHeader) {
      idToken = authHeader.replace(/^Bearer\s+/i, "").trim();
    }

    var uid = "anonymous_guest";
    if (idToken) {
      var verifiedAuth = verifyFirebaseIdToken(idToken);
      if (!verifiedAuth.valid) {
        return createJsonResponse({ error: "Authentication failed: " + verifiedAuth.error }, 401);
      }
      uid = verifiedAuth.uid;
    }

    // 2. Enforce Concurrency-Safe Daily Rate Limiting
    var rateLimitCheck = enforceUserRateLimit(uid);
    if (!rateLimitCheck.allowed) {
      return createJsonResponse({ error: rateLimitCheck.message }, 429);
    }

    // 3. Validate Request Payload
    var valValidation = validateRequestPayload(requestData);
    if (!valValidation.valid) {
      return createJsonResponse({ error: valValidation.error }, 400);
    }

    // 4. Retrieve Gemini API Key from Script Properties
    var apiKey = PropertiesService.getScriptProperties().getProperty("GEMINI_API_KEY");
    if (!apiKey) {
      return createJsonResponse({ error: "Server configuration error: GEMINI_API_KEY property is missing in Script Properties" }, 500);
    }

    // 5. Evaluate Audio with Gemini 2.0 Flash API
    var evaluationResult = processGeminiEvaluation(requestData.audioBase64, requestData.mimeType, requestData.answerKey, apiKey);
    if (!evaluationResult.success) {
      return createJsonResponse({ error: evaluationResult.error }, 502);
    }

    // 6. Return Validated Evaluation Result JSON
    return createJsonResponse(evaluationResult.data, 200);

  } catch (err) {
    return createJsonResponse({ error: "Server execution error: " + err.toString() }, 500);
  }
}

/**
 * Verifies Firebase ID Token using official Firebase Identity Toolkit REST API (accounts:lookup)
 */
function verifyFirebaseIdToken(idToken) {
  try {
    var firebaseWebApiKey = PropertiesService.getScriptProperties().getProperty("FIREBASE_WEB_API_KEY") || DEFAULT_FIREBASE_WEB_API_KEY;

    var url = "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=" + encodeURIComponent(firebaseWebApiKey);
    var payload = {
      idToken: idToken
    };

    var options = {
      method: "post",
      contentType: "application/json",
      payload: JSON.stringify(payload),
      muteHttpExceptions: true
    };

    var response = UrlFetchApp.fetch(url, options);
    var resCode = response.getResponseCode();

    if (resCode !== 200) {
      var errObj = {};
      try {
        errObj = JSON.parse(response.getContentText());
      } catch (e) {}
      var errMsg = (errObj.error && errObj.error.message) ? errObj.error.message : ("HTTP " + resCode);
      return { valid: false, error: "Firebase token verification failed: " + errMsg };
    }

    var resData = JSON.parse(response.getContentText());
    if (!resData.users || !Array.isArray(resData.users) || resData.users.length === 0) {
      return { valid: false, error: "No active user account associated with ID token" };
    }

    var user = resData.users[0];
    var uid = user.localId;
    if (!uid) {
      return { valid: false, error: "Missing user UID in Firebase user profile" };
    }

    return { valid: true, uid: uid, email: user.email || "" };
  } catch (e) {
    return { valid: false, error: "Token verification exception: " + e.toString() };
  }
}

/**
 * Concurrency-Safe Daily Rate Limiter using LockService and PropertiesService
 */
function enforceUserRateLimit(uid) {
  var lock = LockService.getScriptLock();
  try {
    if (!lock.tryLock(5000)) {
      return { allowed: false, message: "Server busy, please retry in a moment" };
    }

    var scriptProps = PropertiesService.getScriptProperties();
    var todayStr = Utilities.formatDate(new Date(), "UTC", "yyyy-MM-dd");
    var rateKey = "ratelimit_" + uid + "_" + todayStr;

    var currentCount = parseInt(scriptProps.getProperty(rateKey) || "0", 10);
    if (currentCount >= MAX_DAILY_REVIEWS_PER_USER) {
      return { allowed: false, message: "Daily AI review limit reached (10 reviews/day). Try again tomorrow!" };
    }

    scriptProps.setProperty(rateKey, (currentCount + 1).toString());
    return { allowed: true };
  } catch (e) {
    return { allowed: true };
  } finally {
    lock.releaseLock();
  }
}

/**
 * Validates Android Request Payload
 */
function validateRequestPayload(data) {
  if (!data.recordingId) return { valid: false, error: "Missing recordingId" };
  if (!data.mimeType) return { valid: false, error: "Missing mimeType" };
  if (!data.audioBase64) return { valid: false, error: "Missing audioBase64 payload" };
  if (!data.answerKey || !data.answerKey.questions || !Array.isArray(data.answerKey.questions)) {
    return { valid: false, error: "Missing or invalid answerKey structure" };
  }

  var estimatedSizeBytes = Math.floor(data.audioBase64.length * 0.75);
  if (estimatedSizeBytes > MAX_AUDIO_SIZE_BYTES) {
    return { valid: false, error: "Audio size exceeds 15 MB limit" };
  }

  return { valid: true };
}

/**
 * Evaluates Audio against Dynamic Answer Key using Gemini 2.0 Flash REST API
 */
function processGeminiEvaluation(audioBase64, mimeType, answerKey, apiKey) {
  try {
    var url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

    var answerKeyText = JSON.stringify(answerKey, null, 2);

    var systemInstruction = "You are Meshy AI, an expert English pronunciation, vocabulary, and spoken language evaluator for TaskMesh.\n" +
      "Listen to the user's spoken audio recording and compare what they spoke against the supplied dynamic Answer Key JSON.\n" +
      "Evaluate each question item in the answer key.\n" +
      "Return ONLY a JSON object matching this EXACT schema with NO Markdown formatting, NO ```json wrapping, and NO additional text:\n" +
      "{\n" +
      "  \"totalScore\": number,\n" +
      "  \"maxScore\": number,\n" +
      "  \"percentage\": number,\n" +
      "  \"transcript\": \"Full verbatim transcript of user spoken audio\",\n" +
      "  \"overallSummary\": \"Concise feedback summary\",\n" +
      "  \"questionsFeedback\": [\n" +
      "    {\n" +
      "      \"questionNumber\": number,\n" +
      "      \"promptText\": \"string\",\n" +
      "      \"isCorrect\": boolean,\n" +
      "      \"score\": number,\n" +
      "      \"userSpokenText\": \"string\",\n" +
      "      \"feedbackText\": \"string\",\n" +
      "      \"missingPoints\": \"string\"\n" +
      "    }\n" +
      "  ]\n" +
      "}";

    var requestBody = {
      contents: [
        {
          parts: [
            {
              inlineData: {
                mimeType: mimeType || "audio/m4a",
                data: audioBase64
              }
            },
            {
              text: "ANSWER KEY JSON:\n" + answerKeyText + "\n\nPlease evaluate my spoken audio against this Answer Key."
            }
          ]
        }
      ],
      systemInstruction: {
        parts: [
          { text: systemInstruction }
        ]
      },
      generationConfig: {
        responseMimeType: "application/json",
        temperature: 0.2
      }
    };

    var options = {
      method: "post",
      contentType: "application/json",
      payload: JSON.stringify(requestBody),
      muteHttpExceptions: true
    };

    var response = UrlFetchApp.fetch(url, options);
    var resCode = response.getResponseCode();
    if (resCode !== 200) {
      return { success: false, error: "Gemini API error HTTP " + resCode + ": " + response.getContentText() };
    }

    var resObj = JSON.parse(response.getContentText());
    var textContent = resObj.candidates[0].content.parts[0].text;

    textContent = textContent.replace(/^```json\s*/i, "").replace(/^```\s*/i, "").replace(/```$/s, "").trim();

    var resultData = JSON.parse(textContent);

    var validated = validateAndFormatResultData(resultData, answerKey);
    return { success: true, data: validated };

  } catch (err) {
    return { success: false, error: "Gemini evaluation failed: " + err.toString() };
  }
}

/**
 * Validates Gemini Response JSON against Answer Key requirements
 */
function validateAndFormatResultData(data, answerKey) {
  var maxScore = answerKey.questions.length;
  var questionsFeedback = data.questionsFeedback || [];

  var calculatedScore = 0;
  var validatedFeedback = [];

  for (var i = 0; i < answerKey.questions.length; i++) {
    var prompt = answerKey.questions[i];
    var fb = questionsFeedback[i] || {};

    var isCorrect = typeof fb.isCorrect === "boolean" ? fb.isCorrect : false;
    var itemScore = isCorrect ? 1 : 0;
    calculatedScore += itemScore;

    validatedFeedback.push({
      questionNumber: prompt.questionNumber,
      promptText: prompt.promptText,
      isCorrect: isCorrect,
      score: itemScore,
      userSpokenText: fb.userSpokenText || "",
      feedbackText: fb.feedbackText || (isCorrect ? "Correct answer" : "Needs review"),
      missingPoints: fb.missingPoints || ""
    });
  }

  var totalScore = Math.min(calculatedScore, maxScore);
  var percentage = Math.round((totalScore / maxScore) * 100);

  return {
    totalScore: totalScore,
    maxScore: maxScore,
    percentage: percentage,
    transcript: data.transcript || "Audio transcribed.",
    overallSummary: data.overallSummary || "Evaluation complete.",
    questionsFeedback: validatedFeedback
  };
}

/**
 * Helper to construct JSON HTTP Response
 */
function createJsonResponse(dataObject, statusCode) {
  var output = ContentService.createTextOutput(JSON.stringify(dataObject));
  output.setMimeType(ContentService.MimeType.JSON);
  return output;
}
