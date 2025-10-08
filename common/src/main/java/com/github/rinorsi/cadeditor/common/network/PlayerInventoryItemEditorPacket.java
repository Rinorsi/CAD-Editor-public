package com.github.rinorsi.cadeditor.common.network;

import com.github.rinorsi.cadeditor.common.EditorType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public final class PlayerInventoryItemEditorPacket extends InventoryItemEditorPacket {
    public static class RequestData extends InventoryItemEditorPacket.RequestData {
        public static final PacketSerializer<RequestData> SERIALIZER = new Serializer<>() {
            @Override
            public void write(RequestData obj, FriendlyByteBuf buf) {
                super.write(obj, buf);
                buf.writeBoolean(obj.isCreativeInventoryScreen());
            }

            @Override
            public void read(RequestData obj, FriendlyByteBuf buf) {
                super.read(obj, buf);
                obj.setCreativeInventoryScreen(buf.readBoolean());
            }

            @Override
            public RequestData createInstance() {
                return new RequestData();
            }
        };

        private boolean creativeInventoryScreen;

        private RequestData() {
        }

        public RequestData(int slot, boolean creativeInventoryScreen) {
            super(slot);
            this.creativeInventoryScreen = creativeInventoryScreen;
        }

        public boolean isCreativeInventoryScreen() {
            return creativeInventoryScreen;
        }

        protected void setCreativeInventoryScreen(boolean creativeInventoryScreen) {
            this.creativeInventoryScreen = creativeInventoryScreen;
        }
    }

    public static class Request extends InventoryItemEditorPacket.Request<RequestData> {
        public static final PacketSerializer<Request> SERIALIZER = new Serializer<>() {
            @Override
            public Request createInstance() {
                return new Request();
            }

            @Override
            protected PacketSerializer<RequestData> getRequestDataSerializer() {
                return RequestData.SERIALIZER;
            }
        };

        private Request() {
        }

        public Request(EditorType editorType, int slot, boolean creativeInventoryScreen) {
            super(editorType, new RequestData(slot, creativeInventoryScreen));
        }

        public boolean isCreativeInventoryScreen() {
            return getRequestData().isCreativeInventoryScreen();
        }
    }

    public static class Response extends InventoryItemEditorPacket.Response<RequestData> {
        public static final PacketSerializer<Response> SERIALIZER = new Serializer<>() {
            @Override
            public Response createInstance() {
                return new Response();
            }

            @Override
            protected PacketSerializer<ResponseData> getResponseDataSerializer() {
                return ResponseData.SERIALIZER;
            }

            @Override
            protected PacketSerializer<RequestData> getRequestDataSerializer() {
                return RequestData.SERIALIZER;
            }
        };

        private Response() {
        }

        public Response(Request request, boolean hasPermission, ItemStack item) {
            super(request, hasPermission, new ResponseData(item));
        }
    }

    public static class Update extends InventoryItemEditorPacket.Update<RequestData> {
        public static final PacketSerializer<Update> SERIALIZER = new Serializer<>() {
            @Override
            public Update createInstance() {
                return new Update();
            }

            @Override
            protected PacketSerializer<RequestData> getRequestDataSerializer() {
                return RequestData.SERIALIZER;
            }

            @Override
            protected PacketSerializer<ResponseData> getResponseDataSerializer() {
                return ResponseData.SERIALIZER;
            }
        };

        private Update() {
        }

        public Update(Response response, ItemStack itemStack) {
            super(response.getRequestData(), new ResponseData(itemStack));
        }
    }
}
