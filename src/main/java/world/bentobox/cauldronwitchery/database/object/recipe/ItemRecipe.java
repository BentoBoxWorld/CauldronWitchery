//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * The type ItemStack recipe.
 */
public class ItemRecipe extends Recipe
{
    /**
     * Instantiates a ItemStack recipe.
     */
    public ItemRecipe()
    {
        // Empty Constructor.
    }


    public ItemRecipe(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }


    /**
     * Gets ItemStack.
     *
     * @return the ItemStack
     */
    public ItemStack getItemStack()
    {
        return itemStack;
    }


    /**
     * Sets ItemStack.
     *
     * @param itemStack the ItemStack
     */
    public void setItemStack(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }


    @Override
    public Recipe clone()
    {
        ItemRecipe recipe = new ItemRecipe();
        recipe.setItemStack(this.itemStack.clone());

        recipe.setCauldronType(this.getCauldronType());
        recipe.setCauldronLevel(this.getCauldronLevel());

        recipe.setExperience(this.getExperience());

        recipe.setPermissions(new ArrayList<>());
        recipe.getPermissions().addAll(this.getPermissions());

        recipe.setMainIngredient(this.getMainIngredient().clone());
        recipe.setExtraIngredients(this.getExtraIngredients().stream().
            map(ItemStack::clone).
            collect(Collectors.toList()));

        return recipe;
    }


    /**
     * Stores the ItemStack.
     */
    @Expose
    private ItemStack itemStack;
}
