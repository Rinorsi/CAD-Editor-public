package com.github.rinorsi.cadeditor.common.network;

import com.github.rinorsi.cadeditor.common.EditorType;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ItemNetworkSerializationTest {

    @BeforeAll
    static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void mainHandResponseSerializerSupportsEmptyStack() {
        MainHandItemEditorPacket.Request request = new MainHandItemEditorPacket.Request(EditorType.STANDARD);
        MainHandItemEditorPacket.Response response = new MainHandItemEditorPacket.Response(request, true, ItemStack.EMPTY);

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess());
        MainHandItemEditorPacket.Response.SERIALIZER.write(response, buf);
        buf.readerIndex(0);

        MainHandItemEditorPacket.Response decoded = MainHandItemEditorPacket.Response.SERIALIZER.read(buf);
        Assertions.assertTrue(decoded.getItemStack().isEmpty());
        Assertions.assertTrue(decoded.hasPermission());
    }

    @Test
    void mainHandUpdateSerializerSupportsEmptyStack() {
        MainHandItemEditorPacket.Request request = new MainHandItemEditorPacket.Request(EditorType.STANDARD);
        MainHandItemEditorPacket.Response response = new MainHandItemEditorPacket.Response(request, true, new ItemStack(Items.STICK));
        MainHandItemEditorPacket.Update update = new MainHandItemEditorPacket.Update(response, ItemStack.EMPTY);

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess());
        MainHandItemEditorPacket.Update.SERIALIZER.write(update, buf);
        buf.readerIndex(0);

        MainHandItemEditorPacket.Update decoded = MainHandItemEditorPacket.Update.SERIALIZER.read(buf);
        Assertions.assertTrue(decoded.getItemStack().isEmpty());
    }

    @Test
    void giveVaultSerializerSupportsEmptyStack() {
        GiveVaultItemPacket packet = new GiveVaultItemPacket(5, ItemStack.EMPTY);

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess());
        GiveVaultItemPacket.SERIALIZER.write(packet, buf);
        buf.readerIndex(0);

        GiveVaultItemPacket decoded = GiveVaultItemPacket.SERIALIZER.read(buf);
        Assertions.assertEquals(5, decoded.slot());
        Assertions.assertTrue(decoded.itemStack().isEmpty());
    }

    private static RegistryAccess registryAccess() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }
}
