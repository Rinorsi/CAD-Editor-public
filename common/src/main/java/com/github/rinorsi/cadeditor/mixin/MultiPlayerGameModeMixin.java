package com.github.rinorsi.cadeditor.mixin;

import com.github.rinorsi.cadeditor.client.ClientContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Redirect(
            method = "startDestroyBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameType;isCreative()Z")
    )
    private boolean cadeditor$instabuildCreativeOnStartDestroy(GameType gameType) {
        return gameType.isCreative() || cadeditor$hasCreativeLikeInstabuild();
    }

    @Redirect(
            method = "continueDestroyBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameType;isCreative()Z")
    )
    private boolean cadeditor$instabuildCreativeOnContinueDestroy(GameType gameType) {
        return gameType.isCreative() || cadeditor$hasCreativeLikeInstabuild();
    }

    @Inject(method = "hasInfiniteItems", at = @At("HEAD"), cancellable = true)
    private void cadeditor$allowInstabuildAsInfiniteItems(CallbackInfoReturnable<Boolean> cir) {
        if (cadeditor$hasCreativeLikeInstabuild()) {
            cir.setReturnValue(true);
        }
    }

    private boolean cadeditor$hasCreativeLikeInstabuild() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean serverSupportsAbilitySync = ClientContext.isModInstalledOnServer() || minecraft.hasSingleplayerServer();
        return serverSupportsAbilitySync && player != null && player.getAbilities().instabuild;
    }
}
