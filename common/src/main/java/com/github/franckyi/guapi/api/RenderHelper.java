package com.github.franckyi.guapi.api;

import com.github.rinorsi.cadeditor.client.util.AttributeTooltipFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RenderHelper {
    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    private static Font font() {
        return mc().font;
    }

    public static int getFontHeight() {
        return font().lineHeight;
    }

    public static int getFontWidth(Component text) {
        return font().width(text);
    }

    public static void drawString(GuiGraphics guiGraphics, Component text, float x, float y, int color, boolean shadow) {
        guiGraphics.drawString(font(), text, (int) x, (int) y, ensureOpaqueColor(color), shadow);
    }

    public static int ensureOpaqueColor(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    public static void fillRectangle(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int color) {
        guiGraphics.fill(x0, y0, x1, y1, color);
    }

    public static void drawVLine(GuiGraphics guiGraphics, int x, int y0, int y1, int color) {
        fillRectangle(guiGraphics, x, y0, x + 1, y1, color);
    }

    public static void drawHLine(GuiGraphics guiGraphics, int y, int x0, int x1, int color) {
        fillRectangle(guiGraphics, x0, y, x1, y + 1, color);
    }

    public static void drawRectangle(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int color) {
        drawHLine(guiGraphics, y0, x0, x1 - 1, color);
        drawVLine(guiGraphics, x1 - 1, y0, y1 - 1, color);
        drawHLine(guiGraphics, y1 - 1, x1, x0 + 1, color);
        drawVLine(guiGraphics, x0, y1, y0 + 1, color);
    }

    public static void drawTexture(GuiGraphics guiGraphics, ResourceLocation id, int x, int y, int width, int height, int imageX, int imageY, int imageWidth, int imageHeight) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, id, x, y, (float) imageX, (float) imageY, width, height, imageWidth, imageHeight);
    }

    public static void drawSprite(GuiGraphics guiGraphics, TextureAtlasSprite sprite, int x, int y, int imageWidth, int imageHeight) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, imageWidth, imageHeight);
    }

    public static void drawTooltip(GuiGraphics guiGraphics, List<Component> text, int x, int y) {
        renderTooltip(guiGraphics, font(), text, Optional.empty(), x, y);
    }

    public static void drawTooltip(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        Item.TooltipContext context;
        if (minecraft.level != null) {
            context = Item.TooltipContext.of(minecraft.level);
        } else if (minecraft.getConnection() != null) {
            context = Item.TooltipContext.of(minecraft.getConnection().registryAccess());
        } else {
            context = Item.TooltipContext.EMPTY;
        }
        List<Component> lines = AttributeTooltipFormatter.buildTooltipLines(itemStack, context, minecraft.player, TooltipFlag.Default.NORMAL);
        renderTooltip(guiGraphics, font(), lines, itemStack.getTooltipImage(), x, y);
    }

    public static void drawItem(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        guiGraphics.renderFakeItem(itemStack, x, y);
    }

    public static void drawItemDecorations(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        guiGraphics.renderItemDecorations(font(), itemStack, x, y);
    }

    private static void renderTooltip(GuiGraphics guiGraphics, Font font, List<Component> lines, Optional<TooltipComponent> image, int x, int y) {
        List<ClientTooltipComponent> components = new ArrayList<>(lines.size() + (image.isPresent() ? 1 : 0));
        for (Component line : lines) {
            components.add(ClientTooltipComponent.create(line.getVisualOrderText()));
        }
        image.ifPresent(extra -> components.add(components.isEmpty() ? 0 : 1, ClientTooltipComponent.create(extra)));
        guiGraphics.renderTooltip(font, components, x, y, DefaultTooltipPositioner.INSTANCE, null);
    }
}
