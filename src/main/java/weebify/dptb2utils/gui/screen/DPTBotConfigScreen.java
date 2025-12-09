package weebify.dptb2utils.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.mixin.DrawContextInvoker;
import weebify.dptb2utils.utils.ExternalIndicatorManager;

import java.io.File;

public class DPTBotConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    private EditBoxWidget hostInput;
    private EditBoxWidget portInput;
    private boolean showIPOptions = false;
    private boolean showError = false;
    private EditBoxWidget discColorInput;
    private EditBoxWidget wptbColorInput;

    public DPTBotConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Text.literal("DPTBot Settings"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("DPTBot Connection: %s", mod.getBoolConfig("others.discordRamper") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("DPTBot Connection: %s", mod.toggleBoolConfig("others.discordRamper") ? "ON" : "OFF")));
            mod.refreshRamperStatus();
        }).dimensions(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Advanced Options: %s", this.showIPOptions ? "ON" : "OFF")), (btn) -> {
            this.showIPOptions = !this.showIPOptions;
            btn.setMessage(Text.of(String.format("Advanced Options: %s", this.showIPOptions ? "ON" : "OFF")));
            this.hostInput.visible = this.showIPOptions;
            this.portInput.visible = this.showIPOptions;
        }).dimensions(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Broadcast Notifs: %s", mod.getBoolConfig("others.broadcastToast") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Broadcast Notifs: %s", mod.toggleBoolConfig("others.broadcastToast") ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Broadcast Chat: %s", mod.getBoolConfig("others.broadcastChat") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Broadcast Chat: %s", mod.toggleBoolConfig("others.broadcastChat") ? "ON" : "OFF")));
        }).dimensions(this.width/2 + 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Reset Indicator Image"), (btn) -> {
            this.showError = false;
            this.mod.setStringConfig("others.indicatorPath", this.mod.config.getDefaultConfig("others.indicatorPath"));
            ExternalIndicatorManager.image = null;
        }).dimensions(this.width/2 - 80 - 75, 125, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Choose Indicator Image"), (btn) -> {
            this.showError = false;
            new Thread(this::chooseFile).start();
        }).dimensions(this.width/2 + 80 - 75, 125, 150, 20).build());

//        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Private Chat: %s", mod.getBoolConfig("others.incognito") ? "ON" : "OFF")), (btn) -> {
//            btn.setMessage(Text.of(String.format("Private Chat: %s", mod.toggleBoolConfig("others.incognito") ? "ON" : "OFF")));
//        }).dimensions(this.width/2 - 80 - 75, 150, 150, 20).build());

        this.discColorInput = EditBoxWidget.builder().x(this.width / 2 - 80 - 75).y(150).placeholder(Text.of("[DISC] Color")).build(this.textRenderer, 150, 20, Text.of(mod.getStringConfig("others.discColor")));
        this.discColorInput.setText(mod.getStringConfig("others.discColor"));
        this.addDrawableChild(this.discColorInput);

        this.wptbColorInput = EditBoxWidget.builder().x(this.width / 2 - 80 - 75).y(175).placeholder(Text.of("[WPTB] Color")).build(this.textRenderer, 150, 20, Text.of(mod.getStringConfig("others.wptbColor")));
        this.wptbColorInput.setText(mod.getStringConfig("others.wptbColor"));
        this.addDrawableChild(this.wptbColorInput);

        this.hostInput = EditBoxWidget.builder().x(this.width / 2 - 80 - 75).y(200).placeholder(Text.of("Websocket Host")).build(this.textRenderer, 150, 20, Text.of(mod.getStringConfig("others.dptbotHost")));
        this.hostInput.setText(mod.getStringConfig("others.dptbotHost"));
        this.hostInput.visible = false;
        this.addDrawableChild(this.hostInput);

        this.portInput = EditBoxWidget.builder().x(this.width / 2 + 80 - 75).y(200).placeholder(Text.of("Websocket Port")).build(this.textRenderer, 150, 20, Text.of(Integer.toString(mod.getIntConfig("others.dptbotPort"))));
        this.portInput.setText(Integer.toString(mod.getIntConfig("others.dptbotPort")));
        this.portInput.visible = false;
        this.addDrawableChild(this.portInput);


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            this.saveIPSettings();
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    public void chooseFile() {
        File selected = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.png"));
            filters.flip();

            String path = TinyFileDialogs.tinyfd_openFileDialog(
                    "Select IndicatorImage",
                    SystemUtils.getUserHome().getAbsolutePath(),
                    filters,
                    "PNG Images (*.png)",
                    false
            );

            if (path != null && !path.trim().isEmpty()) {
                selected = new File(path);
            }
        }

        DPTB2Utils.LOGGER.info("Selected indicator file: {}", selected != null ? selected.getAbsolutePath() : "None");

        File finalSelected = selected;
        MinecraftClient.getInstance().execute(() -> {
            if (finalSelected != null && ExternalIndicatorManager.registerExternal(finalSelected)) {
                String name = finalSelected.getName();
                if (mod.getStringConfig("others.indicatorPath").startsWith("external/")  && !mod.getStringConfig("others.indicatorPath").equals("external/" + name)) {
                    ExternalIndicatorManager.unregisterTexture(Identifier.of(DPTB2Utils.MOD_ID, mod.getStringConfig("others.indicatorPath")));
                }
                this.mod.setStringConfig("others.indicatorPath", "external/" + name);
            } else {
                this.showError = true;
            }
        });
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.saveIPSettings();
        this.mod.saveSettings();
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean res = super.keyPressed(keyCode, scanCode, modifiers);
        this.saveIPSettings();
        return res;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, Colors.WHITE);

        context.drawTextWithShadow(this.textRenderer, Text.of("§8[§xDISC§8] §xWeebify§f: Example Discord broadcast!"), this.width/2 + 5, 154, DPTB2Utils.hexToInt(this.discColorInput.getText()));
        context.drawTextWithShadow(this.textRenderer, Text.of("§8[§yWPTB§8] §yWeebify§f: Example WPTB client broadcast!"), this.width/2 + 5, 179, DPTB2Utils.hexToInt(this.wptbColorInput.getText()));

        if (this.showError) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Error loading custom indicator image:" + ExternalIndicatorManager.errorMessage), this.width/2, this.height - 70, Colors.RED);
        }

        ((DrawContextInvoker)context).invokeDrawTexturedQuad(RenderPipelines.GUI_TEXTURED, Identifier.of(DPTB2Utils.MOD_ID, this.mod.getStringConfig("others.indicatorPath")), this.width/2 + 160, this.width/2 + 180, 125, 145, 0.f, 1.f, 0.f, 1.f, Colors.WHITE);
    }

    private void saveIPSettings() {
        mod.setStringConfig("others.dptbotHost", this.hostInput.getText());
        try {
            mod.setIntConfig("others.dptbotPort", Integer.parseInt(this.portInput.getText()));
        } catch (NumberFormatException e) {
            // Handle invalid port input
        }

        if (DPTB2Utils.hexToInt(this.discColorInput.getText()) != 0) {
            mod.setStringConfig("others.discColor", this.discColorInput.getText());
        }
        if (DPTB2Utils.hexToInt(this.wptbColorInput.getText()) != 0) {
            mod.setStringConfig("others.wptbColor", this.wptbColorInput.getText());
        }
    }
}
