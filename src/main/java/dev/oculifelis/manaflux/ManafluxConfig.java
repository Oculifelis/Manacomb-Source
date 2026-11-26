package dev.oculifelis.manaflux;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server config (per world, serverconfig/manaflux-server.toml). Per-machine rates are not here: they are a data
 * map, so a datapack can change them (docs/DATA.md).
 */
public final class ManafluxConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue MANA_ENABLED;
    private static final ModConfigSpec.BooleanValue ANY_FE_BLOCK;
    private static final ModConfigSpec.DoubleValue DEFAULT_FE_PER_MANA;
    private static final ModConfigSpec.BooleanValue RF_INPUT_DISABLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        MANA_ENABLED = b.comment("Blocks accept Botania mana and store it as FE.")
                .define("manaEnabled", true);
        ANY_FE_BLOCK = b.comment(
                        "When true, every block that accepts FE also accepts mana, at defaultFePerMana unless the",
                        "manaflux:mana_input data map gives it its own rate. Block entity types in the",
                        "manaflux:no_mana tag are left out. When false, only the types listed in the data map take mana.")
                .define("anyFeBlock", true);
        DEFAULT_FE_PER_MANA = b.comment("FE stored per point of mana for blocks without their own rate in the data map.")
                .defineInRange("defaultFePerMana", 10.0, 0.01, 1_000_000.0);
        RF_INPUT_DISABLED = b.comment(
                        "Complicated Bees only. When true, its machines (centrifuge, gyrofuge, mellarium energy cells)",
                        "refuse FE from every other source, its own generators included, and run only on Botania mana.",
                        "Off by default: mana is then an extra way to power them.",
                        "With Rotaflux installed this key does nothing: use Rotaflux's rfInputDisabled instead.")
                .define("rfInputDisabled", false);
        SPEC = b.build();
    }

    private ManafluxConfig() {}

    // The server config exists only on a server (or the integrated one); before it loads, the defaults apply.
    public static boolean manaEnabled() {
        return !SPEC.isLoaded() || MANA_ENABLED.get();
    }

    public static boolean anyFeBlock() {
        return !SPEC.isLoaded() || ANY_FE_BLOCK.get();
    }

    public static float defaultFePerMana() {
        return SPEC.isLoaded() ? DEFAULT_FE_PER_MANA.get().floatValue() : 10.0f;
    }

    public static boolean rfInputDisabled() {
        return SPEC.isLoaded() && RF_INPUT_DISABLED.get();
    }

    /** Flips the switch for the running server (GameTests use it); saved to the config file. */
    public static void setRfInputDisabled(boolean value) {
        RF_INPUT_DISABLED.set(value);
    }

    /** GameTests use it; saved to the config file. */
    public static void setAnyFeBlock(boolean value) {
        ANY_FE_BLOCK.set(value);
    }
}
