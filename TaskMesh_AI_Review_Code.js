/**
 * TaskMesh AI Review — Google Apps Script Backend (Code.gs)
 *
 * Production-ready server endpoint for Meshy AI Audio Review on Firebase Spark (Free) Plan.
 * Evaluates audio using Gemini Flash API (API Keys & Model ID in Script Properties).
 * Includes Secure TaskMesh Admin Error Visibility & Diagnostic Notification System.
 * Enforces Mandatory Firebase Authentication & Server-Verified Token Gating.
 *
 * Supports four actions:
 * 1. default (Share Work): Audio evaluation against a specific answer key.
 * 2. transcribe: Verbatim audio-to-text conversion.
 * 3. evaluateDailySpeech: 100-point rubric evaluation against Day content.
 * 4. getAIUsage: Protected retrieval of authenticated user's real daily AI usage and quota stats.
 */

var FIREBASE_PROJECT_ID = "taskmesh-6242d";
var DEFAULT_FIREBASE_WEB_API_KEY = "AIzaSyC9cmhj1euAcl9MuKAWH0m0V43zwQLGbnw";
var DEFAULT_GEMINI_MODEL_ID = "gemini-2.5-flash";
var MAX_DAILY_REVIEWS_PER_USER = 20;
var MAX_AUDIO_SIZE_BYTES = 15 * 1024 * 1024; // 15MB

/**
 * HTTP POST Request Handler
 */
