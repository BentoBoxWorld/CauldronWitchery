//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;
import org.bukkit.inventory.ItemStack;


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
        this.populateClone(recipe);
        return recipe;
    }


    /**
     * Stores the ItemStack.
     */
    @Expose
    private ItemStack itemStack;
}
