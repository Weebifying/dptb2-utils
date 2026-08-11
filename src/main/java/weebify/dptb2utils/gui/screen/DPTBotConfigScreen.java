package weebify.dptb2utils.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.mixin.GuiGraphicsInvoker;
import weebify.dptb2utils.utils.ExternalIndicatorManager;

import java.io.File;

public class DPTBotConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    private MultiLineEditBox hostInput;
    private MultiLineEditBox portInput;
    private boolean showIPOptions = false;
    private boolean showError = false;
    private MultiLineEditBox discColorInput;
    private MultiLineEditBox wptbColorInput;

    public DPTBotConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Component.literal("DPTBot Settings"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("DPTBot Connection: %s", mod.getBoolConfig("others.discordRamper") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("DPTBot Connection: %s", mod.toggleBoolConfig("others.discordRamper") ? "ON" : "OFF")));
            mod.refreshWptbStatus();
        }).bounds(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Agree to Ramp: %s", mod.getBoolConfig("others.consentRamper") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Agree to Ramp: %s", mod.toggleBoolConfig("others.consentRamper") ? "ON" : "OFF")));
            DPTB2Utils.LOGGER.info("consentRamper set to {}", mod.getBoolConfig("others.consentRamper"));
            mod.reassessRamperStatus();
        }).bounds(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Broadcast Notifs: %s", mod.getBoolConfig("others.broadcastToast") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Broadcast Notifs: %s", mod.toggleBoolConfig("others.broadcastToast") ? "ON" : "OFF")));
        }).bounds(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Broadcast Chat: %s", mod.getBoolConfig("others.broadcastChat") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Broadcast Chat: %s", mod.toggleBoolConfig("others.broadcastChat") ? "ON" : "OFF")));
        }).bounds(this.width/2 + 80 - 75, 100, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Reset Indicator Image"), (btn) -> {
            this.showError = false;
            this.mod.setStringConfig("others.indicatorPath", this.mod.config.getDefaultConfig("others.indicatorPath"));
            ExternalIndicatorManager.image = null;
        }).bounds(this.width/2 - 80 - 75, 125, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Choose Indicator Image"), (btn) -> {
            this.showError = false;
            new Thread(this::chooseFile).start();
        }).bounds(this.width/2 + 80 - 75, 125, 150, 20).build());

//        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Private Chat: %s", mod.getBoolConfig("others.incognito") ? "ON" : "OFF")), (btn) -> {
//            btn.setMessage(Text.of(String.format("Private Chat: %s", mod.toggleBoolConfig("others.incognito") ? "ON" : "OFF")));
//        }).dimensions(this.width/2 - 80 - 75, 150, 150, 20).build());

        this.discColorInput = MultiLineEditBox.builder().setX(this.width / 2 - 80 - 75).setY(150).setPlaceholder(Component.nullToEmpty("[DISC] Color")).build(this.font, 150, 20, Component.nullToEmpty(mod.getStringConfig("others.discColor")));
        this.discColorInput.setValue(mod.getStringConfig("others.discColor"));
        this.addRenderableWidget(this.discColorInput);

        this.wptbColorInput = MultiLineEditBox.builder().setX(this.width / 2 - 80 - 75).setY(175).setPlaceholder(Component.nullToEmpty("[WPTB] Color")).build(this.font, 150, 20, Component.nullToEmpty(mod.getStringConfig("others.wptbColor")));
        this.wptbColorInput.setValue(mod.getStringConfig("others.wptbColor"));
        this.addRenderableWidget(this.wptbColorInput);

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Broadcast Sounds: %s", mod.getBoolConfig("others.broadcastSounds") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Component.nullToEmpty(String.format("Broadcast Sounds: %s", mod.toggleBoolConfig("others.broadcastSounds") ? "ON" : "OFF")));
        }).bounds(this.width/2 - 80 - 75, 200, 150, 20).build());

        this.hostInput = MultiLineEditBox.builder().setX(this.width / 2 - 80 - 75).setY(225).setPlaceholder(Component.nullToEmpty("Websocket Host")).build(this.font, 150, 20, Component.nullToEmpty(mod.getStringConfig("others.dptbotHost")));
        this.hostInput.setValue(mod.getStringConfig("others.dptbotHost"));
        this.hostInput.visible = false;
        this.hostInput.active = true;
        this.addRenderableWidget(this.hostInput);

        this.portInput = MultiLineEditBox.builder().setX(this.width / 2 + 80 - 75).setY(225).setPlaceholder(Component.nullToEmpty("Websocket Port")).build(this.font, 150, 20, Component.nullToEmpty(Integer.toString(mod.getIntConfig("others.dptbotPort"))));
        this.portInput.setValue(Integer.toString(mod.getIntConfig("others.dptbotPort")));
        this.portInput.visible = false;
        this.portInput.active = true;
        this.addRenderableWidget(this.portInput);

        this.addRenderableWidget(Button.builder(Component.nullToEmpty(String.format("Advanced Options: %s", this.showIPOptions ? "ON" : "OFF")), (btn) -> {
            this.showIPOptions = !this.showIPOptions;
            btn.setMessage(Component.nullToEmpty(String.format("Advanced Options: %s", this.showIPOptions ? "ON" : "OFF")));
            this.hostInput.visible = this.showIPOptions;
            this.portInput.visible = this.showIPOptions;
        }).bounds(30, this.height - 30 - 10,150, 20).build());

