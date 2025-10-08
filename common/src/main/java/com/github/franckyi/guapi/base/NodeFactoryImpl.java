package com.github.franckyi.guapi.base;

import com.github.franckyi.guapi.api.NodeFactory;
import com.github.franckyi.guapi.api.node.Node;
import com.github.franckyi.guapi.api.node.TreeView;
import com.github.franckyi.guapi.api.node.builder.*;
import com.github.franckyi.guapi.base.node.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NodeFactoryImpl implements NodeFactory {
    public static final NodeFactory INSTANCE = new NodeFactoryImpl();

    protected NodeFactoryImpl() {
    }

    @Override
    public ButtonBuilder createButton() {
        return new ButtonImpl();
    }

    @Override
    public ButtonBuilder createButton(String text) {
        return new ButtonImpl(text);
    }

    @Override
    public ButtonBuilder createButton(Component text) {
        return new ButtonImpl(text);
    }

    @Override
    public ButtonBuilder createButton(Consumer<ButtonBuilder> with) {
        return createButton().with(with);
    }

    @Override
    public CheckBoxBuilder createCheckBox() {
        return new CheckBoxImpl();
    }

    @Override
    public CheckBoxBuilder createCheckBox(String text) {
        return new CheckBoxImpl(text);
    }

    @Override
    public CheckBoxBuilder createCheckBox(Component text) {
        return new CheckBoxImpl(text);
    }

    @Override
    public CheckBoxBuilder createCheckBox(Consumer<CheckBoxBuilder> with) {
        return createCheckBox().with(with);
    }

    @Override
    public <E> EnumButtonBuilder<E> createEnumButton() {
        return new EnumButtonImpl<>();
    }

    @Override
    public <E> EnumButtonBuilder<E> createEnumButton(E[] values) {
        return new EnumButtonImpl<>(values);
    }

    @Override
    public <E> EnumButtonBuilder<E> createEnumButton(Collection<? extends E> values) {
        return new EnumButtonImpl<>(values);
    }

    @Override
    public <E> EnumButtonBuilder<E> createEnumButton(E[] values, E value) {
        return new EnumButtonImpl<>(values, value);
    }

    @Override
    public <E> EnumButtonBuilder<E> createEnumButton(Collection<? extends E> values, E value) {
        return new EnumButtonImpl<>(values, value);
    }

    @Override
    public <E> EnumButtonBuilder<E> createEnumButton(Consumer<EnumButtonBuilder<E>> with) {
        return this.<E>createEnumButton().with(with);
    }

    @Override
    public <E extends Enum<E>> EnumButtonBuilder<E> createEnumButton(E value) {
        return EnumButtonImpl.fromEnum(value);
    }

    @Override
    public HBoxBuilder createHBox() {
        return new HBoxImpl();
    }

    @Override
    public HBoxBuilder createHBox(int spacing) {
        return new HBoxImpl(spacing);
    }

    @Override
    public HBoxBuilder createHBox(Node... children) {
        return new HBoxImpl(children);
    }

    @Override
    public HBoxBuilder createHBox(Collection<? extends Node> children) {
        return new HBoxImpl(children);
    }

    @Override
    public HBoxBuilder createHBox(int spacing, Node... children) {
        return new HBoxImpl(spacing, children);
    }

    @Override
    public HBoxBuilder createHBox(int spacing, Collection<? extends Node> children) {
        return new HBoxImpl(spacing, children);
    }

    @Override
    public HBoxBuilder createHBox(Consumer<HBoxBuilder> with) {
        return createHBox().with(with);
    }

    @Override
    public ImageViewBuilder createImageView(ResourceLocation id) {
        return new ImageViewImpl(id);
    }

    @Override
    public ImageViewBuilder createImageView(ResourceLocation id, int imageWidth, int imageHeight) {
        return new ImageViewImpl(id, imageWidth, imageHeight);
    }

    @Override
    public ImageViewBuilder createImageView(ResourceLocation id, Consumer<ImageViewBuilder> with) {
        return createImageView(id).with(with);
    }

    @Override
    public ItemViewBuilder createItemView() {
        return new ItemViewImpl();
    }

    @Override
    public ItemViewBuilder createItemView(ItemStack itemStack) {
        return new ItemViewImpl(itemStack);
    }

    @Override
    public ItemViewBuilder createItemView(Consumer<ItemViewBuilder> with) {
        return createItemView().with(with);
    }

    @Override
    public LabelBuilder createLabel() {
        return new LabelImpl();
    }

    @Override
    public LabelBuilder createLabel(String text) {
        return new LabelImpl(text);
    }

    @Override
    public LabelBuilder createLabel(Component text) {
        return new LabelImpl(text);
    }

    @Override
    public LabelBuilder createLabel(String text, boolean shadow) {
        return new LabelImpl(text, shadow);
    }

    @Override
    public LabelBuilder createLabel(Component text, boolean shadow) {
        return new LabelImpl(text, shadow);
    }

    @Override
    public LabelBuilder createLabel(Consumer<LabelBuilder> with) {
        return createLabel().with(with);
    }

    @Override
    public <E> ListViewBuilder<E> createListView(Class<E> eClass) {
        return new ListViewImpl<>();
    }

    @Override
    public <E> ListViewBuilder<E> createListView(Class<E> eClass, int itemHeight) {
        return new ListViewImpl<>(itemHeight);
    }

    @Override
    public <E> ListViewBuilder<E> createListView(int itemHeight, Collection<? extends E> items) {
        return new ListViewImpl<>(itemHeight, items);
    }

    @Override
    public <E> ListViewBuilder<E> createListView(Class<E> eClass, Consumer<ListViewBuilder<E>> with) {
        return createListView(eClass).with(with);
    }

    @Override
    public SliderBuilder createSlider() {
        return new SliderImpl();
    }

    @Override
    public SliderBuilder createSlider(double value) {
        return new SliderImpl(value);
    }

    @Override
    public SliderBuilder createSlider(double value, double minValue, double maxValue) {
        return new SliderImpl(value, minValue, maxValue);
    }

    @Override
    public SliderBuilder createSlider(double value, double minValue, double maxValue, double step) {
        return new SliderImpl(value, minValue, maxValue, step);
    }

    @Override
    public SpriteViewBuilder createSpriteView() {
        return new SpriteViewImpl();
    }

    @Override
    public SpriteViewBuilder createSpriteView(Supplier<TextureAtlasSprite> spriteFactory) {
        return new SpriteViewImpl(spriteFactory);
    }

    @Override
    public SpriteViewBuilder createSpriteView(Supplier<TextureAtlasSprite> spriteFactory, int imageWidth, int imageHeight) {
        return new SpriteViewImpl(spriteFactory, imageWidth, imageHeight);
    }

    @Override
    public TextFieldBuilder createTextField() {
        return new TextFieldImpl();
    }

    @Override
    public TextFieldBuilder createTextField(String value) {
        return new TextFieldImpl(value);
    }

    @Override
    public TextFieldBuilder createTextField(String label, String value) {
        return new TextFieldImpl(label, value);
    }

    @Override
    public TextFieldBuilder createTextField(Component label, String value) {
        return new TextFieldImpl(label, value);
    }

    @Override
    public TextFieldBuilder createTextField(Consumer<TextFieldBuilder> with) {
        return createTextField().with(with);
    }

    @Override
    public TextAreaBuilder createTextArea() {
        return new TextAreaImpl();
    }

    @Override
    public TextAreaBuilder createTextArea(String value) {
        return new TextAreaImpl(value);
    }

    @Override
    public TextAreaBuilder createTextArea(String label, String value) {
        return new TextAreaImpl(label, value);
    }

    @Override
    public TextAreaBuilder createTextArea(Component label, String value) {
        return new TextAreaImpl(label, value);
    }

    @Override
    public TextAreaBuilder createTextArea(Consumer<TextAreaBuilder> with) {
        return createTextArea().with(with);
    }

    @Override
    public TexturedButtonBuilder createTexturedButton(ResourceLocation id, boolean drawButton) {
        return new TexturedButtonImpl(id, drawButton);
    }

    @Override
    public TexturedButtonBuilder createTexturedButton(ResourceLocation id, int imageWidth, int imageHeight, boolean drawButton) {
        return new TexturedButtonImpl(id, imageWidth, imageHeight, drawButton);
    }

    @Override
    public TexturedButtonBuilder createTexturedButton(ResourceLocation id, boolean drawButton, Consumer<TexturedButtonBuilder> with) {
        return createTexturedButton(id, drawButton).with(with);
    }

    @Override
    public TexturedToggleButtonBuilder createTexturedToggleButton(ResourceLocation id, boolean drawButton) {
        return new TexturedToggleButtonImpl(id, drawButton);
    }

    @Override
    public TexturedToggleButtonBuilder createTexturedToggleButton(ResourceLocation id, int imageWidth, int imageHeight, boolean drawButton) {
        return new TexturedToggleButtonImpl(id, imageWidth, imageHeight, drawButton);
    }

    @Override
    public TexturedToggleButtonBuilder createTexturedToggleButton(ResourceLocation id, boolean drawButton, Consumer<TexturedToggleButtonBuilder> with) {
        return createTexturedToggleButton(id, drawButton).with(with);
    }

    @Override
    public ToggleButtonBuilder createToggleButton() {
        return new ToggleButtonImpl();
    }

    @Override
    public ToggleButtonBuilder createToggleButton(String text) {
        return new ToggleButtonImpl(text);
    }

    @Override
    public ToggleButtonBuilder createToggleButton(Component text) {
        return new ToggleButtonImpl(text);
    }

    @Override
    public ToggleButtonBuilder createToggleButton(Consumer<ToggleButtonBuilder> with) {
        return createToggleButton().with(with);
    }

    @Override
    public <E extends TreeView.TreeItem<E>> TreeViewBuilder<E> createTreeView(Class<E> eClass) {
        return new TreeViewImpl<>();
    }

    @Override
    public <E extends TreeView.TreeItem<E>> TreeViewBuilder<E> createTreeView(Class<E> eClass, int itemHeight) {
        return new TreeViewImpl<>(itemHeight);
    }

    @Override
    public <E extends TreeView.TreeItem<E>> TreeViewBuilder<E> createTreeView(int itemHeight, E root) {
        return new TreeViewImpl<>(itemHeight, root);
    }

    @Override
    public <E extends TreeView.TreeItem<E>> TreeViewBuilder<E> createTreeView(Class<E> eClass, Consumer<TreeViewBuilder<E>> with) {
        return createTreeView(eClass).with(with);
    }

    @Override
    public VBoxBuilder createVBox() {
        return new VBoxImpl();
    }

    @Override
    public VBoxBuilder createVBox(int spacing) {
        return new VBoxImpl(spacing);
    }

    @Override
    public VBoxBuilder createVBox(Node... children) {
        return new VBoxImpl(children);
    }

    @Override
    public VBoxBuilder createVBox(Collection<? extends Node> children) {
        return new VBoxImpl(children);
    }

    @Override
    public VBoxBuilder createVBox(int spacing, Collection<? extends Node> children) {
        return new VBoxImpl(spacing, children);
    }

    @Override
    public VBoxBuilder createVBox(int spacing, Node... children) {
        return new VBoxImpl(spacing, children);
    }

    @Override
    public VBoxBuilder createVBox(Consumer<VBoxBuilder> with) {
        return createVBox().with(with);
    }

    @Override
    public SceneBuilder createScene() {
        return new SceneImpl();
    }

    @Override
    public SceneBuilder createScene(Node root) {
        return new SceneImpl(root);
    }

    @Override
    public SceneBuilder createScene(Node root, boolean fullScreen) {
        return new SceneImpl(root, fullScreen);
    }

    @Override
    public SceneBuilder createScene(Node root, boolean fullScreen, boolean texturedBackground) {
        return new SceneImpl(root, fullScreen, texturedBackground);
    }

    @Override
    public SceneBuilder createScene(Consumer<SceneBuilder> with) {
        return createScene().with(with);
    }
}
