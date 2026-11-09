package dev.oculifelis.manacomb.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/** How Manacomb's mana reaches a machine's FE storage, past the RF gate (its own or Apiflux's). */
public final class EnergyAccess {
    private EnergyAccess() {}

    @Nullable
    public static IEnergyStorage trusted(Level level, BlockPos pos, @Nullable BlockState state,
                                         @Nullable BlockEntity be, @Nullable Direction side) {
        // A gated Complicated Bees machine: take its own storage directly, so neither mod's gate is in the way.
        IEnergyStorage direct = CbEnergyGate.ungated(be != null ? be : level.getBlockEntity(pos));
        if (direct != null) return direct;
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, state, be, side);
        if (storage == null && side != null) {
            storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, state, be, null);
        }
        return unwrap(storage);
    }

    @Nullable
    public static IEnergyStorage unwrap(@Nullable IEnergyStorage storage) {
        return storage instanceof GatedEnergyStorage gated ? gated.inner() : storage;
    }
}
