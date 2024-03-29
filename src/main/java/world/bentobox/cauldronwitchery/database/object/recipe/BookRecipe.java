//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import world.bentobox.bentobox.api.user.User;


/**
 * The type Book recipe.
 */
public class BookRecipe extends Recipe
{
    /**
     * Instantiates a new Book recipe.
     */
    public BookRecipe()
    {
        // Empty Constructor.
    }


    public BookRecipe(String book)
    {
        this.bookName = book;
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


    /**
     * Returns name of the book.
     * @param user User who wants to get recipe name.
     * @return Name of book.
     */
    @Override
    public String getRecipeName(User user)
    {
        if (this.getRecipeDisplayName() == null ||
            this.getRecipeDisplayName().isBlank() ||
            user.getTranslation(this.getRecipeDisplayName()).isBlank())
        {
            return this.bookName;
        }
        else
        {
            return user.getTranslation(this.getRecipeDisplayName());
        }
    }


    /**
     * Returns book as icon.
     * @return Icon.
     */
    @Override
    public ItemStack getIcon()
    {
        return new ItemStack(Material.KNOWLEDGE_BOOK);
    }


    @Override
    public Recipe clone()
    {
        BookRecipe recipe = new BookRecipe();
        recipe.setBookName(this.bookName);
        this.populateClone(recipe);
        return recipe;
    }


    /**
     * Stores the name of the book.
     */
    @Expose
    private String bookName = "";
}
