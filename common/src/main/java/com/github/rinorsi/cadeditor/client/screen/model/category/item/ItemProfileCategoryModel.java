package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@SuppressWarnings("unused")
public class ItemProfileCategoryModel extends ItemEditorCategoryModel {
    private StringEntryModel nameEntry;
    private StringEntryModel uuidEntry;

    public ItemProfileCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("profile"), editor);
    }

    @Override
    protected void setupEntries() {
        String name = "";
        String uuid = "";

        CompoundTag data = getData();
        if (data != null && data.contains("components", Tag.TAG_COMPOUND)) {
            CompoundTag components = data.getCompound("components");
            if (components.contains("minecraft:profile", Tag.TAG_COMPOUND)) {
                CompoundTag profile = components.getCompound("minecraft:profile");
                if (profile.contains("name", Tag.TAG_STRING)) name = profile.getString("name");
                if (profile.contains("id", Tag.TAG_STRING)) uuid = profile.getString("id");
            }
        }

        nameEntry = new StringEntryModel(this, ModTexts.gui("player_name"), name, v -> {});
        uuidEntry = new StringEntryModel(this, ModTexts.gui("uuid"), uuid, v -> {});
        getEntries().add(nameEntry);
        getEntries().add(uuidEntry);
    }

    @Override
    public int getEntryListStart() { return -1; }

    @Override
    public void apply() {
        super.apply();
        String name = nameEntry.getValue() == null ? "" : nameEntry.getValue().trim();
        String uuid = uuidEntry.getValue() == null ? "" : uuidEntry.getValue().trim();

        CompoundTag data = getData();
        if (data == null) return;
        CompoundTag components = data.contains("components", Tag.TAG_COMPOUND)
                ? data.getCompound("components") : new CompoundTag();

        if (name.isEmpty() && uuid.isEmpty()) {
            components.remove("minecraft:profile");
        } else {
            CompoundTag profile = new CompoundTag();
            if (!name.isEmpty()) profile.putString("name", name);
            if (!uuid.isEmpty()) profile.putString("id", uuid);
            components.put("minecraft:profile", profile);
        }
        if (!data.contains("components", Tag.TAG_COMPOUND) && !components.isEmpty()) {
            data.put("components", components);
        }
    }
}

