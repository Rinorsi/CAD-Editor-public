package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.nbt.CompoundTag;
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
        if (data != null) {
            CompoundTag components = data.getCompound("components").orElse(null);
            if (components != null) {
                CompoundTag profile = components.getCompound("minecraft:profile").orElse(null);
                if (profile != null) {
                    name = NbtHelper.getString(profile, "name", name);
                    uuid = NbtHelper.getString(profile, "id", uuid);
                }
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
        CompoundTag components = data.getCompound("components").orElseGet(() -> {
            CompoundTag created = new CompoundTag();
            data.put("components", created);
            return created;
        });

        if (name.isEmpty() && uuid.isEmpty()) {
            components.remove("minecraft:profile");
        } else {
            CompoundTag profile = new CompoundTag();
            if (!name.isEmpty()) profile.putString("name", name);
            if (!uuid.isEmpty()) profile.putString("id", uuid);
            components.put("minecraft:profile", profile);
        }
        if (components.isEmpty()) {
            data.remove("components");
        }
    }
}

