package com.frqme.just_let_me_do_it.client;
import com.frqme.just_let_me_do_it.network.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
public class GolemDialogScreen extends Screen {
    private final int entityId;
    private TextFieldWidget promptField;
    private TextFieldWidget apiKeyField;
    private boolean showApiKey = false;
    private int selectedModel = 0;
    private static final String[] MODEL_NAMES = {
            "Claude Sonnet 4",
            "Claude Haiku 3.5",
            "Gemini 2.5 Flash",
            "Gemini 2.5 Pro"
    };
    private static final String[] MODEL_IDS = {
            "anthropic/claude-sonnet-4",
            "anthropic/claude-3.5-haiku",
            "google/gemini-2.5-flash",
            "google/gemini-2.5-pro"
    };
    private static final int COPPER   = 0xFFB87333;
    private static final int DARK_BG  = 0xCC1A0F07;
    private static final int LIGHT    = 0xFFE8D5B7;
    private static final int GREEN    = 0xFF55FF55;
    public GolemDialogScreen(int entityId) {
        super(Text.literal("Copper Golem"));
        this.entityId = entityId;
    }
    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.promptField = new TextFieldWidget(this.textRenderer, cx - 150, cy - 10, 300, 20, Text.literal("Prompt"));
        this.promptField.setMaxLength(512);
        this.promptField.setPlaceholder(Text.literal("e.g. build me a cozy wooden cabin with a chimney..."));
        this.promptField.setFocused(true);
        this.addSelectableChild(this.promptField);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Let's Build!"), btn -> sendPrompt())
                .dimensions(cx - 75, cy + 20, 150, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Model: " + MODEL_NAMES[selectedModel]), btn -> {
            selectedModel = (selectedModel + 1) % MODEL_NAMES.length;
            btn.setMessage(Text.literal("Model: " + MODEL_NAMES[selectedModel]));
        }).dimensions(cx - 100, cy + 45, 200, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Set API Key"), btn -> toggleApiKey())
                .dimensions(cx - 75, cy + 70, 150, 20).build());
        this.apiKeyField = new TextFieldWidget(this.textRenderer, cx - 150, cy + 95, 300, 20, Text.literal("API Key"));
        this.apiKeyField.setMaxLength(256);
        this.apiKeyField.setPlaceholder(Text.literal("Paste your OpenRouter API key here"));
        this.apiKeyField.setVisible(false);
        this.addSelectableChild(this.apiKeyField);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save Key"), btn -> saveApiKey())
                .dimensions(cx - 75, cy + 120, 150, 20).build());
        String[] examples = {"cozy wooden cabin", "medieval watchtower", "japanese garden", "modern fountain"};
        for (int i = 0; i < examples.length; i++) {
            final String ex = examples[i];
            int col = i % 2, row = i / 2;
            this.addDrawableChild(ButtonWidget.builder(Text.literal("> " + ex), btn -> {
                this.promptField.setText(ex);
                this.promptField.setFocused(true);
            }).dimensions(cx - 155 + col * 160, cy - 80 + row * 22, 150, 18).build());
        }
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> this.close())
                .dimensions(cx - 30, cy + 145, 60, 18).build());
    }
    private void toggleApiKey() {
        showApiKey = !showApiKey;
        this.apiKeyField.setVisible(showApiKey);
        if (showApiKey) this.apiKeyField.setFocused(true);
    }
    private void sendPrompt() {
        String prompt = this.promptField.getText().trim();
        if (prompt.isEmpty()) return;
        ClientPlayNetworking.send(new ModPackets.SetModelPayload(entityId, MODEL_IDS[selectedModel]));
        ClientPlayNetworking.send(new ModPackets.SendPromptPayload(entityId, prompt));
        this.close();
    }
    private void saveApiKey() {
        String key = this.apiKeyField.getText().trim();
        if (key.isEmpty()) return;
        ClientPlayNetworking.send(new ModPackets.SetApiKeyPayload(entityId, key));
        this.apiKeyField.setText("");
        showApiKey = false;
        this.apiKeyField.setVisible(false);
    }
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int cx = this.width / 2, cy = this.height / 2;
        ctx.fillGradient(0, 0, this.width, this.height, 0xAA000000, 0xCC0A0500);
        int pw = 380, ph = 320, px = cx - pw / 2, py = cy - ph / 2 - 20;
        ctx.fill(px, py, px + pw, py + ph, DARK_BG);
        ctx.fill(px, py, px + pw, py + 3, COPPER);
        ctx.fill(px, py + ph - 3, px + pw, py + ph, COPPER);
        ctx.fill(px, py, px + 3, py + ph, COPPER);
        ctx.fill(px + pw - 3, py, px + pw, py + ph, COPPER);
        String title = "COPPER GOLEM - AI Builder";
        ctx.drawText(this.textRenderer, title, cx - this.textRenderer.getWidth(title) / 2, py + 10, COPPER, false);
        String sub = "Describe what you want me to build!";
        ctx.drawText(this.textRenderer, sub, cx - this.textRenderer.getWidth(sub) / 2, py + 25, LIGHT, false);
        ctx.fill(px + 10, py + 38, px + pw - 10, py + 39, COPPER);
        ctx.drawText(this.textRenderer, "Quick ideas:", px + 15, cy - 95, 0xFFAAAAAA, false);
        ctx.drawText(this.textRenderer, "Your prompt:", px + 15, cy - 25, LIGHT, false);
        this.promptField.render(ctx, mouseX, mouseY, delta);
        if (showApiKey) {
            ctx.drawText(this.textRenderer, "OpenRouter API Key:", px + 15, cy + 83, LIGHT, false);
            this.apiKeyField.render(ctx, mouseX, mouseY, delta);
        }
        String tip1 = "Tip: Be descriptive! 'cozy cabin with stone chimney' > 'house'";
        ctx.drawText(this.textRenderer, tip1, cx - this.textRenderer.getWidth(tip1) / 2, py + ph - 28, 0xFF888888, false);
        String tip2 = "Get your API key at openrouter.ai/keys";
        ctx.drawText(this.textRenderer, tip2, cx - this.textRenderer.getWidth(tip2) / 2, py + ph - 15, 0xFF555555, false);
        super.render(ctx, mouseX, mouseY, delta);
    }
    @Override
    public boolean shouldPause() { return false; }
}
