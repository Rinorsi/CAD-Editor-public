package com.github.rinorsi.cadeditor.client.screen.model.category.item;

import com.github.rinorsi.cadeditor.client.screen.model.ItemEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple editor for the multi-field custom_model_data component introduced in 1.21.4.
 * Users can enter comma or newline separated values for each field.
 */
public class ItemCustomModelDataCategoryModel extends ItemEditorCategoryModel {
    private StringEntryModel floatsEntry;
    private StringEntryModel flagsEntry;
    private StringEntryModel stringsEntry;
    private StringEntryModel colorsEntry;

    public ItemCustomModelDataCategoryModel(ItemEditorModel editor) {
        super(ModTexts.gui("custom_model_data"), editor);
    }

    @Override
    protected void setupEntries() {
        CustomModelData data = getStack().get(DataComponents.CUSTOM_MODEL_DATA);

        floatsEntry = createEntry(ModTexts.gui("custom_model_data.floats"),
                joinNumeric(data == null ? null : data.floats()));
        floatsEntry.setPlaceholder(placeholder("custom_model_data.floats.placeholder"));
        getEntries().add(floatsEntry);

        flagsEntry = createEntry(ModTexts.gui("custom_model_data.flags"),
                joinFlags(data == null ? null : data.flags()));
        flagsEntry.setPlaceholder(placeholder("custom_model_data.flags.placeholder"));
        getEntries().add(flagsEntry);

        stringsEntry = createEntry(ModTexts.gui("custom_model_data.strings"),
                joinStrings(data == null ? null : data.strings()));
        stringsEntry.setPlaceholder(placeholder("custom_model_data.strings.placeholder"));
        getEntries().add(stringsEntry);

        colorsEntry = createEntry(ModTexts.gui("custom_model_data.colors"),
                joinNumeric(data == null ? null : data.colors()));
        colorsEntry.setPlaceholder(placeholder("custom_model_data.colors.placeholder"));
        getEntries().add(colorsEntry);
    }

    @Override
    public void apply() {
        super.apply();
        ItemStack stack = getStack();

        ParseResult<Float> floats = parseFloatList(floatsEntry);
        ParseResult<Boolean> flags = parseBooleanList(flagsEntry);
        ParseResult<String> strings = parseStringList(stringsEntry);
        ParseResult<Integer> colors = parseColorList(colorsEntry);

        if (!floats.valid || !flags.valid || !strings.valid || !colors.valid) {
            return;
        }

        if (floats.values.isEmpty()
                && flags.values.isEmpty()
                && strings.values.isEmpty()
                && colors.values.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
            return;
        }

        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(
                        List.copyOf(floats.values),
                        List.copyOf(flags.values),
                        List.copyOf(strings.values),
                        List.copyOf(colors.values)
                ));
    }

    private StringEntryModel createEntry(MutableComponent label, String initialValue) {
        return new StringEntryModel(this, label, initialValue == null ? "" : initialValue, v -> { });
    }

    private String placeholder(String key) {
        return ModTexts.gui(key).getString();
    }

    private ItemStack getStack() {
        return getParent().getContext().getItemStack();
    }

    private static String joinNumeric(List<? extends Number> values) {
        if (values == null || values.isEmpty()) return "";
        return values.stream()
                .map(number -> number.toString())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private static String joinFlags(List<Boolean> values) {
        if (values == null || values.isEmpty()) return "";
        return values.stream()
                .map(flag -> flag ? "true" : "false")
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private static String joinStrings(List<String> values) {
        if (values == null || values.isEmpty()) return "";
        return String.join("\n", values);
    }

    private ParseResult<Float> parseFloatList(StringEntryModel entry) {
        return parseList(entry, token -> {
            try {
                return Float.parseFloat(token);
            } catch (NumberFormatException ex) {
                return null;
            }
        });
    }

    private ParseResult<Boolean> parseBooleanList(StringEntryModel entry) {
        return parseList(entry, token -> {
            String lower = token.toLowerCase();
            if ("true".equals(lower)) return Boolean.TRUE;
            if ("false".equals(lower)) return Boolean.FALSE;
            return null;
        });
    }

    private ParseResult<String> parseStringList(StringEntryModel entry) {
        return parseList(entry, token -> token);
    }

    private ParseResult<Integer> parseColorList(StringEntryModel entry) {
        return parseList(entry, token -> {
            try {
                return Integer.decode(token);
            } catch (NumberFormatException ex) {
                return null;
            }
        });
    }

    private <T> ParseResult<T> parseList(StringEntryModel entry, Parser<T> parser) {
        String raw = entry.getValue();
        if (raw == null || raw.isBlank()) {
            entry.setValid(true);
            return ParseResult.valid(Collections.emptyList());
        }
        List<T> values = new ArrayList<>();
        boolean valid = true;
        for (String token : raw.split("[,\\n]")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) continue;
            T parsed = parser.parse(trimmed);
            if (parsed == null) {
                valid = false;
                break;
            }
            values.add(parsed);
        }
        entry.setValid(valid);
        return valid ? ParseResult.valid(values) : ParseResult.invalid();
    }

    private interface Parser<T> {
        T parse(String token);
    }

    private static final class ParseResult<T> {
        final List<T> values;
        final boolean valid;

        private ParseResult(List<T> values, boolean valid) {
            this.values = values;
            this.valid = valid;
        }

        static <T> ParseResult<T> valid(List<T> values) {
            return new ParseResult<>(values, true);
        }

        static <T> ParseResult<T> invalid() {
            return new ParseResult<>(List.of(), false);
        }
    }
}
