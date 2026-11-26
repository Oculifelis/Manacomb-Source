package dev.oculifelis.manaflux.compat.botania;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.ManaSparkHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Botania's sparks pull: a block that wants mana from pools asks the pool sparks around its own spark, every
 * tick, to send some (that is what the Terrestrial Agglomeration Plate does). Other mods' machines do not tick
 * for us, so this does it for every FE block that has a mana spark on top.
 *
 * <p>Machines are found when their spark enters the level: when it is placed, and when its chunk loads again.
 */
public final class SparkPuller {
    private static final Map<ResourceKey<Level>, Set<BlockPos>> TRACKED = new HashMap<>();

    private SparkPuller() {}

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ManaSpark spark)) return;
        BlockPos pos = spark.getAttachPos();
        BlockEntity be = event.getLevel().getBlockEntity(pos);
        if (be != null && MachineManaReceiver.of(be, null) != null) {
            TRACKED.computeIfAbsent(event.getLevel().dimension(), k -> new HashSet<>()).add(pos.immutable());
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Set<BlockPos> positions = TRACKED.get(level.dimension());
        if (positions == null || positions.isEmpty()) return;
        for (Iterator<BlockPos> it = positions.iterator(); it.hasNext(); ) {
            BlockPos pos = it.next();
            if (!level.isLoaded(pos)) {
                // Unloaded: forget it; the spark joining again on load brings it back.
                it.remove();
                continue;
            }
            BlockEntity be = level.getBlockEntity(pos);
            MachineManaReceiver receiver = be == null ? null : MachineManaReceiver.of(be, null);
            ManaSpark spark = ManaSparkHelper.getAttachedSpark(level, pos);
            if (receiver == null || spark == null) {
                it.remove();
                continue;
            }
            if (!receiver.isFull()) {
                ManaSparkHelper.registerTransferFromSparksAround(spark, level, pos);
            }
        }
    }
}
