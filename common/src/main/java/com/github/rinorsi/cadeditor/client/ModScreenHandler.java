package com.github.rinorsi.cadeditor.client;

import com.github.franckyi.guapi.api.Guapi;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.Scene;
import com.github.rinorsi.cadeditor.client.context.BlockEditorContext;
import com.github.rinorsi.cadeditor.client.context.EditorContext;
import com.github.rinorsi.cadeditor.client.context.EntityEditorContext;
import com.github.rinorsi.cadeditor.client.context.ItemEditorContext;
import com.github.rinorsi.cadeditor.client.screen.model.*;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ColorSelectionScreenModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ListSelectionFilter;
import com.github.rinorsi.cadeditor.client.screen.model.selection.ListSelectionScreenModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ItemListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.ListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.SelectableItemListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.SelectableSpriteListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.SelectableTagListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.SpriteListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.model.selection.element.TagListSelectionElementModel;
import com.github.rinorsi.cadeditor.client.screen.mvc.*;
import com.github.rinorsi.cadeditor.client.util.ScreenScalingManager;
import com.github.rinorsi.cadeditor.common.EditorType;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public final class ModScreenHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void openSettingsScreen() {
        openScaledScreen(mvc(ConfigEditorMVC.INSTANCE, new ConfigEditorScreenModel()));
    }

    public static void openUpdateLogScreen() {
        openScaledScreen(mvc(UpdateLogScreenMVC.INSTANCE, new UpdateLogScreenModel()));
    }

    public static void openListSelectionScreen(MutableComponent title, String attributeName,
                                               List<? extends ListSelectionElementModel> items, Consumer<String> action) {
        openListSelectionScreen(title, attributeName, items, action, false, null, Set.of(), Collections.emptyList(), null);
    }

    public static void openListSelectionScreen(MutableComponent title, String attributeName, List<? extends ListSelectionElementModel> items,
                                               Consumer<String> action, boolean multiSelect,
                                               Consumer<List<Identifier>> multiAction, Set<Identifier> initiallySelected) {
        openListSelectionScreen(title, attributeName, items, action, multiSelect, multiAction, initiallySelected,
                Collections.emptyList(), null);
    }

    public static void openListSelectionScreen(MutableComponent title, String attributeName,
                                               List<? extends ListSelectionElementModel> items, Consumer<String> action,
                                               List<ListSelectionFilter> filters, String initialFilterId) {
        openListSelectionScreen(title, attributeName, items, action, false, null, Set.of(), filters, initialFilterId);
    }

    public static void openListSelectionScreen(MutableComponent title, String attributeName,
                                               List<? extends ListSelectionElementModel> items, Consumer<String> action,
                                               boolean multiSelect,
                                               Consumer<List<Identifier>> multiAction,
                                               Set<Identifier> initiallySelected,
                                               List<ListSelectionFilter> filters,
                                               String initialFilterId) {
        List<? extends ListSelectionElementModel> renderedItems = multiSelect
                ? items
                : toSingleSelectionItems(items);
        openScaledScreen(mvc(ListSelectionScreenMVC.INSTANCE,
                new ListSelectionScreenModel(title, attributeName, renderedItems, action, multiSelect, multiAction,
                        initiallySelected, filters == null ? Collections.emptyList() : filters, initialFilterId)));
    }

    private static List<ListSelectionElementModel> toSingleSelectionItems(List<? extends ListSelectionElementModel> items) {
        return items.stream()
                .map(ModScreenHandler::toSingleSelectionItem)
                .collect(Collectors.toList());
    }

    private static ListSelectionElementModel toSingleSelectionItem(ListSelectionElementModel item) {
        if (item instanceof SelectableItemListSelectionElementModel selectable) {
            return new ItemListSelectionElementModel(selectable.getName(), selectable.getId(), () -> selectable.getItem().copy());
        }
        if (item instanceof SelectableTagListSelectionElementModel selectable) {
            return new TagListSelectionElementModel(selectable.getId());
        }
        if (item instanceof SelectableSpriteListSelectionElementModel selectable) {
            return new SpriteListSelectionElementModel(selectable.getName(), selectable.getId(), selectable.getSpriteFactory());
        }
        return item;
    }

    public static void openColorSelectionScreen(ColorSelectionScreenModel.Target target, int color, Consumer<String> action) {
        openScaledScreen(mvc(ColorSelectionScreenMVC.INSTANCE, new ColorSelectionScreenModel(target, action, color)));
    }

    public static void openVault() {
        openScaledScreen(mvc(VaultScreenMVC.INSTANCE, new VaultScreenModel()));
    }

    private static void openScaledScreen(Node root) {
        openScaledScreen(root, false);
    }

    private static void openScaledScreen(Node root, boolean replace) {
        Consumer<Scene> action = replace ? Guapi.getScreenHandler()::replaceScene : Guapi.getScreenHandler()::showScene;
        try {
            action.accept(scene(root, true, true).show(scene -> {
                ScreenScalingManager.get().setBaseScale(ClientConfiguration.INSTANCE.getEditorScale());
                scene.widthProperty().addListener(ScreenScalingManager.get()::refresh);
                scene.heightProperty().addListener(ScreenScalingManager.get()::refresh);
            }).hide(scene -> {
                ClientConfiguration.INSTANCE.setEditorScale(ScreenScalingManager.get().getScaleAndReset());
                ClientConfiguration.save();
            }));
        } catch (Exception e) {
            LOGGER.error("打开界面时出错", e);
            ClientUtil.showMessage(ModTexts.Messages.ERROR_GENERIC);
        }
    }

    public static void openEditor(EditorType editorType, EditorContext<?> context) {
        openEditor(editorType, context, false);
    }

    public static void openEditor(EditorType editorType, EditorContext<?> context, boolean replace) {
        EditorType resolvedEditorType = editorType;
        if (resolvedEditorType != EditorType.STANDARD && context.getTag() == null) {
            if (context instanceof BlockEditorContext blockContext && !blockContext.getBlockState().hasBlockEntity()) {
                resolvedEditorType = EditorType.STANDARD;
            } else {
                ClientUtil.showMessage(ModTexts.Messages.NO_BLOCK_DATA);
                return;
            }
        }
        openScaledScreen(switch (resolvedEditorType) {
            case STANDARD -> {
                if (context instanceof ItemEditorContext ctx) {
                    yield mvc(StandardEditorMVC.INSTANCE, new ItemEditorModel(ctx));
                } else if (context instanceof BlockEditorContext ctx) {
                    yield mvc(StandardEditorMVC.INSTANCE, new BlockEditorModel(ctx));
                } else if (context instanceof EntityEditorContext ctx) {
                    yield mvc(StandardEditorMVC.INSTANCE, new EntityEditorModel(ctx));
                } else {
                    throw new IllegalStateException("context should be an instance of ItemEditorContext, BlockEditorContext or EntityEditorContext");
                }
            }
            case NBT -> mvc(NBTEditorMVC.INSTANCE, new NBTEditorModel(context));
            case SNBT -> mvc(SNBTEditorMVC.INSTANCE, new SNBTEditorModel(context));
        }, replace);
    }
}


