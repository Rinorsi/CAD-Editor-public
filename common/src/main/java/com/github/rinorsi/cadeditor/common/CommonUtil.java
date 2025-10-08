package com.github.rinorsi.cadeditor.common;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CommonUtil {
    public static void showMessage(Player player, Component component) {
        player.displayClientMessage(component, false);
    }

    public static void showUpdateSuccess(Player player, MutableComponent component) {
        showMessage(player, ModTexts.Messages.successUpdate(component));
    }

    public static void showItemUpdateSuccess(Player player, ItemStack stack) {
        try {
            String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            MutableComponent msg = Component.literal("[CADE-" + date + "] ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("物品 ").withStyle(ChatFormatting.GOLD))
                    .append(stack.getHoverName().copy().withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" 已更新。"));
            showMessage(player, msg);
        } catch (Exception e) {
            showUpdateSuccess(player, ModTexts.ITEM);
        }
    }

    public static void showItemUpdateFailure(Player player, ItemStack stack, Component reason) {
        try {
            String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            MutableComponent msg = Component.literal("[CADE-" + date + "] ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("物品 ").withStyle(ChatFormatting.GOLD))
                    .append((stack.isEmpty() ? Component.literal("(未知)") : stack.getHoverName().copy()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" 更新失败："));
            if (reason != null) {
                msg.append(reason.copy().withStyle(ChatFormatting.RED));
            }
            showMessage(player, msg);
        } catch (Exception e) {
            if (reason != null) {
                showMessage(player, reason);
            } else {
                showMessage(player, ModTexts.Messages.ERROR_GENERIC);
            }
        }
    }

    public static void showTargetError(Player player, MutableComponent component) {
        showMessage(player, ModTexts.Messages.errorNoTargetFound(component));
    }

    public static void showPermissionError(Player player, MutableComponent component) {
        showMessage(player, ModTexts.Messages.errorPermissionDenied(component));
    }

    public static void showVaultItemGiveSuccess(Player player) {
        showMessage(player, ModTexts.Messages.VAULT_ITEM_GIVE_SUCCESS);
    }
}
