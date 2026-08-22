package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import org.jspecify.annotations.NonNull;

import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.utils.BlockListManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockListScreen extends Screen {
    private final DPTB2Utils mod;
    public final Screen parent;

    private String discErrorMessage = "";
    private String wptbErrorMessage = "";

    private BlockEntryList discList;
    private BlockEntryList wptbList;

    private enum PopupState { INITIAL, CHOOSE, INPUT }
    private PopupState discPopupState = PopupState.INITIAL;
    private PopupState wptbPopupState = PopupState.INITIAL;

    private boolean discInputIsId = true;
    private boolean wptbInputIsId = true;

    // ── bottom popup widgets ──
    private Button discBlockUserBtn;
    private Button discEnterIdBtn;
    private Button discEnterUsernameBtn;
    private EditBox discTextInput;
    private Button discConfirmBtn;

    private Button wptbBlockUserBtn;
    private Button wptbEnterIdBtn;
    private Button wptbEnterUsernameBtn;
    private EditBox wptbTextInput;
    private Button wptbConfirmBtn;

    public BlockListScreen(Screen parent, DPTB2Utils mod) {
        super(Component.literal("Block List"));
        this.parent = parent;
        this.mod = mod;
    }

    // ── public setters so external code can push errors into the screen ──

    public void setDiscErrorMessage(String msg) { this.discErrorMessage = msg; }
    public void setWptbErrorMessage(String msg) { this.wptbErrorMessage = msg; }

    /** Call after the server resolves a block request to re‑enable the button. */
    public void onDiscBlockResolved() { resetDiscPopup(); }
    public void onWptbBlockResolved() { resetWptbPopup(); }

    // ────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        int halfWidth = this.width / 2;
        int padding = 5;

        int listTop = 40;                  // leave room for title + error
        int listBottom = this.height - 80; // leave room for popup region + done button
        int listHeight = listBottom - listTop;

        // discord side
        discList = new BlockEntryList(
                padding, listTop,
                halfWidth - padding * 2, listHeight,
                mod.getListConfig("others.discBlocks"),
                BlockListManager.getDiscUsername(),
                this.font,
                this::removeDiscBlock
        );
        this.addRenderableWidget(discList);

        wptbList = new BlockEntryList(
                halfWidth + padding, listTop,
                halfWidth - padding * 2, listHeight,
                mod.getListConfig("others.wptbBlocks"),
                BlockListManager.getWptbUsername(),
                this.font,
                this::removeWptbBlock
        );
        this.addRenderableWidget(wptbList);

        int popupY = listBottom + 4;
        int btnW = 100;
        int btnH = 20;

        // --- Discord side ---
        int discCenterX = halfWidth / 2;

        discBlockUserBtn = Button.builder(Component.nullToEmpty("Block User"), (btn) -> {
            setDiscPopupState(PopupState.CHOOSE);
        }).bounds(discCenterX - btnW / 2, popupY, btnW, btnH).build();

        discEnterIdBtn = Button.builder(Component.nullToEmpty("Enter ID"), (btn) -> {
            discInputIsId = true;
            setDiscPopupState(PopupState.INPUT);
        }).bounds(discCenterX - btnW - 2, popupY, btnW, btnH).build();

        discEnterUsernameBtn = Button.builder(Component.nullToEmpty("Enter Username"), (btn) -> {
            discInputIsId = false;
            setDiscPopupState(PopupState.INPUT);
        }).bounds(discCenterX + 2, popupY, btnW, btnH).build();

        discTextInput = new EditBox(this.font, discCenterX - btnW / 2, popupY, btnW, btnH, Component.empty());
        discTextInput.setMaxLength(64);

        discConfirmBtn = Button.builder(Component.nullToEmpty("Confirm"), (btn) -> {
            String value = discTextInput.getValue().trim();
            if (!value.isEmpty()) {
                addDiscBlock(value);
            }

            resetDiscPopup();
        }).bounds(discCenterX + btnW / 2 + 4, popupY, 60, btnH).build();

        // wptb side
        int wptbCenterX = halfWidth + halfWidth / 2;

        wptbBlockUserBtn = Button.builder(Component.nullToEmpty("Block User"), (btn) -> {
            setWptbPopupState(PopupState.CHOOSE);
        }).bounds(wptbCenterX - btnW / 2, popupY, btnW, btnH).build();

        wptbEnterIdBtn = Button.builder(Component.nullToEmpty("Enter ID"), (btn) -> {
            wptbInputIsId = true;
            setWptbPopupState(PopupState.INPUT);
        }).bounds(wptbCenterX - btnW - 2, popupY, btnW, btnH).build();

        wptbEnterUsernameBtn = Button.builder(Component.nullToEmpty("Enter Username"), (btn) -> {
            wptbInputIsId = false;
            setWptbPopupState(PopupState.INPUT);
        }).bounds(wptbCenterX + 2, popupY, btnW, btnH).build();

        wptbTextInput = new EditBox(this.font, wptbCenterX - btnW / 2, popupY, btnW, btnH, Component.empty());
        wptbTextInput.setMaxLength(64);

        wptbConfirmBtn = Button.builder(Component.nullToEmpty("Confirm"), (btn) -> {
            String value = wptbTextInput.getValue().trim();
            if (!value.isEmpty()) {
                addWptbBlock(value.replace("-", ""));
            }
            resetWptbPopup();
        }).bounds(wptbCenterX + btnW / 2 + 4, popupY, 60, btnH).build();

        // start both sides in INITIAL state
        setDiscPopupState(PopupState.INITIAL);
        setWptbPopupState(PopupState.INITIAL);

        // ── Done button ──
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), (btn) -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    private void clearDiscPopupWidgets() {
        this.removeWidget(discBlockUserBtn);
        this.removeWidget(discEnterIdBtn);
        this.removeWidget(discEnterUsernameBtn);
        this.removeWidget(discTextInput);
        this.removeWidget(discConfirmBtn);
    }

    private void clearWptbPopupWidgets() {
        this.removeWidget(wptbBlockUserBtn);
        this.removeWidget(wptbEnterIdBtn);
        this.removeWidget(wptbEnterUsernameBtn);
        this.removeWidget(wptbTextInput);
        this.removeWidget(wptbConfirmBtn);
    }

    private void setDiscPopupState(PopupState state) {
        clearDiscPopupWidgets();
        discPopupState = state;
        switch (state) {
            case INITIAL -> {
                this.addRenderableWidget(discBlockUserBtn);
            }
            case CHOOSE -> {
                this.addRenderableWidget(discEnterIdBtn);
                this.addRenderableWidget(discEnterUsernameBtn);
            }
            case INPUT -> {
                discTextInput.setValue("");
                this.addRenderableWidget(discTextInput);
                this.addRenderableWidget(discConfirmBtn);
            }
        }
    }

    private void setWptbPopupState(PopupState state) {
        clearWptbPopupWidgets();
        wptbPopupState = state;
        switch (state) {
            case INITIAL -> {
                this.addRenderableWidget(wptbBlockUserBtn);
            }
            case CHOOSE -> {
                this.addRenderableWidget(wptbEnterIdBtn);
                this.addRenderableWidget(wptbEnterUsernameBtn);
            }
            case INPUT -> {
                wptbTextInput.setValue("");
                this.addRenderableWidget(wptbTextInput);
                this.addRenderableWidget(wptbConfirmBtn);
            }
        }
    }

    private void resetDiscPopup() {
        setDiscPopupState(PopupState.INITIAL);
    }

    private void resetWptbPopup() {
        setWptbPopupState(PopupState.INITIAL);
    }

    private void addDiscBlock(String userId) {
        List<String> list = new ArrayList<>(mod.getListConfig("others.discBlocks"));
        if (!list.contains(userId)) {
            list.add(userId);
            mod.setListConfig("others.discBlocks", list);
            rebuildDiscList();
        }
    }

    private void removeDiscBlock(String userId) {
        List<String> list = new ArrayList<>(mod.getListConfig("others.discBlocks"));
        list.remove(userId);
        mod.setListConfig("others.discBlocks", list);
        BlockListManager.removeDiscUsername(userId);
        rebuildDiscList();
    }

    private void addWptbBlock(String userId) {
        List<String> list = new ArrayList<>(mod.getListConfig("others.wptbBlocks"));
        if (!list.contains(userId)) {
            list.add(userId);
            mod.setListConfig("others.wptbBlocks", list);
            rebuildWptbList();
        }
    }

    private void removeWptbBlock(String userId) {
        List<String> list = new ArrayList<>(mod.getListConfig("others.wptbBlocks"));
        list.remove(userId);
        mod.setListConfig("others.wptbBlocks", list);
        BlockListManager.removeWptbUsername(userId);
        rebuildWptbList();
    }

    private void rebuildDiscList() {
        if (discList != null) {
            this.removeWidget(discList);
        }
        int halfWidth = this.width / 2;
        int padding = 5;
        int listTop = 40;
        int listHeight = (this.height - 80) - listTop;
        discList = new BlockEntryList(
                padding, listTop,
                halfWidth - padding * 2, listHeight,
                mod.getListConfig("others.discBlocks"),
                BlockListManager.getDiscUsername(),
                this.font,
                this::removeDiscBlock
        );
        this.addRenderableWidget(discList);
    }

    private void rebuildWptbList() {
        if (wptbList != null) {
            this.removeWidget(wptbList);
        }
        int halfWidth = this.width / 2;
        int padding = 5;
        int listTop = 40;
        int listHeight = (this.height - 80) - listTop;
        wptbList = new BlockEntryList(
                halfWidth + padding, listTop,
                halfWidth - padding * 2, listHeight,
                mod.getListConfig("others.wptbBlocks"),
                BlockListManager.getWptbUsername(),
                this.font,
                this::removeWptbBlock
        );
        this.addRenderableWidget(wptbList);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.mod.saveSettings();
        super.onClose();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontalAmount, double verticalAmount) {
        // forward scroll to whichever list the mouse is over
        if (discList != null && discList.isMouseOver(x, y)) {
            if (discList.mouseScrolled(x, y, 0, verticalAmount)) return true;
        }
        if (wptbList != null && wptbList.isMouseOver(x, y)) {
            if (wptbList.mouseScrolled(x, y, 0, verticalAmount)) return true;
        }
        return super.mouseScrolled(x, y, horizontalAmount, verticalAmount);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int halfWidth = this.width / 2;

        // ── list background fills ──
        if (discList != null) {
            graphics.fill(discList.getX(), discList.getY(),
                    discList.getX() + discList.getWidth(),
                    discList.getY() + discList.getHeight(), 0x33000000);
        }
        if (wptbList != null) {
            graphics.fill(wptbList.getX(), wptbList.getY(),
                    wptbList.getX() + wptbList.getWidth(),
                    wptbList.getY() + wptbList.getHeight(), 0x33000000);
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.centeredText(this.font, this.title, this.width / 2, 8, CommonColors.WHITE);

        graphics.centeredText(this.font, "Discord Blocks", halfWidth / 2, 28, DPTB2Utils.hexToInt(mod.getStringConfig("others.discColor")));
        graphics.centeredText(this.font, "WPTB Client Blocks", halfWidth + halfWidth / 2, 28, DPTB2Utils.hexToInt(mod.getStringConfig("others.wptbColor")));

        if (!discErrorMessage.isEmpty()) {
            graphics.centeredText(this.font, discErrorMessage, halfWidth / 2, 18, CommonColors.RED);
        }
        if (!wptbErrorMessage.isEmpty()) {
            graphics.centeredText(this.font, wptbErrorMessage, halfWidth + halfWidth / 2, 18, CommonColors.RED);
        }

        // ── divider line down the centre ──
        graphics.fill(halfWidth - 1, 28, halfWidth, this.height - 50, 0x55FFFFFF);
    }

    @FunctionalInterface
    public interface RemoveAction {
        void remove(String userId);
    }

    public static class BlockEntryList extends AbstractScrollArea {
        private final List<String> blockIds;
        private final Map<String, String> usernameCache;
        private final Font textRenderer;
        private final RemoveAction removeAction;

        private static final int LINE_HEIGHT = 14;
        private static final int PADDING = 4;
        private static final int REMOVE_BTN_WIDTH = 12;

        public BlockEntryList(int x, int y, int width, int height,
                              List<String> blockIds,
                              Map<String, String> usernameCache,
                              Font textRenderer,
                              RemoveAction removeAction) {
            super(x, y, width, height, Component.empty(), AbstractScrollArea.defaultSettings(10));
            this.blockIds = blockIds;
            this.usernameCache = usernameCache;
            this.textRenderer = textRenderer;
            this.removeAction = removeAction;
        }

        @Override
        protected int contentHeight() {
            return PADDING * 2 + blockIds.size() * LINE_HEIGHT;
        }

        @Override
        protected double scrollRate() {
            return LINE_HEIGHT;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            // enable scissor so content is clipped to the widget bounds
            graphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
            this.drawContent(graphics, mouseX, mouseY);
            graphics.disableScissor();
            this.extractScrollbar(graphics, mouseX, mouseY);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {}

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            double mouseX = click.x();
            double mouseY = click.y();

            if (!this.isMouseOver(mouseX, mouseY)) return false;

            if (updateScrolling(click)) {
                return true;
            }

            // check if the click hit a remove button
            int startY = getY() + PADDING - (int) scrollAmount();
            for (int i = 0; i < blockIds.size(); i++) {
                int drawY = startY + i * LINE_HEIGHT;
                int btnX = getX() + PADDING;
                int btnY = drawY;
                if (mouseX >= btnX && mouseX <= btnX + REMOVE_BTN_WIDTH
                        && mouseY >= btnY && mouseY <= btnY + textRenderer.lineHeight) {
                    removeAction.remove(blockIds.get(i));
                    return true;
                }
            }
            return super.mouseClicked(click, doubled);
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double dx, double dy) {
            return super.mouseScrolled(mx, my, 0, dy);
        }

        private void drawContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
            int startY = getY() + PADDING - (int) scrollAmount();

            for (int i = 0; i < blockIds.size(); i++) {
                int drawY = startY + i * LINE_HEIGHT;

                // only draw if visible
                if (drawY + textRenderer.lineHeight < getY() || drawY > getY() + getHeight()) {
                    continue;
                }

                String userId = blockIds.get(i);
                String cachedName = usernameCache.get(userId);

                // ── red X button ──
                int btnX = getX() + PADDING;
                boolean hoveringX = mouseX >= btnX && mouseX <= btnX + REMOVE_BTN_WIDTH
                        && mouseY >= drawY && mouseY <= drawY + textRenderer.lineHeight;
                int xColor = hoveringX ? 0xFFFF0000 : 0xFFFF5555;
                graphics.text(textRenderer, Component.literal("✕"), btnX, drawY, xColor);

                // ── user id (+ cached username) ──
                String displayText = cachedName != null
                        ? userId + " (" + cachedName + ")"
                        : userId;
                graphics.text(
                        textRenderer,
                        Component.literal(displayText),
                        btnX + REMOVE_BTN_WIDTH + 4,
                        drawY,
                        CommonColors.WHITE
                );
            }
        }
    }
}