package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.franckyi.databindings.api.BooleanProperty;
import com.github.franckyi.databindings.api.ObservableList;
import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.guapi.api.node.TreeView;
import net.minecraft.nbt.*;

/**
 * Read-only tree representation of an SNBT payload. Keeps the structure lightweight so it can be
 * rendered alongside the raw SNBT text without providing editing affordances.
 */
public final class SNBTPreviewNode implements TreeView.TreeItem<SNBTPreviewNode> {
    private final ObservableList<SNBTPreviewNode> children = ObservableList.create();
    private final BooleanProperty expandedProperty = BooleanProperty.create(true);
    private final ObjectProperty<SNBTPreviewNode> parentProperty = ObjectProperty.create();
    private final BooleanProperty childrenChangedProperty = BooleanProperty.create();
    private final String label;

    private SNBTPreviewNode(String label) {
        this.label = label;
    }

    public static SNBTPreviewNode fromTag(Tag tag) {
        if (tag == null) {
            return new SNBTPreviewNode("null");
        }
        SNBTPreviewNode root = new SNBTPreviewNode(describe(null, tag, null));
        populate(root, tag);
        return root;
    }

    private static void populate(SNBTPreviewNode node, Tag tag) {
        if (tag == null) {
            return;
        }
        switch (tag.getId()) {
            case Tag.TAG_COMPOUND -> {
                CompoundTag compound = (CompoundTag) tag;
                for (String key : compound.getAllKeys()) {
                    Tag child = compound.get(key);
                    SNBTPreviewNode childNode = new SNBTPreviewNode(describe(key, child, null));
                    addChild(node, childNode);
                    if (child != null) {
                        populate(childNode, child);
                    }
                }
            }
            case Tag.TAG_LIST -> {
                ListTag listTag = (ListTag) tag;
                for (int i = 0; i < listTag.size(); i++) {
                    Tag child = listTag.get(i);
                    SNBTPreviewNode childNode = new SNBTPreviewNode(describe(null, child, i));
                    addChild(node, childNode);
                    populate(childNode, child);
                }
            }
            case Tag.TAG_BYTE_ARRAY -> {
                byte[] values = ((ByteArrayTag) tag).getAsByteArray();
                for (int i = 0; i < values.length; i++) {
                    addChild(node, new SNBTPreviewNode("[%d] %d".formatted(i, values[i])));
                }
            }
            case Tag.TAG_INT_ARRAY -> {
                int[] values = ((IntArrayTag) tag).getAsIntArray();
                for (int i = 0; i < values.length; i++) {
                    addChild(node, new SNBTPreviewNode("[%d] %d".formatted(i, values[i])));
                }
            }
            case Tag.TAG_LONG_ARRAY -> {
                long[] values = ((LongArrayTag) tag).getAsLongArray();
                for (int i = 0; i < values.length; i++) {
                    addChild(node, new SNBTPreviewNode("[%d] %d".formatted(i, values[i])));
                }
            }
            default -> {
                // Primitives and strings are leaf nodes; no further population needed.
            }
        }
    }

    private static void addChild(SNBTPreviewNode parent, SNBTPreviewNode child) {
        child.parentProperty().setValue(parent);
        parent.children.add(child);
        parent.childrenChangedProperty().setValue(true);
    }

    private static String describe(String name, Tag tag, Integer listIndex) {
        String prefix = "";
        if (name != null) {
            prefix = name + ": ";
        } else if (listIndex != null) {
            prefix = "[" + listIndex + "]: ";
        }
        if (tag == null) {
            return prefix + "null";
        }
        return switch (tag.getId()) {
            case Tag.TAG_COMPOUND -> prefix + "Compound (" + ((CompoundTag) tag).size() + ")";
            case Tag.TAG_LIST -> {
                ListTag list = (ListTag) tag;
                String type = TagTypes.getType(list.getElementType()).getName();
                yield prefix + "List<" + type + "> (" + list.size() + ")";
            }
            case Tag.TAG_BYTE_ARRAY -> prefix + "ByteArray (" + ((ByteArrayTag) tag).getAsByteArray().length + ")";
            case Tag.TAG_INT_ARRAY -> prefix + "IntArray (" + ((IntArrayTag) tag).getAsIntArray().length + ")";
            case Tag.TAG_LONG_ARRAY -> prefix + "LongArray (" + ((LongArrayTag) tag).getAsLongArray().length + ")";
            default -> prefix + tag.getAsString();
        };
    }

    public String getLabel() {
        return label;
    }

    @Override
    public ObservableList<SNBTPreviewNode> getChildren() {
        return children;
    }

    @Override
    public BooleanProperty expandedProperty() {
        return expandedProperty;
    }

    @Override
    public ObjectProperty<SNBTPreviewNode> parentProperty() {
        return parentProperty;
    }

    @Override
    public BooleanProperty childrenChangedProperty() {
        return childrenChangedProperty;
    }
}
