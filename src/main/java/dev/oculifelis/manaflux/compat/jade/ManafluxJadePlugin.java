package dev.oculifelis.manaflux.compat.jade;

import dev.oculifelis.manaflux.Manaflux;
import dev.oculifelis.manaflux.ManafluxConfig;
import dev.oculifelis.manaflux.compat.botania.MachineManaReceiver;
import dev.oculifelis.manaflux.energy.GatedEnergyStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/**
 * One Jade line on a block that takes mana, with its rate. The rates are server data (the mana_input data
 * map and the config are not synced), so the server side of this provider looks them up and sends them along. The "outside FE
 * refused" line shows only for Manaflux's own gate; with Rotaflux installed, Rotaflux shows it.
 */
@WailaPlugin
public final class ManafluxJadePlugin implements IWailaPlugin, IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    private static final ResourceLocation UID = Manaflux.id("machine_power");
    private static final String MANA = "manaflux_fe_per_mana";
    private static final String GATED = "manaflux_rf_refused";

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(this, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(this, Block.class);
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be == null) return;
        Float rate = MachineManaReceiver.rateFor(be, null);
        if (rate != null) data.putFloat(MANA, rate);
        IEnergyStorage energy = accessor.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, be.getBlockPos(),
                be.getBlockState(), be, null);
        if (energy instanceof GatedEnergyStorage && ManafluxConfig.rfInputDisabled()) data.putBoolean(GATED, true);
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() != null;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(MANA)) {
            tooltip.add(Component.translatable("manaflux.jade.mana", formatRate(data.getFloat(MANA)))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (data.getBoolean(GATED)) {
            tooltip.add(Component.translatable("manaflux.jade.rf_refused").withStyle(ChatFormatting.GRAY));
        }
    }

    private static String formatRate(float rate) {
        return rate == Math.rint(rate) ? Integer.toString((int) rate) : String.format("%.2f", rate);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
