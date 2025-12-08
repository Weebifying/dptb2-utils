package weebify.dptb2utils.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.SystemUtils;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.utils.ExternalIndicatorManager;
import weebify.dptb2utils.utils.TinyFDJNA;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;

public class GuiDPTBotConfig extends GuiScreen {
    private final DPTB2Utils mod;
    public GuiScreen parent;
    private GuiTextField hostInput;
    private GuiTextField portInput;
    private boolean showIPOptions = false;
    private boolean showError = false;
    private GuiTextField discColorInput;
    private GuiTextField wptbColorInput;

    public GuiDPTBotConfig(GuiScreen parent, DPTB2Utils mod) {
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, width / 2 - 80 - 75, 75, 150, 20, String.format("DPTBot Connection: %s", mod.getBoolConfig("others.discordRamper") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(2, width / 2 + 80 - 75, 75, 150, 20, String.format("Advanced Options: %s", this.showIPOptions ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(3, width / 2 - 80 - 75, 100, 150, 20, String.format("Broadcast Notifs: %s", mod.getBoolConfig("others.broadcastToast") ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(4, width / 2 + 80 - 75, 100, 150, 20, String.format("Broadcast Chat: %s", mod.toggleBoolConfig("others.broadcastToast") ? "ON" : "OFF")));

        this.buttonList.add(new GuiButton(5, width / 2 - 80 - 75, 125, 150, 20, "Reset Indicator Image"));
        this.buttonList.add(new GuiButton(6, width / 2 + 80 - 75, 125, 150, 20, "Choose Indicator Image"));
        
        this.discColorInput = new GuiTextField(-3, fontRendererObj, width / 2 - 80 - 75, 150, 150, 20);
        this.discColorInput.setText(mod.getStringConfig("others.discColor"));
        
        this.wptbColorInput = new GuiTextField(-4, fontRendererObj, width / 2 - 80 - 75, 175, 150, 20);
        this.wptbColorInput.setText(mod.getStringConfig("others.wptbColor"));

        this.hostInput = new GuiTextField(-1, fontRendererObj, width / 2 - 80 - 75, 200, 150, 20);
        this.hostInput.setText(mod.getStringConfig("others.dptbotHost"));
        this.hostInput.setVisible(false);

        this.portInput = new GuiTextField(-2, fontRendererObj, width / 2 + 80 - 75, 200, 150, 20);
        this.portInput.setText(Integer.toString(mod.getIntConfig("others.dptbotPort")));
        this.portInput.setVisible(false);
        
        this.buttonList.add(new GuiButton(999, width / 2 - 75, height - 30 - 10, 150, 20, I18n.format("gui.done")));
    }

    public void chooseFile() {
        File selected = null;
        String path = TinyFDJNA.openFileDialog(
                "Select Indicator Image",
                SystemUtils.getUserHome().getAbsolutePath(),
                new String[]{"*.png"},
                "PNG Images (*.png)",
                false
        );
        if (path != null && !path.trim().isEmpty()) {
            selected = new File(path);
        }

//        JFileChooser fc = new JFileChooser();
//        fc.setDialogTitle("Select Indicator Image");
//        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        fc.setFileFilter(new FileNameExtensionFilter(".PNG Images (*.png)", "png"));
//        fc.setMultiSelectionEnabled(false);
//        int result = fc.showOpenDialog(null);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            selected = fc.getSelectedFile();
//        }

//        try (MemoryStack stack = MemoryStack.stackPush()) {
//            DPTB2Utils.LOGGER.info("past memory stack check");
//            PointerBuffer filters = stack.mallocPointer(1);
//            filters.put(stack.UTF8("*.png"));
//            filters.flip();
//
//            String path = TinyFileDialogs.tinyfd_openFileDialog(
//                    "Select Indicator Image",
//                    SystemUtils.getUserHome().getAbsolutePath(),
//                    filters,
//                    "PNG Images (*.png)",
//                    false
//            );
//
//            if (path != null && !path.trim().isEmpty()) {
//                selected = new File(path);
//            }
//        }

        if (selected != null && ExternalIndicatorManager.registerExternal(selected)) {
            String name = selected.getName();
            if (mod.getStringConfig("others.indicatorPath").startsWith("external/")  && !mod.getStringConfig("others.indicatorPath").equals("external/" + name)) {
                ExternalIndicatorManager.unregisterTexture(new ResourceLocation(DPTB2Utils.MOD_ID, mod.getStringConfig("others.indicatorPath")));
            }
            this.mod.setStringConfig("others.indicatorPath", "external/" + name);
        } else {
            this.showError = true;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        this.saveIPSettings();
        this.mod.saveSettings();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch(button.id) {
            case 1:
                button.displayString = String.format("DPTBot Connection: %s", mod.toggleBoolConfig("others.discordRamper") ? "ON" : "OFF");
                this.mod.refreshRamperStatus();
                break;
            case 2:
                this.showIPOptions = !this.showIPOptions;
                button.displayString = String.format("Advanced Options: %s", this.showIPOptions ? "ON" : "OFF");
                this.hostInput.setVisible(this.showIPOptions);
                this.portInput.setVisible(this.showIPOptions);
                break;
            case 3:
                button.displayString = String.format("Broadcast Notifs: %s", mod.toggleBoolConfig("others.broadcastToast") ? "ON" : "OFF");
                break;
            case 4:
                button.displayString = String.format("Broadcast Chat: %s", mod.toggleBoolConfig("others.broadcastChat") ? "ON" : "OFF");
                break;
            case 5:
                this.showError = false;
                this.mod.setStringConfig("others.indicatorPath", this.mod.config.getDefaultConfig("others.indicatorPath"));
                ExternalIndicatorManager.image = null;
                break;
            case 6:
                this.showError = false;
                mod.scheduleTask(1, () -> new Thread(this::chooseFile).start());
                break;
            case 999:
                this.saveIPSettings();
                this.mc.displayGuiScreen(this.parent);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.hostInput.drawTextBox();
        this.portInput.drawTextBox();
        this.discColorInput.drawTextBox();
        this.wptbColorInput.drawTextBox();
        this.drawCenteredString(fontRendererObj, "DPTBot Settings", width / 2, 20, 0xFFFFFF);

        this.drawString(fontRendererObj, "§8[§xDISC§8] §xWeebify§f: Example Discord broadcast!", width/2 + 5, 154, DPTB2Utils.hexToInt(this.discColorInput.getText()));
        this.drawString(fontRendererObj, "§8[§yWPTB§8] §yWeebify§f: Example WPTB client broadcast!", width/2 + 5, 179, DPTB2Utils.hexToInt(this.discColorInput.getText()));

        if (this.showError) {
            this.drawCenteredString(fontRendererObj, "Error loading custom indicator image:" + ExternalIndicatorManager.errorMessage, this.width/2, this.height - 70, 0xFF5555);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(DPTB2Utils.MOD_ID, this.mod.getStringConfig("others.indicatorPath")));
        if (ExternalIndicatorManager.image != null) {
            Gui.drawScaledCustomSizeModalRect(
                    this.width/2 + 160, 125,
                    0, 0,
                    ExternalIndicatorManager.image.getWidth(), ExternalIndicatorManager.image.getHeight(),
                    20, 20,
                    ExternalIndicatorManager.image.getWidth(), ExternalIndicatorManager.image.getHeight()
            );
        } else {
            Gui.drawScaledCustomSizeModalRect(
                    this.width/2 + 160, 125,
                    0, 0,
                    256, 256,
                    20, 20,
                    256, 256
            );
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.hostInput.textboxKeyTyped(typedChar, keyCode);
        this.portInput.textboxKeyTyped(typedChar, keyCode);
        this.discColorInput.textboxKeyTyped(typedChar, keyCode);
        this.wptbColorInput.textboxKeyTyped(typedChar, keyCode);
        this.saveIPSettings();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.hostInput.mouseClicked(mouseX, mouseY, mouseButton);
        this.portInput.mouseClicked(mouseX, mouseY, mouseButton);
        this.discColorInput.mouseClicked(mouseX, mouseY, mouseButton);
        this.wptbColorInput.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.hostInput.updateCursorCounter();
        this.portInput.updateCursorCounter();
        this.discColorInput.updateCursorCounter();
        this.wptbColorInput.updateCursorCounter();
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