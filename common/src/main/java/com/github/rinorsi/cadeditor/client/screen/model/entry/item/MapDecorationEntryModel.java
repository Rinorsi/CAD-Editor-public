package com.github.rinorsi.cadeditor.client.screen.model.entry.item;

import com.github.franckyi.databindings.api.ObjectProperty;
import com.github.rinorsi.cadeditor.client.screen.model.category.item.ItemEditorCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntryModel;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.network.chat.Component;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

@SuppressWarnings({"unused", "this-escape"})
public class MapDecorationEntryModel extends EntryModel {
    private final ObjectProperty<String> nameProperty = ObjectProperty.create("");
    private final ObjectProperty<String> typeProperty = ObjectProperty.create("");
    private final ObjectProperty<String> xTextProperty = ObjectProperty.create("");
    private final ObjectProperty<String> zTextProperty = ObjectProperty.create("");
    private final ObjectProperty<String> rotationTextProperty = ObjectProperty.create("");

    private String defaultName = "";
    private String defaultType = "";
    private String defaultXText = "";
    private String defaultZText = "";
    private String defaultRotationText = "";

    public MapDecorationEntryModel(ItemEditorCategoryModel category) {
        this(category, "", null);
    }

    public MapDecorationEntryModel(ItemEditorCategoryModel category, String name, MapDecorations.Entry entry) {
        super(category);
        if (name != null) {
            nameProperty.setValue(name);
        }
        if (entry != null) {
            entry.type().unwrapKey()
                    .map(ResourceKey::identifier)
                    .map(Identifier::toString)
                    .ifPresent(typeProperty::setValue);
            xTextProperty.setValue(formatDouble(entry.x()));
            zTextProperty.setValue(formatDouble(entry.z()));
            rotationTextProperty.setValue(formatDouble(entry.rotation()));
        }
        captureDefaults();
    }

    public ObjectProperty<String> nameProperty() {
        return nameProperty;
    }

    public ObjectProperty<String> typeProperty() {
        return typeProperty;
    }

    public ObjectProperty<String> xTextProperty() {
        return xTextProperty;
    }

    public ObjectProperty<String> zTextProperty() {
        return zTextProperty;
    }

    public ObjectProperty<String> rotationTextProperty() {
        return rotationTextProperty;
    }

    public String getName() {
        String value = nameProperty.getValue();
        return value == null ? "" : value.trim();
    }

    public String getTypeId() {
        String value = typeProperty.getValue();
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        Identifier normalized = DecorationTypeOption.normalizeId(value);
        return normalized == null ? "" : normalized.toString();
    }

    public String getXText() {
        String value = xTextProperty.getValue();
        return value == null ? "" : value.trim();
    }

    public String getZText() {
        String value = zTextProperty.getValue();
        return value == null ? "" : value.trim();
    }

    public String getRotationText() {
        String value = rotationTextProperty.getValue();
        return value == null ? "" : value.trim();
    }

    public boolean isBlank() {
        return getName().isEmpty() && getTypeId().isEmpty();
    }

    public Optional<Map.Entry<String, MapDecorations.Entry>> buildDecoration(HolderLookup.RegistryLookup<MapDecorationType> lookup) {
        String name = getName();
        String type = getTypeId();
        if (name.isEmpty() && type.isEmpty() && getXText().isEmpty() && getZText().isEmpty() && getRotationText().isEmpty()) {
            setValid(true);
            return Optional.empty();
        }
        if (name.isEmpty() || type.isEmpty()) {
            setValid(false);
            return Optional.empty();
        }
        if (lookup == null) {
            setValid(false);
            return Optional.empty();
        }
        Identifier typeId = Identifier.tryParse(type);
        if (typeId == null) {
            setValid(false);
            return Optional.empty();
        }
        Optional<Holder.Reference<MapDecorationType>> holder = lookup.get(ResourceKey.create(Registries.MAP_DECORATION_TYPE, typeId));
        if (holder.isEmpty()) {
            setValid(false);
            return Optional.empty();
        }
        Double parsedX = parseDouble(getXText());
        Double parsedZ = parseDouble(getZText());
        Float parsedRotation = parseFloat(getRotationText());
        if (parsedX == null || parsedZ == null || parsedRotation == null) {
            setValid(false);
            return Optional.empty();
        }
        MapDecorations.Entry entry = new MapDecorations.Entry(holder.get(), parsedX, parsedZ, parsedRotation);
        setValid(true);
        return Optional.of(new AbstractMap.SimpleEntry<>(name, entry));
    }

