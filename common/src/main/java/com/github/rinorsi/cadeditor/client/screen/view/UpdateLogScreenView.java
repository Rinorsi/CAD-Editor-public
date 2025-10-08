
package com.github.rinorsi.cadeditor.client.screen.view;

import com.github.franckyi.guapi.api.node.Button;
import com.github.franckyi.guapi.api.node.HBox;
import com.github.franckyi.guapi.api.node.Label;
import com.github.franckyi.guapi.api.node.ListView;
import com.github.franckyi.guapi.api.node.Node;
import com.github.rinorsi.cadeditor.client.screen.model.UpdateLogScreenModel.SectionLine;
import com.github.rinorsi.cadeditor.client.UpdateLogRegistry.UpdateLogEntry;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;

import static com.github.franckyi.guapi.api.GuapiHelper.*;

public class UpdateLogScreenView extends ScreenView {
    private Label introLabel;
    private ListView<UpdateLogEntry> versionList;
    private ListView<SectionLine> detailList;
    private Label versionTitle;
    private Label versionMeta;
    private Button markReadButton;
    private Button markAllReadButton;
    private Button copyButton;
    private HBox unreadBadge;
    private final Map<String, Label> versionUnreadIndicators = new HashMap<>();
    private boolean hasUnreadLatest;
    private String latestUnreadVersion = "";

    @Override
    public void build() {
        super.build();
        buttonBarCenter.getChildren().add(hBox(center -> {
            center.add(markAllReadButton = button(ModTexts.UPDATE_LOG_MARK_ALL_READ));
            center.add(markReadButton = button(ModTexts.UPDATE_LOG_MARK_READ));
            center.add(copyButton = button(ModTexts.UPDATE_LOG_COPY));
            center.spacing(6);
        }));
        setUnreadBadgeVisible(false);
    }

    @Override
    protected MutableComponent getHeaderLabelText() {
        return ModTexts.UPDATE_LOG_TITLE;
    }

    @Override
    protected Node createEditor() {
        return hBox(root -> {
            root.add(vBox(left -> {
                left.add(label(ModTexts.UPDATE_LOG_RECENT).textAlign(CENTER_LEFT));
                left.add(versionList = listView(UpdateLogEntry.class, list -> list
                        .itemHeight(18)
                        .childrenFocusable(false)
                        .renderer(entry -> {
                            var indicator = label(text("●").withStyle(ChatFormatting.GOLD))
                                    .textAlign(CENTER)
                                    .prefWidth(12)
                                    .visible(false);
                            versionUnreadIndicators.put(entry.version(), indicator);
                            var rowLabel = label(entry.displayName()).textAlign(CENTER_LEFT).padding(4);
                            rowLabel.onMouseClick(() -> versionList.setFocusedElement(entry));
                            var row = hBox(item -> {
                                item.add(indicator);
                                item.add(rowLabel, 1);
                                item.spacing(4).align(CENTER_LEFT);
                            });
                            row.onMouseClick(() -> versionList.setFocusedElement(entry));
                            indicator.setVisible(hasUnreadLatest && entry.version().equals(latestUnreadVersion));
                            return row;
                        })), 1);
                left.spacing(5).fillWidth();
            }), 2);
            root.add(vBox(right -> {
                right.add(hBox(titleRow -> {
                    titleRow.add(versionTitle = label().textAlign(CENTER_LEFT), 1);
                    titleRow.add(unreadBadge = hBox(badge -> {
                        badge.add(label(ModTexts.UPDATE_LOG_NEW));
                        badge.padding(2, 6).backgroundColor(0xAAFF5555).align(CENTER);
                    }));
                    titleRow.align(CENTER_LEFT).spacing(5);
                }));
                right.add(versionMeta = label().textAlign(CENTER_LEFT));
                right.add(introLabel = label().textAlign(CENTER_LEFT));
                right.add(detailList = listView(SectionLine.class, list -> list
                        .itemHeight(18)
                        .childrenFocusable(false)
                        .renderer(line -> label(line.text()).textAlign(CENTER_LEFT).padding(4))
                        .padding(4)), 1);
                right.spacing(5).fillWidth();
            }), 5);
            root.fillHeight().spacing(10);
        });
    }

    public Label getIntroLabel() {
        return introLabel;
    }

    public ListView<UpdateLogEntry> getVersionList() {
        return versionList;
    }

    public ListView<SectionLine> getDetailList() {
        return detailList;
    }

    public Button getMarkReadButton() {
        return markReadButton;
    }

    public Button getMarkAllReadButton() {
        return markAllReadButton;
    }

    public Button getCopyButton() {
        return copyButton;
    }

    public void renderEntry(UpdateLogEntry entry) {
        if (entry == null) {
            versionTitle.setLabel(EMPTY_TEXT);
            versionMeta.setLabel(EMPTY_TEXT);
            introLabel.setLabel(EMPTY_TEXT);
            return;
        }
        versionTitle.setLabel(entry.displayName());
        versionMeta.setLabel(entry.dateLabel());
    }

    public void setUnreadBadgeVisible(boolean visible) {
        unreadBadge.setVisible(visible);
    }

    public void resetVersionIndicators() {
        versionUnreadIndicators.clear();
    }

    public void updateUnreadState(boolean hasUnread, String latestVersion) {
        hasUnreadLatest = hasUnread;
        latestUnreadVersion = latestVersion == null ? "" : latestVersion;
        updateVersionIndicators();
    }

    private void updateVersionIndicators() {
        versionUnreadIndicators.forEach((version, indicator) ->
                indicator.setVisible(hasUnreadLatest && version.equals(latestUnreadVersion)));
    }
}

