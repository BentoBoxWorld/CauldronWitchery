package world.bentobox.cauldronwitchery.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


class UtilsMigrateItemSyntaxTest
{
    @Test
    void testTippedArrowIsMigrated()
    {
        assertEquals("        icon: tipped_arrow[potion_contents={custom_color:11546150}]",
            Utils.migrateLegacyItemSyntax("        icon: tipped_arrow{CustomPotionColor:11546150}"));
    }


    @Test
    void testPotionsAreMigrated()
    {
        assertEquals("icon: SPLASH_POTION[potion_contents={custom_color:8439583}]",
            Utils.migrateLegacyItemSyntax("icon: SPLASH_POTION{CustomPotionColor:8439583}"));
        assertEquals("icon: potion[potion_contents={custom_color:1}]",
            Utils.migrateLegacyItemSyntax("icon: potion{CustomPotionColor:1}"));
    }


    @Test
    void testMultipleLines()
    {
        String text = "a: tipped_arrow{CustomPotionColor:1}\nb: tipped_arrow{CustomPotionColor:2}\n";
        assertEquals("""
                a: tipped_arrow[potion_contents={custom_color:1}]
                b: tipped_arrow[potion_contents={custom_color:2}]
                """,
            Utils.migrateLegacyItemSyntax(text));
    }


    @Test
    void testCurrentSyntaxIsUnchanged()
    {
        String text = "icon: tipped_arrow[potion_contents={custom_color:11546150}]\nicon: STICK\n";
        assertEquals(text, Utils.migrateLegacyItemSyntax(text));
    }
}
