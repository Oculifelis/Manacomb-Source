package dev.oculifelis.manacomb.energy;

import com.accbdd.complicated_bees.block.entity.CentrifugeBlockEntity;
import com.accbdd.complicated_bees.block.entity.gyrofuge.GyrofugeControllerBlockEntity;
import com.accbdd.complicated_bees.block.entity.gyrofuge.GyrofugeEnergyCellBlockEntity;
import com.accbdd.complicated_bees.block.entity.mellarium.MellariumEnergyCellBlockEntity;
import com.accbdd.complicated_bees.registry.BlockEntitiesRegistration;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Puts a {@link GatedEnergyStorage} in front of every Complicated Bees block entity that takes FE.
 *
 * Registered at HIGHEST priority, so NeoForge asks this provider before Complicated Bees' own and keeps its
 * answer. It hands back the very storage Complicated Bees would have, wrapped; with the config off the wrapper
 * changes nothing. These are the four FE inputs Complicated Bees 3.6 for 1.21.1 registers. The generators only
 * give FE out and are left alone.
 *
 * <p>Not registered when Apiflux is installed: Apiflux puts its own gate in front of the same four machines,
 * and NeoForge only ever uses the first provider that answers. {@link #ungated} lets mana in past either gate.
 */
public final class CbEnergyGate {
    private CbEnergyGate() {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntitiesRegistration.CENTRIFUGE_ENTITY.get(),
                (be, side) -> gate(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntitiesRegistration.GYROFUGE_CONTROLLER_BLOCK_ENTITY.get(),
                (be, side) -> gate(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntitiesRegistration.GYROFUGE_ENERGY_CELL_BLOCK_ENTITY.get(),
                (be, side) -> gate(be.getEnergyHandler()));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, BlockEntitiesRegistration.MELLARIUM_ENERGY_CELL_BLOCK_ENTITY.get(),
                (be, side) -> gate(be.getEnergyHandler()));
    }

    /**
     * The machine's own FE storage, straight from Complicated Bees, for the four gated machines; null for any
     * other block entity. Whoever's gate is in front, this is what sits behind it.
     */
    @Nullable
    public static IEnergyStorage ungated(@Nullable BlockEntity be) {
        if (be instanceof CentrifugeBlockEntity c) return c.getEnergyHandler();
        if (be instanceof GyrofugeControllerBlockEntity c) return c.getEnergyHandler();
        if (be instanceof GyrofugeEnergyCellBlockEntity c) return c.getEnergyHandler();
        if (be instanceof MellariumEnergyCellBlockEntity c) return c.getEnergyHandler();
        return null;
    }

    @Nullable
    private static IEnergyStorage gate(@Nullable IEnergyStorage storage) {
        return storage == null ? null : new GatedEnergyStorage(storage);
    }
}
