package com.github.rinorsi.cadeditor.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record GiveVaultItemPacket(int slot, ItemStack itemStack) {
    public static final PacketSerializer<GiveVaultItemPacket> SERIALIZER = new PacketSerializer<>() {
        @Override
        public void write(GiveVaultItemPacket obj, FriendlyByteBuf buf) {
            buf.writeInt(obj.slot);
            if (buf instanceof RegistryFriendlyByteBuf registryBuf) {
                ItemStack.STREAM_CODEC.encode(registryBuf, obj.itemStack);
            } else {
                throw new IllegalStateException("Expected registry-friendly buffer for item stack serialization");
            }
        }

        @Override
        public GiveVaultItemPacket read(FriendlyByteBuf buf) {
            int slot = buf.readInt();
            if (buf instanceof RegistryFriendlyByteBuf registryBuf) {
                ItemStack stack = ItemStack.STREAM_CODEC.decode(registryBuf);
                return new GiveVaultItemPacket(slot, stack);
            }
            throw new IllegalStateException("Expected registry-friendly buffer for item stack deserialization");
        }
    };
}
