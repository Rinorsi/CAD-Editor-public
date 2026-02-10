package com.github.rinorsi.cadeditor.client.screen.model.category.entity;

import com.github.rinorsi.cadeditor.client.ClientUtil;
import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.ActionEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.DoubleEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.EntityEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.StringEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.entity.EntitySingleItemEntryModel;
import com.github.rinorsi.cadeditor.client.util.NbtUuidHelper;
import com.github.rinorsi.cadeditor.common.ModTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

public class EntityMountCategoryModel extends EntityCategoryModel {
    private static final String EQUIPMENT_TAG = "equipment";
    private static final String SADDLE_EQUIPMENT_KEY = EquipmentSlot.SADDLE.getSerializedName();

    private BooleanEntryModel saddledEntry;
    private EntitySingleItemEntryModel saddleItemEntry;
    private BooleanEntryModel chestedEntry;
    private StringEntryModel leashHolderEntry;
    private BooleanEntryModel leashAnchorEntry;
    private DoubleEntryModel leashXEntry;
    private DoubleEntryModel leashYEntry;
    private DoubleEntryModel leashZEntry;
    private IntegerEntryModel temperEntry;
    private IntegerEntryModel strengthEntry;

    private int passengerListStart = -1;

    public EntityMountCategoryModel(EntityEditorModel editor) {
        super(ModTexts.ENTITY_MOUNT, editor);
    }

    @Override
    protected void setupEntries() {
        CompoundTag data = getData();
        if (data == null) {
            return;
        }
        temperEntry = null;
        strengthEntry = null;
        ItemStack saddleStack = readSaddleItem(data);
        boolean saddled = !saddleStack.isEmpty() || data.getBooleanOr("Saddled", false) || data.getBooleanOr("Saddle", false);
        boolean chestedHorse = data.getBooleanOr("ChestedHorse", false);
        String leashHolder = readLeashHolder(data);
        CompoundTag leashTag = data.getCompound("Leash").map(CompoundTag::copy).orElse(null);
        boolean hasLeashAnchor = leashTag != null && (leashTag.contains("X") || leashTag.contains("Y") || leashTag.contains("Z"));
        double leashX = leashTag != null ? leashTag.getDoubleOr("X", 0d) : 0d;
        double leashY = leashTag != null ? leashTag.getDoubleOr("Y", 0d) : 0d;
        double leashZ = leashTag != null ? leashTag.getDoubleOr("Z", 0d) : 0d;

        saddledEntry = new BooleanEntryModel(this, ModTexts.SADDLED, saddled, value -> {});
        saddleItemEntry = new EntitySingleItemEntryModel(this, ModTexts.SADDLE_ITEM, saddleStack);
        chestedEntry = new BooleanEntryModel(this, ModTexts.CHESTED_HORSE, chestedHorse, value -> {});
        leashHolderEntry = new StringEntryModel(this, ModTexts.LEASH_HOLDER, leashHolder, value -> {});
        leashAnchorEntry = new BooleanEntryModel(this, ModTexts.LEASH_ANCHOR, hasLeashAnchor, value -> {});
        leashXEntry = new DoubleEntryModel(this, ModTexts.LEASH_POS_X, leashX, value -> {});
        leashYEntry = new DoubleEntryModel(this, ModTexts.LEASH_POS_Y, leashY, value -> {});
        leashZEntry = new DoubleEntryModel(this, ModTexts.LEASH_POS_Z, leashZ, value -> {});

        getEntries().add(saddledEntry);
        getEntries().add(chestedEntry);
        getEntries().add(saddleItemEntry);

        if (hasTemper()) {
            int temper = data.getIntOr("Temper", 0);
            temperEntry = new IntegerEntryModel(this, ModTexts.MOUNT_TEMPER, temper, value -> {});
            getEntries().add(temperEntry);
        }

        if (hasStrength()) {
            int strength = data.getIntOr("Strength", 1);
            strengthEntry = new IntegerEntryModel(this, ModTexts.MOUNT_STRENGTH, strength, value -> {});
            getEntries().add(strengthEntry);
        }

        getEntries().add(leashHolderEntry);
        getEntries().add(new ActionEntryModel(this, ModTexts.USE_SELF_UUID, this::setLeashHolderSelf));
        getEntries().add(leashAnchorEntry);
        getEntries().add(leashXEntry);
        getEntries().add(leashYEntry);
        getEntries().add(leashZEntry);

        passengerListStart = getEntries().size();
        ListTag passengers = data.getList("Passengers").orElseGet(ListTag::new);
        for (Tag tag : passengers) {
            if (tag instanceof CompoundTag passengerTag) {
                getEntries().add(createPassengerEntry(passengerTag));
            }
        }
    }

    private void setLeashHolderSelf() {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        leashHolderEntry.setValue(Minecraft.getInstance().player.getUUID().toString());
    }

    private EntityEntryModel createPassengerEntry(CompoundTag passengerTag) {
        EntityType<?> type = null;
        String id = passengerTag.getString("id").orElse("");
        if (!id.isEmpty()) {
            type = EntityType.byString(id).orElse(null);
        }
        EntityEntryModel model = new EntityEntryModel(this, type, passengerTag, value -> {});
        model.setReorderable(false);
        return model;
    }

