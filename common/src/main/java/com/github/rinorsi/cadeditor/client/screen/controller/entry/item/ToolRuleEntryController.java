package com.github.rinorsi.cadeditor.client.screen.controller.entry.item;

import com.github.franckyi.databindings.api.event.ObservableValueChangeListener;
import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.Parent;
import com.github.rinorsi.cadeditor.client.ClientCache;
import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.ModScreenHandler;
import com.github.rinorsi.cadeditor.client.screen.controller.entry.EntryController;
import com.github.rinorsi.cadeditor.client.screen.model.entry.item.ToolRuleEntryModel;
import com.github.rinorsi.cadeditor.client.screen.view.entry.item.ToolRuleEntryView;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.github.franckyi.guapi.api.GuapiHelper.CENTER_LEFT;
import static com.github.franckyi.guapi.api.GuapiHelper.label;

public class ToolRuleEntryController extends EntryController<ToolRuleEntryModel, ToolRuleEntryView> {
    private Component summaryDisplay = Component.empty();
    private Component renderedSummary = Component.empty();
    private String summaryText = "";

    private boolean updatingSelectionWidth;
    private boolean handlingSelectionContainerChange;
    private HBox selectionContainer;
    private ObservableValueChangeListener<? super Integer> selectionContainerWidthListener;
    private ObservableValueChangeListener<? super Integer> selectionContainerComputedWidthListener;
    private ObservableValueChangeListener<? super Integer> selectionContainerParentPrefWidthListener;

    private Label summaryLabel;
    private String inlineText = "";

    public ToolRuleEntryController(ToolRuleEntryModel model, ToolRuleEntryView view) {
        super(model, view);
    }

    @Override
    public void bind() {
        super.bind();
        view.setListButtonsVisible(true);

        updateRuleIndex();
        model.listIndexProperty().addListener(v -> updateRuleIndex());

        installSummaryLabelOverlay();

        if (summaryLabel != null) {
            summaryLabel.widthProperty().addListener(v -> refreshSummaryRenderer());
            summaryLabel.computedWidthProperty().addListener(v -> refreshSummaryRenderer());
            summaryLabel.parentPrefWidthProperty().addListener(v -> refreshSummaryRenderer());
        }
        view.getSelectionField().widthProperty().addListener(v -> refreshSummaryRenderer());
        view.getSelectionField().computedWidthProperty().addListener(v -> refreshSummaryRenderer());
        view.getSelectionField().parentPrefWidthProperty().addListener(v -> refreshSummaryRenderer());
        view.getSelectionField().parentProperty().addListener((oldP, newP) -> observeSelectionContainer(newP));
        observeSelectionContainer(view.getSelectionField().getParent());

        updateSummary();
        inlineText = toInlineTextFromModel();

        enterDisplayMode();

        view.getSelectionField().focusedProperty().addListener(focused -> {
            if (Boolean.TRUE.equals(focused)) {
                enterEditMode();
            } else {
                inlineText = view.getSelectionField().getText();
                InlineSelection parsed = parseInlineSelection(inlineText);
                model.setBlockIds(new ArrayList<>(new LinkedHashSet<>(parsed.blockIds)));
                model.setTagIds(new ArrayList<>(new LinkedHashSet<>(parsed.tagIds)));
                updateSummary();
                validateRule();
                enterDisplayMode();
            }
        });

        view.getSelectionField().textProperty().addListener(value -> {
            if (!Boolean.TRUE.equals(view.getSelectionField().focusedProperty().getValue())) return;
            inlineText = value;
            InlineSelection parsed = parseInlineSelection(value);
            model.setBlockIds(new ArrayList<>(new LinkedHashSet<>(parsed.blockIds)));
            model.setTagIds(new ArrayList<>(new LinkedHashSet<>(parsed.tagIds)));
            updateSummary();
            validateRule();
        });

        view.getSelectBlocksButton().onAction(this::openBlockSelection);
        view.getSelectTagsButton().onAction(this::openTagSelection);
        view.getDeleteRuleButton().onAction(() -> {
            if (model.getListIndex() >= 0) model.getCategory().deleteEntry(model.getListIndex());
        });
        view.getDeleteRuleButton().disableProperty().bind(model.listIndexProperty().lt(0));

        view.getSpeedField().setText(model.getSpeedText());
        view.getSpeedField().textProperty().addListener(value -> {
            boolean valid = model.setSpeedText(value);
            if (!valid) {
                updateSummary();
                model.setValid(false);
                return;
            }
            updateSummary();
            validateRule();
        });

        view.getBehaviorButton().getValues().setAll(List.of(ToolRuleEntryModel.DropBehavior.values()));
        view.getBehaviorButton().setTextFactory(ToolRuleEntryModel.DropBehavior::getText);
        view.getBehaviorButton().setValue(model.getBehavior());
        view.getBehaviorButton().valueProperty().addListener(value -> {
            model.setBehavior(value);
            updateSummary();
            updateBehaviorTooltip();
            validateRule();
        });
        updateBehaviorTooltip();
        validateRule();
    }

