package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.InstrumentSelectionEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.component.InstrumentComponent;

import java.util.Optional;

public class ItemInstrumentCategoryModel extends ItemEditorCategoryModel {
    private String instrumentId = "";
    private InstrumentSelectionEntryModel instrumentEntry;

    public ItemInstrumentCategoryModel(ItemEditorModel editor) {
        super(ModTexts.INSTRUMENT, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        InstrumentComponent component = stack.get(DataComponents.INSTRUMENT);
        if (component != null) {
            HolderLookup.Provider provider = ClientUtil.registryAccess();
            if (provider != null) {
                component.unwrap(provider)
                        .flatMap(Holder::unwrapKey)
                        .map(ResourceKey::location)
                        .map(ResourceLocation::toString)
                        .ifPresent(id -> instrumentId = id);
            }
        }
        instrumentEntry = new InstrumentSelectionEntryModel(this, instrumentId,
                value -> instrumentId = value == null ? "" : value.trim());
        getEntries().add(instrumentEntry);
        //TODO 或许可以搞个试听？（实现起来比较麻烦，排期靠后）
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        HolderLookup.RegistryLookup<Instrument> lookup = ClientUtil.registryAccess()
                .lookup(Registries.INSTRUMENT)
                .orElse(null);
        if (lookup == null || instrumentId.isBlank()) {
            instrumentEntry.setValid(true);
            stack.remove(DataComponents.INSTRUMENT);
            return;
        }
        ResourceLocation location = ResourceLocation.tryParse(instrumentId);
        if (location == null) {
            instrumentEntry.setValid(false);
            return;
        }
        ResourceKey<Instrument> key = ResourceKey.create(Registries.INSTRUMENT, location);
        Optional<Holder.Reference<Instrument>> instrumentHolder = lookup.get(key);
        if (instrumentHolder.isEmpty()) {
            instrumentEntry.setValid(false);
            return;
        }
        instrumentEntry.setValid(true);
        stack.set(DataComponents.INSTRUMENT, new InstrumentComponent(instrumentHolder.get()));
    }
}
