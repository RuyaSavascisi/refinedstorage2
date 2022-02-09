package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridScrollModeUtil;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class GridScrollPacket {
    private final ItemResource itemResource;
    private final GridScrollMode mode;
    private final int slot;

    public GridScrollPacket(ItemResource itemResource, GridScrollMode mode, int slot) {
        this.itemResource = itemResource;
        this.mode = mode;
        this.slot = slot;
    }

    public static GridScrollPacket decode(FriendlyByteBuf buf) {
        return new GridScrollPacket(
                PacketUtil.readItemResource(buf),
                GridScrollModeUtil.getMode(buf.readByte()),
                buf.readInt()
        );
    }

    public static void encode(GridScrollPacket packet, FriendlyByteBuf buf) {
        PacketUtil.writeItemResource(buf, packet.itemResource);
        GridScrollModeUtil.writeMode(buf, packet.mode);
        buf.writeInt(packet.slot);
    }

    public static void handle(GridScrollPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(GridScrollPacket packet, Player player) {
        AbstractContainerMenu screenHandler = player.containerMenu;
        if (screenHandler instanceof ItemGridEventHandler gridEventHandler) {
            gridEventHandler.onScroll(packet.itemResource, packet.mode, packet.slot);
        }
    }
}