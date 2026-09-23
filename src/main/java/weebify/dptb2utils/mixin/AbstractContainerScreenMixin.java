package weebify.dptb2utils.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;


@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {
    @Unique
    private boolean opened = false;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderInject(GuiGraphics graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        AbstractContainerScreen that = (AbstractContainerScreen) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        String title = that.getTitle().getString();
        if (title.contains("Backpack - Items")) {
            if (!opened) {
                opened = true;
                DPTB2Utils mod = DPTB2Utils.getInstance();

                if (mod.openBoots) {
                    mod.openBoots = false;
                    mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 2, 0, ClickType.PICKUP, mc.player);
                } else if (mod.openRoutes) {
                    mod.openRoutes = false;
                    mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 3, 0, ClickType.PICKUP, mc.player);
                }
            }
        }
    }
}
