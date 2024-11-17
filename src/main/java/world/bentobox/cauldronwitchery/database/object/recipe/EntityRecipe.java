//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.utils.Utils;


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


    /**
     * Returns name of the entity.
     * @param user User who wants to get recipe name.
     * @return Name of entity.
     */
    @Override
    public String getRecipeName(User user)
    {
        if (this.getRecipeDisplayName() == null ||
            this.getRecipeDisplayName().isBlank() ||
            user.getTranslation(this.getRecipeDisplayName()).isBlank())
        {
            return Utils.prettifyObject(this.entityType, user);
        }
        else
        {
            return user.getTranslation(this.getRecipeDisplayName());
        }
    }

    /**
     * Returns entity as icon.
     * @return Icon.
     */
    @Override
    public ItemStack getIcon()
    {
        return this.entityType == null ? new ItemStack(Material.BARRIER) : PanelUtils.getEntityHead(this.entityType);
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
     * Returns true if entity type is not null.
     * @return If entity type is valid.
     */
    @Override
    public boolean isValid()
    {
        return super.isValid() && this.entityType != null;
    }


    /**
     * Stores the EntityType.
     */
    @Expose
    private EntityType entityType;
}
