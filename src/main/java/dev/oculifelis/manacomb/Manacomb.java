package dev.oculifelis.manacomb;

import dev.oculifelis.manacomb.compat.botania.BotaniaCompat;
import dev.oculifelis.manacomb.data.ManacombDataMaps;
import dev.oculifelis.manacomb.energy.CbEnergyGate;
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
 * Manacomb lets Complicated Bees' FE machines run on Botania mana: {@link BotaniaCompat} makes them mana
 * receivers. {@link CbEnergyGate} is the optional "instead of RF" switch.
 */
@Mod(Manacomb.MOD_ID)
public final class Manacomb {
    public static final String MOD_ID = "manacomb";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    /**
     * Apiflux (Create rotation for the same machines) has the same "instead of RF" switch. Only one gate can sit
     * in front of a machine, so with Apiflux installed its gate and its switch are the one in use and Manacomb's
     * stays out of the way. Manacomb's mana gets past either gate (see EnergyAccess).
     */
    public static final boolean APIFLUX_GATE = ModList.get().isLoaded("apiflux");

    public Manacomb(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ManacombConfig.SPEC);
        modBus.addListener(ManacombDataMaps::register);
        if (APIFLUX_GATE) {
            LOG.info("Manacomb: Apiflux is installed, so its rfInputDisabled switch is the one in use");
        } else {
            // HIGHEST so the gate answers before Complicated Bees' own (NORMAL) provider would.
            modBus.addListener(EventPriority.HIGHEST, CbEnergyGate::registerCapabilities);
        }
        BotaniaCompat.init(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
