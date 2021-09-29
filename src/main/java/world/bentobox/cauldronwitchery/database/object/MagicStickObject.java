//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object;


import com.google.gson.annotations.Expose;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;


/**
 * The magic stick object.
 */
@Table(name = "MagicStick")
public class MagicStickObject implements DataObject
{
    /**
     * @return Unique id.
     */
    @Override
    public String getUniqueId()
    {
        return this.uniqueId;
    }


    /**
     * @param uniqueId Unique id.
     */
    @Override
    public void setUniqueId(String uniqueId)
    {
        this.uniqueId = uniqueId;
    }


    /**
     * Gets magic stick.
     *
     * @return the magic stick
     */
    public ItemStack getMagicStick()
    {
        return magicStick;
    }


    /**
     * Sets magic stick.
     *
     * @param magicStick the magic stick
     */
    public void setMagicStick(ItemStack magicStick)
    {
        this.magicStick = magicStick;
    }


    /**
     * Gets recipe list.
     *
     * @return the recipe list
     */
    public List<Recipe> getRecipeList()
    {
        return recipeList;
    }


    /**
     * Sets recipe list.
     *
     * @param recipeList the recipe list
     */
    public void setRecipeList(List<Recipe> recipeList)
    {
        this.recipeList = recipeList;
    }


    /**
     * Gets book name.
     *
     * @return the book name
     */
    public String getBookName()
    {
        return bookName;
    }


    /**
     * Sets book name.
     *
     * @param bookName the book name
     */
    public void setBookName(String bookName)
    {
        this.bookName = bookName;
    }


// ---------------------------------------------------------------------
// Section: Implementations
// ---------------------------------------------------------------------


    @Override
    public MagicStickObject clone()
    {
        MagicStickObject object = new MagicStickObject();
        object.setUniqueId(this.uniqueId);
        object.setMagicStick(this.magicStick.clone());
        object.setBookName(this.bookName);
        object.setRecipeList(this.recipeList.stream().
            map(Recipe::clone).
            collect(Collectors.toList()));

        object.setFriendlyName(this.friendlyName);
        object.setDescription(this.description);

        object.setPermissions(new HashSet<>(this.getPermissions()));
        object.setComplexity(this.complexity);

        return object;
    }


    /**
     * Gets friendly name.
     *
     * @return the friendly name
     */
    public String getFriendlyName()
    {
        return friendlyName;
    }


    /**
     * Sets friendly name.
     *
     * @param friendlyName the friendly name
     */
    public void setFriendlyName(String friendlyName)
    {
        this.friendlyName = friendlyName;
    }


    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }


    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description)
    {
        this.description = description;
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


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Unique id for this stick.
     */
    @Expose
    private String uniqueId = "";

    /**
     * Magic Stick item;
     */
    @Expose
    private ItemStack magicStick;

    /**
     * The name of magic stick.
     * Mostly for GUI's.
     */
    @Expose
    private String friendlyName;

    /**
     * The description of magic stick.
     * Mostly for GUI's.
     */
    @Expose
    private String description;

    /**
     * Permissions for using the stick.
     * Stricter over recipe permissions.
     */
    @Expose
    private Set<String> permissions = new HashSet<>();

    /**
     * List of recipes for this magic stick.
     */
    @Expose
    private List<Recipe> recipeList = new ArrayList<>();

    /**
     * Name of the book that will contain recipes for this magic stick.
     */
    @Expose
    private String bookName;

    /**
     * Allows to set complexity of the stick usage.
     * Larger complexity, bigger chance for it to fail.
     * TODO: Requires for MASTERY_SKILL feature
     */
    @Expose
    private float complexity;
}
