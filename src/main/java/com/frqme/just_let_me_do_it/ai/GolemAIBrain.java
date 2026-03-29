package com.frqme.just_let_me_do_it.ai;

import com.frqme.just_let_me_do_it.CopperGolemEntity;
import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class GolemAIBrain {
    private final CopperGolemEntity golem;
    private String apiKey;
    private String modelId = "anthropic/claude-sonnet-4";

    public static final String MODEL_CLAUDE_SONNET = "anthropic/claude-sonnet-4";
    public static final String MODEL_CLAUDE_HAIKU  = "anthropic/claude-3.5-haiku";
    public static final String MODEL_GEMINI_FLASH  = "google/gemini-2.5-flash";
    public static final String MODEL_GEMINI_PRO    = "google/gemini-2.5-pro";

    public record BuildStep(int x, int y, int z, String blockId, Map<String, String> properties) {
        public BuildStep(int x, int y, int z, String blockId) {
            this(x, y, z, blockId, new HashMap<>());
        }
    }

    public GolemAIBrain(CopperGolemEntity golem) {
        this.golem = golem;
    }

    public String getApiKey()  { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public List<BuildStep> processPrompt(String prompt, BlockPos origin) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("No API key set! Right-click me, hit 'Set API Key', and paste your OpenRouter key.");
        }
        String systemPrompt   = buildSystemPrompt();
        String enhancedPrompt = enhancePrompt(prompt);
        String response       = callOpenRouter(systemPrompt, enhancedPrompt);
        return parseBuildSteps(response, origin);
    }

    private String enhancePrompt(String userPrompt) {
        return userPrompt + """

REMINDER: Output ONLY the raw JSON array. 
Include ALL blockstate properties (facing, axis, half, shape, etc.) on every block that has them. 
Generate a complete, high-quality build without omitting structural or decorative details.
""";
    }

    private String buildSystemPrompt() {
        return """
You are MasterBuilder-9000, an elite Minecraft construction AI. 
Your sole purpose is to transform natural language descriptions into precise, 
fully-functional Minecraft structures represented as JSON data.

╔══════════════════════════════════════════════════════════════════════════════╗
║  ABSOLUTE OUTPUT RULE — NEVER VIOLATE                                       ║
║  Output ONLY a raw JSON array.                                               ║
║  NO markdown. NO ```json fences. NO prose. NO explanations.                 ║
║  First character MUST be '['. Last character MUST be ']'.                    ║
╚══════════════════════════════════════════════════════════════════════════════╝

JSON format per block:
{"x":INT,"y":INT,"z":INT,"block":"minecraft:block_name","facing":"north","half":"bottom","powered":false}

Include every property that applies (facing, axis, half, type, shape, powered, open, lit, delay, etc.).

COORDINATES:
• Origin (0,0,0) = golem's feet / ground level.
• Y=0 = foundation/ground surface.
• Y+ = Up, X+ = East, Z+ = South.
• Sort output by Y ascending, then X, then Z.
""";
    }

    private String callOpenRouter(String systemPrompt, String userPrompt) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        JsonObject message1 = new JsonObject();
        message1.addProperty("role", "system");
        message1.addProperty("content", systemPrompt);

        JsonObject message2 = new JsonObject();
        message2.addProperty("role", "user");
        message2.addProperty("content", userPrompt);

        JsonArray messages = new JsonArray();
        messages.add(message1);
        messages.add(message2);

        JsonObject body = new JsonObject();
        body.addProperty("model", modelId);
        body.add("messages", messages);
        body.addProperty("max_tokens", 8000);
        body.addProperty("temperature", 0.3);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("[https://openrouter.ai/api/v1/chat/completions](https://openrouter.ai/api/v1/chat/completions)"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", "[https://github.com/naves/just_let_me_do_it](https://github.com/naves/just_let_me_do_it)")
                .header("X-Title", "Copper Golem Builder")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .timeout(Duration.ofSeconds(240))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("API Error " + response.statusCode() + ": " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (json.has("error")) {
            throw new Exception("OpenRouter error: " + json.get("error").toString());
        }

        return json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }

    private List<BuildStep> parseBuildSteps(String response, BlockPos origin) {
        List<BuildStep> steps = new ArrayList<>();
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
        else if (cleaned.startsWith("```"))  cleaned = cleaned.substring(3);
        if (cleaned.endsWith("```"))         cleaned = cleaned.substring(0, cleaned.length() - 3);
        cleaned = cleaned.trim();

        int start = cleaned.indexOf('[');
        int end   = cleaned.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) {
            return steps;
        }
        cleaned = cleaned.substring(start, end + 1);

        try {
            JsonArray arr = JsonParser.parseString(cleaned).getAsJsonArray();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();

                String blockKey = obj.has("block") ? "block" : obj.has("blockId") ? "blockId" : null;
                if (blockKey == null) continue;
                if (!obj.has("x") || !obj.has("y") || !obj.has("z")) continue;

                int x = obj.get("x").getAsInt() + origin.getX();
                int y = obj.get("y").getAsInt() + origin.getY();
                int z = obj.get("z").getAsInt() + origin.getZ();

                String block = obj.get(blockKey).getAsString().toLowerCase().trim();
                if (!block.contains(":")) block = "minecraft:" + block;

                Map<String, String> props = new HashMap<>();

                for (String propsKey : new String[]{"props", "properties", "state", "blockstate"}) {
                    if (obj.has(propsKey) && obj.get(propsKey).isJsonObject()) {
                        for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject(propsKey).entrySet()) {
                            props.put(e.getKey().toLowerCase(), e.getValue().getAsString().toLowerCase());
                        }
                        break;
                    }
                }

                for (String propKey : new String[]{
                        "facing", "axis", "half", "type", "shape",
                        "powered", "open", "waterlogged", "delay", "mode",
                        "face", "lit", "part", "occupied", "extended",
                        "triggered", "enabled", "inverted", "locked"
                }) {
                    if (obj.has(propKey)) {
                        props.put(propKey, obj.get(propKey).getAsString().toLowerCase());
                    }
                }
                steps.add(new BuildStep(x, y, z, block, props));
            }

            steps.sort((a, b) -> {
                if (a.y() != b.y()) return Integer.compare(a.y(), b.y());
                if (a.x() != b.x()) return Integer.compare(a.x(), b.x());
                return Integer.compare(a.z(), b.z());
            });
        } catch (Exception e) {
            System.err.println("[GolemAI] Parse error: " + e.getMessage());
        }
        return steps;
    }
}