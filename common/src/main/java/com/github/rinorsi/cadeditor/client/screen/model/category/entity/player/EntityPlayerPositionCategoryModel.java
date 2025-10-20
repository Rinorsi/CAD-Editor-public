package com.github.rinorsi.cadeditor.client.screen.model.category.entity.player;

import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.DoubleEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.IntegerEntryModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Allows editing of player position, rotation, motion and timers such as fire/air.
 */
public class EntityPlayerPositionCategoryModel extends EntityCategoryModel {
    private double posX, posY, posZ;
    private double motionX, motionY, motionZ;
    private float rotY, rotX;
    private int fireTicks;
    private int airTicks;
    private float fallDistance;

    public EntityPlayerPositionCategoryModel(EntityEditorModel editor) {
        super(Component.translatable("cadeditor.gui.player_position"), editor);
    }

    @Override
    protected void setupEntries() {
        readValues();
        getEntries().add(new DoubleEntryModel(this, Component.translatable("cadeditor.gui.player_pos_x"), posX, value -> posX = value));
        getEntries().add(new DoubleEntryModel(this, Component.translatable("cadeditor.gui.player_pos_y"), posY, value -> posY = value));
        getEntries().add(new DoubleEntryModel(this, Component.translatable("cadeditor.gui.player_pos_z"), posZ, value -> posZ = value));
        getEntries().add(new FloatEntryModel(this, Component.translatable("cadeditor.gui.player_rot_yaw"), rotY, value -> rotY = value));
        getEntries().add(new FloatEntryModel(this, Component.translatable("cadeditor.gui.player_rot_pitch"), rotX, value -> rotX = value));
        getEntries().add(new DoubleEntryModel(this, Component.translatable("cadeditor.gui.player_motion_x"), motionX, value -> motionX = value));
        getEntries().add(new DoubleEntryModel(this, Component.translatable("cadeditor.gui.player_motion_y"), motionY, value -> motionY = value));
        getEntries().add(new DoubleEntryModel(this, Component.translatable("cadeditor.gui.player_motion_z"), motionZ, value -> motionZ = value));
        getEntries().add(new IntegerEntryModel(this, Component.translatable("cadeditor.gui.player_fire"), fireTicks, value -> fireTicks = Math.max(0, value)));
        getEntries().add(new IntegerEntryModel(this, Component.translatable("cadeditor.gui.player_air"), airTicks, value -> airTicks = Math.max(0, value)));
        getEntries().add(new FloatEntryModel(this, Component.translatable("cadeditor.gui.player_fall_distance"), fallDistance, value -> fallDistance = Math.max(0f, value)));
    }

    @Override
    public void apply() {
        super.apply();
        writeValues();
        syncPlayerInstance();
    }

    private void readValues() {
        CompoundTag data = ensurePlayerTag();
        ListTag posList = data.getList("Pos", Tag.TAG_DOUBLE);
        if (posList.size() == 3) {
            posX = posList.getDouble(0);
            posY = posList.getDouble(1);
            posZ = posList.getDouble(2);
        }
        ListTag rotList = data.getList("Rotation", Tag.TAG_FLOAT);
        if (rotList.size() == 2) {
            rotY = rotList.getFloat(0);
            rotX = rotList.getFloat(1);
        }
        ListTag motionList = data.getList("Motion", Tag.TAG_DOUBLE);
        if (motionList.size() == 3) {
            motionX = motionList.getDouble(0);
            motionY = motionList.getDouble(1);
            motionZ = motionList.getDouble(2);
        }
        fireTicks = data.getInt("Fire");
        airTicks = data.getInt("Air");
        fallDistance = data.contains("FallDistance", Tag.TAG_FLOAT) ? data.getFloat("FallDistance") : 0f;
    }

    private void writeValues() {
        CompoundTag data = ensurePlayerTag();
        ListTag pos = new ListTag();
        pos.add(net.minecraft.nbt.DoubleTag.valueOf(posX));
        pos.add(net.minecraft.nbt.DoubleTag.valueOf(posY));
        pos.add(net.minecraft.nbt.DoubleTag.valueOf(posZ));
        data.put("Pos", pos);

        ListTag rotation = new ListTag();
        rotation.add(net.minecraft.nbt.FloatTag.valueOf(rotY));
        rotation.add(net.minecraft.nbt.FloatTag.valueOf(rotX));
        data.put("Rotation", rotation);

        ListTag motion = new ListTag();
        motion.add(net.minecraft.nbt.DoubleTag.valueOf(motionX));
        motion.add(net.minecraft.nbt.DoubleTag.valueOf(motionY));
        motion.add(net.minecraft.nbt.DoubleTag.valueOf(motionZ));
        data.put("Motion", motion);

        data.putInt("Fire", Math.max(0, fireTicks));
        data.putInt("Air", Math.max(0, airTicks));
        if (fallDistance > 0f) {
            data.putFloat("FallDistance", fallDistance);
        } else {
            data.remove("FallDistance");
        }
    }

    private void syncPlayerInstance() {
        Entity entity = getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }
        player.setPos(posX, posY, posZ);
        player.setDeltaMovement(motionX, motionY, motionZ);
        player.setYRot(rotY);
        player.setXRot(rotX);
        player.setYHeadRot(rotY);
        player.setYBodyRot(rotY);
        player.setRemainingFireTicks(Math.max(0, fireTicks));
        player.setAirSupply(Math.max(0, airTicks));
        player.fallDistance = Math.max(0f, fallDistance);
    }

    private CompoundTag ensurePlayerTag() {
        CompoundTag data = getData();
        if (data == null) {
            data = new CompoundTag();
            getContext().setTag(data);
        }
        return data;
    }
}
