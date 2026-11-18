package world.bentobox.cauldronwitchery.database.object;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class WitcheryPlayerDataTest
{
    private WitcheryPlayerData data;


    @BeforeEach
    void setUp()
    {
        this.data = new WitcheryPlayerData();
    }


    @Test
    void testDefaults()
    {
        assertEquals("", this.data.getUniqueId());
        assertEquals(0, this.data.getSticksPurchased());
        assertEquals(0, this.data.getRecipesUsed());
        assertEquals(0, this.data.getSummons());
        assertTrue(this.data.getRecipeUsage().isEmpty());
        assertEquals(0, this.data.getRecipeUsage("sheep"));
    }


    @Test
    void testAddStickPurchase()
    {
        this.data.addStickPurchase();
        this.data.addStickPurchase();
        assertEquals(2, this.data.getSticksPurchased());
    }


    @Test
    void testAddRecipeUsage()
    {
        this.data.addRecipeUsage("sheep");
        this.data.addRecipeUsage("sheep");
        this.data.addRecipeUsage("diamond");

        assertEquals(3, this.data.getRecipesUsed());
        assertEquals(2, this.data.getRecipeUsage("sheep"));
        assertEquals(1, this.data.getRecipeUsage("diamond"));
        assertEquals(0, this.data.getRecipeUsage("cow"));
    }


    @Test
    void testAddRecipeUsageBlankIdCountsTotalOnly()
    {
        this.data.addRecipeUsage("");
        this.data.addRecipeUsage(null);

        assertEquals(2, this.data.getRecipesUsed());
        assertTrue(this.data.getRecipeUsage().isEmpty());
    }


    @Test
    void testAddSummon()
    {
        this.data.addSummon();
        assertEquals(1, this.data.getSummons());
    }


    @Test
    void testNullRecipeUsageMapIsRecreated()
    {
        // Gson may leave the map null for old database entries.
        this.data.setRecipeUsage(null);
        this.data.addRecipeUsage("sheep");
        assertEquals(1, this.data.getRecipeUsage("sheep"));
    }
}
