package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.EnchantmentEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ItemEnchantmentsCategoryModel extends ItemEditorCategoryModel {
    private ListTag newEnch;
    private boolean editingStored;

    public ItemEnchantmentsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.ENCHANTMENTS, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getStack();
        editingStored = shouldEditStoredEnchantments(stack);
        ItemEnchantments active = stack.get(getCurrentComponentType());
        boolean any = false;
        if (active != null && !active.isEmpty()) {
            any = true;
            active.entrySet().stream()
                    .map(this::createEnchantment)
                    .forEach(getEntries()::add);
        }
        if (!any) {
            CompoundTag tag = getTag();
            if (tag != null && tag.contains(getLegacyKey(), Tag.TAG_LIST)) {
                tag.getList(getLegacyKey(), Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(this::createEnchantment)
                    .forEach(getEntries()::add);
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
        return createEnchantment(tag.getString("id"), tag.getInt("lvl"));
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
        if (lookupOpt.isPresent()) {
            var lookup = lookupOpt.get();
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (Tag tag : newEnch) {
                if (tag instanceof CompoundTag compoundTag) {
                    String id = compoundTag.getString("id");
                    int lvl = compoundTag.getInt("lvl");
                    if (lvl > 0) {
                        ResourceLocation rl = ResourceLocation.tryParse(id);
                        if (rl != null) {
                            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, rl);
                            lookup.get(key).ifPresent(holder -> mutable.set(holder, lvl));
                        }
                    }
                }
            }
            ItemEnchantments compiled = mutable.toImmutable();
            if (compiled.isEmpty()) {
                stack.remove(getCurrentComponentType());
            } else {
                stack.set(getCurrentComponentType(), compiled);
            }
            clearLegacyEnchantments();
        } else {
            if (!newEnch.isEmpty()) {
                getOrCreateTag().put(getLegacyKey(), newEnch);
            } else if (getTag() != null && getTag().contains(getLegacyKey())) {
                getTag().remove(getLegacyKey());
            }
        }
    }

    private ItemStack getStack() {
        return getParent().getContext().getItemStack();
    }

    private void clearLegacyEnchantments() {
        if (getData().contains("tag", Tag.TAG_COMPOUND)) {
            CompoundTag tag = getTag();
            if (tag != null && tag.contains(getLegacyKey())) {
                tag.remove(getLegacyKey());
            }
        }
    }

    private boolean shouldEditStoredEnchantments(ItemStack stack) {
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        ItemEnchantments regular = stack.get(DataComponents.ENCHANTMENTS);
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return true;
        }
        return stored != null && !stored.isEmpty() && (regular == null || regular.isEmpty());
    }

    private DataComponentType<ItemEnchantments> getCurrentComponentType() {
        return editingStored ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }

    private String getLegacyKey() {
        return editingStored ? "StoredEnchantments" : "Enchantments";
    }
}
