package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.EnchantmentEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ItemEnchantmentsCategoryModel extends ItemEditorCategoryModel {
    private static final Logger LOGGER = LogManager.getLogger();
    private ListTag newEnch;
    private boolean editingStored;

    public ItemEnchantmentsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.ENCHANTMENTS, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getStack();
        editingStored = shouldEditStored(stack);
        ItemEnchantments target = stack.get(editingStored ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS);
        boolean any = false;
        if (target != null && !target.isEmpty()) {
            any = true;
            target.entrySet().stream()
                    .map(this::createEnchantment)
                    .forEach(getEntries()::add);
        }
        if (!any) {
            CompoundTag data = getData();
            CompoundTag legacyTag = data != null ? data.getCompound("tag").orElse(null) : null;
            if (legacyTag != null) {
                String legacyKey = editingStored ? "StoredEnchantments" : "Enchantments";
                ListTag enchantList = legacyTag.getList(legacyKey).orElse(null);
                if (enchantList != null) {
                    enchantList.stream()
                            .map(CompoundTag.class::cast)
                            .map(this::createEnchantment)
                            .forEach(getEntries()::add);
                }
            }
        }
    }

    @Override
    public int getEntryListStart() {
        return 0;
    }

    @Override
    protected MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.ENCHANTMENTS;
    }

    @Override
    public EntryModel createNewListEntry() {
        return createEnchantment("", 0);
    }

    private EnchantmentEntryModel createEnchantment(Object2IntMap.Entry<Holder<Enchantment>> entry) {
        Holder<Enchantment> holder = entry.getKey();
        String id = holder.unwrapKey().map(key -> key.location().toString()).orElse("");
        return createEnchantment(id, entry.getIntValue());
    }

    private EnchantmentEntryModel createEnchantment(CompoundTag tag) {
        return createEnchantment(NbtHelper.getString(tag, "id", ""), tag.getIntOr("lvl", 0));
    }

    private EnchantmentEntryModel createEnchantment(String id, int level) {
        return new EnchantmentEntryModel(this, id, level, this::addEnchantment);
    }

    public Set<ResourceLocation> getExistingEnchantmentIds() {
        Set<ResourceLocation> set = new HashSet<>();
        getEntries().stream()
                .filter(EnchantmentEntryModel.class::isInstance)
                .map(EnchantmentEntryModel.class::cast)
                .map(EnchantmentEntryModel::getValue)
                .map(this::normalizeId)
                .filter(Objects::nonNull)
                .forEach(set::add);
        return set;
    }

    public void addEnchantmentEntryIfAbsent(String id, int level) {
        ResourceLocation rl = normalizeId(id);
        if (rl == null || getExistingEnchantmentIds().contains(rl)) {
            return;
        }
        EnchantmentEntryModel entry = createEnchantment(rl.toString(), level);
        int insertIndex = canAddEntryInList() ? Math.max(getEntries().size() - 1, getEntryListStart()) : getEntries().size();
        getEntries().add(insertIndex, entry);
        updateEntryListIndexes();
    }

    private ResourceLocation normalizeId(String id) {
        String value = id.contains(":") ? id : "minecraft:" + id;
        return ResourceLocation.tryParse(value);
    }

    private void addEnchantment(String id, int lvl) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putInt("lvl", lvl);
        newEnch.add(tag);
    }

    @Override
    public void apply() {
        newEnch = new ListTag();
        super.apply();
        ItemStack stack = getStack();
        var lookupOpt = ClientUtil.registryAccess().lookup(Registries.ENCHANTMENT);
        if (lookupOpt.isEmpty()) {
            LOGGER.error("Missing enchantment registry; cannot apply enchantments to {}", stack);
            return;
        }
        var lookup = lookupOpt.get();
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Tag tag : newEnch) {
            if (!(tag instanceof CompoundTag compoundTag)) {
                continue;
            }
            String id = NbtHelper.getString(compoundTag, "id", "");
            int lvl = compoundTag.getIntOr("lvl", 0);
            if (lvl <= 0) {
                continue;
            }
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl == null) {
                continue;
            }
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, rl);
            lookup.get(key).ifPresent(holder -> mutable.set(holder, lvl));
        }
        ItemEnchantments applied = mutable.toImmutable();
        if (editingStored) {
            if (applied.isEmpty()) {
                stack.remove(DataComponents.STORED_ENCHANTMENTS);
            } else {
                stack.set(DataComponents.STORED_ENCHANTMENTS, applied);
            }
        } else {
            if (applied.isEmpty()) {
                stack.remove(DataComponents.ENCHANTMENTS);
            } else {
                stack.set(DataComponents.ENCHANTMENTS, applied);
            }
        }
        clearLegacyEnchantments();
    }

    private ItemStack getStack() {
        return getParent().getContext().getItemStack();
    }

    private void clearLegacyEnchantments() {
        CompoundTag data = getData();
        if (data == null) {
            return;
        }
        CompoundTag tag = data.getCompound("tag").orElse(null);
        if (tag == null) {
            return;
        }
        String key = editingStored ? "StoredEnchantments" : "Enchantments";
        if (tag.contains(key)) {
            tag.remove(key);
        }
    }

    private boolean shouldEditStored(ItemStack stack) {
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        ItemEnchantments normal = stack.get(DataComponents.ENCHANTMENTS);
        return stored != null && (normal == null || normal.isEmpty());
    }
}