function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      var errObj = sanitizeUserError("INVALID_REQUEST", 400);
      return createJsonResponse(errObj, 400);
    }

    var requestData = JSON.parse(e.postData.contents);

    // 1. Mandatory Firebase ID Token Authentication Enforcement
    var authHeader = e.parameter.auth || (e.postData.type === "application/json" ? requestData.idToken : null);
    if (!authHeader && e.parameter.Authorization) {
      authHeader = e.parameter.Authorization;
    }

    var idToken = null;
    if (authHeader) {
      idToken = authHeader.replace(/^Bearer\s+/i, "").trim();
    }

    if (!idToken) {
      var errObj = sanitizeUserError("AUTH_CONFIG_FAILURE", 401);
      return createJsonResponse(errObj, 401);
    }

    var verifiedAuth = verifyFirebaseIdToken(idToken);
    if (!verifiedAuth.valid) {
      var errObj = sanitizeUserError("AUTH_CONFIG_FAILURE", 401);
      notifyAdminOfBackendError({
        action: requestData.action || "auth",
        httpStatus: 401,
        category: "AUTH_CONFIG_FAILURE",
        errorDetails: verifiedAuth.error,
        uid: "unauthenticated_user",
        userEmail: "",
        modelId: DEFAULT_GEMINI_MODEL_ID
      });
      return createJsonResponse(errObj, 401);
    }

    var uid = verifiedAuth.uid;
    var userEmail = verifiedAuth.email || "";

    var action = requestData.action || "default";

    // 2. Action: getAIUsage (Does NOT consume AI quota or call Gemini)
    if (action === "getAIUsage") {
      var scriptProps = PropertiesService.getScriptProperties();
      var todayStr = Utilities.formatDate(new Date(), "UTC", "yyyy-MM-dd");
      var rateKey = "ratelimit_" + uid + "_" + todayStr;
      var currentCount = parseInt(scriptProps.getProperty(rateKey) || "0", 10);
      var dailyLimit = MAX_DAILY_REVIEWS_PER_USER;
      var remaining = Math.max(0, dailyLimit - currentCount);

      var usageData = {
        dailyLimit: dailyLimit,
        usedCount: currentCount,
        remainingCount: remaining,
        resetTime: "00:00 UTC",
        resetTimezone: "UTC"
      };

      return createJsonResponse(usageData, 200);
    }

    // 3. Enforce Concurrency-Safe Daily Rate Limiting per Verified UID
    var rateLimitCheck = enforceUserRateLimit(uid);
    if (!rateLimitCheck.allowed) {
      var errObj = sanitizeUserError("DAILY_LIMIT_REACHED", 429);
      return createJsonResponse(errObj, 429);
    }

    // 4. Retrieve Gemini API Key & Model ID from Script Properties
    var apiKey = PropertiesService.getScriptProperties().getProperty("GEMINI_API_KEY");
    if (!apiKey) {
      var errObj = sanitizeUserError("AUTH_CONFIG_FAILURE", 500);
      notifyAdminOfBackendError({
        action: action,
        httpStatus: 500,
        category: "AUTH_CONFIG_FAILURE",
        errorDetails: "GEMINI_API_KEY property is missing in Script Properties",
        uid: uid,
        userEmail: userEmail,
        modelId: DEFAULT_GEMINI_MODEL_ID
      });
      return createJsonResponse(errObj, 500);
    }

    var modelId = PropertiesService.getScriptProperties().getProperty("GEMINI_MODEL_ID") || DEFAULT_GEMINI_MODEL_ID;

    // 5. Dispatch based on Action
    if (action === "transcribe") {
      if (!requestData.audioBase64) {
        var errObj = sanitizeUserError("INVALID_AUDIO", 400);
        return createJsonResponse(errObj, 400);
      }
      var estimatedSize = Math.floor(requestData.audioBase64.length * 0.75);
      if (estimatedSize > MAX_AUDIO_SIZE_BYTES) {
        var errObj = sanitizeUserError("INVALID_AUDIO", 400);
        return createJsonResponse(errObj, 400);
      }
      var transcriptionResult = processGeminiTranscription(requestData.audioBase64, requestData.mimeType, apiKey, modelId);
      if (!transcriptionResult.success) {
        var httpStatus = transcriptionResult.httpStatus || 502;
        var category = transcriptionResult.category || "UNKNOWN_ERROR";
        var errObj = sanitizeUserError(category, httpStatus);

        notifyAdminOfBackendError({
          action: "transcribe",
          httpStatus: httpStatus,
          category: category,
          errorDetails: transcriptionResult.error,
          uid: uid,
          userEmail: userEmail,
          modelId: modelId
        });

        return createJsonResponse(errObj, httpStatus);
      }
      return createJsonResponse(transcriptionResult.data, 200);

    } else if (action === "evaluateDailySpeech") {
      if (!requestData.confirmedTranscript || !requestData.dayNumber) {
        var errObj = sanitizeUserError("INVALID_REQUEST", 400);
        return createJsonResponse(errObj, 400);
      }
      var dsResult = processGeminiDailySpeechEvaluation(
          requestData.confirmedTranscript,
          requestData.dayNumber,
          requestData.topicTitle || "Daily Speech Topic",
          requestData.authoritativeStudyContent || {},
          apiKey,
          modelId
      );
      if (!dsResult.success) {
        var httpStatus = dsResult.httpStatus || 502;
        var category = dsResult.category || "UNKNOWN_ERROR";
        var errObj = sanitizeUserError(category, httpStatus);

        notifyAdminOfBackendError({
          action: "evaluateDailySpeech",
          httpStatus: httpStatus,
          category: category,
          errorDetails: dsResult.error,
          uid: uid,
          userEmail: userEmail,
          dayNumber: requestData.dayNumber,
          topicTitle: requestData.topicTitle,
          modelId: modelId
        });

        return createJsonResponse(errObj, httpStatus);
      }
      return createJsonResponse(dsResult.data, 200);

    } else {
      // Default action: Existing Share Work Evaluation (Backward Compatibility)
      var valValidation = validateRequestPayload(requestData);
      if (!valValidation.valid) {
        var errObj = sanitizeUserError("INVALID_REQUEST", 400);
        return createJsonResponse(errObj, 400);
      }
      var evaluationResult = processGeminiEvaluation(requestData.audioBase64, requestData.mimeType, requestData.answerKey, apiKey, modelId);
      if (!evaluationResult.success) {
        var httpStatus = evaluationResult.httpStatus || 502;
        var category = evaluationResult.category || "UNKNOWN_ERROR";
        var errObj = sanitizeUserError(category, httpStatus);

        notifyAdminOfBackendError({
          action: "evaluateShareWork",
          httpStatus: httpStatus,
          category: category,
          errorDetails: evaluationResult.error,
          uid: uid,
          userEmail: userEmail,
          modelId: modelId
        });

        return createJsonResponse(errObj, httpStatus);
      }
      return createJsonResponse(evaluationResult.data, 200);
    }

  } catch (err) {
    var errObj = sanitizeUserError("UNKNOWN_ERROR", 500);
    notifyAdminOfBackendError({
      action: "doPost_exception",
      httpStatus: 500,
      category: "UNKNOWN_ERROR",
      errorDetails: err.toString(),
      uid: "unknown",
      userEmail: "",
      modelId: DEFAULT_GEMINI_MODEL_ID
    });
    return createJsonResponse(errObj, 500);
  }
}

/**
 * Server-Side Admin Authorization Check
 */
