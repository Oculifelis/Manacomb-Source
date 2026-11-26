package dev.oculifelis.manaflux.compat.botania;

import dev.oculifelis.manaflux.Manaflux;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;
import vazkii.botania.api.neoforge.BotaniaNeoForgeCapabilities;

/**
 * Mana power. Every block entity type gets a mana receiver provider that answers for blocks with an FE storage
 * that accepts energy (or only for types in the {@code manaflux:mana_input} data map, with anyFeBlock off).
 * Types in the {@code manaflux:no_mana} tag never answer. Registered at
 * LOWEST, after Botania's own providers, so a real Botania block always keeps its own receiver.
 */
public final class BotaniaCompat {
    private BotaniaCompat() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(EventPriority.LOWEST, BotaniaCompat::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(SparkPuller::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(SparkPuller::onLevelTick);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        BlockCapability<ManaReceiver, Direction> receiver = BotaniaNeoForgeCapabilities.getBlockApiLookupById(ManaReceiver.LOOKUP);
        BlockCapability<ManaSparkAttachable, Void> spark = BotaniaNeoForgeCapabilities.getBlockApiLookupById(ManaSparkAttachable.LOOKUP);
        if (receiver == null || spark == null) {
            Manaflux.LOG.error("Manaflux: Botania's mana capabilities are missing; mana power is off");
            return;
        }
        int count = 0;
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            register(event, receiver, spark, type);
            count++;
        }
        Manaflux.LOG.debug("Manaflux: mana providers on {} block entity types", count);
    }

    private static <T extends BlockEntity> void register(RegisterCapabilitiesEvent event,
                                                         BlockCapability<ManaReceiver, Direction> receiver,
                                                         BlockCapability<ManaSparkAttachable, Void> spark,
                                                         BlockEntityType<T> type) {
        event.registerBlockEntity(receiver, type, (be, side) -> MachineManaReceiver.of(be, side));
        event.registerBlockEntity(spark, type, (be, unused) -> MachineManaReceiver.of(be, null));
    }
}
