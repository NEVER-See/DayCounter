package dev.daycounter.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DaySyncPayload(long day, boolean announce) implements CustomPacketPayload {
    public static final Type<DaySyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("daycounter", "day_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DaySyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, DaySyncPayload::day,
            ByteBufCodecs.BOOL, DaySyncPayload::announce,
            DaySyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
