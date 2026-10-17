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

/**
 * HTTP POST Request Handler
 * Requires a valid Firebase ID token.
 */
function doPost(e) {
  try {
    if (!e || !e.postData || !e.postData.contents) {
      return createJsonResponse({ error: "Invalid request payload." });
    }

    var requestData;

    try {
      requestData = JSON.parse(e.postData.contents);
    } catch (parseError) {
      return createJsonResponse({ error: "Invalid JSON payload." });
    }

    if (!requestData || typeof requestData !== "object" ||
        Array.isArray(requestData)) {
      return createJsonResponse({ error: "Invalid request payload." });
    }

    // Authentication is mandatory.
    var idToken = requestData.idToken;

    if (typeof idToken !== "string" ||
        idToken.trim().length === 0 ||
        idToken.length > 10000) {
      return createJsonResponse({
        error: "Authentication required. Please sign in again."
      });
    }

    var verifiedAuth = verifyFirebaseIdToken(idToken.trim());

    if (!verifiedAuth ||
        verifiedAuth.valid !== true ||
        typeof verifiedAuth.uid !== "string" ||
        !verifiedAuth.uid) {
      return createJsonResponse({
        error: "Authentication failed. Please sign in again."
      });
    }

    var uid = verifiedAuth.uid;

    // Validate before consuming the daily review allowance.
    var validation = validateRequestPayload(requestData);

    if (!validation || validation.valid !== true) {
      return createJsonResponse({
        error: "Invalid review request. Check the recording and answer key."
      });
    }

    var rateLimitCheck = enforceUserRateLimit(uid);

    if (!rateLimitCheck || rateLimitCheck.allowed !== true) {
      return createJsonResponse({
        error: "Daily review limit reached or server is busy. Please retry later."
      });
    }

    var apiKey = PropertiesService.getScriptProperties()
      .getProperty("GEMINI_API_KEY");

    if (!apiKey) {
      return createJsonResponse({
        error: "AI review service is not configured."
      });
    }

    var result = processGeminiEvaluation(
      requestData.audioBase64,
      requestData.mimeType,
      requestData.answerKey,
      apiKey
    );

    if (!result || result.success !== true || !result.data) {
      return createJsonResponse({
        error: "AI evaluation failed. Please try again later."
      });
    }

    return createJsonResponse(result.data);

  } catch (err) {
    // Keep internal errors and secrets out of client responses.
    console.error("Meshy AI review request failed: " + String(err));

    return createJsonResponse({
      error: "An internal server error occurred."
    });
  }
}

/**
 * Concurrency-Safe Daily Rate Limiter using LockService and PropertiesService
 */

function enforceUserRateLimit(uid) {
  var lock = LockService.getScriptLock();
  var lockAcquired = false;

  try {
    lockAcquired = lock.tryLock(5000);

    if (!lockAcquired) {
      return {
        allowed: false,
        message: "Server busy. Please retry shortly."
      };
    }

    var scriptProps = PropertiesService.getScriptProperties();
    var todayStr = Utilities.formatDate(
      new Date(),
      "UTC",
      "yyyy-MM-dd"
    );

    var rateKey = "ratelimit_" + uid + "_" + todayStr;
    var currentCount = parseInt(
      scriptProps.getProperty(rateKey) || "0",
      10
    );

    if (!Number.isFinite(currentCount) || currentCount < 0) {
      return {
        allowed: false,
        message: "Rate limit service unavailable."
      };
    }

    if (currentCount >= MAX_DAILY_REVIEWS_PER_USER) {
      return {
        allowed: false,
        message: "Daily AI review limit reached. Try again tomorrow."
      };
    }

    scriptProps.setProperty(rateKey, String(currentCount + 1));

    return { allowed: true };

  } catch (err) {
    console.error("Rate limiter failed: " + String(err));

    return {
      allowed: false,
      message: "Rate limit service temporarily unavailable."
    };

  } finally {
    if (lockAcquired) {
      lock.releaseLock();
    }
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
