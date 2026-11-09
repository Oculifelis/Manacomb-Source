package dev.oculifelis.manacomb.compat.jei;

import dev.oculifelis.manacomb.Manacomb;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Info page: how to power the machines with mana. */
@JeiPlugin
public final class ManacombJeiPlugin implements IModPlugin {
    private static final List<String> MANA_MACHINES =
            List.of("centrifuge", "gyrofuge_controller", "gyrofuge_energy_cell", "mellarium_energy_cell");

    @Override
    public ResourceLocation getPluginUid() {
        return Manacomb.id("info");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // The default mana_input entries. The data map itself is server-only, so JEI lists the shipped set.
        List<ItemStack> machines = new ArrayList<>();
        for (String id : MANA_MACHINES) {
            BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("complicated_bees", id))
                    .ifPresent(item -> machines.add(new ItemStack(item)));
        }
        registration.addIngredientInfo(machines, VanillaTypes.ITEM_STACK, Component.translatable("manacomb.jei.mana"));
    }
}