    private ItemStack readSaddleItem(CompoundTag data) {
        ItemStack fromEquipment = data.getCompound(EQUIPMENT_TAG)
                .flatMap(equipment -> equipment.getCompound(SADDLE_EQUIPMENT_KEY))
                .map(tag -> ClientUtil.parseItemStack(ClientUtil.registryAccess(), tag))
                .orElse(ItemStack.EMPTY);
        if (!fromEquipment.isEmpty()) {
            return fromEquipment;
        }
        return data.getCompound("SaddleItem")
                .map(tag -> ClientUtil.parseItemStack(ClientUtil.registryAccess(), tag))
                .orElse(ItemStack.EMPTY);
    }

    private String readLeashHolder(CompoundTag data) {
        UUID uuid = NbtUuidHelper.getUuid(data, "LeashHolder");
        if (uuid != null) {
            return uuid.toString();
        }
        return data.getString("LeashHolder").orElse("");
    }

    @Override
    public int getEntryListStart() {
        return passengerListStart;
    }

    @Override
    public boolean canAddEntryInList() {
        return true;
    }

    @Override
    public EntityEntryModel createNewListEntry() {
        return createPassengerEntry(new CompoundTag());
    }

    @Override
    protected net.minecraft.network.chat.MutableComponent getAddListEntryButtonTooltip() {
        return ModTexts.PASSENGERS;
    }

    @Override
    public void apply() {
        super.apply();
        CompoundTag data = getData();
        if (data == null) {
            return;
        }
        applySaddleData(data);
        applyChestData(data);
        applyMountStats(data);
        applyLeashData(data);
        applyPassengers(data);
    }

    private void applySaddleData(CompoundTag data) {
        boolean saddled = Boolean.TRUE.equals(saddledEntry.getValue());
        ItemStack saddleStack = saddleItemEntry.getItemStack();

        CompoundTag equipmentTag = data.getCompound(EQUIPMENT_TAG).orElseGet(CompoundTag::new);
        if (saddled) {
            if (saddleStack.isEmpty()) {
                saddleStack = new ItemStack(Items.SADDLE);
                saddleItemEntry.setItemStack(saddleStack.copy());
            }
            CompoundTag saddleTag = ClientUtil.saveItemStack(ClientUtil.registryAccess(), saddleStack);
            equipmentTag.put(SADDLE_EQUIPMENT_KEY, saddleTag);
            data.put(EQUIPMENT_TAG, equipmentTag);
        } else {
            equipmentTag.remove(SADDLE_EQUIPMENT_KEY);
            if (equipmentTag.isEmpty()) {
                data.remove(EQUIPMENT_TAG);
            } else {
                data.put(EQUIPMENT_TAG, equipmentTag);
            }
            data.remove("Saddle");
            data.remove("Saddled");
            data.remove("SaddleItem");
            if (!saddleStack.isEmpty()) {
                saddleItemEntry.setItemStack(ItemStack.EMPTY);
            }
        }
        // Legacy cleanup to avoid duplicated state
        data.remove("Saddle");
        data.remove("Saddled");
        data.remove("SaddleItem");
    }

    private void applyMountStats(CompoundTag data) {
        if (temperEntry != null) {
            Integer value = temperEntry.getValue();
            if (value == null) {
                data.remove("Temper");
            } else {
                data.putInt("Temper", clamp(value, 0, 100));
            }
        }
        if (strengthEntry != null) {
            Integer value = strengthEntry.getValue();
            if (value == null) {
                data.remove("Strength");
            } else {
                data.putInt("Strength", clamp(value, 1, 5));
            }
        }
    }

    private void applyChestData(CompoundTag data) {
        if (Boolean.TRUE.equals(chestedEntry.getValue())) {
            data.putBoolean("ChestedHorse", true);
        } else {
            data.remove("ChestedHorse");
        }
    }

    private void applyLeashData(CompoundTag data) {
        String leashHolder = normalize(leashHolderEntry.getValue());
        if (leashHolder.isEmpty()) {
            data.remove("LeashHolder");
        } else if (isUuidString(leashHolder)) {
            NbtUuidHelper.putUuid(data, "LeashHolder", UUID.fromString(leashHolder));
        } else {
            data.putString("LeashHolder", leashHolder);
        }

        boolean anchorEnabled = Boolean.TRUE.equals(leashAnchorEntry.getValue());
        if (!anchorEnabled) {
            data.remove("Leash");
            return;
        }
        CompoundTag leash = data.getCompound("Leash").orElseGet(CompoundTag::new);
        leash.putDouble("X", leashXEntry.getValue());
        leash.putDouble("Y", leashYEntry.getValue());
        leash.putDouble("Z", leashZEntry.getValue());
        data.put("Leash", leash);
    }

    private void applyPassengers(CompoundTag data) {
        ListTag passengers = new ListTag();
        int listStart = getEntryListStart();
        if (listStart >= 0) {
            for (int i = listStart; i < getEntries().size(); i++) {
                var entry = getEntries().get(i);
                if (!(entry instanceof EntityEntryModel passenger)) {
                    continue;
                }
                CompoundTag tag = passenger.copyValue();
                if (tag.isEmpty()) {
                    continue;
                }
                passengers.add(tag);
            }
        }
        if (passengers.isEmpty()) {
            data.remove("Passengers");
        } else {
            data.put("Passengers", passengers);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isUuidString(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private boolean hasTemper() {
        if (getEntity() instanceof AbstractHorse) {
            return true;
        }
        CompoundTag data = getData();
        return data != null && data.getInt("Temper").isPresent();
    }

    private boolean hasStrength() {
        if (getEntity() instanceof Llama) {
            return true;
        }
        CompoundTag data = getData();
        return data != null && data.getInt("Strength").isPresent();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
