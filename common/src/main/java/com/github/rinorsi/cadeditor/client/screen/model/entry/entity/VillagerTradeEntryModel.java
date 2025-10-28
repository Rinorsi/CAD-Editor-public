package com.github.rinorsi.cadeditor.client.screen.model.entry.entity;

import com.github.franckyi.databindings.api.BooleanProperty;
import com.github.franckyi.databindings.api.IntegerProperty;
import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityCategoryModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("this-escape")
public class VillagerTradeEntryModel extends EntryModel {
    private static final Set<String> KNOWN_KEYS = Set.of(
            "buy", "buyB", "sell",
            "maxUses", "uses", "demand", "specialPrice",
            "priceMultiplier", "rewardExp", "xp"
    );

    private final ObjectProperty<ItemStack> primaryItemProperty;
    private final ObjectProperty<ItemStack> secondaryItemProperty;
    private final ObjectProperty<ItemStack> resultItemProperty;
    private final IntegerProperty maxUsesProperty;
    private final IntegerProperty usesProperty;
    private final IntegerProperty demandProperty;
    private final IntegerProperty specialPriceProperty;
    private final ObjectProperty<Float> priceMultiplierProperty;
    private final BooleanProperty rewardExpProperty;
    private final IntegerProperty xpProperty;

    private ItemStack defaultPrimaryItem;
    private ItemStack defaultSecondaryItem;
    private ItemStack defaultResultItem;
    private int defaultMaxUses;
    private int defaultUses;
    private int defaultDemand;
    private int defaultSpecialPrice;
    private float defaultPriceMultiplier;
   private boolean defaultRewardExp;
   private int defaultXp;

    private final CompoundTag originalTag;
    private boolean editorFieldsValid = true;

    public VillagerTradeEntryModel(EntityCategoryModel category, CompoundTag tag) {
        super(category);
        CompoundTag source = tag == null ? new CompoundTag() : tag.copy();

        primaryItemProperty = ObjectProperty.create(readItem(source, "buy"));
        secondaryItemProperty = ObjectProperty.create(readItem(source, "buyB"));
        resultItemProperty = ObjectProperty.create(readItem(source, "sell"));
        maxUsesProperty = IntegerProperty.create(source.contains("maxUses", Tag.TAG_INT) ? source.getInt("maxUses") : 12);
        usesProperty = IntegerProperty.create(source.contains("uses", Tag.TAG_INT) ? source.getInt("uses") : 0);
        demandProperty = IntegerProperty.create(source.contains("demand", Tag.TAG_INT) ? source.getInt("demand") : 0);
        specialPriceProperty = IntegerProperty.create(source.contains("specialPrice", Tag.TAG_INT) ? source.getInt("specialPrice") : 0);
        priceMultiplierProperty = ObjectProperty.create(source.contains("priceMultiplier", Tag.TAG_FLOAT) ? source.getFloat("priceMultiplier") : 0.05f);
        rewardExpProperty = BooleanProperty.create(!source.contains("rewardExp", Tag.TAG_BYTE) || source.getBoolean("rewardExp"));
        xpProperty = IntegerProperty.create(source.contains("xp", Tag.TAG_INT) ? source.getInt("xp") : 0);

        originalTag = source;

        captureDefaults();
        setupListeners();
    }

    private void setupListeners() {
        primaryItemProperty.addListener(value -> updateValidity());
        resultItemProperty.addListener(value -> updateValidity());
        maxUsesProperty.addListener(value -> updateValidity());
        usesProperty.addListener(value -> updateValidity());
        priceMultiplierProperty.addListener(value -> updateValidity());
        updateValidity();
    }

    private static ItemStack readItem(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.parseOptional(ClientUtil.registryAccess(), tag.getCompound(key));
    }

    private ItemStack sanitize(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        if (copy.getCount() <= 0) {
            copy.setCount(1);
        }
        return copy;
    }

    public ObjectProperty<ItemStack> primaryItemProperty() {
        return primaryItemProperty;
    }

