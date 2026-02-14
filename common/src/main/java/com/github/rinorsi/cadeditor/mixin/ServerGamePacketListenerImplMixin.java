package com.github.rinorsi.cadeditor.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Redirect(
            method = "handleSetCreativeModeSlot(Lnet/minecraft/network/protocol/game/ServerboundSetCreativeModeSlotPacket;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z")
    )
    private boolean cadeditor$allowInstabuildCreativeSlot(ServerPlayerGameMode gameMode) {
        return gameMode.isCreative() || this.player.getAbilities().instabuild;
    }
}