    private void installSummaryLabelOverlay() {
        summaryLabel = label().prefHeight(16).textAlign(CENTER_LEFT);
        summaryLabel.setDisable(false);
        summaryLabel.setPrefWidth(0);
        summaryLabel.setMaxWidth(Integer.MAX_VALUE);

        Parent parent = view.getSelectionField().getParent();
        if (parent instanceof HBox container) {
            int idx = container.getChildren().indexOf(view.getSelectionField());
            if (idx < 0) idx = container.getChildren().size();
            container.getChildren().add(idx, summaryLabel);
            summaryLabel.setPrefWidth(view.getSelectionField().getPrefWidth());
        }
    }

    private void enterEditMode() {
        if (summaryLabel != null) summaryLabel.setVisible(false);
        view.getSelectionField().setDisable(false);
        view.getSelectionField().setVisible(true);
        view.getSelectionField().setMinWidth(60);
        view.getSelectionField().setPrefWidth(0);
        view.getSelectionField().setMaxWidth(Integer.MAX_VALUE);
        view.getSelectionField().setText(inlineText);
        int end = view.getSelectionField().getText().length();
        view.getSelectionField().setCursorPosition(end);
        view.getSelectionField().setHighlightPosition(end);
    }

    private void enterDisplayMode() {
        view.getSelectionField().setVisible(false);
        view.getSelectionField().setDisable(true);
        view.getSelectionField().setMinWidth(0);
        view.getSelectionField().setPrefWidth(0);
        view.getSelectionField().setMaxWidth(0);

        if (summaryLabel != null) {
            summaryLabel.setVisible(true);
            summaryLabel.setPrefWidth(computeSelectionContainerWidth());
            summaryLabel.setMaxWidth(Integer.MAX_VALUE);
            refreshSummaryRenderer();
            summaryLabel.setLabel(renderedSummary.copy());
        }
    }

    private void openBlockSelection() {
        Set<ResourceLocation> initiallySelected = new LinkedHashSet<>(model.getBlockIds());
        ModScreenHandler.openListSelectionScreen(
                ModTexts.BLOCK, "tool_rule_blocks",
                ClientCache.getBlockSelectionItems(), null, true,
                selected -> {
                    model.setBlockIds(new ArrayList<>(new LinkedHashSet<>(selected)));
                    inlineText = toInlineTextFromModel();
                    if (Boolean.TRUE.equals(view.getSelectionField().focusedProperty().getValue())) {
                        view.getSelectionField().setText(inlineText);
                    } else {
                        updateSummary();
                        if (summaryLabel != null) summaryLabel.setLabel(renderedSummary.copy());
                    }
                    validateRule();
                },
                initiallySelected
        );
    }

    private void openTagSelection() {
        Set<ResourceLocation> initiallySelected = new LinkedHashSet<>(model.getTagIds());
        ModScreenHandler.openListSelectionScreen(
                ModTexts.gui("tool_rule_tags"), "tool_rule_tags",
                ClientCache.getBlockTagSelectionItems(), null, true,
                selected -> {
                    model.setTagIds(new ArrayList<>(new LinkedHashSet<>(selected)));
                    inlineText = toInlineTextFromModel();
                    if (Boolean.TRUE.equals(view.getSelectionField().focusedProperty().getValue())) {
                        view.getSelectionField().setText(inlineText);
                    } else {
                        updateSummary();
                        if (summaryLabel != null) summaryLabel.setLabel(renderedSummary.copy());
                    }
                    validateRule();
                },
                initiallySelected
        );
    }

