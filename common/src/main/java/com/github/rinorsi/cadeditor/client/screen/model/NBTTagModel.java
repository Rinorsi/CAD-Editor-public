package com.github.rinorsi.cadeditor.client.screen.model;

import com.github.franckyi.databindings.api.BooleanProperty;
import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.franckyi.databindings.api.ObservableList;
import com.github.franckyi.databindings.api.StringProperty;
import com.github.franckyi.guapi.api.mvc.Model;
import com.github.franckyi.guapi.api.node.TreeView;
import com.github.rinorsi.cadeditor.client.context.EditorContext;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.ArrayUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("this-escape")
public class NBTTagModel implements TreeView.TreeItem<NBTTagModel>, Model {
    private final ObservableList<NBTTagModel> children = ObservableList.create();
    private final BooleanProperty expandedProperty = BooleanProperty.create();
    private final ObjectProperty<NBTTagModel> parentProperty;
    private final BooleanProperty childrenChangedProperty = BooleanProperty.create();
    private final StringProperty nameProperty;
    private final StringProperty valueProperty;
    private final BooleanProperty validProperty = BooleanProperty.create();
    protected final Tag tag;
    protected byte forcedTagType;
    private final EditorContext<?> context;

    public NBTTagModel(EditorContext<?> context, Tag tag) {
        this(context, tag, null, null, null);
        setExpanded(true);
    }

    public NBTTagModel(EditorContext<?> context, byte forcedTagType, NBTTagModel parent, String value) {
        this(context, null, parent, null, value);
        this.forcedTagType = forcedTagType;
    }

    public NBTTagModel(EditorContext<?> context, Tag tag, NBTTagModel parent, String name, String value) {
        this.tag = tag;
        this.context = context != null ? context : parent != null ? parent.getContext() : null;
        parentProperty = ObjectProperty.create(parent);
        nameProperty = StringProperty.create(name);
        valueProperty = StringProperty.create(value);
        if (tag != null) {
            switch (tag.getId()) {
                case Tag.TAG_COMPOUND -> children.setAll(((CompoundTag) tag).entrySet()
                        .stream()
                        .map(entry -> new NBTTagModel(getContext(), entry.getValue(), this, entry.getKey(), null))
                        .toList()
                );
                case Tag.TAG_LIST -> children.setAll(((ListTag) tag)
                        .stream()
                        .map(tag1 -> new NBTTagModel(getContext(), tag1, this, null, null))
                        .toList()
                );
                case Tag.TAG_BYTE_ARRAY -> children.setAll(Stream.of(ArrayUtils.toObject(((ByteArrayTag) tag).getAsByteArray()))
                        .map(b -> new NBTTagModel(getContext(), Tag.TAG_BYTE, this, Byte.toString(b)))
                        .toList()
                );
                case Tag.TAG_INT_ARRAY -> children.setAll(Stream.of(ArrayUtils.toObject(((IntArrayTag) tag).getAsIntArray()))
                        .map(i -> new NBTTagModel(getContext(), Tag.TAG_INT, this, Integer.toString(i)))
                        .toList()
                );
                case Tag.TAG_LONG_ARRAY -> children.setAll(Stream.of(ArrayUtils.toObject(((LongArrayTag) tag).getAsLongArray()))
                        .map(l -> new NBTTagModel(getContext(), Tag.TAG_LONG, this, Long.toString(l)))
                        .toList()
                );
                case Tag.TAG_BYTE -> setValue(Byte.toString(((ByteTag) tag).byteValue()));
                case Tag.TAG_SHORT -> setValue(Short.toString(((ShortTag) tag).shortValue()));
                case Tag.TAG_INT -> setValue(Integer.toString(((IntTag) tag).intValue()));
                case Tag.TAG_LONG -> setValue(Long.toString(((LongTag) tag).longValue()));
                case Tag.TAG_FLOAT -> setValue(Float.toString(((FloatTag) tag).floatValue()));
                case Tag.TAG_DOUBLE -> setValue(Double.toString(((DoubleTag) tag).doubleValue()));
                case Tag.TAG_STRING -> setValue(((StringTag) tag).value());
                default -> setValue(tag.toString());
            }
        }
        getChildren().addListener(() -> getRoot().setChildrenChanged(true));
        validProperty().bind(getChildren().allMatch(NBTTagModel::isValid, NBTTagModel::validProperty));
    }