function isAuthorizedAdmin(verifiedEmail) {
  var adminEmail = PropertiesService.getScriptProperties().getProperty("TASKMESH_ADMIN_EMAIL");
  if (!adminEmail || !verifiedEmail) return false;
  return adminEmail.trim().toLowerCase() === verifiedEmail.trim().toLowerCase();
}

/**
 * Sanitizes User-Facing Error Messages and Error Codes (Zero Raw Provider Exposure)
 */
function sanitizeUserError(category, httpStatus) {
  if (httpStatus === 503 || category === "AI_HIGH_DEMAND" || category === "RESOURCE_EXHAUSTED") {
    return {
      error: "Meshy AI is experiencing high demand right now. Please try again in a little while.",
      errorCode: "AI_HIGH_DEMAND"
    };
  } else if (httpStatus === 429 || category === "DAILY_LIMIT_REACHED") {
    return {
      error: "You've reached today's AI review limit. Please try again tomorrow.",
      errorCode: "DAILY_LIMIT_REACHED"
    };
  } else if (category === "NO_SPEECH_DETECTED") {
    return {
      error: "We couldn't detect clear speech in this recording. Please try recording again.",
      errorCode: "NO_SPEECH_DETECTED"
    };
  } else if (category === "INVALID_AUDIO") {
    return {
      error: "We couldn't process this recording. Please try another recording.",
      errorCode: "INVALID_AUDIO"
    };
  } else if (httpStatus === 401 || httpStatus === 403 || category === "AUTH_CONFIG_FAILURE") {
    return {
      error: "Meshy AI is temporarily unavailable. Please try again later.",
      errorCode: "AUTH_CONFIG_FAILURE"
    };
  } else if (category === "NETWORK_TIMEOUT") {
    return {
      error: "Unable to connect to Meshy AI right now. Please check your connection and try again.",
      errorCode: "NETWORK_TIMEOUT"
    };
  } else {
    return {
      error: "Something went wrong while checking your speech. Please try again.",
      errorCode: "UNKNOWN_ERROR"
    };
  }
}

/**
 * Strips secrets, API keys, tokens, and audioBase64 payloads from error diagnostic strings
 */
function sanitizeDiagnosticError(rawErrorText) {
  if (!rawErrorText) return "";
  var text = String(rawErrorText);
  text = text.replace(/key=[a-zA-Z0-9_\-]+/gi, "key=[REDACTED_GEMINI_KEY]");
  text = text.replace(/AIzaSy[a-zA-Z0-9_\-]+/gi, "[REDACTED_API_KEY]");
  text = text.replace(/Bearer\s+[a-zA-Z0-9\._\-]+/gi, "Bearer [REDACTED_TOKEN]");
  text = text.replace(/eyJ[a-zA-Z0-9\._\-]+/gi, "[REDACTED_JWT]");
  text = text.replace(/"audioBase64"\s*:\s*"[^"]+"/gi, '"audioBase64":"[REDACTED_AUDIO_BASE64]"');
  return text;
}

/**
 * Server-Side Error Throttling / Deduplication
 * Suppresses duplicate admin diagnostic emails within a 15-minute window (900,000 ms)
 */
function shouldSendAdminNotification(fingerprint) {
  try {
    var scriptProps = PropertiesService.getScriptProperties();
    var lastSentKey = "admin_notify_time_" + fingerprint;
    var lastSentTime = parseInt(scriptProps.getProperty(lastSentKey) || "0", 10);
    var now = Date.now();
    var throttleWindowMs = 15 * 60 * 1000; // 15 minutes

    if (now - lastSentTime < throttleWindowMs) {
      return false; // Suppress duplicate email notification
    }

    scriptProps.setProperty(lastSentKey, now.toString());
    return true;
  } catch (e) {
    return true; // Fallback to send
  }
}

/**
 * Sends Diagnostic Error Email ONLY to TASKMESH_ADMIN_EMAIL
 * Email notification failure does NOT break or alter the user's HTTP response!
 */
