
/**
 * TASKMESH — MESHY AI REVIEW
 *
 * Current prototype:
 * - Reads a private Google Drive audio file.
 * - Transcribes the recording using Gemini.
 * - Evaluates Day 12 vocabulary against its answer key.
 * - Caches the transcript so review retries can reuse it.
 *
 * IMPORTANT:
 * - Keep GEMINI_API_KEY in Script Properties.
 * - Do not expose this script or API key publicly.
 * - Day 12 answer key is temporary until Android dataset integration.
 */

const MESHY_CONFIG = {
  MODEL: "gemini-3.8-flash",
  AUDIO_FILE_ID: "17gk3ijS7lX8C7DHsyr_gvK--QAw3BKkB",
  MAX_AUDIO_BYTES: 5 * 1024 * 1024,
  MAX_RETRIES: 1,
  TRANSCRIPT_CACHE_KEY: "MESHY_DAY12_TRANSCRIPT_V1"
};

const DAY12_ANSWER_KEY = [
  {
    word: "Senility",
    expected:
      "Mental and physical decline due to old age, especially memory loss and confusion."
  },
  {
    word: "Imbecility",
    expected:
      "Extreme stupidity or very low intelligence."
  },
  {
    word: "Dotage",
    expected:
      "Extreme old age when a person behaves like a fool."
  },
  {
    word: "Superannuation",
    expected:
      "Retirement from a job because of reaching a certain age."
  },
  {
    word: "Empathy",
    expected:
      "The ability to understand and share another person's feelings."
  },
  {
    word: "Inevitable",
    expected:
      "Certain to happen; impossible to avoid or prevent."
  }
];


/**
 * MAIN FUNCTION
 * Select this function in Apps Script and click Run.
 */
function meshyAIReview() {
  const apiKey = getMeshyApiKey();

  // Reuse transcript if a previous run already transcribed the audio.
  let transcript = getCachedTranscript();

  if (!transcript) {
    Logger.log("Step 1: Transcribing actual Drive audio...");

    transcript = transcribeDriveAudio(apiKey);

    // Save transcript before requesting evaluation.
    saveCachedTranscript(transcript);

    Logger.log("Step 1 complete: Transcript cached.");
  } else {
    Logger.log("Using cached transcript; audio will not be retranscribed.");
  }

  Logger.log("Step 2: Evaluating vocabulary with Meshy AI...");

  const review = evaluateTranscript(
    transcript,
    DAY12_ANSWER_KEY,
    apiKey
  );

  validateReview(review, DAY12_ANSWER_KEY);

  Logger.log("MESHy AI REVIEW RESULT");
  Logger.log(JSON.stringify(review, null, 2));

  return review;
}


/**
 * Test API connection without processing audio.
 */
function testGeminiConnection() {
  const apiKey = getMeshyApiKey();

  const result = callGemini(
    [
      {
        text: "Reply with exactly: TaskMesh Gemini connection successful"
      }
    ],
    apiKey,
    {}
  );

  Logger.log("Gemini response: " + result);
}


/**
 * Transcribe the private Drive audio.
 */
function transcribeDriveAudio(apiKey) {
  const file = DriveApp.getFileById(
    MESHY_CONFIG.AUDIO_FILE_ID
  );

  const blob = file.getBlob();
  const bytes = blob.getBytes();

  if (bytes.length > MESHY_CONFIG.MAX_AUDIO_BYTES) {
    throw new Error("Audio file 5 MB se badi hai.");
  }

  const mimeType = blob.getContentType() || "audio/ogg";

  const prompt = [
    "Transcribe this spoken English vocabulary lesson accurately.",
    "Return only the words actually spoken.",
    "Preserve the vocabulary words and explanations.",
    "Do not summarize or invent missing speech.",
    "Mark unclear speech as [unclear]."
  ].join(" ");

  const result = callGemini(
    [
      { text: prompt },
      {
        inlineData: {
          mimeType: mimeType,
          data: Utilities.base64Encode(bytes)
        }
      }
    ],
    apiKey,
    { temperature: 0 }
  );

  const transcript = String(result || "").trim();

  if (!transcript) {
    throw new Error("Gemini ne transcript return nahi ki.");
  }

  Logger.log("TRANSCRIPTION COMPLETE");
  Logger.log("Transcript length: " + transcript.length);

  return transcript;
}


/**
 * Evaluate the transcript against the selected answer key.
 */
