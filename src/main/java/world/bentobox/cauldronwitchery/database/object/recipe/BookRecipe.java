//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.database.object.recipe;


import com.google.gson.annotations.Expose;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.stream.Collectors;


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
     * Stores the name of the book.
     */
    @Expose
    private String bookName;
}