    private void updateSummary() {
        summaryDisplay = model.getSummaryComponent().copy();
        summaryText = model.hasSelection() ? summaryDisplay.getString() : "";
        refreshSummaryRenderer();

        var tfTooltip = view.getSelectionField().getTooltip();
        tfTooltip.clear();
        model.getSummaryTooltip().forEach(c -> tfTooltip.add(c.copy()));

        if (summaryLabel != null) {
            var lblTooltip = summaryLabel.getTooltip();
            lblTooltip.clear();
            model.getSummaryTooltip().forEach(c -> lblTooltip.add(c.copy()));
            summaryLabel.setLabel(renderedSummary.copy());
        }
    }

    private void refreshSummaryRenderer() {
        renderedSummary = summaryText.isEmpty() ? Component.empty() : trimSummaryComponent(summaryDisplay.copy());
        if (summaryLabel != null && summaryLabel.isVisible()) {
            summaryLabel.setLabel(renderedSummary.copy());
        }
    }

    private Component trimSummaryComponent(Component component) {
        int maxWidth = computeSummaryInnerWidth();
        if (maxWidth <= 0) return renderedSummary.copy();

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return component.copy();
        var font = mc.font;

        if (font.width(component) <= maxWidth) return component.copy();

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        int availableWidth = Math.max(0, maxWidth - ellipsisWidth);
        String trimmed = font.plainSubstrByWidth(component.getString(), availableWidth);
        while (!trimmed.isEmpty() && font.width(trimmed) > availableWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        MutableComponent trimmedComponent = Component.literal(trimmed).withStyle(component.getStyle());
        if (trimmed.isEmpty()) {
            return Component.literal(ellipsis).withStyle(component.getStyle());
        }
        return trimmedComponent.append(Component.literal(ellipsis).withStyle(component.getStyle()));
    }

    private int computeSummaryInnerWidth() {
        Node n = (summaryLabel != null && summaryLabel.isVisible()) ? summaryLabel : view.getSelectionField();

        int width = n.getWidth();
        if (width <= 0) width = n.getComputedWidth();
        if (width <= 0) width = n.getParentPrefWidth();
        if (width <= 0) width = n.getPrefWidth();
        if (width <= 0) width = computeSelectionContainerWidth();
        if (width <= 0) return 0;

        int padding = 0;
        if (n == view.getSelectionField()) {
            padding = view.getSelectionField().getPadding().getHorizontal();
        }

        int usable = width - padding;
        int maxWidth = (int) (Math.max(0, usable) * 0.90);
        return Math.max(0, maxWidth);
    }

    private int computeSelectionContainerWidth() {
        Parent parent = view.getSelectionField().getParent();
        if (!(parent instanceof HBox container)) return 0;

        int parentWidth = container.getWidth();
        if (parentWidth <= 0) parentWidth = container.getComputedWidth();
        if (parentWidth <= 0) parentWidth = container.getParentPrefWidth();
        if (parentWidth <= 0) parentWidth = container.getPrefWidth();
        if (parentWidth <= 0) return 0;

        int spacing = container.getSpacing();
        int totalSpacing = spacing * Math.max(0, container.getChildren().size() - 1);
        int padding = container.getPadding().getHorizontal();
        int buttonWidth = 0;
        if (container.getChildren().contains(view.getSelectBlocksButton())) {
            buttonWidth += getNodeWidth(view.getSelectBlocksButton());
        }
        if (container.getChildren().contains(view.getSelectTagsButton())) {
            buttonWidth += getNodeWidth(view.getSelectTagsButton());
        }
        return Math.max(0, parentWidth - totalSpacing - padding - buttonWidth);
    }

    private void observeSelectionContainer(Parent parent) {
        if (!(parent instanceof HBox container)) {
            detachSelectionContainerListeners();
            return;
        }
        if (selectionContainer != container) {
            detachSelectionContainerListeners();
            selectionContainer = container;
            selectionContainerWidthListener = container.widthProperty().addListener(v -> handleSelectionContainerSizeChange());
            selectionContainerComputedWidthListener = container.computedWidthProperty().addListener(v -> handleSelectionContainerSizeChange());
            selectionContainerParentPrefWidthListener = container.parentPrefWidthProperty().addListener(v -> handleSelectionContainerSizeChange());
        }
        handleSelectionContainerSizeChange();
    }

    private void detachSelectionContainerListeners() {
        if (selectionContainer == null) return;
        if (selectionContainerWidthListener != null) {
            selectionContainer.widthProperty().removeListener(selectionContainerWidthListener);
            selectionContainerWidthListener = null;
        }
        if (selectionContainerComputedWidthListener != null) {
            selectionContainer.computedWidthProperty().removeListener(selectionContainerComputedWidthListener);
            selectionContainerComputedWidthListener = null;
        }
        if (selectionContainerParentPrefWidthListener != null) {
            selectionContainer.parentPrefWidthProperty().removeListener(selectionContainerParentPrefWidthListener);
            selectionContainerParentPrefWidthListener = null;
        }
        selectionContainer = null;
    }

    private void handleSelectionContainerSizeChange() {
        if (handlingSelectionContainerChange) return;
        handlingSelectionContainerChange = true;
        try {
            updateSelectionFieldPreferredWidth();
            refreshSummaryRenderer();
            if (summaryLabel != null) summaryLabel.setLabel(renderedSummary.copy());
        } finally {
            handlingSelectionContainerChange = false;
        }
    }

    private void updateSelectionFieldPreferredWidth() {
        if (updatingSelectionWidth) return;
        updatingSelectionWidth = true;
        try {
            int width = computeSelectionContainerWidth();
            if (width > 0) {
                int pref = Math.max(width, view.getSelectionField().getMinWidth());
                if (summaryLabel != null && summaryLabel.isVisible()) {
                    summaryLabel.setPrefWidth(pref);
                } else {
                    view.getSelectionField().setPrefWidth(pref);
                }
            }
        } finally {
            updatingSelectionWidth = false;
        }
    }

    private int getNodeWidth(Node node) {
        int width = node.getWidth();
        if (width <= 0) width = node.getComputedWidth();
        if (width <= 0) width = node.getParentPrefWidth();
        if (width <= 0) width = node.getPrefWidth();
        return Math.max(width, 0);
    }

    private void validateRule() {
        if (!model.hasSelection() && model.getSpeedText().isBlank()) {
            model.setValid(true);
            return;
        }
        HolderLookup.RegistryLookup<Block> lookup = ClientUtil.registryAccess()
                .lookup(Registries.BLOCK)
                .orElse(null);
        if (lookup == null) {
            model.setValid(false);
            return;
        }
        model.setValid(model.toRule(lookup).isPresent());
    }

    private void updateBehaviorTooltip() {
        var tooltip = view.getBehaviorButton().getTooltip();
        tooltip.clear();
        tooltip.add(ModTexts.gui("tool_rule_behavior").copy().withStyle(ChatFormatting.GRAY));
        tooltip.add(model.getBehavior().getDescription());
    }

    private void updateRuleIndex() {
        int index = Math.max(0, model.getListIndex());
        view.getRuleLabel().setLabel(Component.literal(String.format(Locale.ROOT, "#%02d", index + 1)));
    }

    private static final class InlineSelection {
        final Set<ResourceLocation> blockIds = new LinkedHashSet<>();
        final Set<ResourceLocation> tagIds = new LinkedHashSet<>();
    }

    private InlineSelection parseInlineSelection(String raw) {
        InlineSelection out = new InlineSelection();
        if (raw == null || raw.isBlank()) return out;

        String[] tokens = raw.split("[,\\s]+");
        for (String t : tokens) {
            if (t.isBlank()) continue;
            String s = t.trim();

            boolean isTag = s.startsWith("#");
            if (isTag) s = s.substring(1);

            if (!s.contains(":")) s = "minecraft:" + s;

            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id == null) continue;

            if (isTag) out.tagIds.add(id);
            else out.blockIds.add(id);
        }
        return out;
    }

    private String toInlineTextFromModel() {
        LinkedHashSet<ResourceLocation> blocks = new LinkedHashSet<>(model.getBlockIds());
        LinkedHashSet<ResourceLocation> tags   = new LinkedHashSet<>(model.getTagIds());
        StringBuilder sb = new StringBuilder();
        for (ResourceLocation id : blocks) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(id.toString());
        }
        for (ResourceLocation id : tags) {
            if (sb.length() > 0) sb.append(", ");
            sb.append('#').append(id.toString());
        }
        return sb.toString();
    }

    @Override
    public ToolRuleEntryModel getModel() {
        return model;
    }
}
