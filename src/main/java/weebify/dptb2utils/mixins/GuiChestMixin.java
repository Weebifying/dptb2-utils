package weebify.dptb2utils.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;

/**
 * Backs the /boots and /routes shortcut commands: once the "Backpack - Items"
 * chest GUI opens (triggered by sending the "backpack" command), this mixin
 * simulates a normal player click on the relevant navigation slot to jump
 * straight to the Boots Tracking or Routes sub-menu.
 */
@Mixin(GuiChest.class)
public abstract class GuiChestMixin {
    @Shadow
    private IInventory lowerChestInventory;

    @Unique
    private boolean dptb2utils$handledOpen = false;

    @Inject(method = "drawGuiContainerForegroundLayer", at = @At("TAIL"))
    private void dptb2utils$onDrawScreen(int mouseX, int mouseY, CallbackInfo ci) {
        GuiChest self = (GuiChest) (Object) this;
        DPTB2Utils mod = DPTB2Utils.getInstance();

        if (!mod.openBoots && !mod.openRoutes) {
            dptb2utils$handledOpen = false;
            return;
        }

        if (this.lowerChestInventory == null) {
            return;
        }

        String title = this.lowerChestInventory.getDisplayName().getUnformattedText();
        if (!title.contains("Backpack - Items")) {
            return;
        }

        if (dptb2utils$handledOpen) {
            return;
        }
        dptb2utils$handledOpen = true;

        Minecraft mc = Minecraft.getMinecraft();
        int windowId = self.inventorySlots.windowId;

        if (mod.openBoots) {
            mod.openBoots = false;
            mc.playerController.windowClick(windowId, 2, 0, 0, mc.thePlayer);
        } else if (mod.openRoutes) {
            mod.openRoutes = false;
            mc.playerController.windowClick(windowId, 3, 0, 0, mc.thePlayer);
        }
    }
}
