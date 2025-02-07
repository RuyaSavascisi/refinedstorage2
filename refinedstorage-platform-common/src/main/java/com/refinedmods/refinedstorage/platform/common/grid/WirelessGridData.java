package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.platform.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.slotreference.SlotReferenceFactory;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record WirelessGridData(GridData gridData, SlotReference slotReference) {
    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessGridData> STREAM_CODEC = StreamCodec.composite(
        GridData.STREAM_CODEC, WirelessGridData::gridData,
        SlotReferenceFactory.STREAM_CODEC, WirelessGridData::slotReference,
        WirelessGridData::new
    );
}
