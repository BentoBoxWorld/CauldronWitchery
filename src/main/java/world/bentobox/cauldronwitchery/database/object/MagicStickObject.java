//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object;


import com.google.gson.annotations.Expose;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
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

        return object;
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
     * List of recipes for this magic stick.
     */
    @Expose
    private List<Recipe> recipeList = new ArrayList<>();

    /**
     * Name of the book that will contain recipes for this magic stick.
     */
    @Expose
    private String bookName;
}
