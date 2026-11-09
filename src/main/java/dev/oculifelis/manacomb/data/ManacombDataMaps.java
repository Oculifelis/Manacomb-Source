package dev.oculifelis.manacomb.data;

import dev.oculifelis.manacomb.Manacomb;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.jetbrains.annotations.Nullable;

/** The data map that holds every rate. Defaults ship in data/manacomb/data_maps/. */
public final class ManacombDataMaps {
    /** Which block entity types take mana, and at what FE per mana. Server side only. */
    public static final DataMapType<BlockEntityType<?>, ManaInput> MANA_INPUT = DataMapType
            .builder(Manacomb.id("mana_input"), Registries.BLOCK_ENTITY_TYPE, ManaInput.CODEC)
            .build();

    private ManacombDataMaps() {}

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(MANA_INPUT);
    }

    @Nullable
    public static ManaInput manaInput(BlockEntityType<?> type) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.wrapAsHolder(type).getData(MANA_INPUT);
    }
}