    private Double parseDouble(String value) {
        if (value.isEmpty()) {
            return 0d;
        }
        try {
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Float parseFloat(String value) {
        if (value.isEmpty()) {
            return 0f;
        }
        try {
            float parsed = Float.parseFloat(value);
            return Float.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String formatDouble(double value) {
        if (Math.abs(value - Math.rint(value)) < 1.0E-6) {
            return Long.toString(Math.round(value));
        }
        String text = Double.toString(value);
        if (text.contains(".")) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text;
    }

    private void captureDefaults() {
        defaultName = getName();
        defaultType = getTypeId();
        defaultXText = getXText();
        defaultZText = getZText();
        defaultRotationText = getRotationText();
    }

    @Override
    public void reset() {
        nameProperty.setValue(defaultName);
        typeProperty.setValue(defaultType);
        xTextProperty.setValue(defaultXText);
        zTextProperty.setValue(defaultZText);
        rotationTextProperty.setValue(defaultRotationText);
        setValid(true);
    }

    @Override
    public void apply() {
        captureDefaults();
        setValid(true);
    }

    @Override
    public Type getType() {
        return Type.MAP_DECORATION;
    }

    public record DecorationTypeOption(Identifier id, Component displayName) {
        private static final List<DecorationTypeOption> DEFAULT_OPTIONS = List.of(
                of("minecraft:player"),
                of("minecraft:player_off_map"),
                of("minecraft:player_off_limits"),
                of("minecraft:player_dead"),
                of("minecraft:frame"),
                of("minecraft:red_marker"),
                of("minecraft:blue_marker"),
                of("minecraft:target_point"),
                of("minecraft:mansion"),
                of("minecraft:monument"),
                of("minecraft:banner_white"),
                of("minecraft:banner_orange"),
                of("minecraft:banner_magenta"),
                of("minecraft:banner_light_blue"),
                of("minecraft:banner_yellow"),
                of("minecraft:banner_lime"),
                of("minecraft:banner_pink"),
                of("minecraft:banner_gray"),
                of("minecraft:banner_light_gray"),
                of("minecraft:banner_cyan"),
                of("minecraft:banner_purple"),
                of("minecraft:banner_blue"),
                of("minecraft:banner_brown"),
                of("minecraft:banner_green"),
                of("minecraft:banner_red"),
                of("minecraft:banner_black"),
                of("minecraft:trial_chambers"),
                of("minecraft:pale_oak"),
                of("minecraft:wind_charge")
        );

        public DecorationTypeOption(Identifier id, Component displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getResourceId() {
            return id.toString();
        }

        public Component getDisplayName() {
            return displayName;
        }

        public static List<DecorationTypeOption> collect(HolderLookup.RegistryLookup<MapDecorationType> lookup) {
            if (lookup == null) {
                return DEFAULT_OPTIONS;
            }
            List<DecorationTypeOption> options = lookup.listElements()
                    .map(holder -> of(holder.key().identifier()))
                    .sorted(Comparator.comparing(DecorationTypeOption::getResourceId))
                    .toList();
            return options.isEmpty() ? DEFAULT_OPTIONS : options;
        }

        public static List<DecorationTypeOption> defaults() {
            return DEFAULT_OPTIONS;
        }

        public static DecorationTypeOption find(Collection<DecorationTypeOption> options, String resourceId) {
            if (options == null || options.isEmpty() || resourceId == null || resourceId.isBlank()) {
                return null;
            }
            Identifier normalized = normalize(resourceId);
            if (normalized == null) {
                return null;
            }
            String idString = normalized.toString();
            for (DecorationTypeOption option : options) {
                if (option.getResourceId().equals(idString)) {
                    return option;
                }
            }
            return null;
        }

        public static Identifier normalizeId(String resourceId) {
            return normalize(resourceId);
        }

        private static Identifier normalize(String resourceId) {
            String value = resourceId.trim();
            if (!value.contains(":")) {
                value = "minecraft:" + value;
            }
            return Identifier.tryParse(value);
        }

        private static DecorationTypeOption of(String id) {
            Identifier parsed = normalize(id);
            if (parsed == null) {
                throw new IllegalArgumentException("Invalid map decoration id: " + id);
            }
            return new DecorationTypeOption(parsed, toDisplayName(parsed));
        }

        private static DecorationTypeOption of(Identifier id) {
            return new DecorationTypeOption(id, toDisplayName(id));
        }

        private static Component toDisplayName(Identifier id) {
            String display = id.getPath();
            if (display == null || display.isBlank()) {
                display = id.toString();
            }
            display = display.replace(':', ' ').replace('_', ' ');
            String[] parts = display.split("\\s+");
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                if (part.isEmpty()) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    builder.append(part.substring(1));
                }
            }
            return Component.literal(builder.toString());
        }
    }

}
