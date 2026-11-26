package dev.oculifelis.manaflux.data;

import dev.oculifelis.manaflux.Manaflux;
import dev.oculifelis.manaflux.ManafluxConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.jetbrains.annotations.Nullable;

/** The data map with per-machine rates, and the tag that keeps block entity types away from mana. */
public final class ManafluxDataMaps {
    /** Block entity types with their own FE per mana. Server side only. */
    public static final DataMapType<BlockEntityType<?>, ManaInput> MANA_INPUT = DataMapType
            .builder(Manaflux.id("mana_input"), Registries.BLOCK_ENTITY_TYPE, ManaInput.CODEC)
            .build();

    /** Block entity types that never take mana, even with anyFeBlock on. */
    public static final TagKey<BlockEntityType<?>> NO_MANA = TagKey.create(Registries.BLOCK_ENTITY_TYPE, Manaflux.id("no_mana"));

    private ManafluxDataMaps() {}

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(MANA_INPUT);
    }

    /**
     * FE per mana for this type, or null when it does not take mana by type: the tag wins, then the data map,
     * then the default rate when anyFeBlock is on. Whether the block really takes FE is checked by the caller.
     */
    @Nullable
    public static Float fePerMana(BlockEntityType<?> type) {
        Holder<BlockEntityType<?>> holder = BuiltInRegistries.BLOCK_ENTITY_TYPE.wrapAsHolder(type);
        if (holder.is(NO_MANA)) return null;
        ManaInput input = holder.getData(MANA_INPUT);
        if (input != null) return input.fePerMana();
        return ManafluxConfig.anyFeBlock() ? ManafluxConfig.defaultFePerMana() : null;
    }
}
