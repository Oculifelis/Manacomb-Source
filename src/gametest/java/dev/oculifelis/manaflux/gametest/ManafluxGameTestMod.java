package dev.oculifelis.manaflux.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

/**
 * Dev-only test mod: GameTests, found by NeoForge from @GameTestHolder, plus a plain FE battery block that stands
 * for "any other mod's machine". Never shipped.
 */
@Mod(ManafluxGameTestMod.ID)
public final class ManafluxGameTestMod {
    public static final String ID = "manaflux_gametest";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    public static final DeferredHolder<Block, Block> BATTERY = BLOCKS.register("test_battery",
            () -> new BatteryBlock(BlockBehaviour.Properties.of()));
    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBlockEntity>> BATTERY_BE =
            BLOCK_ENTITIES.register("test_battery", () -> BlockEntityType.Builder.of(BatteryBlockEntity::new, BATTERY.get()).build(null));

    public ManafluxGameTestMod(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(RegisterCapabilitiesEvent.class, e ->
                e.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BATTERY_BE.get(), (be, side) -> be.energy));
    }

    public static final class BatteryBlock extends Block implements EntityBlock {
        BatteryBlock(Properties p) {
            super(p);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new BatteryBlockEntity(pos, state);
        }
    }

    public static final class BatteryBlockEntity extends BlockEntity {
        public final EnergyStorage energy = new EnergyStorage(100_000);

        public BatteryBlockEntity(BlockPos pos, BlockState state) {
            super(BATTERY_BE.get(), pos, state);
        }
    }
}