function notifyAdminOfBackendError(details) {
  try {
    var adminEmail = PropertiesService.getScriptProperties().getProperty("TASKMESH_ADMIN_EMAIL");
    if (!adminEmail || adminEmail.trim().length === 0) {
      return; // No admin email configured
    }

    // Do NOT notify admin for expected user behaviors
    var category = details.category || "UNKNOWN_ERROR";
    if (category === "DAILY_LIMIT_REACHED" || category === "NO_SPEECH_DETECTED" || category === "INVALID_AUDIO") {
      return;
    }

    var fingerprint = (details.action || "general") + "_" + (details.httpStatus || 500) + "_" + category;
    if (!shouldSendAdminNotification(fingerprint)) {
      return; // Throttled duplicate
    }

    var sanitizedMsg = sanitizeDiagnosticError(details.errorDetails);
    var subject = "[TaskMesh AI Error] " + (details.action || "Backend") + " - HTTP " + (details.httpStatus || 500);

    var body = "TaskMesh AI Error Report\n\n" +
      "Time: " + Utilities.formatDate(new Date(), "UTC", "yyyy-MM-dd HH:mm:ss 'UTC'") + "\n" +
      "Action: " + (details.action || "N/A") + "\n" +
      "HTTP Status: " + (details.httpStatus || 500) + "\n" +
      "Error Category: " + category + "\n" +
      "User UID: " + (details.uid || "anonymous_guest") + "\n" +
      "User Email: " + (details.userEmail || "N/A") + "\n" +
      "Day: " + (details.dayNumber || "N/A") + "\n" +
      "Topic: " + (details.topicTitle || "N/A") + "\n" +
      "Model: " + (details.modelId || DEFAULT_GEMINI_MODEL_ID) + "\n" +
      "Provider Message: " + sanitizedMsg + "\n";

    MailApp.sendEmail({
      to: adminEmail.trim(),
      subject: subject,
      body: body
    });
  } catch (e) {
    // Admin email notification failure MUST NEVER crash or alter user response!
    Logger.log("Admin email notification error: " + e.toString());
  }
}

/**
 * Transcribe Audio using Gemini API
 */
