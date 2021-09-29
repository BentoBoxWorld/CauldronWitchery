//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;
import org.bukkit.entity.EntityType;


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
        this.populateClone(recipe);
        return recipe;
    }


    /**
     * Stores the EntityType.
     */
    @Expose
    private EntityType entityType;
}
