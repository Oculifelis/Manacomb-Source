package dev.oculifelis.manaflux.energy;

import dev.oculifelis.manaflux.Manaflux;
import dev.oculifelis.manaflux.compat.cb.CbEnergyGate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/** How Manaflux's mana reaches a block's FE storage, past the RF gate (its own or Rotaflux's). */
public final class EnergyAccess {
    private EnergyAccess() {}

    /**
     * The FE storage mana should go into, or null when the block has none that accepts FE. Asks the given side,
     * then no side, then every side (some machines only expose energy on certain faces).
     */
    @Nullable
    public static IEnergyStorage trusted(Level level, BlockPos pos, @Nullable BlockState state,
                                         @Nullable BlockEntity be, @Nullable Direction side) {
        if (Manaflux.CB_LOADED) {
            // A gated Complicated Bees machine: take its own storage directly, so neither mod's gate is in the way.
            IEnergyStorage direct = CbEnergyGate.ungated(be != null ? be : level.getBlockEntity(pos));
            if (direct != null) return direct;
        }
        IEnergyStorage storage = accepting(level, pos, state, be, side);
        if (storage == null && side != null) storage = accepting(level, pos, state, be, null);
        if (storage == null) {
            for (Direction d : Direction.values()) {
                if (d == side) continue;
                storage = accepting(level, pos, state, be, d);
                if (storage != null) break;
            }
        }
        return storage;
    }

    @Nullable
    private static IEnergyStorage accepting(Level level, BlockPos pos, @Nullable BlockState state,
                                            @Nullable BlockEntity be, @Nullable Direction side) {
        IEnergyStorage storage = unwrap(level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, state, be, side));
        return storage != null && storage.canReceive() ? storage : null;
    }

    @Nullable
    public static IEnergyStorage unwrap(@Nullable IEnergyStorage storage) {
        return storage instanceof GatedEnergyStorage gated ? gated.inner() : storage;
    }
}
