package dev.oculifelis.manacomb.compat.jade;

import dev.oculifelis.manacomb.Manacomb;
import dev.oculifelis.manacomb.ManacombConfig;
import dev.oculifelis.manacomb.data.ManacombDataMaps;
import dev.oculifelis.manacomb.data.ManaInput;
import dev.oculifelis.manacomb.energy.GatedEnergyStorage;
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
 * One Jade line on a machine that takes mana, with its rate. The rates are server data (the mana_input data
 * map is not synced), so the server side of this provider looks them up and sends them along. The "outside FE
 * refused" line shows only for Manacomb's own gate; with Apiflux installed, Apiflux shows it.
 */
@WailaPlugin
public final class ManacombJadePlugin implements IWailaPlugin, IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    private static final ResourceLocation UID = Manacomb.id("machine_power");
    private static final String MANA = "manacomb_fe_per_mana";
    private static final String GATED = "manacomb_rf_refused";

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
        if (ManacombConfig.manaEnabled()) {
            ManaInput input = ManacombDataMaps.manaInput(be.getType());
            if (input != null) data.putFloat(MANA, input.fePerMana());
        }
        IEnergyStorage energy = accessor.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, be.getBlockPos(),
                be.getBlockState(), be, null);
        if (energy instanceof GatedEnergyStorage && ManacombConfig.rfInputDisabled()) data.putBoolean(GATED, true);
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() != null;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(MANA)) {
            tooltip.add(Component.translatable("manacomb.jade.mana", formatRate(data.getFloat(MANA)))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (data.getBoolean(GATED)) {
            tooltip.add(Component.translatable("manacomb.jade.rf_refused").withStyle(ChatFormatting.GRAY));
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
