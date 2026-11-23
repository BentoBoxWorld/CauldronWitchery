//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.panels;


import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.utils.Constants;


/**
 * This panel implements common things for Paged pages.
 */
public abstract class CommonPagedPanel<T> extends CommonPanel
{
    /**
     * Instantiates a new Common paged panel.
     *
     * @param addon the addon
     * @param user the user
     * @param world the world
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     */
    protected CommonPagedPanel(CauldronWitcheryAddon addon,
        User user,
        World world,
        String topLabel,
        String permissionPrefix)
    {
        super(addon, user, world, topLabel, permissionPrefix);
    }


    /**
     * Instantiates a new Common paged panel.
     *
     * @param parentPanel the parent panel
     */
    protected CommonPagedPanel(@NotNull CommonPanel parentPanel)
    {
        super(parentPanel);
    }


    /**
     * This method is called when filter value is updated.
     */
    protected abstract void updateFilters();


    /**
     * Create element button panel item.
     *
     * @param object the object
     * @return the panel item
     */
    protected abstract PanelItem createElementButton(T object);


    /**
     * Populate elements.
     *
     * @param panelBuilder the panel builder
     * @param objectList the object list
     */
    protected void populateElements(PanelBuilder panelBuilder, List<T> objectList)
    {
        final int MAX_ELEMENTS = 21;
        final int size = objectList.size();

        if (this.pageIndex < 0)
        {
            this.pageIndex = size / MAX_ELEMENTS;
        }
        else if (this.pageIndex > (size / MAX_ELEMENTS))
        {
            this.pageIndex = 0;
        }

        int objectIndex = MAX_ELEMENTS * this.pageIndex;

        // I want first row to be only for navigation and return button.
        int index = 10;

        while (objectIndex < ((this.pageIndex + 1) * MAX_ELEMENTS) &&
            objectIndex < size &&
            index < 36)
        {
            if (!panelBuilder.slotOccupied(index))
            {
                panelBuilder.item(index, this.createElementButton(objectList.get(objectIndex++)));
            }

            index++;
        }

        if (size > MAX_ELEMENTS && 1.0 * size / MAX_ELEMENTS > this.pageIndex + 1)
        {
            panelBuilder.item(26, this.getButton(CommonButtons.NEXT));

        }

        if (this.pageIndex > 0)
        {
            panelBuilder.item(18, this.getButton(CommonButtons.PREVIOUS));
        }

        // Add search button only if there is more than MAX_ELEMENTS objects or searchString
        // is not blank.
        if (!this.searchString.isBlank() || objectList.size() > MAX_ELEMENTS)
        {
            panelBuilder.item(40, this.getButton(CommonButtons.SEARCH));
        }
    }


    /**
     * This method returns PanelItem that represents given Button.
     * @param button Button that must be returned.
     * @return PanelItem with requested functionality.
     */
    protected PanelItem getButton(CommonButtons button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);

        ItemStack icon = switch (button)
        {
            case NEXT -> this.describeNextButton(reference, description);
            case PREVIOUS -> this.describePreviousButton(reference, description);
            case SEARCH -> this.describeSearchButton(reference, description);
        };

        PanelItem.ClickHandler clickHandler = switch (button)
        {
            case NEXT -> (panel, user, clickType, slot) ->
            {
                this.pageIndex++;
                this.build();
                return true;
            };
            case PREVIOUS -> (panel, user, clickType, slot) ->
            {
                this.pageIndex--;
                this.build();
                return true;
            };
            case SEARCH -> this::onSearchClick;
        };

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            clickHandler(clickHandler).
            build();
    }


    /**
     * Populates the next page button description and returns its icon.
     */
    private ItemStack describeNextButton(String reference, List<String> description)
    {
        description.add(this.user.getTranslation(reference + "description",
            Constants.PARAMETER_NUMBER, String.valueOf(this.pageIndex + 2)));

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-next"));

        return new ItemStack(Material.OAK_SIGN, this.pageIndex + 2);
    }


    /**
     * Populates the previous page button description and returns its icon.
     */
    private ItemStack describePreviousButton(String reference, List<String> description)
    {
        description.add(this.user.getTranslation(reference + "description",
            Constants.PARAMETER_NUMBER, String.valueOf(this.pageIndex)));

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-previous"));

        return new ItemStack(Material.OAK_SIGN, Math.max(1, this.pageIndex));
    }


    /**
     * Populates the search button description and returns its icon.
     */
    private ItemStack describeSearchButton(String reference, List<String> description)
    {
        description.add(this.user.getTranslation(reference + "description"));

        boolean hasSearch = this.searchString != null && !this.searchString.isEmpty();

        if (hasSearch)
        {
            description.add(this.user.getTranslation(reference + "search",
                Constants.PARAMETER_VALUE, this.searchString));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (hasSearch)
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-clear"));
        }

        return new ItemStack(Material.ANVIL);
    }


    /**
     * Handles a click on the search button: right click clears the search, any other click asks for a new value.
     */
    private boolean onSearchClick(Panel panel, User user, ClickType clickType, int slot)
    {
        if (clickType.isRightClick())
        {
            // Clear string.
            this.searchString = "";
            this.updateFilters();
            // Rebuild gui.
            this.build();
        }
        else
        {
            // Create consumer that process description change
            Consumer<String> consumer = value ->
            {
                if (value != null)
                {
                    this.searchString = value;
                    this.updateFilters();
                }

                this.build();
            };

            // start conversation
            ConversationUtils.createStringInput(consumer,
                user,
                user.getTranslation(Constants.CONVERSATIONS + "write-search"),
                user.getTranslation(Constants.CONVERSATIONS + "search-updated"));
        }

        return true;
    }


    /**
     * Next and Previous Buttons.
     */
    private enum CommonButtons
    {
        NEXT,
        PREVIOUS,
        SEARCH
    }


    /**
     * Current page index.
     */
    private int pageIndex;

    /**
     * Text that contains filter string.
     */
    protected String searchString = "";
}
