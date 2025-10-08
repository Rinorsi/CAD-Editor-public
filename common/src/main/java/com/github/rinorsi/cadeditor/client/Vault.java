package com.github.rinorsi.cadeditor.client;

import com.github.rinorsi.cadeditor.PlatformUtil;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public final class Vault {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int CURRENT_VERSION = 0;
    private static final Path VAULT_FILE_OLD = PlatformUtil.getConfigDir().resolve("ibeeditor-clipboard.dat");
    private static final Path VAULT_FILE_LEGACY = PlatformUtil.getConfigDir().resolve("ibeeditor-vault.dat");
    private static final Path VAULT_FILE = PlatformUtil.getConfigDir().resolve("cadeditor-vault.dat");
    private static final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    private static Vault INSTANCE;

    private int version;
    private List<CompoundTag> items;
    private List<CompoundTag> entities;

    private Vault() {
        version = CURRENT_VERSION;
        items = new ArrayList<>();
        entities = new ArrayList<>();
    }

    public List<CompoundTag> getItems() {
        return items;
    }

    public List<CompoundTag> getEntities() {
        return entities;
    }

    public boolean saveItem(CompoundTag tag) {
        if (items.contains(tag)) {
            return false;
        }
        items.add(tag);
        save();
        return true;
    }

    public boolean saveEntity(CompoundTag tag) {
        if (entities.contains(tag)) {
            return false;
        }
        entities.add(tag);
        save();
        return true;
    }

    public void clear() {
        items.clear();
        entities.clear();
    }

    public static void load() {
        if (Files.exists(VAULT_FILE)) {
            loadFromFile(VAULT_FILE);
        } else if (Files.exists(VAULT_FILE_LEGACY)) {
            LOGGER.info("检测到旧保险库文件，正在迁移");
            loadFromFile(VAULT_FILE_LEGACY);
            save();
            try {
                Files.deleteIfExists(VAULT_FILE_LEGACY);
            } catch (IOException e) {
                LOGGER.error("删除旧保险库文件时出错", e);
            }
        } else if (Files.exists(VAULT_FILE_OLD)) {
            LOGGER.info("检测到旧剪贴板文件，正在转换为保险库文件");
            loadFromFile(VAULT_FILE_OLD);
            try {
                Files.delete(VAULT_FILE_OLD);
            } catch (IOException e) {
                LOGGER.error("删除旧剪贴板文件时出错", e);
            }
            INSTANCE.version = 0;
            save();
        } else {
            LOGGER.info("生成空保险库");
            INSTANCE = new Vault();
            save();
        }
    }

    private static void loadFromFile(Path path) {
        INSTANCE = new Vault();
        try (var is = Files.newInputStream(path)) {
            buffer.clear();
            readIntoBuffer(is);
            buffer.readerIndex(0);
            INSTANCE.version = readVersion();
            int itemCount = readSize("items");
            IntStream.range(0, itemCount).forEach(i -> INSTANCE.items.add(safeReadNbt()));
            int entityCount = readSize("entities");
            IntStream.range(0, entityCount).forEach(i -> INSTANCE.entities.add(safeReadNbt()));
            LOGGER.info("保险库已加载");
        } catch (VaultFileFormatException e) {
            LOGGER.warn("保险库文件格式无效：{}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("加载保险库时出错", e);
        } finally {
            buffer.clear();
        }
    }

    private static void readIntoBuffer(java.io.InputStream is) throws IOException {
        byte[] chunk = new byte[8192];
        int read;
        while ((read = is.read(chunk)) != -1) {
            buffer.writeBytes(chunk, 0, read);
        }
    }

    private static int readVersion() throws VaultFileFormatException {
        if (buffer.readableBytes() < Integer.BYTES) {
            throw new VaultFileFormatException("缺少版本头");
        }
        int version = buffer.readInt();
        if (version < 0 || version > CURRENT_VERSION) {
            throw new VaultFileFormatException("不支持的版本：" + version);
        }
        return version;
    }

    private static int readSize(String label) throws VaultFileFormatException {
        if (buffer.readableBytes() < Integer.BYTES) {
            throw new VaultFileFormatException(label + " 数量缺失");
        }
        int size = buffer.readInt();
        if (size < 0) {
            throw new VaultFileFormatException(label + " 数量为负数：" + size);
        }
        return size;
    }

    private static CompoundTag safeReadNbt() {
        var i = buffer.readerIndex();
        try {
            var result = buffer.readNbt();
            if (result == null || result.isEmpty()) throw new RuntimeException("Tag is empty, this must be an error");
            return result;
        } catch (Exception e) {
            buffer.readerIndex(i);
            byte b0 = buffer.readByte();
            if (b0 != Tag.TAG_COMPOUND) {
                throw e;
            } else {
                buffer.readerIndex(i);
                try {
                    return NbtIo.read(new ByteBufInputStream(buffer));
                } catch (IOException e0) {
                    throw new RuntimeException(e0);
                }
            }
        }
    }

    public static void save() {
        try (var os = Files.newOutputStream(VAULT_FILE)) {
            buffer.clear();
            buffer.writeInt(INSTANCE.version);
            buffer.writeInt(INSTANCE.items.size());
            INSTANCE.items.forEach(buffer::writeNbt);
            buffer.writeInt(INSTANCE.entities.size());
            INSTANCE.entities.forEach(buffer::writeNbt);
            buffer.readerIndex(0);
            buffer.readBytes(os, buffer.readableBytes());
            LOGGER.info("保险库已保存");
        } catch (IOException e) {
            LOGGER.error("保存保险库时出错", e);
        } finally {
            buffer.clear();
        }
    }

    private static final class VaultFileFormatException extends IOException {
        private static final long serialVersionUID = 1L;

        private VaultFileFormatException(String message) {
            super(message);
        }
    }

    public static Vault getInstance() {
        return INSTANCE;
    }
}


