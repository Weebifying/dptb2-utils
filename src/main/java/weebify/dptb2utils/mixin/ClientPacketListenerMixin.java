package weebify.dptb2utils.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import weebify.dptb2utils.DPTB2Utils;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleDamageEvent", at = @At("HEAD"))
    private void handleDamageEventInject(ClientboundDamageEventPacket packet, CallbackInfo ci) {
//        Minecraft client = Minecraft.getInstance();
//        if (client.player == null || client.level == null) return;
//
//        DPTB2Utils.LOGGER.info("handleDamageEvent");
//
//        if (packet.entityId() != client.player.getId()) return;
//        DPTB2Utils.LOGGER.info("not from player");
//
//        DamageSource source = packet.getSource(client.level);
//        Entity attacker = source.getEntity();
//
//        if (attacker instanceof Player player) {
//            DPTB2Utils.LOGGER.info("attacker = {}", player.getGameProfile().name());
//        }
    }

    @Inject(method = "handleHurtAnimation", at = @At("HEAD"))
    private void handleHurtAnimationInject(ClientboundHurtAnimationPacket packet, CallbackInfo ci) {
//        DPTB2Utils.LOGGER.info("handleHurtAnimation(yaw={})", packet.yaw());
    }

//    @Inject(method =  "handleSetEntityMotion", at = @At("HEAD"))
//    private void handleSetEntityMotionInject(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
//        DPTB2Utils.LOGGER.info("handleSetEntityMotion");
//    }
}