//        this.addDrawableChild(ButtonWidget.builder(Text.of("Block List"), (btn) -> {
//            assert this.client != null;
//            this.client.setScreen(new BlockListScreen(this, mod));
//        }).dimensions(30, this.height - 30 - 35, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            assert this.minecraft != null;
            this.saveIPSettings();
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
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

        File finalSelected = selected;
        Minecraft.getInstance().execute(() -> {
            if (finalSelected != null && ExternalIndicatorManager.registerExternal(finalSelected)) {
                DPTB2Utils.LOGGER.info("Successfully registered external indicator image.");
                String name = finalSelected.getName();
                if (mod.getStringConfig("others.indicatorPath").startsWith("external/") && !mod.getStringConfig("others.indicatorPath").equals("external/" + name)) {
                    ExternalIndicatorManager.unregisterTexture(Identifier.fromNamespaceAndPath(DPTB2Utils.MOD_ID, mod.getStringConfig("others.indicatorPath")));
                }
                this.mod.setStringConfig("others.indicatorPath", "external/" + name);
                DPTB2Utils.LOGGER.info("Updated indicator path in config to: {}", "external/" + name);
            } else {
                this.showError = true;
                DPTB2Utils.LOGGER.error("Error registering external indicator image: {}", ExternalIndicatorManager.errorMessage);
            }
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.saveIPSettings();
        this.mod.saveSettings();
        super.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean res = super.keyPressed(input);
        this.saveIPSettings();
        return res;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width/2, 20, CommonColors.WHITE);
        context.drawCenteredString(this.font, String.format("isRamper: %b", mod.isRamper), this.width/2, this.height - 45 - 10, mod.isRamper ? 0xFF55FF55 : 0xFFFF5555);

        context.drawString(this.font, Component.nullToEmpty("§8[§xDISC§8] §xWeebify§f: Example Discord broadcast!"), this.width/2 + 5, 154, DPTB2Utils.hexToInt(this.discColorInput.getValue()));
        context.drawString(this.font, Component.nullToEmpty("§8[§yWPTB§8] §yWeebify§f: Example WPTB client broadcast!"), this.width/2 + 5, 179, DPTB2Utils.hexToInt(this.wptbColorInput.getValue()));

        if (this.showError) {
            context.drawCenteredString(this.font, Component.nullToEmpty("Error loading custom indicator image:" + ExternalIndicatorManager.errorMessage), this.width/2, this.height - 70, CommonColors.RED);
        }

        ((GuiGraphicsInvoker)context).invokeInnerBlit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(DPTB2Utils.MOD_ID, this.mod.getStringConfig("others.indicatorPath")), this.width/2 + 160, this.width/2 + 180, 125, 145, 0.f, 1.f, 0.f, 1.f, CommonColors.WHITE);
    }

    private void saveIPSettings() {
        mod.setStringConfig("others.dptbotHost", this.hostInput.getValue());
        try {
            mod.setIntConfig("others.dptbotPort", Integer.parseInt(this.portInput.getValue()));
        } catch (NumberFormatException e) {
            // Handle invalid port input
        }

        if (DPTB2Utils.hexToInt(this.discColorInput.getValue()) != 0) {
            mod.setStringConfig("others.discColor", this.discColorInput.getValue());
        }
        if (DPTB2Utils.hexToInt(this.wptbColorInput.getValue()) != 0) {
            mod.setStringConfig("others.wptbColor", this.wptbColorInput.getValue());
        }
    }
}
