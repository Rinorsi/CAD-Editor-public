package com.github.rinorsi.cadeditor.client.screen.model.category.entity.player;

import com.github.rinorsi.cadeditor.client.screen.model.EntityEditorModel;
import com.github.rinorsi.cadeditor.client.screen.model.category.entity.EntityCategoryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.BooleanEntryModel;
import com.github.rinorsi.cadeditor.client.screen.model.entry.FloatEntryModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.Tag;

/**
 * Exposes player abilities toggles and movement speeds.
 */
public class EntityPlayerAbilitiesCategoryModel extends EntityCategoryModel {
    private static final String ABILITIES_TAG = "abilities";

    private boolean invulnerable;
    private boolean mayFly;
    private boolean flying;
    private boolean mayBuild;
    private boolean instabuild;
    private float flySpeed;
    private float walkSpeed;

    public EntityPlayerAbilitiesCategoryModel(EntityEditorModel editor) {
        super(Component.translatable("cadeditor.gui.player_abilities"), editor);
    }

    @Override
    protected void setupEntries() {
        CompoundTag data = ensurePlayerTag();
        CompoundTag abilities = readAbilitiesTag(data);
        boolean creativeDefaults = isCreativeGameType(data);

        invulnerable = abilities.contains("invulnerable") && abilities.getBoolean("invulnerable");
        mayFly = abilities.contains("mayfly") ? abilities.getBoolean("mayfly") : creativeDefaults;
        flying = abilities.contains("flying") && abilities.getBoolean("flying");
        mayBuild = !abilities.contains("mayBuild") || abilities.getBoolean("mayBuild");
        instabuild = abilities.contains("instabuild") ? abilities.getBoolean("instabuild") : creativeDefaults;
        flySpeed = abilities.contains("flySpeed") ? abilities.getFloat("flySpeed") : 0.05f;
        walkSpeed = abilities.contains("walkSpeed") ? abilities.getFloat("walkSpeed") : 0.1f;

        getEntries().add(new BooleanEntryModel(this, Component.translatable("cadeditor.gui.ability_invulnerable"), invulnerable, value -> invulnerable = value));
        getEntries().add(new BooleanEntryModel(this, Component.translatable("cadeditor.gui.ability_mayfly"), mayFly, value -> mayFly = value));
        getEntries().add(new BooleanEntryModel(this, Component.translatable("cadeditor.gui.ability_flying"), flying, value -> flying = value));
        getEntries().add(new BooleanEntryModel(this, Component.translatable("cadeditor.gui.ability_may_build"), mayBuild, value -> mayBuild = value));
        getEntries().add(new BooleanEntryModel(this, Component.translatable("cadeditor.gui.ability_instabuild"), instabuild, value -> instabuild = value));
        getEntries().add(new FloatEntryModel(this, Component.translatable("cadeditor.gui.ability_walk_speed"), walkSpeed, value -> walkSpeed = clampSpeed(value)));
        getEntries().add(new FloatEntryModel(this, Component.translatable("cadeditor.gui.ability_fly_speed"), flySpeed, value -> flySpeed = clampSpeed(value)));
    }

    @Override
    public void apply() {
        super.apply();
        CompoundTag data = ensurePlayerTag();
        CompoundTag abilities = new CompoundTag();
        boolean effectiveMayBuild = mayBuild || instabuild;
        abilities.putBoolean("invulnerable", invulnerable);
        abilities.putBoolean("mayfly", mayFly);
        abilities.putBoolean("flying", flying && mayFly);
        abilities.putBoolean("mayBuild", effectiveMayBuild);
        abilities.putBoolean("instabuild", instabuild);
        abilities.putFloat("walkSpeed", clampSpeed(walkSpeed));
        abilities.putFloat("flySpeed", clampSpeed(flySpeed));
        data.put(ABILITIES_TAG, abilities);
        syncPlayerInstance();
    }

    private void syncPlayerInstance() {
        Player player = getEntity() instanceof Player p ? p : null;
        if (player == null) {
            return;
        }
        Abilities abilities = player.getAbilities();
        abilities.invulnerable = invulnerable;
        abilities.mayfly = mayFly;
        abilities.flying = flying && mayFly;
        abilities.mayBuild = mayBuild || instabuild;
        abilities.instabuild = instabuild;
        abilities.setWalkingSpeed(clampSpeed(walkSpeed));
        abilities.setFlyingSpeed(clampSpeed(flySpeed));
        player.onUpdateAbilities();
        if (player instanceof LocalPlayer localPlayer && localPlayer.connection != null) {
            localPlayer.connection.send(new ServerboundPlayerAbilitiesPacket(abilities));
        }
    }

    private static float clampSpeed(float value) {
        return Math.max(0f, Math.min(value, 1f));
    }

    private CompoundTag ensurePlayerTag() {
        CompoundTag data = getData();
        if (data == null) {
            data = new CompoundTag();
            getContext().setTag(data);
        }
        return data;
    }

    private static CompoundTag readAbilitiesTag(CompoundTag root) {
        if (root.contains(ABILITIES_TAG, Tag.TAG_COMPOUND)) {
            return root.getCompound(ABILITIES_TAG);
        }
        if (root.contains("Abilities", Tag.TAG_COMPOUND)) {
            return root.getCompound("Abilities");
        }
        return new CompoundTag();
    }

    private static boolean isCreativeGameType(CompoundTag root) {
        return root.contains("playerGameType", Tag.TAG_INT) && root.getInt("playerGameType") == 1;
    }
}
