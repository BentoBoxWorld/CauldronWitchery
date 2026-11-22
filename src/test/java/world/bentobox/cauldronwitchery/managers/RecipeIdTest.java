package world.bentobox.cauldronwitchery.managers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.EntityRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.ItemRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;


/**
 * Tests recipe id generation used by statistics and placeholders.
 */
class RecipeIdTest
{
    @BeforeEach
    void setUp() throws Exception
    {
        MockBukkit.mock();
        // MagicStickObject builds a NamespacedKey from the BentoBox instance.
        BentoBox plugin = mock(BentoBox.class);
        when(plugin.getName()).thenReturn("BentoBox");
        when(plugin.namespace()).thenReturn("bentobox");
        Field instance = BentoBox.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, plugin);
    }


    @AfterEach
    void tearDown()
    {
        MockBukkit.unmock();
    }


    private static MagicStickObject stick(String id, Recipe... recipes)
    {
        MagicStickObject stick = new MagicStickObject();
        stick.setUniqueId(id);
        stick.setRecipeList(new ArrayList<>(List.of(recipes)));
        return stick;
    }


    @Test
    void testDefaultRecipeIds()
    {
        assertEquals("sheep", new EntityRecipe(EntityType.SHEEP).getDefaultRecipeId());
        assertEquals("diamond", new ItemRecipe(new ItemStack(Material.DIAMOND)).getDefaultRecipeId());
        assertEquals("first_grimoire", new BookRecipe("first_grimoire").getDefaultRecipeId());
        assertEquals("entity", new EntityRecipe().getDefaultRecipeId());
    }


    @Test
    void testGameModePrefix()
    {
        assertEquals("bskyblock", CauldronWitcheryManager.getGameModePrefix("bskyblock_fragile_stick"));
        assertEquals("bskyblock", CauldronWitcheryManager.getGameModePrefix("BSkyBlock_stick"));
        assertEquals("stick", CauldronWitcheryManager.getGameModePrefix("stick"));
    }


    @Test
    void testSanitizeRecipeId()
    {
        assertEquals("my_book_1", CauldronWitcheryManager.sanitizeRecipeId("My Book-1"));
        assertEquals("recipe", CauldronWitcheryManager.sanitizeRecipeId(""));
        assertEquals("recipe", CauldronWitcheryManager.sanitizeRecipeId(null));
    }


    @Test
    void testAssignRecipeIdsDeduplicatesInsideStick()
    {
        MagicStickObject stick = stick("bskyblock_stick",
            new EntityRecipe(EntityType.SHEEP),
            new EntityRecipe(EntityType.SHEEP),
            new EntityRecipe(EntityType.COW));

        assertTrue(CauldronWitcheryManager.assignRecipeIds(stick, List.of(stick)));

        assertEquals("sheep", stick.getRecipeList().get(0).getRecipeId());
        assertEquals("sheep_2", stick.getRecipeList().get(1).getRecipeId());
        assertEquals("cow", stick.getRecipeList().get(2).getRecipeId());
    }


    @Test
    void testAssignRecipeIdsKeepsExistingAndAvoidsOtherSticksInGameMode()
    {
        Recipe existing = new EntityRecipe(EntityType.SHEEP);
        existing.setRecipeId("sheep");
        MagicStickObject other = stick("bskyblock_other", existing);

        // Same id in another gamemode must not cause a suffix.
        Recipe otherGameMode = new EntityRecipe(EntityType.COW);
        otherGameMode.setRecipeId("cow");
        MagicStickObject acid = stick("acidisland_stick", otherGameMode);

        Recipe kept = new EntityRecipe(EntityType.PIG);
        kept.setRecipeId("custom_pig");
        MagicStickObject stick = stick("bskyblock_stick",
            new EntityRecipe(EntityType.SHEEP),
            new EntityRecipe(EntityType.COW),
            kept);

        assertTrue(CauldronWitcheryManager.assignRecipeIds(stick, List.of(other, acid, stick)));

        assertEquals("sheep_2", stick.getRecipeList().get(0).getRecipeId());
        assertEquals("cow", stick.getRecipeList().get(1).getRecipeId());
        assertEquals("custom_pig", stick.getRecipeList().get(2).getRecipeId());
    }


    @Test
    void testAssignRecipeIdsNoChange()
    {
        Recipe recipe = new EntityRecipe(EntityType.SHEEP);
        recipe.setRecipeId("sheep");
        MagicStickObject stick = stick("bskyblock_stick", recipe);

        assertFalse(CauldronWitcheryManager.assignRecipeIds(stick, List.of(stick)));
        assertEquals("sheep", recipe.getRecipeId());
    }


    @Test
    void testCloneKeepsRecipeId()
    {
        Recipe recipe = new ItemRecipe(new ItemStack(Material.DIAMOND));
        recipe.setRecipeId("shiny");
        assertEquals("shiny", recipe.copy().getRecipeId());
    }
}
