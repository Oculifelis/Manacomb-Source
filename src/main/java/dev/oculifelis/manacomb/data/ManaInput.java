package dev.oculifelis.manacomb.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

/**
 * One entry of the {@code manacomb:mana_input} data map (registry: block entity types). A block entity type
 * listed there, if it has an FE storage, becomes a Botania mana receiver: each point of mana it receives is
 * stored as {@code fe_per_mana} FE.
 */
public record ManaInput(float fePerMana) {
    public static final Codec<ManaInput> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.POSITIVE_FLOAT.fieldOf("fe_per_mana").forGetter(ManaInput::fePerMana)
    ).apply(i, ManaInput::new));
}