    public ItemStack getPrimaryItem() {
        ItemStack stack = primaryItemProperty.getValue();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public void setPrimaryItem(ItemStack stack) {
        ItemStack sanitized = sanitize(stack);
        if (!ItemStack.matches(sanitized, getPrimaryItem())) {
            primaryItemProperty.setValue(sanitized);
        }
    }

    public ObjectProperty<ItemStack> secondaryItemProperty() {
        return secondaryItemProperty;
    }

    public ItemStack getSecondaryItem() {
        ItemStack stack = secondaryItemProperty.getValue();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public void setSecondaryItem(ItemStack stack) {
        ItemStack sanitized = sanitize(stack);
        if (!ItemStack.matches(sanitized, getSecondaryItem())) {
            secondaryItemProperty.setValue(sanitized);
        }
    }

    public ObjectProperty<ItemStack> resultItemProperty() {
        return resultItemProperty;
    }

    public ItemStack getResultItem() {
        ItemStack stack = resultItemProperty.getValue();
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public void setResultItem(ItemStack stack) {
        ItemStack sanitized = sanitize(stack);
        if (!ItemStack.matches(sanitized, getResultItem())) {
            resultItemProperty.setValue(sanitized);
        }
    }

    public IntegerProperty maxUsesProperty() {
        return maxUsesProperty;
    }

    public int getMaxUses() {
        return maxUsesProperty.getValue();
    }

    public void setMaxUses(int value) {
        maxUsesProperty.setValue(Math.max(1, value));
    }

    public IntegerProperty usesProperty() {
        return usesProperty;
    }

    public int getUses() {
        return usesProperty.getValue();
    }

    public void setUses(int value) {
        usesProperty.setValue(Math.max(0, value));
    }

    public IntegerProperty demandProperty() {
        return demandProperty;
    }

    public int getDemand() {
        return demandProperty.getValue();
    }

    public void setDemand(int value) {
        demandProperty.setValue(value);
    }

    public IntegerProperty specialPriceProperty() {
        return specialPriceProperty;
    }

    public int getSpecialPrice() {
        return specialPriceProperty.getValue();
    }

    public void setSpecialPrice(int value) {
        specialPriceProperty.setValue(value);
    }

    public ObjectProperty<Float> priceMultiplierProperty() {
        return priceMultiplierProperty;
    }

    public float getPriceMultiplier() {
        Float value = priceMultiplierProperty.getValue();
        return value == null ? 0.05f : value;
    }

    public void setPriceMultiplier(float value) {
        float sanitized = Float.isFinite(value) ? value : 0.05f;
        priceMultiplierProperty.setValue(sanitized);
    }

    public BooleanProperty rewardExpProperty() {
        return rewardExpProperty;
    }

    public boolean isRewardExp() {
        return rewardExpProperty.getValue();
    }

    public void setRewardExp(boolean value) {
        rewardExpProperty.setValue(value);
    }

    public IntegerProperty xpProperty() {
        return xpProperty;
    }

    public int getXp() {
        return xpProperty.getValue();
    }

    public void setXp(int value) {
        xpProperty.setValue(Math.max(0, value));
    }

    public boolean hasSecondaryItem() {
        return !getSecondaryItem().isEmpty();
    }

    public boolean isTradeValid() {
        return isValid()
                && !getResultItem().isEmpty()
                && getMaxUses() >= getUses();
    }

    public CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        if (originalTag != null) {
            Set<String> keys = new HashSet<>(originalTag.getAllKeys());
            for (String key : keys) {
                if (!KNOWN_KEYS.contains(key)) {
                    Tag value = originalTag.get(key);
                    if (value != null) {
                        tag.put(key, value.copy());
                    }
                }
            }
        }

        tag.put("buy", createItemTag(getPrimaryItem()));
        if (hasSecondaryItem()) {
            tag.put("buyB", createItemTag(getSecondaryItem()));
        } else {
            tag.remove("buyB");
        }
        tag.put("sell", createItemTag(getResultItem()));
        tag.putInt("maxUses", Math.max(1, getMaxUses()));
        tag.putInt("uses", Math.max(0, getUses()));
        tag.putInt("demand", getDemand());
        tag.putInt("specialPrice", getSpecialPrice());
        tag.putFloat("priceMultiplier", getPriceMultiplier());
        tag.putBoolean("rewardExp", isRewardExp());
        tag.putInt("xp", Math.max(0, getXp()));

        return tag;
    }

    private CompoundTag createItemTag(ItemStack stack) {
        if (stack.isEmpty()) {
            return new CompoundTag();
        }
        return (CompoundTag) stack.save(ClientUtil.registryAccess(), new CompoundTag());
    }

    @Override
    public void reset() {
        setPrimaryItem(defaultPrimaryItem);
        setSecondaryItem(defaultSecondaryItem);
        setResultItem(defaultResultItem);
        setMaxUses(defaultMaxUses);
        setUses(defaultUses);
        setDemand(defaultDemand);
        setSpecialPrice(defaultSpecialPrice);
        setPriceMultiplier(defaultPriceMultiplier);
        setRewardExp(defaultRewardExp);
        setXp(defaultXp);
        setValid(true);
        setEditorFieldsValid(true);
    }

    @Override
    public void apply() {
        captureDefaults();
        setValid(true);
    }

    private void captureDefaults() {
        defaultPrimaryItem = getPrimaryItem().copy();
        defaultSecondaryItem = getSecondaryItem().copy();
        defaultResultItem = getResultItem().copy();
        defaultMaxUses = getMaxUses();
        defaultUses = getUses();
        defaultDemand = getDemand();
        defaultSpecialPrice = getSpecialPrice();
        defaultPriceMultiplier = getPriceMultiplier();
        defaultRewardExp = isRewardExp();
        defaultXp = getXp();
    }

    private void updateValidity() {
        boolean valid = true;
        if (getResultItem().isEmpty()) {
            valid = false;
        }
        if (getMaxUses() < 1) {
            valid = false;
        }
        if (getUses() < 0 || getUses() > getMaxUses()) {
            valid = false;
        }
        Float multiplier = priceMultiplierProperty.getValue();
        if (multiplier == null || !Float.isFinite(multiplier)) {
            valid = false;
        }
        if (!editorFieldsValid) {
            valid = false;
        }
        setValid(valid);
    }

    public void setEditorFieldsValid(boolean value) {
        if (editorFieldsValid != value) {
            editorFieldsValid = value;
            updateValidity();
        }
    }

    @Override
    public Type getType() {
        return Type.VILLAGER_TRADE;
    }
}