function evaluateTranscript(transcript, answerKey, apiKey) {
  const prompt = `
You are Meshy AI, an English vocabulary speaking evaluator.

Evaluate only the definitions actually spoken in the transcript
against the supplied answer key.

SCORING:
- Give each word an integer score from 0 to 10.
- Judge semantic correctness, important details and clarity.
- Ignore minor grammar mistakes and filler words.
- Do not require exact wording if the meaning is correct.
- Do not award points for details the student did not say.
- Do not invent spoken content.
- If a definition is not present or cannot be assessed, say so
  in feedback and do not guess.

Return ONLY valid JSON in this exact structure:
{
  "results": [
    {
      "word": "string",
      "score": 0,
      "maxScore": 10,
      "feedback": "string",
      "missingPoints": ["string"]
    }
  ],
  "totalScore": 0,
  "maxTotalScore": ${answerKey.length * 10},
  "overallFeedback": "string"
}

Return exactly ${answerKey.length} results, in the same order as
the answer key. Use the exact word spellings from the answer key.
All scores must be integers from 0 to 10.
totalScore must equal the sum of all individual scores.

ANSWER KEY:
${JSON.stringify(answerKey)}

ACTUAL AUDIO TRANSCRIPT:
${transcript}
`;

  const output = callGemini(
    [{ text: prompt }],
    apiKey,
    {
      temperature: 0,
      responseMimeType: "application/json"
    }
  );

  try {
    return JSON.parse(output);
  } catch (error) {
    throw new Error(
      "Gemini review response valid JSON nahi hai. " +
      String(error.message || error)
    );
  }
}


/**
 * Validate the response before showing a score.
 */
function validateReview(review, answerKey) {
  if (
    !review ||
    !Array.isArray(review.results) ||
    review.results.length !== answerKey.length
  ) {
    throw new Error("Review mein expected number of results nahi hain.");
  }

  let calculatedTotal = 0;

  for (let i = 0; i < answerKey.length; i++) {
    const item = review.results[i];

    if (item.word !== answerKey[i].word) {
      throw new Error("Word order/name mismatch at result " + (i + 1));
    }

    if (
      !Number.isInteger(item.score) ||
      item.score < 0 ||
      item.score > 10
    ) {
      throw new Error("Invalid score for " + item.word);
    }

    item.maxScore = 10;

    if (typeof item.feedback !== "string") {
      throw new Error("Feedback missing for " + item.word);
    }

    if (!Array.isArray(item.missingPoints)) {
      throw new Error("Missing points invalid for " + item.word);
    }

    calculatedTotal += item.score;
  }

  review.maxTotalScore = answerKey.length * 10;

  if (calculatedTotal !== review.totalScore) {
    throw new Error("Total score individual scores se match nahi karta.");
  }

  if (typeof review.overallFeedback !== "string") {
    throw new Error("Overall feedback missing hai.");
  }
}


/**
 * Call Gemini with a limited retry for temporary server/rate errors.
 */
function callGemini(parts, apiKey, generationConfig) {
  const url =
    "https://generativelanguage.googleapis.com/v1beta/models/" +
    MESHY_CONFIG.MODEL +
    ":generateContent";

  let lastError = "";

  for (
    let attempt = 0;
    attempt <= MESHY_CONFIG.MAX_RETRIES;
    attempt++
  ) {
    const response = UrlFetchApp.fetch(url, {
      method: "post",
      contentType: "application/json",
      headers: {
        "x-goog-api-key": apiKey
      },
      payload: JSON.stringify({
        contents: [{ parts: parts }],
        generationConfig: generationConfig
      }),
      muteHttpExceptions: true
    });

    const status = response.getResponseCode();
    const body = response.getContentText();

    if (status >= 200 && status < 300) {
      const data = JSON.parse(body);
      const candidate = data.candidates &&
        data.candidates[0];

      const textParts = candidate &&
        candidate.content &&
        candidate.content.parts;

      const output = textParts
        ? textParts.map(function(part) {
            return part.text || "";
          }).join("").trim()
        : "";

      if (!output) {
        throw new Error(
          "Gemini response mein usable text nahi mila."
        );
      }

      return output;
    }

    lastError =
      "Gemini HTTP " + status + ": " + body.substring(0, 800);

    const retryable = [
      429, 500, 502, 503, 504
    ].indexOf(status) !== -1;

    if (
      !retryable ||
      attempt >= MESHY_CONFIG.MAX_RETRIES
    ) {
      throw new Error(lastError);
    }

    Logger.log(
      "Temporary Gemini error " + status +
      ". One retry will be attempted."
    );

    Utilities.sleep(2000);
  }

  throw new Error(lastError || "Gemini request failed.");
}


/**
 * API key comes only from Script Properties.
 */
function getMeshyApiKey() {
  const key = PropertiesService
    .getScriptProperties()
    .getProperty("GEMINI_API_KEY");

  if (!key) {
    throw new Error(
      "GEMINI_API_KEY Script Property missing hai."
    );
  }

  return key;
}


/**
 * Cache transcript for reuse after a failed review.
 * CacheService is temporary; it is not permanent storage.
 */
function getCachedTranscript() {
  return CacheService
    .getScriptCache()
    .get(MESHY_CONFIG.TRANSCRIPT_CACHE_KEY);
}


function saveCachedTranscript(transcript) {
  CacheService
    .getScriptCache()
    .put(
      MESHY_CONFIG.TRANSCRIPT_CACHE_KEY,
      transcript,
      21600
    );
}


/**
 * Clear cache only if you want to force a fresh transcription.
 */
function clearMeshyTranscriptCache() {
  CacheService
    .getScriptCache()
    .remove(MESHY_CONFIG.TRANSCRIPT_CACHE_KEY);

  Logger.log("Cached transcript cleared.");
}
