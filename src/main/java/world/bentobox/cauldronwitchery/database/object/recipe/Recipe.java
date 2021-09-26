//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.cauldronwitchery.database.object.adapters.RecipeAdapter;


/**
 * Defines a recipe for a magic stick
 */
@JsonAdapter(RecipeAdapter.class)
public abstract class Recipe
{
    /**
     * Instantiates a new Recipe.
     */
    public Recipe()
    {
        // Empty constructor.
    }


// ---------------------------------------------------------------------
// Section: Getters and setters
// ---------------------------------------------------------------------


    /**
     * Gets main ingredient.
     *
     * @return the main ingredient
     */
    public ItemStack getMainIngredient()
    {
        return mainIngredient;
    }


    /**
     * Sets main ingredient.
     *
     * @param mainIngredient the main ingredient
     */
    public void setMainIngredient(ItemStack mainIngredient)
    {
        this.mainIngredient = mainIngredient;
    }


    /**
     * Gets extra ingredients.
     *
     * @return the extra ingredients
     */
    public List<ItemStack> getExtraIngredients()
    {
        return extraIngredients;
    }


    /**
     * Sets extra ingredients.
     *
     * @param extraIngredients the extra ingredients
     */
    public void setExtraIngredients(List<ItemStack> extraIngredients)
    {
        this.extraIngredients = extraIngredients;
    }


    /**
     * Gets experience.
     *
     * @return the experience
     */
    public long getExperience()
    {
        return experience;
    }


    /**
     * Sets experience.
     *
     * @param experience the experience
     */
    public void setExperience(long experience)
    {
        this.experience = experience;
    }


    /**
     * Gets cauldron type.
     *
     * @return the cauldron type
     */
    public Material getCauldronType()
    {
        return cauldronType;
    }


    /**
     * Sets cauldron type.
     *
     * @param cauldronType the cauldron type
     */
    public void setCauldronType(Material cauldronType)
    {
        this.cauldronType = cauldronType;
    }


    /**
     * Gets cauldron level.
     *
     * @return the cauldron level
     */
    public int getCauldronLevel()
    {
        return cauldronLevel;
    }


    /**
     * Sets cauldron level.
     *
     * @param cauldronLevel the cauldron level
     */
    public void setCauldronLevel(int cauldronLevel)
    {
        this.cauldronLevel = cauldronLevel;
    }


    /**
     * Gets permissions.
     *
     * @return the permissions
     */
    public List<String> getPermissions()
    {
        return permissions;
    }


    /**
     * Sets permissions.
     *
     * @param permissions the permissions
     */
    public void setPermissions(List<String> permissions)
    {
        this.permissions = permissions;
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    @Override
    public abstract Recipe clone();


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * The main ingredient -> last item.
     */
    @Expose
    private ItemStack mainIngredient;

    /**
     * List of extra items for the recipe
     */
    @Expose
    private List<ItemStack> extraIngredients;

    /**
     * Experience level for recipe.
     */
    @Expose
    private long experience;

    /**
     * Allows to define 4 different cauldron types.
     * @see Material#CAULDRON
     * @see Material#WATER_CAULDRON
     * @see Material#LAVA_CAULDRON
     * @see Material#POWDER_SNOW_CAULDRON
     */
    @Expose
    private Material cauldronType;

    /**
     * Cauldron fill level.
     * Works for water and snow. Lava and empty one is always 1
     */
    @Expose
    private int cauldronLevel;

    /**
     * Stores the list of permissions that are required for this recipe.
     */
    @Expose
    private List<String> permissions = new ArrayList<>();
}
