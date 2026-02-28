package weebify.dptb2utils.gui.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
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
    private ButtonWidget discBlockUserBtn;
    private ButtonWidget discEnterIdBtn;
    private ButtonWidget discEnterUsernameBtn;
    private TextFieldWidget discTextInput;
    private ButtonWidget discConfirmBtn;

    private ButtonWidget wptbBlockUserBtn;
    private ButtonWidget wptbEnterIdBtn;
    private ButtonWidget wptbEnterUsernameBtn;
    private TextFieldWidget wptbTextInput;
    private ButtonWidget wptbConfirmBtn;

    public BlockListScreen(Screen parent, DPTB2Utils mod) {
        super(Text.literal("Block List"));
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
                this.textRenderer,
                this::removeDiscBlock
        );
        this.addDrawableChild(discList);

        wptbList = new BlockEntryList(
                halfWidth + padding, listTop,
                halfWidth - padding * 2, listHeight,
                mod.getListConfig("others.wptbBlocks"),
                BlockListManager.getWptbUsername(),
                this.textRenderer,
                this::removeWptbBlock
        );
        this.addDrawableChild(wptbList);

        int popupY = listBottom + 4;
        int btnW = 100;
        int btnH = 20;

        // --- Discord side ---
        int discCenterX = halfWidth / 2;

        discBlockUserBtn = ButtonWidget.builder(Text.of("Block User"), (btn) -> {
            setDiscPopupState(PopupState.CHOOSE);
        }).dimensions(discCenterX - btnW / 2, popupY, btnW, btnH).build();

        discEnterIdBtn = ButtonWidget.builder(Text.of("Enter ID"), (btn) -> {
            discInputIsId = true;
            setDiscPopupState(PopupState.INPUT);
        }).dimensions(discCenterX - btnW - 2, popupY, btnW, btnH).build();

        discEnterUsernameBtn = ButtonWidget.builder(Text.of("Enter Username"), (btn) -> {
            discInputIsId = false;
            setDiscPopupState(PopupState.INPUT);
        }).dimensions(discCenterX + 2, popupY, btnW, btnH).build();

        discTextInput = new TextFieldWidget(this.textRenderer, discCenterX - btnW / 2, popupY, btnW, btnH, Text.empty());
        discTextInput.setMaxLength(64);

        discConfirmBtn = ButtonWidget.builder(Text.of("Confirm"), (btn) -> {
            String value = discTextInput.getText().trim();
            if (!value.isEmpty()) {
                addDiscBlock(value);
            }

            resetDiscPopup();
        }).dimensions(discCenterX + btnW / 2 + 4, popupY, 60, btnH).build();

        // wptb side
        int wptbCenterX = halfWidth + halfWidth / 2;

        wptbBlockUserBtn = ButtonWidget.builder(Text.of("Block User"), (btn) -> {
            setWptbPopupState(PopupState.CHOOSE);
        }).dimensions(wptbCenterX - btnW / 2, popupY, btnW, btnH).build();

        wptbEnterIdBtn = ButtonWidget.builder(Text.of("Enter ID"), (btn) -> {
            wptbInputIsId = true;
            setWptbPopupState(PopupState.INPUT);
        }).dimensions(wptbCenterX - btnW - 2, popupY, btnW, btnH).build();

        wptbEnterUsernameBtn = ButtonWidget.builder(Text.of("Enter Username"), (btn) -> {
            wptbInputIsId = false;
            setWptbPopupState(PopupState.INPUT);
        }).dimensions(wptbCenterX + 2, popupY, btnW, btnH).build();

        wptbTextInput = new TextFieldWidget(this.textRenderer, wptbCenterX - btnW / 2, popupY, btnW, btnH, Text.empty());
        wptbTextInput.setMaxLength(64);

        wptbConfirmBtn = ButtonWidget.builder(Text.of("Confirm"), (btn) -> {
            String value = wptbTextInput.getText().trim();
            if (!value.isEmpty()) {
                addWptbBlock(value.replace("-", ""));
            }
            resetWptbPopup();
        }).dimensions(wptbCenterX + btnW / 2 + 4, popupY, 60, btnH).build();

        // start both sides in INITIAL state
        setDiscPopupState(PopupState.INITIAL);
        setWptbPopupState(PopupState.INITIAL);

        // ── Done button ──
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            assert this.client != null;
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    private void clearDiscPopupWidgets() {
        this.remove(discBlockUserBtn);
        this.remove(discEnterIdBtn);
        this.remove(discEnterUsernameBtn);
        this.remove(discTextInput);
        this.remove(discConfirmBtn);
    }

    private void clearWptbPopupWidgets() {
        this.remove(wptbBlockUserBtn);
        this.remove(wptbEnterIdBtn);
        this.remove(wptbEnterUsernameBtn);
        this.remove(wptbTextInput);
        this.remove(wptbConfirmBtn);
    }

    private void setDiscPopupState(PopupState state) {
        clearDiscPopupWidgets();
        discPopupState = state;
        switch (state) {
            case INITIAL -> {
                this.addDrawableChild(discBlockUserBtn);
            }
            case CHOOSE -> {
                this.addDrawableChild(discEnterIdBtn);
                this.addDrawableChild(discEnterUsernameBtn);
            }
            case INPUT -> {
                discTextInput.setText("");
                this.addDrawableChild(discTextInput);
                this.addDrawableChild(discConfirmBtn);
            }
        }
    }

    private void setWptbPopupState(PopupState state) {
        clearWptbPopupWidgets();
        wptbPopupState = state;
        switch (state) {
            case INITIAL -> {
                this.addDrawableChild(wptbBlockUserBtn);
            }
            case CHOOSE -> {
                this.addDrawableChild(wptbEnterIdBtn);
                this.addDrawableChild(wptbEnterUsernameBtn);
            }
            case INPUT -> {
                wptbTextInput.setText("");
                this.addDrawableChild(wptbTextInput);
                this.addDrawableChild(wptbConfirmBtn);
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
            this.remove(discList);
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
                this.textRenderer,
                this::removeDiscBlock
        );
        this.addDrawableChild(discList);
    }

    private void rebuildWptbList() {
        if (wptbList != null) {
            this.remove(wptbList);
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
                this.textRenderer,
                this::removeWptbBlock
        );
        this.addDrawableChild(wptbList);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.mod.saveSettings();
        super.close();
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int halfWidth = this.width / 2;

        // ── list background fills ──
        if (discList != null) {
            context.fill(discList.getX(), discList.getY(),
                    discList.getX() + discList.getWidth(),
                    discList.getY() + discList.getHeight(), 0x33000000);
        }
        if (wptbList != null) {
            context.fill(wptbList.getX(), wptbList.getY(),
                    wptbList.getX() + wptbList.getWidth(),
                    wptbList.getY() + wptbList.getHeight(), 0x33000000);
        }

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, Colors.WHITE);

        context.drawCenteredTextWithShadow(this.textRenderer, "Discord Blocks", halfWidth / 2, 28, DPTB2Utils.hexToInt(mod.getStringConfig("others.discColor")));
        context.drawCenteredTextWithShadow(this.textRenderer, "WPTB Client Blocks", halfWidth + halfWidth / 2, 28, DPTB2Utils.hexToInt(mod.getStringConfig("others.wptbColor")));

        if (!discErrorMessage.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, discErrorMessage, halfWidth / 2, 18, Colors.RED);
        }
        if (!wptbErrorMessage.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, wptbErrorMessage, halfWidth + halfWidth / 2, 18, Colors.RED);
        }

        // ── divider line down the centre ──
        context.fill(halfWidth - 1, 28, halfWidth, this.height - 50, 0x55FFFFFF);
    }

    @FunctionalInterface
    public interface RemoveAction {
        void remove(String userId);
    }

    public static class BlockEntryList extends ScrollableWidget {
        private final List<String> blockIds;
        private final Map<String, String> usernameCache;
        private final TextRenderer textRenderer;
        private final RemoveAction removeAction;

        private static final int LINE_HEIGHT = 14;
        private static final int PADDING = 4;
        private static final int REMOVE_BTN_WIDTH = 12;

        public BlockEntryList(int x, int y, int width, int height,
                              List<String> blockIds,
                              Map<String, String> usernameCache,
                              TextRenderer textRenderer,
                              RemoveAction removeAction) {
            super(x, y, width, height, Text.empty());
            this.blockIds = blockIds;
            this.usernameCache = usernameCache;
            this.textRenderer = textRenderer;
            this.removeAction = removeAction;
        }

        @Override
        protected int getContentsHeightWithPadding() {
            return PADDING * 2 + blockIds.size() * LINE_HEIGHT;
        }

        @Override
        protected double getDeltaYPerScroll() {
            return LINE_HEIGHT;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // enable scissor so content is clipped to the widget bounds
            context.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
            drawContent(context, mouseX, mouseY);
            context.disableScissor();
            drawScrollbar(context);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.isMouseOver(mouseX, mouseY)) return false;

            if (checkScrollbarDragged(mouseX, mouseY, button)) {
                return true;
            }

            // check if the click hit a remove button
            int startY = getY() + PADDING - (int) getScrollY();
            for (int i = 0; i < blockIds.size(); i++) {
                int drawY = startY + i * LINE_HEIGHT;
                int btnX = getX() + PADDING;
                int btnY = drawY;
                if (mouseX >= btnX && mouseX <= btnX + REMOVE_BTN_WIDTH
                        && mouseY >= btnY && mouseY <= btnY + textRenderer.fontHeight) {
                    removeAction.remove(blockIds.get(i));
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double dx, double dy) {
            return super.mouseScrolled(mx, my, 0, dy);
        }

        private void drawContent(DrawContext context, int mouseX, int mouseY) {
            int startY = getY() + PADDING - (int) getScrollY();

            for (int i = 0; i < blockIds.size(); i++) {
                int drawY = startY + i * LINE_HEIGHT;

                // only draw if visible
                if (drawY + textRenderer.fontHeight < getY() || drawY > getY() + getHeight()) {
                    continue;
                }

                String userId = blockIds.get(i);
                String cachedName = usernameCache.get(userId);

                // ── red X button ──
                int btnX = getX() + PADDING;
                boolean hoveringX = mouseX >= btnX && mouseX <= btnX + REMOVE_BTN_WIDTH
                        && mouseY >= drawY && mouseY <= drawY + textRenderer.fontHeight;
                int xColor = hoveringX ? 0xFFFF0000 : 0xFFFF5555;
                context.drawTextWithShadow(textRenderer, Text.literal("✕"), btnX, drawY, xColor);

                // ── user id (+ cached username) ──
                String displayText = cachedName != null
                        ? userId + " (" + cachedName + ")"
                        : userId;
                context.drawTextWithShadow(
                        textRenderer,
                        Text.literal(displayText),
                        btnX + REMOVE_BTN_WIDTH + 4,
                        drawY,
                        Colors.WHITE
                );
            }
        }
    }
}