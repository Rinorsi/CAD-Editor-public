package com.github.rinorsi.cadeditor.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public abstract class ItemEditorPacket {
    protected record ResponseData(ItemStack itemStack) {
        public static final PacketSerializer<ResponseData> SERIALIZER = new PacketSerializer<>() {
            @Override
            public void write(ResponseData obj, FriendlyByteBuf buf) {
                if (buf instanceof RegistryFriendlyByteBuf registryBuf) {
                    ItemStack.STREAM_CODEC.encode(registryBuf, obj.itemStack());
                } else {
                    throw new IllegalStateException("Expected registry-friendly buffer for item stack serialization");
                }
            }

            @Override
            public ResponseData read(FriendlyByteBuf buf) {
                if (buf instanceof RegistryFriendlyByteBuf registryBuf) {
                    return new ResponseData(ItemStack.STREAM_CODEC.decode(registryBuf));
                }
                throw new IllegalStateException("Expected registry-friendly buffer for item stack deserialization");
            }
        };
    }

    protected abstract static class Response<REQ> extends AbstractEditorResponse<REQ, ResponseData> {
        protected Response() {
        }

        protected Response(AbstractEditorRequest<REQ> request, boolean permission, ResponseData responseData) {
            super(request, permission, responseData);
        }

        public ItemStack getItemStack() {
            return getResponseData().itemStack();
        }
    }

    protected abstract static class Update<REQ> extends AbstractEditorUpdate<REQ, ResponseData> {
        protected Update() {
        }

        protected Update(REQ requestData, ResponseData responseData) {
            super(requestData, responseData);
        }

        public ItemStack getItemStack() {
            return getResponseData().itemStack();
        }
    }
}
