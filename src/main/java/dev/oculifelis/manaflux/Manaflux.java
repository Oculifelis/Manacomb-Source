package dev.oculifelis.manaflux;

import dev.oculifelis.manaflux.compat.botania.BotaniaCompat;
import dev.oculifelis.manaflux.compat.cb.CbEnergyGate;
import dev.oculifelis.manaflux.data.ManafluxDataMaps;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manaflux lets any block that takes FE run on Botania mana: {@link BotaniaCompat} makes such blocks mana
 * receivers. With Complicated Bees installed, {@link CbEnergyGate} adds the optional "instead of RF" switch for
 * its machines.
 */
@Mod(Manaflux.MOD_ID)
public final class Manaflux {
    public static final String MOD_ID = "manaflux";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    /** Complicated Bees is optional. Nothing under compat/cb is loaded without it. */
    public static final boolean CB_LOADED = ModList.get().isLoaded("complicated_bees");

    /**
     * Rotaflux (Create rotation into FE) has the same "instead of RF" switch. Only one gate can
     * sit in front of a machine, so with Rotaflux installed its gate and its switch are the one in use and
     * Manaflux's stays out of the way. Manaflux's mana gets past either gate (see EnergyAccess).
     */
    public static final boolean ROTAFLUX_GATE = ModList.get().isLoaded("rotaflux");

    public Manaflux(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ManafluxConfig.SPEC);
        modBus.addListener(ManafluxDataMaps::register);
        if (CB_LOADED) {
            if (ROTAFLUX_GATE) {
                LOG.info("Manaflux: Rotaflux is installed, so its rfInputDisabled switch is the one in use");
            } else {
                // HIGHEST so the gate answers before Complicated Bees' own (NORMAL) provider would.
                modBus.addListener(EventPriority.HIGHEST, CbEnergyGate::registerCapabilities);
            }
        }
        BotaniaCompat.init(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
