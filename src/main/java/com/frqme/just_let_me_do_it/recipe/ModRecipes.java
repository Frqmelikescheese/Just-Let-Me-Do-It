package com.frqme.just_let_me_do_it.recipe;
import com.frqme.just_let_me_do_it.Just_let_me_do_it;
import com.frqme.just_let_me_do_it.item.ModItems;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
public class ModRecipes {
    public static void register() {
        Just_let_me_do_it.LOGGER.info("Registering mod recipes");
    }
}
