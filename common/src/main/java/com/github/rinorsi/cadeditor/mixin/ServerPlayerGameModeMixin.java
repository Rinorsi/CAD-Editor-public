package com.github.rinorsi.cadeditor.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Shadow
    protected ServerPlayer player;

    @Redirect(
            method = "handleBlockBreakAction",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z")
    )
    private boolean cadeditor$instabuildCreativeBreakAction(ServerPlayerGameMode gameMode) {
        return gameMode.isCreative() || this.player.getAbilities().instabuild;
    }

    @Redirect(
            method = "destroyBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z")
    )
    private boolean cadeditor$instabuildCreativeDestroyBlock(ServerPlayerGameMode gameMode) {
        return gameMode.isCreative() || this.player.getAbilities().instabuild;
    }
}
