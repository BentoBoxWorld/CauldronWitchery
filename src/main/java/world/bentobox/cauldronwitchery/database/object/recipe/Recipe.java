//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.user.User;
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
        return this.mainIngredient != null ? mainIngredient.clone() : new ItemStack(Material.BARRIER);
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
        return extraIngredients.stream().map(ItemStack::clone).collect(Collectors.toList());
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
    public Set<String> getPermissions()
    {
        return permissions;
    }


    /**
     * Sets permissions.
     *
     * @param permissions the permissions
     */
    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }


    /**
     * Gets temperature.
     *
     * @return the temperature
     */
    public Temperature getTemperature()
    {
        return temperature;
    }


    /**
     * Sets temperature.
     *
     * @param temperature the temperature
     */
    public void setTemperature(Temperature temperature)
    {
        this.temperature = temperature;
    }


    /**
     * Gets reward points.
     *
     * @return the reward points
     */
    public int getRewardPoints()
    {
        return rewardPoints;
    }


    /**
     * Sets reward points.
     *
     * @param rewardPoints the reward points
     */
    public void setRewardPoints(int rewardPoints)
    {
        this.rewardPoints = rewardPoints;
    }


    /**
     * Gets complexity.
     *
     * @return the complexity
     */
    public float getComplexity()
    {
        return complexity;
    }


    /**
     * Sets complexity.
     *
     * @param complexity the complexity
     */
    public void setComplexity(float complexity)
    {
        this.complexity = complexity;
    }


    /**
     * Gets order.
     *
     * @return the order
     */
    public int getOrder()
    {
        return order;
    }


    /**
     * Sets order.
     *
     * @param order the order
     */
    public void setOrder(int order)
    {
        this.order = order;
    }


    /**
     * Gets recipe display name.
     *
     * @return the recipe display name
     */
    public String getRecipeDisplayName()
    {
        return recipeDisplayName;
    }


    /**
     * Sets recipe display name.
     *
     * @param recipeDisplayName the recipe display name
     */
    public void setRecipeDisplayName(String recipeDisplayName)
    {
        this.recipeDisplayName = recipeDisplayName;
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method clones given object.
     * @return Clone of recipe.
     */
    @Override
    public abstract Recipe clone();


    /**
     * Returns the name of recipe.
     * @param user User who wants to get recipe name.
     * @return String of recipe name.
     */
    public abstract String getRecipeName(User user);


    /**
     * Returns ItemStack of icon for recipe.
     * @return Recipe icon ItemStack.
     */
    public abstract ItemStack getIcon();

    /**
     * This method populates clone with items from current object.
     * @param clone Clone object that must be populated.
     */
    protected void populateClone(Recipe clone)
    {
        clone.setRecipeDisplayName(this.getRecipeDisplayName());
        clone.setCauldronType(this.getCauldronType());
        clone.setCauldronLevel(this.getCauldronLevel());
        clone.setTemperature(this.getTemperature());

        clone.setExperience(this.getExperience());
        clone.setPermissions(new HashSet<>(this.getPermissions()));

        clone.setMainIngredient(this.mainIngredient == null ? null : this.mainIngredient.clone());
        clone.setExtraIngredients(this.getExtraIngredients());

        clone.setRewardPoints(this.getRewardPoints());
        clone.setComplexity(this.getComplexity());

        clone.setOrder(this.getOrder());
    }


    /**
     * This method returns true if recipe is valid.
     * @return {@code true} if recipe is valid, otherwise {@code false}.
     */
    public boolean isValid()
    {
        return this.mainIngredient != null;
    }


// ---------------------------------------------------------------------
// Section: Enum
// ---------------------------------------------------------------------


    /**
     * The type of temperature that required for recipe.
     */
    public enum Temperature
    {
        /**
         * Requires ice block below cauldron.
         */
        COOL,
        /**
         * Does not require anything.
         */
        NORMAL,
        /**
         * Requires lava, fire, campfire or burning furnace below cauldron.
         */
        HEAT
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Stores recipe name
     */
    @Expose
    private String recipeDisplayName = "";

    /**
     * The main ingredient -> last item.
     */
    @Expose
    private ItemStack mainIngredient;

    /**
     * List of extra items for the recipe
     */
    @Expose
    private List<ItemStack> extraIngredients = new ArrayList<>();

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
    private Material cauldronType = Material.WATER_CAULDRON;

    /**
     * Cauldron fill level.
     * Works for water and snow. Lava and empty one is always 1
     */
    @Expose
    private int cauldronLevel = 3;

    /**
     * Allows to define 4 different cauldron types.
     * @see Temperature#HEAT
     * @see Temperature#NORMAL
     * @see Temperature#COOL
     */
    @Expose
    private Temperature temperature = Temperature.NORMAL;

    /**
     * Stores the list of permissions that are required for this recipe.
     */
    @Expose
    private Set<String> permissions = new HashSet<>();

    /**
     * Stores the reward points for using this recipe.
     * TODO: Requires for MASTERY_SKILL feature
     */
    @Expose
    private int rewardPoints;

    /**
     * Complexity level for recipe.
     * TODO: Requires for MASTERY_SKILL feature
     */
    @Expose
    private float complexity;

    /**
     * Order number for recipe.
     */
    @Expose
    private int order;
}
