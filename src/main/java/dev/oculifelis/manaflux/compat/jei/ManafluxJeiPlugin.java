package dev.oculifelis.manaflux.compat.jei;

import dev.oculifelis.manaflux.Manaflux;
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

/**
 * Info page on Complicated Bees' FE machines, when it is installed. Any other FE block takes mana too, but which
 * ones is only known on the server, so JEI shows the page on the bee machines as the example.
 */
@JeiPlugin
public final class ManafluxJeiPlugin implements IModPlugin {
    private static final List<String> MANA_MACHINES =
            List.of("centrifuge", "gyrofuge_controller", "gyrofuge_energy_cell", "mellarium_energy_cell");

    @Override
    public ResourceLocation getPluginUid() {
        return Manaflux.id("info");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ItemStack> machines = new ArrayList<>();
        for (String id : MANA_MACHINES) {
            BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("complicated_bees", id))
                    .ifPresent(item -> machines.add(new ItemStack(item)));
        }
        if (machines.isEmpty()) return;
        registration.addIngredientInfo(machines, VanillaTypes.ITEM_STACK, Component.translatable("manaflux.jei.mana"));
    }
}
