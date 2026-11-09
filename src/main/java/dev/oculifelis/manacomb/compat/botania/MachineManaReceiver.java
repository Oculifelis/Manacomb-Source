package dev.oculifelis.manacomb.compat.botania;

import dev.oculifelis.manacomb.ManacombConfig;
import dev.oculifelis.manacomb.data.ManacombDataMaps;
import dev.oculifelis.manacomb.data.ManaInput;
import dev.oculifelis.manacomb.energy.EnergyAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;

/**
 * A machine's FE storage seen as a Botania mana receiver. Mana that arrives is stored at once as FE, at the
 * machine's {@code fe_per_mana}; nothing is buffered as mana. Bursts from spreaders land here, and so do spark
 * transfers from pools (see {@link SparkPuller}).
 *
 * <p>Botania hands a receiver the whole burst. If the machine's storage cannot take all of it, the rest is
 * lost, as with any full Botania block; {@link #isFull} keeps spreaders from firing at a full machine.
 */
public final class MachineManaReceiver implements ManaReceiver, ManaSparkAttachable {
    private final BlockEntity be;
    @Nullable
    private final Direction side;
    private final float fePerMana;

    private MachineManaReceiver(BlockEntity be, @Nullable Direction side, float fePerMana) {
        this.be = be;
        this.side = side;
        this.fePerMana = fePerMana;
    }

    /** The receiver for this block entity, or null when its type is not in the mana_input data map. */
    @Nullable
    public static MachineManaReceiver of(BlockEntity be, @Nullable Direction side) {
        if (be.getLevel() == null || !ManacombConfig.manaEnabled()) return null;
        ManaInput input = ManacombDataMaps.manaInput(be.getType());
        if (input == null) return null;
        return new MachineManaReceiver(be, side, input.fePerMana());
    }

    @Nullable
    private IEnergyStorage energy() {
        Level level = be.getLevel();
        if (level == null || be.isRemoved()) return null;
        return EnergyAccess.trusted(level, be.getBlockPos(), be.getBlockState(), be, side);
    }

    @Override
    public Level getManaReceiverLevel() {
        return be.getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return be.getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        IEnergyStorage energy = energy();
        return energy == null ? 0 : (int) (energy.getEnergyStored() / fePerMana);
    }

    @Override
    public boolean isFull() {
        return spaceForMana() <= 0;
    }

    @Override
    public void receiveMana(int mana) {
        // Negative mana means "take mana out"; only pools are ever asked for that, and a machine has none to give.
        if (mana <= 0) return;
        IEnergyStorage energy = energy();
        if (energy == null) return;
        long fe = (long) Math.floor(mana * (double) fePerMana);
        energy.receiveEnergy((int) Math.min(Integer.MAX_VALUE, fe), false);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    // Sparks: a mana spark may sit on top of the machine and draw from pool sparks of the same colour.

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, spaceForMana());
    }

    @Override
    public boolean areIncomingTransfersDone() {
        return isFull();
    }

    private int spaceForMana() {
        IEnergyStorage energy = energy();
        if (energy == null) return 0;
        // Ask the storage itself: Complicated Bees' energy cells also cap how much they take per call.
        int room = energy.receiveEnergy(Integer.MAX_VALUE, true);
        return (int) (room / fePerMana);
    }
}
