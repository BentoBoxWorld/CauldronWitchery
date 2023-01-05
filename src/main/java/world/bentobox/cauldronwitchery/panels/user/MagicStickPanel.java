//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.panels.user;


import org.bukkit.World;
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
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.panels.CommonPanel;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * Magic stick panel builder.
 */
public class MagicStickPanel extends CommonPanel
{
    private MagicStickPanel(CauldronWitcheryAddon addon,
        World world,
        User user,
        String topLabel,
        String permissionPrefix)
    {
        super(addon, user, world, topLabel, permissionPrefix);

        this.magicSticks = this.manager.getAllMagicSticks(this.world);
    }


    /**
     * Open the Magic Stick GUI.
     *
     * @param addon the addon
     * @param world the world
     * @param user the user
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     */
    public static void open(CauldronWitcheryAddon addon,
        World world,
        User user,
        String topLabel,
        String permissionPrefix)
    {
        new MagicStickPanel(addon, world, user, topLabel, permissionPrefix).build();
    }


    protected void build()
    {
        // Do not open gui if there is no magic sticks.
        if (this.magicSticks.isEmpty())
        {
            this.addon.logError("There are no magic sticks set up!");
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.ERRORS + "no-available-sticks",
                Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));
            return;
        }

        // Start building panel.
        TemplatedPanelBuilder panelBuilder = new TemplatedPanelBuilder();

        // Set main template.
        panelBuilder.template("stick_panel", new File(this.addon.getDataFolder(), "panels"));
        panelBuilder.user(this.user);
        panelBuilder.world(this.user.getWorld());

        // Register button builders
        panelBuilder.registerTypeBuilder("STICK", this::createStickButton);

        // Register next and previous builders
        panelBuilder.registerTypeBuilder("NEXT", this::createNextButton);
        panelBuilder.registerTypeBuilder("PREVIOUS", this::createPreviousButton);

        // Register unknown type builder.
        panelBuilder.build();
    }


    @Nullable
    private PanelItem createStickButton(ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        if (this.magicSticks.isEmpty())
        {
            // Does not contain any sticks.
            return null;
        }

        MagicStickObject magicStick;

        // Check if that is a specific sticks
        if (template.dataMap().containsKey("id"))
        {
            String id = (String) template.dataMap().get("id");

            // Find a challenge with given id;
            magicStick = this.magicSticks.stream().
                filter(stickId -> stickId.getUniqueId().equals(id)).
                findFirst().
                orElse(null);

            if (magicStick == null)
            {
                // There is no stick in the list with specific id.
                return null;
            }
        }
        else
        {
            int index = this.stickIndex * slot.amountMap().getOrDefault("STICK", 1) + slot.slot();

            if (index >= this.magicSticks.size())
            {
                // Out of index.
                return null;
            }

            magicStick = this.magicSticks.get(index);
        }

        return this.createStickButton(template, magicStick);
    }


    @NonNull
    private PanelItem createStickButton(ItemTemplateRecord template, MagicStickObject magicStick)
    {
        PanelItemBuilder builder = new PanelItemBuilder();

        // Template specification are always more important than dynamic content.
        builder.icon(template.icon() != null ? template.icon().clone() : magicStick.getMagicStick());

        // Template specific title is always more important than magicStick name.
        if (template.title() != null && !template.title().isBlank())
        {
            builder.name(this.user.getTranslation(this.world, template.title(),
                Constants.PARAMETER_NAME, magicStick.getFriendlyName()));
        }
        else
        {
            builder.name(Util.translateColorCodes(magicStick.getFriendlyName()));
        }

        if (template.description() != null && !template.description().isBlank())
        {
            // TODO: adding parameters could be useful.
            builder.description(this.user.getTranslation(this.world, template.description()));
        }
        else
        {
            builder.description(this.generateStickDescription(magicStick, this.user));
        }

        // Add Click handler
        builder.clickHandler((panel, user, clickType, i) -> {
            for (ItemTemplateRecord.ActionRecords action : template.actions())
            {
                if (clickType == action.clickType())
                {
                    switch (action.actionType().toUpperCase())
                    {
                        case "PURCHASE":
                            this.manager.purchaseStick(this.user, magicStick);
                            break;
                        case "RECIPES":
                            RecipesPanel.open(this, magicStick);
                            break;
                    }
                }
            }

            return true;
        });

        // Collect tooltips.
        List<String> tooltips = template.actions().stream().
            filter(action -> action.tooltip() != null).
            filter(action -> this.manager.canPurchase(magicStick, this.user) || "RECIPES".equalsIgnoreCase(action.actionType())).
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

        // Click Handlers are managed by custom addon buttons.
        return builder.build();
    }


    @Nullable
    private PanelItem createNextButton(@NonNull ItemTemplateRecord template, TemplatedPanel.ItemSlot slot)
    {
        int size = this.magicSticks.size();

        if (size <= slot.amountMap().getOrDefault("STICK", 1) ||
            1.0 * size / slot.amountMap().getOrDefault("STICK", 1) <= this.stickIndex + 1)
        {
            // There are no next elements
            return null;
        }

        int nextPageIndex = this.stickIndex + 2;

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
            this.stickIndex++;
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
        if (this.stickIndex == 0)
        {
            // There are no next elements
            return null;
        }

        int previousPageIndex = this.stickIndex;

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
            this.stickIndex--;
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


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * This will be used for paging.
     */
    private int stickIndex;

    /**
     * This list contains all information about sticks in current world.
     */
    private final List<MagicStickObject> magicSticks;
}
