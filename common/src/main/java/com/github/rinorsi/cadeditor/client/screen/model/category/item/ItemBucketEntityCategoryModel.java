package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EnumEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

@SuppressWarnings("unused")
public class ItemBucketEntityCategoryModel extends ItemEditorCategoryModel {
    private boolean advancedMode;
    private String snbtData = "";

    private AxolotlVariant axolotlVariant = AxolotlVariant.LEUCISTIC;
    private TropicalPattern tropicalPattern = TropicalPattern.KOB;
    private DyeColor tropicalBodyColor = DyeColor.WHITE;
    private DyeColor tropicalPatternColor = DyeColor.ORANGE;

    private BooleanEntryModel advancedToggleEntry;
    private EnumEntryModel<AxolotlVariant> axolotlEntry;
    private EnumEntryModel<TropicalPattern> tropicalPatternEntry;
    private EnumEntryModel<DyeColor> tropicalBodyEntry;
    private EnumEntryModel<DyeColor> tropicalPatternEntryColor;
    private StringEntryModel snbtEntry;

    public ItemBucketEntityCategoryModel(ItemEditorModel editor) {
        super(ModTexts.BUCKET_ENTITY, editor);
    }

    @Override
    protected void setupEntries() {
        ItemStack stack = getParent().getContext().getItemStack();
        Item item = stack.getItem();
        CustomData data = stack.get(DataComponents.BUCKET_ENTITY_DATA);
        snbtData = data != null ? data.copyTag().toString() : "";

        advancedMode = false;

        advancedToggleEntry = new BooleanEntryModel(this, ModTexts.BUCKET_USE_ADVANCED, advancedMode,
                value -> advancedMode = value != null && value);
        getEntries().add(advancedToggleEntry);

        if (item == Items.AXOLOTL_BUCKET) {
            if (!advancedMode && data != null && data.copyTag().contains("Variant")) {
                axolotlVariant = AxolotlVariant.byId(data.copyTag().getInt("Variant"));
            }
            axolotlEntry = new EnumEntryModel<>(this, ModTexts.BUCKET_AXOLOTL_VARIANT, AxolotlVariant.values(), axolotlVariant,
                    value -> axolotlVariant = value == null ? AxolotlVariant.LEUCISTIC : value);
            getEntries().add(axolotlEntry);
        } else if (item == Items.TROPICAL_FISH_BUCKET) {
            if (!advancedMode && data != null && data.copyTag().contains("BucketVariantTag")) {
                int encoded = data.copyTag().getInt("BucketVariantTag");
                tropicalPattern = TropicalPattern.byId((encoded >> 8) & 0xFF);
                tropicalBodyColor = DyeColor.byId((encoded >> 4) & 0xF);
                tropicalPatternColor = DyeColor.byId(encoded & 0xF);
            }
            tropicalPatternEntry = new EnumEntryModel<>(this, ModTexts.BUCKET_TROPICAL_PATTERN, TropicalPattern.values(), tropicalPattern,
                    value -> tropicalPattern = value == null ? TropicalPattern.KOB : value);
            tropicalBodyEntry = new EnumEntryModel<>(this, ModTexts.BUCKET_TROPICAL_BODY_COLOR, DyeColor.values(), tropicalBodyColor,
                    value -> tropicalBodyColor = value == null ? DyeColor.WHITE : value);
            tropicalPatternEntryColor = new EnumEntryModel<>(this, ModTexts.BUCKET_TROPICAL_PATTERN_COLOR, DyeColor.values(), tropicalPatternColor,
                    value -> tropicalPatternColor = value == null ? DyeColor.ORANGE : value);
            getEntries().add(tropicalPatternEntry);
            getEntries().add(tropicalBodyEntry);
            getEntries().add(tropicalPatternEntryColor);

        }

        snbtEntry = new StringEntryModel(this, ModTexts.BUCKET_ENTITY_DATA, snbtData,
                value -> snbtData = value == null ? "" : value.trim());
        getEntries().add(snbtEntry);
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getParent().getContext().getItemStack();
        Item item = stack.getItem();
        if (advancedMode) {
            if (snbtData.isBlank()) {
                stack.remove(DataComponents.BUCKET_ENTITY_DATA);
                snbtEntry.setValid(true);
                return;
            }
            try {
                CompoundTag tag = TagParser.parseTag(snbtData);
                stack.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(tag));
                snbtEntry.setValid(true);
            } catch (Exception ex) {
                snbtEntry.setValid(false);
            }
            return;
        }

        CompoundTag tag = new CompoundTag();
        if (item == Items.AXOLOTL_BUCKET) {
            tag.putInt("Variant", axolotlVariant.ordinal());
        } else if (item == Items.TROPICAL_FISH_BUCKET) {
            tag.putInt("BucketVariantTag", encodeTropicalVariant());
        }
        if (tag.isEmpty()) {
            stack.remove(DataComponents.BUCKET_ENTITY_DATA);
        } else {
            stack.set(DataComponents.BUCKET_ENTITY_DATA, CustomData.of(tag));
        }
    }

    private int encodeTropicalVariant() {
        int patternId = tropicalPattern.getId() & 0xFF;
        int body = tropicalBodyColor.getId() & 0xF;
        int patternColor = tropicalPatternColor.getId() & 0xF;
        return (patternId << 8) | (body << 4) | patternColor;
    }

    public enum AxolotlVariant {
        LEUCISTIC,
        WILD,
        GOLD,
        CYAN,
        BLUE;

        public static AxolotlVariant byId(int id) {
            if (id < 0 || id >= values().length) {
                return LEUCISTIC;
            }
            return values()[id];
        }
    }

    public enum TropicalPattern {
        KOB(0),
        SUNSTREAK(1),
        SNOOPER(2),
        DASHER(3),
        BRINELY(4),
        SPOTTY(5),
        FLOPPER(6),
        STRIPEY(7),
        GLITTER(8),
        BLOCKFISH(9),
        BETTY(10),
        CLAYFISH(11);

        private final int id;

        TropicalPattern(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static TropicalPattern byId(int id) {
            for (TropicalPattern pattern : values()) {
                if (pattern.id == (id & 0xFF)) {
                    return pattern;
                }
            }
            return KOB;
        }
    }
}

