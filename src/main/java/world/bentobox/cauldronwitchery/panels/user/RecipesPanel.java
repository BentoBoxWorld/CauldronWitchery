//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.panels.user;


import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.TemplatedPanel;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.panels.builders.TemplatedPanelBuilder;
import world.bentobox.bentobox.api.panels.reader.ItemTemplateRecord;
import world.bentobox.bentobox.util.Util;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.panels.CommonPanel;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * Recipe panel builder.
 */
public class RecipesPanel extends CommonPanel
{
    private RecipesPanel(CommonPanel parentPanel, MagicStickObject magicStick)
    {
        super(parentPanel);

        this.magicStick = magicStick;
        this.recipes = magicStick.getRecipeList();
    }


    /**
     * Opens recipes view panel.
     *
     * @param commonPanel the common panel
     * @param magicStick the magic stick
     */
    public static void open(CommonPanel commonPanel, MagicStickObject magicStick)
    {
        new RecipesPanel(commonPanel, magicStick).build();
    }


    protected void build()
    {
        // Do not open gui if there is no magic sticks.
        if (this.recipes.isEmpty())
        {
            this.addon.logError("There are no recipes available for stick!");
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.ERRORS + "no-recipes",
                Constants.PARAMETER_NAME, this.magicStick.getFriendlyName()));
            return;
        }

        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("recipe_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder("RECIPE", this::createRecipeButton);

        // Register next and previous builders
        panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
        panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);
        panelBuilder.registerTypeBuilder("RETURN", this::createReturnButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


    @Nullable
    private PanelItem createRecipeButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.recipes.isEmpty())
        {
            // Does not contain any sticks.
            return null;
        }

        int index = this.recipeIndex * slot.amountMap().getOrDefault("RECIPE", 1) + slot.slot();

        if (index >= this.recipes.size())
        {
            // Out of index.
            return null;
        }

        return this.createRecipeButton(template, this.recipes.get(index));
    }


    @NonNull
    private PanelItem createRecipeButton(ItemTemplateRecord template, Recipe recipe)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.
        builder.icon(template.icon() != null ? template.icon().clone() : recipe.getIcon());

        // Template specific title is always more important than recipe name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.PARAMETER_NAME, recipe.getRecipeName(this.user)));
        }
        else
        {
            builder.name(Util.translateColorCodes(recipe.getRecipeName(this.user)));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            // TODO: adding parameters could be useful.
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.generateRecipeDescription(recipe, this.user));
        }

        // Click Handlers are managed by custom addon buttons.
        return builder.build();
    }


    @Nullable
    private PanelItem createNextButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        int size = this.recipes.size();

        if (size <= slot.amountMap().getOrDefault("RECIPE", 1) ||
            1.0 * size / slot.amountMap().getOrDefault("RECIPE", 1) <= this.recipeIndex + 1)
        {
            // There are no next elements
            return null;
        }

        int nextPageIndex = this.recipeIndex + 2;

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(nextPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()),
                Constants.PARAMETER_NUMBER, String.valueOf(nextPageIndex));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            // Next button ignores click type currently.
            this.recipeIndex++;
            this.build();
            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    @Nullable
    private PanelItem createPreviousButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.recipeIndex == 0)
        {
            // There are no next elements
            return null;
        }

        int previousPageIndex = this.recipeIndex;

        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            ItemStack clone = template.icon().clone();

            if ((Boolean) template.dataMap().getOrDefault("indexing", false))
            {
                clone.setAmount(previousPageIndex);
            }

            builder.icon(clone);
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()),
                Constants.PARAMETER_NUMBER, String.valueOf(previousPageIndex));
        }

        // Add ClickHandler
        builder.clickHandler((panel, user, clickType, i) ->
        {
            // Next button ignores click type currently.
            this.recipeIndex--;
            this.build();

            // Always return true.
            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


    @Nullable
    private PanelItem createReturnButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        if (template.icon() != null)
        {
            builder.icon(template.icon().clone());
        }

        if (template.title() != null)
        {
            builder.name(this.user.getTranslation(this.world, template.title()));
        }

        if (template.description() != null)
        {
            builder.description(this.user.getTranslation(this.world, template.description()));
        }

        // Add ClickHandler
        if (this.returnButton.getClickHandler().isPresent())
        {
            builder.clickHandler(this.returnButton.getClickHandler().get());
        }

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            map(action -> this.user.getTranslation(this.world, action.tooltip())).
            filter(text -> !text.isBlank()).
            collect(Collectors.toCollection(() -> new ArrayList<>(template.actions().size())));

        // Add tooltips.
        if (!tooltips.isEmpty())
        {
            // Empty line and tooltips.
            builder.description("");
            builder.description(tooltips);
        }

        return builder.build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This will be used for paging.
     */
    private int recipeIndex;

    /**
     * This list contains all information about sticks in current world.
     */
    private final List<Recipe> recipes;

    /**
     * Magic stick that owns the recipes.
     */
    private final MagicStickObject magicStick;
}