    @Override
    public ObservableList<NBTTagModel> getChildren() {
        return children;
    }

    @Override
    public BooleanProperty expandedProperty() {
        return expandedProperty;
    }

    @Override
    public ObjectProperty<NBTTagModel> parentProperty() {
        return parentProperty;
    }

    @Override
    public BooleanProperty childrenChangedProperty() {
        return childrenChangedProperty;
    }

    public EditorContext<?> getContext() {
        return context;
    }

    public String getName() {
        return nameProperty().getValue();
    }

    public StringProperty nameProperty() {
        return nameProperty;
    }

    public void setName(String value) {
        nameProperty().setValue(value);
    }

    public String getValue() {
        return valueProperty().getValue();
    }

    public StringProperty valueProperty() {
        return valueProperty;
    }

    public void setValue(String value) {
        valueProperty().setValue(value);
    }

    public boolean isValid() {
        return validProperty().getValue();
    }

    public BooleanProperty validProperty() {
        return validProperty;
    }

    public void setValid(boolean value) {
        validProperty().setValue(value);
    }

    public NBTTagModel getParent() {
        return parentProperty().getValue();
    }

    public byte getTagType() {
        return tag != null ? tag.getId() : forcedTagType;
    }

    public boolean canBuild() {
        return tag != null;
    }

    public Tag build() {
        if (canBuild()) {
            switch (tag.getId()) {
                case Tag.TAG_BYTE:
                    return ByteTag.valueOf(Byte.parseByte(getValue()));
                case Tag.TAG_SHORT:
                    return ShortTag.valueOf(Short.parseShort(getValue()));
                case Tag.TAG_INT:
                    return IntTag.valueOf(Integer.parseInt(getValue()));
                case Tag.TAG_LONG:
                    return LongTag.valueOf(Long.parseLong(getValue()));
                case Tag.TAG_FLOAT:
                    return FloatTag.valueOf(Float.parseFloat(getValue()));
                case Tag.TAG_DOUBLE:
                    return DoubleTag.valueOf(Double.parseDouble(getValue()));
                case Tag.TAG_BYTE_ARRAY: {
                    byte[] bytes = new byte[getChildren().size()];
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = Byte.parseByte(getChildren().get(i).getValue());
                    }
                    return new ByteArrayTag(bytes);
                }
                case Tag.TAG_STRING:
                    return StringTag.valueOf(getValue());
                case Tag.TAG_LIST:
                    ListTag listTag = new ListTag();
                    listTag.addAll(getChildren()
                            .stream()
                            .map(NBTTagModel::build)
                            .toList());
                    return listTag;
                case Tag.TAG_COMPOUND:
                    CompoundTag compoundTag = new CompoundTag();
                    getChildren().forEach(childTag -> compoundTag.put(childTag.getName(), childTag.build()));
                    return compoundTag;
                case Tag.TAG_INT_ARRAY: {
                    int[] ints = new int[getChildren().size()];
                    for (int i = 0; i < ints.length; i++) {
                        ints[i] = Integer.parseInt(getChildren().get(i).getValue());
                    }
                    return new IntArrayTag(ints);
                }
                case Tag.TAG_LONG_ARRAY: {
                    long[] longs = new long[getChildren().size()];
                    for (int i = 0; i < longs.length; i++) {
                        longs[i] = Long.parseLong(getChildren().get(i).getValue());
                    }
                    return new LongArrayTag(longs);
                }
            }
        }
        return null;
    }

    public NBTTagModel createClipboardTag() {
        return canBuild()
                ? new NBTTagModel(getContext(), build(), null, getName(), getValue())
                : new NBTTagModel(getContext(), getTagType(), null, getValue());
    }

    public List<String> getPath() {
        LinkedList<String> path = new LinkedList<>();
        NBTTagModel current = this;
        while (current != null) {
            String name = current.getName();
            if (name != null) {
                path.addFirst(name);
            } else if (current.getParent() != null && current.getParent().getTagType() == Tag.TAG_LIST) {
                int index = current.getParent().getChildren().indexOf(current);
                path.addFirst("[" + index + "]");
            }
            current = current.getParent();
        }
        return List.copyOf(path);
    }

    public List<String> getStringSuggestions() {
        EditorContext<?> ctx = getContext();
        if (ctx == null) {
            return List.of();
        }
        return ctx.getStringSuggestions(getPath());
    }
}
