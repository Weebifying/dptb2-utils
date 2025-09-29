package weebify.dptb2utils.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.mixin.DrawContextInvoker;
import weebify.dptb2utils.utils.ExternalIndicatorManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;

public class DPTBotConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    private EditBoxWidget host;
    private EditBoxWidget port;
    private boolean showIPOptions = false;
    private boolean showError = false;

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
            this.host.visible = this.showIPOptions;
            this.port.visible = this.showIPOptions;
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
        }).dimensions(this.width/2 - 150 - 5, 125, 135, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Choose Indicator Image"), (btn) -> {
            this.showError = false;
            new Thread(this::chooseFile).start();
        }).dimensions(this.width/2 - 15 + 5, 125, 135, 20).build());

        this.host = new EditBoxWidget(this.textRenderer, this.width / 2 - 80 - 75, 150, 150, 20, Text.of("Websocket Host"), Text.empty());
        this.host.setText(mod.getStringConfig("others.dptbotHost"));
        this.host.visible = false;
        this.addDrawableChild(this.host);

        this.port = new EditBoxWidget(this.textRenderer, this.width / 2 + 80 - 75, 150, 150, 20, Text.of("Websocket Port"), Text.empty());
        this.port.setText(Integer.toString(mod.getIntConfig("others.dptbotPort")));
        this.port.visible = false;
        this.addDrawableChild(this.port);


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            this.saveIPSettings();
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    public void chooseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Indicator Image");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileNameExtensionFilter(".PNG files", "png"));
        fc.setMultiSelectionEnabled(false);

        int result = fc.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();
            if (ExternalIndicatorManager.registerExternal(selected)) {
                String name = selected.getName();
                if (mod.getStringConfig("others.indicatorPath").startsWith("external/")) {
                    ExternalIndicatorManager.unregisterTexture(Identifier.of(DPTB2Utils.MOD_ID, mod.getStringConfig("others.indicatorPath")));
                }
                this.mod.setStringConfig("others.indicatorPath", "external/" + name);
            } else {
                this.showError = true;
            }
        }
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
        this.saveIPSettings();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, Colors.WHITE);

        if (this.showError) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Error loading custom indicator image:" + ExternalIndicatorManager.errorMessage), this.width/2, this.height - 70, Colors.RED);
        }

        ((DrawContextInvoker)context).invokeDrawTexturedQuad(RenderLayer::getGuiTextured, Identifier.of(DPTB2Utils.MOD_ID, this.mod.getStringConfig("others.indicatorPath")), this.width/2 + 135, this.width/2 + 155, 125, 145, 0.f, 1.f, 0.f, 1.f, Colors.WHITE);
    }

    private void saveIPSettings() {
        mod.setStringConfig("others.dptbotHost", this.host.getText());
        try {
            mod.setIntConfig("others.dptbotPort", Integer.parseInt(this.port.getText()));
        } catch (NumberFormatException e) {
            // Handle invalid port input
        }
    }
}
