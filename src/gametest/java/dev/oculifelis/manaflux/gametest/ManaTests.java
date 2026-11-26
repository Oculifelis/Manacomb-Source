package dev.oculifelis.manaflux.gametest;

import com.accbdd.complicated_bees.block.entity.CentrifugeBlockEntity;
import dev.oculifelis.manaflux.ManafluxConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slf4j.LoggerFactory;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;
import vazkii.botania.api.mana.spark.ManaSparkHelper;
import vazkii.botania.api.neoforge.BotaniaNeoForgeCapabilities;

/**
 * Mana power: a centrifuge and a plain FE battery (any other mod's machine) are mana receivers, take bursts' mana
 * as FE, and draw from a pool by spark. A furnace (no FE) never is.
 */
@GameTestHolder("manaflux_gametest")
@PrefixGameTestTemplate(false)
public final class ManaTests {
    private static BlockCapability<ManaReceiver, Direction> receiverCap() {
        return BotaniaNeoForgeCapabilities.getBlockApiLookupById(ManaReceiver.LOOKUP);
    }

    @GameTest(template = "arena")
    public static void manaToFe(GameTestHelper helper) {
        helper.setBlock(GateTests.MACHINE, GateTests.cb("centrifuge"));
        helper.setBlock(GateTests.MACHINE.east(), Blocks.FURNACE);
        BlockPos abs = helper.absolutePos(GateTests.MACHINE);
        ManaReceiver receiver = helper.getLevel().getCapability(receiverCap(), abs, Direction.UP);
        helper.assertTrue(receiver != null, "centrifuge is not a mana receiver");
        helper.assertTrue(helper.getLevel().getCapability(receiverCap(), abs.east(), Direction.UP) == null,
                "a furnace (no FE storage) became a mana receiver");
        helper.assertTrue(receiver.canReceiveManaFromBursts() && !receiver.isFull(), "receiver refuses bursts");

        receiver.receiveMana(50);
        CentrifugeBlockEntity be = helper.getBlockEntity(GateTests.MACHINE);
        helper.assertTrue(be.getStoredPower() == 500, "50 mana gave " + be.getStoredPower() + " FE, expected 500");

        // Mana still gets in with outside FE switched off.
        ManafluxConfig.setRfInputDisabled(true);
        try {
            receiver.receiveMana(10);
        } finally {
            ManafluxConfig.setRfInputDisabled(false);
        }
        helper.assertTrue(be.getStoredPower() == 600, "mana blocked by the RF gate: " + be.getStoredPower());
        helper.succeed();
    }

    /** Any FE block takes mana at the default rate; with anyFeBlock off only data-map machines do. */
    @GameTest(template = "arena")
    public static void anyFeBlock(GameTestHelper helper) {
        BlockPos batteryPos = new BlockPos(1, 1, 1);
        helper.setBlock(batteryPos, ManafluxGameTestMod.BATTERY.get());
        helper.setBlock(GateTests.MACHINE, GateTests.cb("centrifuge"));
        BlockPos abs = helper.absolutePos(batteryPos);
        ManaReceiver receiver = helper.getLevel().getCapability(receiverCap(), abs, Direction.NORTH);
        helper.assertTrue(receiver != null, "an FE battery from another mod is not a mana receiver");
        receiver.receiveMana(7);
        ManafluxGameTestMod.BatteryBlockEntity be = helper.getBlockEntity(batteryPos);
        helper.assertTrue(be.energy.getEnergyStored() == 70, "7 mana gave " + be.energy.getEnergyStored() + " FE, expected 70");

        ManafluxConfig.setAnyFeBlock(false);
        try {
            helper.assertTrue(helper.getLevel().getCapability(receiverCap(), abs, Direction.NORTH) == null,
                    "anyFeBlock off: the battery still takes mana");
            helper.assertTrue(helper.getLevel().getCapability(receiverCap(), helper.absolutePos(GateTests.MACHINE), Direction.UP) != null,
                    "anyFeBlock off: the centrifuge (in the data map) lost mana");
        } finally {
            ManafluxConfig.setAnyFeBlock(true);
        }
        helper.succeed();
    }

