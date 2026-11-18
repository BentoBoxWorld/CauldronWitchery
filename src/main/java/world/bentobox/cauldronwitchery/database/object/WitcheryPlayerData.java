package world.bentobox.cauldronwitchery.database.object;


import com.google.gson.annotations.Expose;
import java.util.HashMap;
import java.util.Map;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;


/**
 * Stores player statistics for a single GameMode.
 * Unique id is built as {@code <gamemode>_<player uuid>}.
 */
@Table(name = "WitcheryPlayerData")
public class WitcheryPlayerData implements DataObject
{
    @Override
    public String getUniqueId()
    {
        return this.uniqueId;
    }


    @Override
    public void setUniqueId(String uniqueId)
    {
        this.uniqueId = uniqueId;
    }


    /**
     * @return number of magic sticks the player has purchased.
     */
    public int getSticksPurchased()
    {
        return this.sticksPurchased;
    }


    /**
     * @param sticksPurchased number of magic sticks the player has purchased.
     */
    public void setSticksPurchased(int sticksPurchased)
    {
        this.sticksPurchased = sticksPurchased;
    }


    /**
     * @return number of recipes the player has successfully used.
     */
    public int getRecipesUsed()
    {
        return this.recipesUsed;
    }


    /**
     * @param recipesUsed number of recipes the player has successfully used.
     */
    public void setRecipesUsed(int recipesUsed)
    {
        this.recipesUsed = recipesUsed;
    }


    /**
     * @return number of successful entity summons.
     */
    public int getSummons()
    {
        return this.summons;
    }


    /**
     * @param summons number of successful entity summons.
     */
    public void setSummons(int summons)
    {
        this.summons = summons;
    }


    /**
     * @return map that links recipe id with the number of successful uses.
     */
    public Map<String, Integer> getRecipeUsage()
    {
        if (this.recipeUsage == null)
        {
            this.recipeUsage = new HashMap<>();
        }

        return this.recipeUsage;
    }


    /**
     * @param recipeUsage map that links recipe id with the number of successful uses.
     */
    public void setRecipeUsage(Map<String, Integer> recipeUsage)
    {
        this.recipeUsage = recipeUsage;
    }


    /**
     * Returns how many times given recipe was successfully used.
     * @param recipeId Recipe id.
     * @return number of successful uses.
     */
    public int getRecipeUsage(String recipeId)
    {
        return this.getRecipeUsage().getOrDefault(recipeId, 0);
    }


    /**
     * Records a successful use of the recipe.
     * @param recipeId Recipe id.
     */
    public void addRecipeUsage(String recipeId)
    {
        this.recipesUsed++;

        if (recipeId != null && !recipeId.isBlank())
        {
            this.getRecipeUsage().merge(recipeId, 1, Integer::sum);
        }
    }


    /**
     * Records a magic stick purchase.
     */
    public void addStickPurchase()
    {
        this.sticksPurchased++;
    }


    /**
     * Records a successful entity summon.
     */
    public void addSummon()
    {
        this.summons++;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Unique id: {@code <gamemode>_<player uuid>}.
     */
    @Expose
    private String uniqueId = "";

    /**
     * Number of purchased magic sticks.
     */
    @Expose
    private int sticksPurchased;

    /**
     * Number of successfully used recipes.
     */
    @Expose
    private int recipesUsed;

    /**
     * Number of successful entity summons.
     */
    @Expose
    private int summons;

    /**
     * Successful uses per recipe id.
     */
    @Expose
    private Map<String, Integer> recipeUsage = new HashMap<>();
}
