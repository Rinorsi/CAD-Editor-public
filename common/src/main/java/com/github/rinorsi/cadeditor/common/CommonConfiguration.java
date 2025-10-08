package com.github.rinorsi.cadeditor.common;

import com.github.rinorsi.cadeditor.PlatformUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CommonConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path OLD_COMMON_CONFIG_FILE = PlatformUtil.getConfigDir().resolve("ibeeditor-base.json");
    private static final Path LEGACY_COMMON_CONFIG_FILE = PlatformUtil.getConfigDir().resolve("ibeeditor-common.json");
    private static final Path COMMON_CONFIG_FILE = PlatformUtil.getConfigDir().resolve("cadeditor-common.json");
    public static CommonConfiguration INSTANCE;
    private static boolean changed;

    private int version;
    private int permissionLevel;
    private boolean creativeOnly;

    private CommonConfiguration() {
        version = 0;
        permissionLevel = 0;
        creativeOnly = false;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(int permissionLevel) {
        if (this.permissionLevel != permissionLevel) {
            this.permissionLevel = permissionLevel;
            changed = true;
        }
    }

    public boolean isCreativeOnly() {
        return creativeOnly;
    }

    public void setCreativeOnly(boolean creativeOnly) {
        if (this.creativeOnly != creativeOnly) {
            this.creativeOnly = creativeOnly;
            changed = true;
        }
    }

    public static void load() {
        if (Files.exists(COMMON_CONFIG_FILE)) {
            try (Reader r = Files.newBufferedReader(COMMON_CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(r, CommonConfiguration.class);
                LOGGER.info("通用配置已加载");
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.error("加载通用配置时出错", e);
                INSTANCE = new CommonConfiguration();
            }
        } else if (Files.exists(LEGACY_COMMON_CONFIG_FILE)) {
            try (Reader r = Files.newBufferedReader(LEGACY_COMMON_CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(r, CommonConfiguration.class);
                LOGGER.info("检测到旧通用配置文件，正在迁移");
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.error("加载旧通用配置文件时出错", e);
                INSTANCE = new CommonConfiguration();
            }
            changed = true;
            save();
            try {
                Files.deleteIfExists(LEGACY_COMMON_CONFIG_FILE);
            } catch (IOException e) {
                LOGGER.error("删除旧通用配置文件时出错", e);
            }
        } else {
            // Config file renamed in version 2.0.8
            if (Files.exists(OLD_COMMON_CONFIG_FILE)) {
                try {
                    LOGGER.info("检测到旧通用配置文件，正在删除");
                    Files.deleteIfExists(OLD_COMMON_CONFIG_FILE);
                } catch (IOException e) {
                    LOGGER.error("删除旧通用配置文件时出错", e);
                }
            }
            LOGGER.info("生成默认通用配置");
            INSTANCE = new CommonConfiguration();
            changed = true;
            save();
        }
        INSTANCE.applyMigrations();
    }

    public static void save() {
        if (changed) {
            try (Writer w = Files.newBufferedWriter(COMMON_CONFIG_FILE)) {
                GSON.toJson(INSTANCE, w);
                changed = false;
                LOGGER.info("通用配置已保存");
            } catch (IOException e) {
                LOGGER.error("保存通用配置时出错", e);
            }
        }
    }

    private void applyMigrations() {
        if (version != 0) {
            LOGGER.warn("Unsupported common configuration version {}, default values may be used", version);
        }
    }
}