    /** A creative pool with a spark, and a battery with a spark: mana flows by spark into a non-bee FE block. */
    @GameTest(template = "arena", timeoutTicks = 200)
    public static void sparkIntoBattery(GameTestHelper helper) {
        BlockPos pool = new BlockPos(1, 1, 3);
        BlockPos battery = new BlockPos(5, 1, 3);
        helper.setBlock(pool, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("botania", "creative_mana_pool")));
        helper.setBlock(battery, ManafluxGameTestMod.BATTERY.get());
        EntityType<?> sparkType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("botania", "mana_spark"));
        spark(helper, sparkType, pool);
        spark(helper, sparkType, battery);
        helper.succeedWhen(() -> {
            ManafluxGameTestMod.BatteryBlockEntity be = helper.getBlockEntity(battery);
            helper.assertTrue(be.energy.getEnergyStored() > 0, "no FE arrived in the battery by spark yet");
        });
    }

    /** A creative pool with a spark, and a centrifuge with a spark, four blocks apart: mana flows by spark. */
    @GameTest(template = "arena", timeoutTicks = 200)
    public static void sparkFromPool(GameTestHelper helper) {
        BlockPos pool = new BlockPos(1, 1, 3);
        BlockPos machine = new BlockPos(5, 1, 3);
        helper.setBlock(pool, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("botania", "creative_mana_pool")));
        helper.setBlock(machine, GateTests.cb("centrifuge"));
        EntityType<?> sparkType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("botania", "mana_spark"));
        spark(helper, sparkType, pool);
        spark(helper, sparkType, machine);
        int[] calls = {0};
        helper.succeedWhen(() -> {
            CentrifugeBlockEntity be = helper.getBlockEntity(machine);
            if (be.getStoredPower() <= 0 && calls[0]++ % 40 == 0) debugSparks(helper, pool, machine);
            helper.assertTrue(be.getStoredPower() > 0, "no FE arrived by spark yet");
        });
    }

    private static void debugSparks(GameTestHelper helper, BlockPos pool, BlockPos machine) {
        var level = helper.getLevel();
        ManaSpark poolSpark = ManaSparkHelper.getAttachedSpark(level, helper.absolutePos(pool));
        ManaSpark ourSpark = ManaSparkHelper.getAttachedSpark(level, helper.absolutePos(machine));
        ManaSparkAttachable ours = ManaSparkAttachable.LOOKUP.find(level, helper.absolutePos(machine));
        LoggerFactory.getLogger("manaflux_gametest").info("SPARKDBG poolSpark={} out={} poolRecv={} ourSpark={} ourTile={} space={} incomingDone={} sparksNear={}",
                poolSpark != null, poolSpark == null ? -1 : poolSpark.getOutgoingTransfers().size(),
                poolSpark == null ? null : poolSpark.getAttachedManaReceiver(),
                ourSpark != null, ours != null, ours == null ? -1 : ours.getAvailableSpaceForMana(),
                ourSpark == null ? null : ourSpark.areIncomingTransfersDone(),
                ManaSparkHelper.getSparksAround(level, helper.absolutePos(machine).getX() + 0.5, helper.absolutePos(machine).getY() + 0.5,
                        helper.absolutePos(machine).getZ() + 0.5, net.minecraft.world.item.DyeColor.WHITE).size());
    }

    private static void spark(GameTestHelper helper, EntityType<?> type, BlockPos on) {
        BlockPos abs = helper.absolutePos(on);
        Entity spark = type.create(helper.getLevel());
        if (spark == null) throw new IllegalStateException("botania:spark entity type missing");
        // Where Botania's spark item puts it: centred, just above the block.
        spark.setPos(abs.getX() + 0.5, abs.getY() + 1.035, abs.getZ() + 0.5);
        helper.getLevel().addFreshEntity(spark);
    }
}
