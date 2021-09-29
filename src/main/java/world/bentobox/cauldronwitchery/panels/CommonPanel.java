//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.panels;


import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.EntityRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.ItemRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.managers.CauldronWitcheryManager;
import world.bentobox.cauldronwitchery.utils.Constants;


/**
 * This class contains common methods for all panels.
 */
public abstract class CommonPanel
{
    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param addon ChallengesAddon instance.
     * @param user User who opens panel.
     */
    protected CommonPanel(CauldronWitcheryAddon addon, User user, World world, String topLabel, String permissionPrefix)
    {
        this.addon = addon;
        this.world = world;
        this.manager = addon.getAddonManager();
        this.user = user;

        this.topLabel = topLabel;
        this.permissionPrefix = permissionPrefix;

        this.parentPanel = null;

        this.returnButton = new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "quit.name")).
            description(this.user.getTranslationOrNothing(Constants.BUTTON + "quit.description")).
            description("").
            description(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-quit")).
            icon(Material.OAK_DOOR).
            clickHandler((panel, user1, clickType, i) -> {
                this.user.closeInventory();
                return true;
            }).build();
    }


    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param parentPanel Parent panel of current panel.
     */
    protected CommonPanel(@NotNull CommonPanel parentPanel)
    {
        this.addon = parentPanel.addon;
        this.manager = parentPanel.manager;
        this.user = parentPanel.user;
        this.world = parentPanel.world;

        this.topLabel = parentPanel.topLabel;
        this.permissionPrefix = parentPanel.permissionPrefix;

        this.parentPanel = parentPanel;

        this.returnButton = new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "return.name")).
            description(this.user.getTranslationOrNothing(Constants.BUTTON + "return.description")).
            description("").
            description(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-return")).
            icon(Material.OAK_DOOR).
            clickHandler((panel, user1, clickType, i) -> {
                this.parentPanel.build();
                return true;
            }).build();
    }


    /**
     * This method allows building panel.
     */
    protected abstract void build();


    protected String generateRecipeDescription(BookRecipe recipe, User user)
    {
        return "";
    }

    protected String generateRecipeDescription(ItemRecipe recipe, User user)
    {
        return "";
    }

    protected String generateRecipeDescription(EntityRecipe recipe, User user)
    {
        return "";
    }

    protected String generateRecipeDescription(Recipe recipe, User user)
    {
        return "";
    }

    /**
     * This method reopens given panel.
     * @param panel Panel that must be reopened.
     */
    public static void reopen(CommonPanel panel)
    {
        panel.build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This variable stores parent gui.
     */
    @Nullable
    protected final CommonPanel parentPanel;

    /**
     * Variable stores Challenges addon.
     */
    protected final CauldronWitcheryAddon addon;

    /**
     * Variable stores Challenges addon manager.
     */
    protected final CauldronWitcheryManager manager;

    /**
     * Variable stores world in which panel is referred to.
     */
    protected final World world;

    /**
     * Variable stores user who created this panel.
     */
    protected final User user;

    /**
     * Variable stores top label of command from which panel was called.
     */
    protected final String topLabel;

    /**
     * Variable stores permission prefix of command from which panel was called.
     */
    protected final String permissionPrefix;

    /**
     * This object holds PanelItem that allows to return to previous panel.
     */
    protected PanelItem returnButton;
}
