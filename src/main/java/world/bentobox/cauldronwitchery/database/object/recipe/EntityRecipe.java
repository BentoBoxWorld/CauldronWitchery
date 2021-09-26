//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * The type EntityType recipe.
 */
public class EntityRecipe extends Recipe
{
    /**
     * Instantiates a new EntityType recipe.
     */
    public EntityRecipe()
    {
        // Empty Constructor.
    }


    public EntityRecipe(EntityType entityType)
    {
        this.entityType = entityType;
    }


    /**
     * Gets EntityType.
     *
     * @return the EntityType
     */
    public EntityType getEntityType()
    {
        return entityType;
    }


    /**
     * Sets EntityType.
     *
     * @param entityType the EntityType
     */
    public void setEntityType(EntityType entityType)
    {
        this.entityType = entityType;
    }


    @Override
    public Recipe clone()
    {
        EntityRecipe recipe = new EntityRecipe();
        recipe.setEntityType(this.entityType);

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
     * Stores the EntityType.
     */
    @Expose
    private EntityType entityType;
}