function processGeminiTranscription(audioBase64, mimeType, apiKey, modelId) {
  try {
    var selectedModel = modelId || DEFAULT_GEMINI_MODEL_ID;
    var url = "https://generativelanguage.googleapis.com/v1beta/models/" + encodeURIComponent(selectedModel) + ":generateContent?key=" + apiKey;

    var systemInstruction = "You are Meshy AI, an expert transcription assistant for TaskMesh.\n" +
      "Listen to the user's spoken audio recording and transcribe it verbatim into English.\n" +
      "Return ONLY a JSON object matching this EXACT schema with NO Markdown formatting, NO ```json wrapping, and NO additional text:\n" +
      "{\n" +
      "  \"transcript\": \"Full verbatim transcript of user spoken audio\"\n" +
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
              text: "Please transcribe the attached audio recording."
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
      var cat = resCode === 503 ? "AI_HIGH_DEMAND" : (resCode === 429 ? "RESOURCE_EXHAUSTED" : "GEMINI_ERROR");
      return { success: false, httpStatus: resCode, category: cat, error: "Gemini API error HTTP " + resCode + ": " + response.getContentText() };
    }

    var resObj = JSON.parse(response.getContentText());
    var textContent = resObj.candidates[0].content.parts[0].text;

    textContent = textContent.replace(/^```json\s*/i, "").replace(/^```\s*/i, "").replace(/```$/s, "").trim();
    var resultData = JSON.parse(textContent);

    if (!resultData.transcript || resultData.transcript.trim() === "") {
      return { success: false, httpStatus: 400, category: "NO_SPEECH_DETECTED", error: "No spoken speech detected in recording." };
    }

    return { success: true, data: { transcript: resultData.transcript.trim() } };

  } catch (err) {
    return { success: false, httpStatus: 502, category: "UNKNOWN_ERROR", error: "Gemini transcription failed: " + err.toString() };
  }
}

/**
 * Evaluate Confirmed Transcript using Gemini API (100-Point Rubric with Strict Relevance Gate)
 */
function processGeminiDailySpeechEvaluation(transcript, dayNumber, topicTitle, studyContent, apiKey, modelId) {
  try {
    if (!transcript || transcript.trim() === "") {
      return { success: false, httpStatus: 400, category: "NO_SPEECH_DETECTED", error: "Transcript is empty or missing." };
    }

    var selectedModel = modelId || DEFAULT_GEMINI_MODEL_ID;
    var url = "https://generativelanguage.googleapis.com/v1beta/models/" + encodeURIComponent(selectedModel) + ":generateContent?key=" + apiKey;

    var systemInstruction = "You are Meshy AI, an expert English pronunciation, vocabulary, and spoken communication evaluator for TaskMesh.\n" +
      "Evaluate the user's confirmed spoken transcript for Day " + dayNumber + ": \"" + topicTitle + "\".\n\n" +
      "AUTHORITATIVE STUDY MATERIAL:\n" + JSON.stringify(studyContent, null, 2) + "\n\n" +
      "USER SPOKEN TRANSCRIPT:\n\"" + transcript + "\"\n\n" +
      "STRICT RELEVANCE & ACCURACY SCORING RULES:\n" +
      "1. Evaluate whether the user's transcript actually answers the assigned topic (\"" + topicTitle + "\") and covers relevant concepts in the study material.\n" +
      "2. If the user transcript is off-topic, unrelated, or contains generic test audio (e.g., 'This is sample audio', 'testing 123'), you MUST assign 0 points for Content Accuracy (out of 30) and 0 points for Completeness & Relevance (out of 20).\n" +
      "3. Award Vocabulary and Language Quality points ONLY for relevant, assessable evidence. Do NOT award generic points just because a sentence is grammatically correct English.\n" +
      "4. Do NOT infer, invent, or assume content that the user did not say.\n\n" +
      "Evaluate using this EXACT 100-Point Rubric:\n" +
      "1. Content Accuracy (Max 30 points)\n" +
      "2. Vocabulary & Word Choice (Max 25 points)\n" +
      "3. Language Quality & Grammar (Max 25 points)\n" +
      "4. Completeness & Relevance (Max 20 points)\n\n" +
      "Return ONLY a JSON object matching this EXACT schema with NO Markdown, NO ```json formatting, and NO additional text:\n" +
      "{\n" +
      "  \"totalScore\": number,\n" +
      "  \"maxScore\": 100,\n" +
      "  \"percentage\": number,\n" +
      "  \"overallSummary\": \"string\",\n" +
      "  \"scores\": {\n" +
      "    \"contentAccuracyScore\": number,\n" +
      "    \"contentAccuracyMax\": 30,\n" +
      "    \"vocabularyScore\": number,\n" +
      "    \"vocabularyMax\": 25,\n" +
      "    \"languageQualityScore\": number,\n" +
      "    \"languageQualityMax\": 25,\n" +
      "    \"completenessScore\": number,\n" +
      "    \"completenessMax\": 20,\n" +
      "    \"totalScore\": number,\n" +
      "    \"maxScore\": 100,\n" +
      "    \"percentage\": number\n" +
      "  },\n" +
      "  \"strengths\": [\"string\"],\n" +
      "  \"improvements\": [\"string\"],\n" +
      "  \"questionsFeedback\": [\n" +
      "    {\n" +
      "      \"questionNumber\": number,\n" +
      "      \"promptText\": \"string\",\n" +
      "      \"isCorrect\": boolean,\n" +
      "      \"score\": number,\n" +
      "      \"maxScore\": number,\n" +
      "      \"spokenEvidence\": \"string\",\n" +
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
              text: "Evaluate my spoken transcript and return JSON."
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
        temperature: 0.1
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
      var cat = resCode === 503 ? "AI_HIGH_DEMAND" : (resCode === 429 ? "RESOURCE_EXHAUSTED" : "GEMINI_ERROR");
      return { success: false, httpStatus: resCode, category: cat, error: "Gemini API error HTTP " + resCode + ": " + response.getContentText() };
    }

    var resObj = JSON.parse(response.getContentText());
    var textContent = resObj.candidates[0].content.parts[0].text;

    textContent = textContent.replace(/^```json\s*/i, "").replace(/^```\s*/i, "").replace(/```$/s, "").trim();

    var resultData = JSON.parse(textContent);

    var validated = validateEvaluationRubric(resultData, transcript, topicTitle);
    if (!validated.valid) {
      return { success: false, httpStatus: 502, category: "UNKNOWN_ERROR", error: "AI evaluation rubric validation failed" };
    }

    return { success: true, data: validated.data };

  } catch (err) {
    return { success: false, httpStatus: 502, category: "UNKNOWN_ERROR", error: "Gemini evaluation failed: " + err.toString() };
  }
}

/**
 * Validates 100-Point Rubric Category Bounds & Sum Equality with Server-Side Relevance Gate
 */
function validateEvaluationRubric(data, inputTranscript, topicTitle) {
  var rawTranscript = (inputTranscript || data.transcript || "").trim();
  if (!rawTranscript) {
    return { valid: false, error: "Transcript is empty or missing." };
  }

  var lowerTranscript = rawTranscript.toLowerCase();

  // Server-Side Relevance Gate: Check for off-topic or generic test audio
  var isOffTopic = lowerTranscript.indexOf("sample audio") !== -1 ||
                   lowerTranscript.indexOf("test test") !== -1 ||
                   lowerTranscript.indexOf("testing 123") !== -1 ||
                   lowerTranscript.indexOf("testing mic") !== -1 ||
                   rawTranscript.split(/\s+/).length < 3;

  var scores = data.scores || {};
  var ca = isOffTopic ? 0 : Math.min(Math.max(scores.contentAccuracyScore || 0, 0), 30);
  var voc = isOffTopic ? Math.min(scores.vocabularyScore || 0, 3) : Math.min(Math.max(scores.vocabularyScore || 0, 0), 25);
  var lq = isOffTopic ? Math.min(scores.languageQualityScore || 0, 5) : Math.min(Math.max(scores.languageQualityScore || 0, 0), 25);
  var comp = isOffTopic ? 0 : Math.min(Math.max(scores.completenessScore || 0, 0), 20);

  var calculatedTotal = ca + voc + lq + comp;
  var percentage = Math.round((calculatedTotal / 100) * 100);

  var summary = data.overallSummary || "Spoken evaluation complete.";
  if (isOffTopic) {
    summary = "The spoken response is off-topic or contains test audio. Content Accuracy and Completeness score 0/50.";
  }

  var formattedResult = {
    totalScore: calculatedTotal,
    maxScore: 100,
    percentage: percentage,
    transcript: rawTranscript, // Preserve exact verbatim transcript!
    overallSummary: summary,
    scores: {
      contentAccuracyScore: ca,
      contentAccuracyMax: 30,
      vocabularyScore: voc,
      vocabularyMax: 25,
      languageQualityScore: lq,
      languageQualityMax: 25,
      completenessScore: comp,
      completenessMax: 20,
      totalScore: calculatedTotal,
      maxScore: 100,
      percentage: percentage
    },
    strengths: isOffTopic ? ["Spoken audio recorded"] : (Array.isArray(data.strengths) ? data.strengths : ["Good spoken engagement"]),
    improvements: isOffTopic ? ["Please record a response addressing the topic: " + (topicTitle || "Daily Speech")] : (Array.isArray(data.improvements) ? data.improvements : ["Continue practicing daily prompts"]),
    questionsFeedback: Array.isArray(data.questionsFeedback) ? data.questionsFeedback.map(function(q) {
      return {
        questionNumber: q.questionNumber || 1,
        promptText: q.promptText || (topicTitle || "Topic Prompt"),
        isCorrect: isOffTopic ? false : (q.isCovered || false),
        score: isOffTopic ? 0 : (q.score || 0),
        userSpokenText: rawTranscript,
        feedbackText: isOffTopic ? "Off-topic response." : (q.feedbackText || ""),
        missingPoints: isOffTopic ? "Address the daily speech topic and concepts." : (q.missingPoints || "")
      };
    }) : []
  };

  return { valid: true, data: formattedResult };
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
      return { allowed: false, message: "Daily AI review limit reached (" + MAX_DAILY_REVIEWS_PER_USER + " reviews/day). Try again tomorrow!" };
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
 * Evaluates Audio against Dynamic Answer Key using Gemini Flash REST API
 */
function processGeminiEvaluation(audioBase64, mimeType, answerKey, apiKey, modelId) {
  try {
    var selectedModel = modelId || DEFAULT_GEMINI_MODEL_ID;
    var url = "https://generativelanguage.googleapis.com/v1beta/models/" + encodeURIComponent(selectedModel) + ":generateContent?key=" + apiKey;

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
      var cat = resCode === 503 ? "AI_HIGH_DEMAND" : (resCode === 429 ? "RESOURCE_EXHAUSTED" : "GEMINI_ERROR");
      return { success: false, httpStatus: resCode, category: cat, error: "Gemini API error HTTP " + resCode + ": " + response.getContentText() };
    }

    var resObj = JSON.parse(response.getContentText());
    var textContent = resObj.candidates[0].content.parts[0].text;

    textContent = textContent.replace(/^```json\s*/i, "").replace(/^```\s*/i, "").replace(/```$/s, "").trim();

    var resultData = JSON.parse(textContent);

    var validated = validateAndFormatResultData(resultData, answerKey);
    return { success: true, data: validated };

  } catch (err) {
    return { success: false, httpStatus: 502, category: "UNKNOWN_ERROR", error: "Gemini evaluation failed: " + err.toString() };
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
