
package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ItemBannerPatternCategoryModel extends ItemEditorCategoryModel {
    private String baseColorName = "white";
    private final List<StringEntryModel> layerEntries = new ArrayList<>();

    public ItemBannerPatternCategoryModel(ItemEditorModel editor) {
        super(ModTexts.BANNER, editor);
    }

    @Override
    protected void setupEntries() {
        layerEntries.clear();
        ItemStack stack = getParent().getContext().getItemStack();
        DyeColor base = stack.get(DataComponents.BASE_COLOR);
        if (base != null) {
            baseColorName = base.getName();
        }
        getEntries().add(new StringEntryModel(this, ModTexts.BANNER_BASE_COLOR, baseColorName,
                value -> baseColorName = value == null ? "" : value.trim()));
        BannerPatternLayers layers = stack.get(DataComponents.BANNER_PATTERNS);
        if (layers != null) {
            layers.layers().forEach(layer -> getEntries().add(createLayerEntry(formatLayer(layer))));
        }
    }

    @Override
    public int getEntryListStart() {
        return 1;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createLayerEntry("");
    }

    private EntryModel createLayerEntry(String spec) {
        StringEntryModel entry = new StringEntryModel(this, ModTexts.BANNER_LAYER, spec, value -> { });
        layerEntries.add(entry);
        return entry;
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        DyeColor base = DyeColor.byName(baseColorName, null);
        if (base != null) {
            stack.set(DataComponents.BASE_COLOR, base);
        } else {
            stack.remove(DataComponents.BASE_COLOR);
        }

        HolderLookup.RegistryLookup<BannerPattern> patternLookup = ClientUtil.registryAccess()
                .lookup(Registries.BANNER_PATTERN)
                .orElse(null);
        List<BannerPatternLayers.Layer> parsedLayers = new ArrayList<>();
        boolean hasInvalid = false;
        for (StringEntryModel entry : layerEntries) {
            String spec = Optional.ofNullable(entry.getValue()).orElse("").trim();
            if (spec.isBlank()) {
                entry.setValid(true);
                continue;
            }
            Optional<BannerPatternLayers.Layer> parsed = parseLayer(spec, patternLookup);
            if (parsed.isPresent()) {
                parsedLayers.add(parsed.get());
                entry.setValid(true);
            } else {
                entry.setValid(false);
                hasInvalid = true;
            }
        }
        if (hasInvalid) {
            return;
        }
        if (parsedLayers.isEmpty()) {
            stack.remove(DataComponents.BANNER_PATTERNS);
        } else {
            stack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(parsedLayers));
        }
        CompoundTag data = getData();
        if (data != null && data.contains("components")) {
            CompoundTag components = data.getCompound("components");
            components.remove("minecraft:banner_patterns");
            components.remove("minecraft:base_color");
            if (components.isEmpty()) {
                data.remove("components");
            }
        }
    }

    private String formatLayer(BannerPatternLayers.Layer layer) {
        String patternId = layer.pattern().unwrapKey()
                .map(ResourceKey::location)
                .map(ResourceLocation::toString)
                .orElse("");
        return patternId + "|" + layer.color().getName();
    }

    private Optional<BannerPatternLayers.Layer> parseLayer(String spec, HolderLookup.RegistryLookup<BannerPattern> lookup) {
        String[] parts = spec.split("\\|", -1);
        if (parts.length < 2) {
            return Optional.empty();
        }
        ResourceLocation patternId = ResourceLocation.tryParse(parts[0].trim());
        if (patternId == null || lookup == null) {
            return Optional.empty();
        }
        Optional<Holder.Reference<BannerPattern>> pattern = lookup.get(ResourceKey.create(Registries.BANNER_PATTERN, patternId));
        if (pattern.isEmpty()) {
            return Optional.empty();
        }
        DyeColor color = DyeColor.byName(parts[1].trim().toLowerCase(Locale.ROOT), null);
        if (color == null) {
            return Optional.empty();
        }
        return Optional.of(new BannerPatternLayers.Layer(pattern.get(), color));
    }
}
