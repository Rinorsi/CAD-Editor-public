package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemEditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ToolRuleEntryModel extends EntryModel {
    private final List<ResourceLocation> blockIds = new ArrayList<>();
    private final List<ResourceLocation> tagIds = new ArrayList<>();
    private final List<ResourceLocation> defaultBlockIds = new ArrayList<>();
    private final List<ResourceLocation> defaultTagIds = new ArrayList<>();
    private final ObjectProperty<DropBehavior> behaviorProperty = ObjectProperty.create(DropBehavior.INHERIT);
    private DropBehavior defaultBehavior = DropBehavior.INHERIT;
    private String speedText = "";
    private Float speedValue;
    private String defaultSpeedText = "";
    private Float defaultSpeedValue;

    public ToolRuleEntryModel(ItemEditorCategoryModel category) {
        this(category, null);
    }

    public ToolRuleEntryModel(ItemEditorCategoryModel category, Tool.Rule rule) {
        super(category);
        if (rule != null) {
            rule.blocks().unwrapKey().ifPresent(tagKey -> tagIds.add(tagKey.location()));
            if (tagIds.isEmpty()) {
                rule.blocks().stream()
                        .map(holder -> holder.unwrapKey().map(ResourceKey::location).orElse(null))
                        .filter(id -> id != null)
                        .forEach(blockIds::add);
            }
            rule.speed().ifPresent(value -> {
                speedValue = value;
                speedText = trimFloat(value);
            });
            behaviorProperty.setValue(DropBehavior.fromOptional(rule.correctForDrops()));
        }
        captureDefaults();
    }

    public List<ResourceLocation> getBlockIds() {
        return List.copyOf(blockIds);
    }

    public void setBlockIds(List<ResourceLocation> ids) {
        blockIds.clear();
        ids.stream().distinct().forEach(blockIds::add);
    }

    public List<ResourceLocation> getTagIds() {
        return List.copyOf(tagIds);
    }

    public void setTagIds(List<ResourceLocation> ids) {
        tagIds.clear();
        ids.stream().distinct().forEach(tagIds::add);
    }

    public void clearSelections() {
        blockIds.clear();
        tagIds.clear();
    }

    public DropBehavior getBehavior() {
        return behaviorProperty.getValue();
    }

    public ObjectProperty<DropBehavior> behaviorProperty() {
        return behaviorProperty;
    }

    public void setBehavior(DropBehavior behavior) {
        behaviorProperty.setValue(behavior == null ? DropBehavior.INHERIT : behavior);
    }

    public String getSpeedText() {
        return speedText;
    }

    public boolean setSpeedText(String text) {
        String trimmed = text == null ? "" : text.trim();
        speedText = trimmed;
        if (trimmed.isEmpty()) {
            speedValue = null;
            return true;
        }
        try {
            float parsed = Float.parseFloat(trimmed);
            speedValue = parsed;
            return true;
        } catch (NumberFormatException ex) {
            speedValue = null;
            return false;
        }
    }

    public boolean hasSelection() {
        return !blockIds.isEmpty() || !tagIds.isEmpty();
    }

    public Component getSummaryComponent() {
        if (!hasSelection()) {
            return ModTexts.gui("tool_rule_blocks_empty").copy().withStyle(ChatFormatting.GRAY);
        }
        List<Component> entries = buildSummaryEntries();
        if (entries.isEmpty()) {
            return ModTexts.gui("tool_rule_blocks_empty").copy().withStyle(ChatFormatting.GRAY);
        }
        MutableComponent first = entries.get(0).copy();
        String text = first.getString();
        int maxLength = 255;
        if (text.length() > maxLength) {
            first = Component.literal(text.substring(0, maxLength - 1) + "...").withStyle(first.getStyle());
        }
        if (entries.size() > 1) {
            first.append(Component.literal(" +" + (entries.size() - 1)).withStyle(ChatFormatting.DARK_GRAY));
        }
        return first;
    }

    public List<Component> getSummaryTooltip() {
        List<Component> tooltip = new ArrayList<>();
        if (!hasSelection()) {
            tooltip.add(ModTexts.gui("tool_rule_blocks_empty").copy().withStyle(ChatFormatting.GRAY));
        } else {
            addSectionLines(tooltip, ModTexts.gui("tool_rule_tags").copy(), tagIds, true);
            addSectionLines(tooltip, ModTexts.gui("tool_rule_blocks").copy(), blockIds, false);
        }
        tooltip.add(Component.empty());
        tooltip.add(buildSpeedTooltip());
        tooltip.add(buildBehaviorTooltip());
        return tooltip;
    }

    private List<Component> buildSummaryEntries() {
        List<Component> entries = new ArrayList<>();
        tagIds.forEach(id -> entries.add(Component.literal("#" + id).withStyle(ChatFormatting.AQUA)));
        blockIds.forEach(id -> entries.add(Component.literal(id.toString()).withStyle(ChatFormatting.WHITE)));
        return entries;
    }

    private void addSectionLines(List<Component> tooltip, MutableComponent title, List<ResourceLocation> ids, boolean tags) {
        if (ids.isEmpty()) {
            return;
        }
        tooltip.add(title.withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(Integer.toString(ids.size())).withStyle(ChatFormatting.GOLD)));
        ids.forEach(id -> {
            String text = tags ? "#" + id : id.toString();
            tooltip.add(Component.literal("  " + text).withStyle(tags ? ChatFormatting.AQUA : ChatFormatting.WHITE));
        });
    }

    private MutableComponent buildSpeedTooltip() {
        MutableComponent line = ModTexts.gui("tool_rule_speed").copy().withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY));
        if (speedValue != null) {
            line.append(Component.literal(trimFloat(speedValue)).withStyle(ChatFormatting.GOLD));
        } else if (!speedText.isEmpty()) {
            line.append(Component.literal(speedText).withStyle(ChatFormatting.RED));
        } else {
            line.append(Component.literal("-").withStyle(ChatFormatting.DARK_GRAY));
        }
        return line;
    }

    private MutableComponent buildBehaviorTooltip() {
        return ModTexts.gui("tool_rule_behavior").copy().withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(behaviorProperty.getValue().getText().copy())
                .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
                .append(behaviorProperty.getValue().getDescription().copy());
    }

    public Optional<List<Tool.Rule>> toRules(HolderLookup.RegistryLookup<Block> lookup) {
        if (!hasSelection() || lookup == null) {
            return Optional.empty();
        }
        Optional<Float> speed = Optional.ofNullable(speedValue);
        Optional<Boolean> behavior = behaviorProperty.getValue().toOptional();
        List<Tool.Rule> rules = new ArrayList<>();

        for (ResourceLocation tagId : tagIds) {
            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagId);
            Optional<HolderSet.Named<Block>> named = lookup.get(tagKey);
            if (named.isEmpty()) {
                return Optional.empty();
            }
            rules.add(new Tool.Rule(named.get(), speed, behavior));
        }

        if (!blockIds.isEmpty()) {
            List<Holder<Block>> holders = new ArrayList<>();
            for (ResourceLocation blockId : blockIds) {
                ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, blockId);
                Optional<Holder.Reference<Block>> holder = lookup.get(key);
                if (holder.isEmpty()) {
                    return Optional.empty();
                }
                holders.add(holder.get());
            }
            rules.add(new Tool.Rule(HolderSet.direct(holders), speed, behavior));
        }

        return rules.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(rules));
    }

    public Optional<Tool.Rule> toRule(HolderLookup.RegistryLookup<Block> lookup) {
        Optional<List<Tool.Rule>> rules = toRules(lookup);
        if (rules.isEmpty() || rules.get().size() != 1) {
            return Optional.empty();
        }
        return Optional.of(rules.get().getFirst());
    }

    public void setFromRule(Tool.Rule rule) {
        tagIds.clear();
        blockIds.clear();
        if (rule.blocks().unwrapKey().isPresent()) {
            tagIds.add(rule.blocks().unwrapKey().get().location());
        } else {
            rule.blocks().stream()
                    .map(holder -> holder.unwrapKey().map(ResourceKey::location).orElse(null))
                    .filter(id -> id != null)
                    .forEach(blockIds::add);
        }
        rule.speed().ifPresentOrElse(value -> {
            speedValue = value;
            speedText = trimFloat(value);
        }, () -> {
            speedValue = null;
            speedText = "";
        });
        behaviorProperty.setValue(DropBehavior.fromOptional(rule.correctForDrops()));
        captureDefaults();
    }

    @Override
    public void reset() {
        blockIds.clear();
        blockIds.addAll(defaultBlockIds);
        tagIds.clear();
        tagIds.addAll(defaultTagIds);
        behaviorProperty.setValue(defaultBehavior);
        speedValue = defaultSpeedValue;
        speedText = defaultSpeedText;
        setValid(true);
    }

    @Override
    public void apply() {
        captureDefaults();
        setValid(true);
    }

    private void captureDefaults() {
        defaultBlockIds.clear();
        defaultBlockIds.addAll(blockIds);
        defaultTagIds.clear();
        defaultTagIds.addAll(tagIds);
        defaultBehavior = behaviorProperty.getValue();
        defaultSpeedValue = speedValue;
        defaultSpeedText = speedText;
    }

    private String trimFloat(float value) {
        String text = Float.toString(value);
        if (text.indexOf('.') >= 0) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text;
    }

    @Override
    public Type getType() {
        return Type.TOOL_RULE;
    }

    public enum DropBehavior {
        INHERIT("inherit"),
        ALLOW("allow"),
        DENY("deny");

        private final String translationSuffix;

        DropBehavior(String translationSuffix) {
            this.translationSuffix = translationSuffix;
        }

        public Component getText() {
            return Component.translatable("cadeditor.gui.tool_rule_behavior." + translationSuffix);
        }

        public MutableComponent getDescription() {
            return Component.translatable("cadeditor.gui.tool_rule_behavior.desc." + translationSuffix)
                    .withStyle(ChatFormatting.DARK_GRAY);
        }

        public Optional<Boolean> toOptional() {
            return switch (this) {
                case INHERIT -> Optional.empty();
                case ALLOW -> Optional.of(true);
                case DENY -> Optional.of(false);
            };
        }

        public static DropBehavior fromOptional(Optional<Boolean> optional) {
            if (optional.isEmpty()) {
                return INHERIT;
            }
            return optional.get() ? ALLOW : DENY;
        }
    }
}
