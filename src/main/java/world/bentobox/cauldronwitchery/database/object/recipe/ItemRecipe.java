//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.utils.Utils;


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
        return this.itemStack != null ? this.itemStack.clone() : new ItemStack(Material.BARRIER);
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
        recipe.setMainIngredient(this.itemStack == null ? null : this.itemStack.clone());
        this.populateClone(recipe);
        return recipe;
    }


    /**
     * Returns name of the item stack.
     * @param user User who wants to get recipe name.
     * @return Name of item stack.
     */
    @Override
    public String getRecipeName(User user)
    {
        if (this.getRecipeDisplayName() == null ||
            this.getRecipeDisplayName().isBlank() ||
            user.getTranslation(this.getRecipeDisplayName()).isBlank())
        {
            return Utils.prettifyObject(this.itemStack, user);
        }
        else
        {
            return user.getTranslation(this.getRecipeDisplayName());
        }
    }


    /**
     * Returns item stack as icon.
     * @return Icon.
     */
    @Override
    public ItemStack getIcon()
    {
        return this.itemStack == null ? new ItemStack(Material.BARRIER) : this.itemStack.clone();
    }


    /**
     * Returns true if item stack is not null.
     * @return If item stack is valid.
     */
    @Override
    public boolean isValid()
    {
        return super.isValid() && this.itemStack != null;
    }


    /**
     * Stores the ItemStack.
     */
    @Expose
    private ItemStack itemStack;
}
