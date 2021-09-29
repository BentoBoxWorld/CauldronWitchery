//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;


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
    private String bookName;
}
