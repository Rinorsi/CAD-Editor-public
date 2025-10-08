package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.EnchantmentEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ItemEnchantmentsCategoryModel extends ItemEditorCategoryModel {
    private ListTag newEnch;

    public ItemEnchantmentsCategoryModel(ItemEditorModel editor) {
        super(ModTexts.ENCHANTMENTS, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getStack();
        ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
        ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
        boolean any = false;
        //TODO 做个附魔冲突小提示
        if (ench != null && !ench.isEmpty()) {
            any = true;
            ench.entrySet().stream()
                    .map(this::createEnchantment)
                    .forEach(getEntries()::add);
        }
        if (stored != null && !stored.isEmpty()) {
            any = true;
            stored.entrySet().stream()
                    .map(this::createEnchantment)
                    .forEach(getEntries()::add);
        }
        if (!any && getData().contains("tag", Tag.TAG_COMPOUND) && getTag().contains("Enchantments", Tag.TAG_LIST)) {
            getTag().getList("Enchantments", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(this::createEnchantment)
                    .forEach(getEntries()::add);
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
            EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
            clearLegacyEnchantments();
        } else {
            if (!newEnch.isEmpty()) {
                getOrCreateTag().put("Enchantments", newEnch);
            } else if (getData().contains("tag", Tag.TAG_COMPOUND) && getTag().contains("Enchantments")) {
                getTag().remove("Enchantments");
            }
        }
    }

    private ItemStack getStack() {
        return getParent().getContext().getItemStack();
    }

    private void clearLegacyEnchantments() {
        if (getData().contains("tag", Tag.TAG_COMPOUND)) {
            CompoundTag tag = getTag();
            if (tag.contains("Enchantments")) {
                tag.remove("Enchantments");
            }
        }
    }
}
