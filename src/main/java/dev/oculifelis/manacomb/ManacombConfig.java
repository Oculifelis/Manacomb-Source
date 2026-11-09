package dev.oculifelis.manacomb;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server config (per world, serverconfig/manacomb-server.toml). Rates are not here: they are a data map, so a
 * datapack can change them (docs/DATA.md).
 */
public final class ManacombConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue RF_INPUT_DISABLED;
    private static final ModConfigSpec.BooleanValue MANA_ENABLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        RF_INPUT_DISABLED = b.comment(
                        "When true, Complicated Bees machines (centrifuge, gyrofuge, mellarium energy cells) refuse FE",
                        "from every other source, Complicated Bees' own generators included, and run only on Botania",
                        "mana. Off by default: mana is then an extra way to power them.",
                        "With Apiflux installed this key does nothing: use Apiflux's rfInputDisabled instead.")
                .define("rfInputDisabled", false);
        MANA_ENABLED = b.comment("Machines listed in the manacomb:mana_input data map accept mana.")
                .define("manaEnabled", true);
        SPEC = b.build();
    }

    private ManacombConfig() {}

    // The server config exists only on a server (or the integrated one); before it loads, the defaults apply.
    public static boolean rfInputDisabled() {
        return SPEC.isLoaded() && RF_INPUT_DISABLED.get();
    }

    /** Flips the switch for the running server (GameTests use it); saved to the config file. */
    public static void setRfInputDisabled(boolean value) {
        RF_INPUT_DISABLED.set(value);
    }

    public static boolean manaEnabled() {
        return !SPEC.isLoaded() || MANA_ENABLED.get();
    }
}
