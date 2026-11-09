package dev.oculifelis.manacomb.energy;

import dev.oculifelis.manacomb.ManacombConfig;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * What other mods see of a Complicated Bees machine's energy: the machine's own storage, except that with
 * {@code rfInputDisabled} on it accepts nothing. Manacomb's mana gets past it through
 * {@link EnergyAccess#trusted}.
 */
public record GatedEnergyStorage(IEnergyStorage inner) implements IEnergyStorage {
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return ManacombConfig.rfInputDisabled() ? 0 : inner.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public boolean canReceive() {
        return !ManacombConfig.rfInputDisabled() && inner.canReceive();
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return inner.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return inner.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return inner.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return inner.canExtract();
    }
}
