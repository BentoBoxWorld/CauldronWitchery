//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.cauldronwitchery.panels.admin;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.*;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Players;
import world.bentobox.cauldronwitchery.panels.CommonPagedPanel;
import world.bentobox.cauldronwitchery.panels.CommonPanel;
import world.bentobox.cauldronwitchery.utils.Constants;


/**
 * This class manages User islands that are stored in database with some custom data. It also allows to add a custom
 * data to island.
 */
public class ManagePlayerPanel extends CommonPagedPanel<User>
{
    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param parentPanel Parent panel of current panel.
     */
    protected ManagePlayerPanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        this.elementList = Bukkit.getOnlinePlayers().stream().map(User::getInstance).collect(Collectors.toList());
        this.filterElements = this.elementList;
        this.searchString = "";
    }


    /**
     * This method allows to build panel.
     */
    @Override
    public void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "manage-players"));

        PanelUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(3, this.createButton(Tab.IS_ONLINE));
        panelBuilder.item(5, this.createButton(Tab.ALL_PLAYERS));

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.build();
    }


    @Override
    protected void updateFilters()
    {
        if (this.searchString == null || this.searchString.isBlank())
        {
            this.filterElements = this.elementList;
        }
        else
        {
            this.filterElements = this.elementList.stream().
                filter(element -> {
                    // If element name is set and name contains search field, then do not filter out.
                    return element.getName().toLowerCase().contains(this.searchString.toLowerCase());
                }).
                distinct().
                collect(Collectors.toList());
        }
    }


    /**
     * This method creates button for given player.
     * @return PanelItem button for given player.
     */
    protected PanelItem createElementButton(User user)
    {
        // Create description list
        List<String> description = new ArrayList<>();

        // Add tip
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-edit"));

        PanelItem.ClickHandler clickHandler = (panel, user1, clickType, i) -> {
            EditPlayerPanel.open(this, user);
            // Always return true.
            return true;
        };

        return new PanelItemBuilder().
            name(user.getName()).
            description(description).
            icon(user.getName()).
            clickHandler(clickHandler).
            build();
    }


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Tab button)
    {
        String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
        List<String> description = new ArrayList<>();
        description.add(this.user.getTranslationOrNothing(Constants.BUTTON + button.name().toLowerCase() + ".description"));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-view"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            if (button == activeTab)
            {
                return true;
            }
            else if (button == Tab.IS_ONLINE)
            {
                this.elementList = Bukkit.getOnlinePlayers().stream().map(User::getInstance).toList();
            }
            else
            {
                this.elementList = this.addon.getPlayers().getPlayers().stream().
                    map(Players::getPlayer).
                    filter(Objects::nonNull).
                    map(User::getInstance).
                    collect(Collectors.toList());
            }

            this.activeTab = button;

            this.searchString = "";
            this.filterElements = this.elementList;

            this.build();
            return true;
        };

        Material material = switch (button) {
            case IS_ONLINE -> Material.WRITTEN_BOOK;
            case ALL_PLAYERS -> Material.CHEST;
        };

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(material).
            clickHandler(clickHandler).
            glow(this.activeTab == button).
            build();
    }


    /**
     * This method build island panel from parent panel.
     *
     * @param panel ParentPanel.
     */
    public static void open(CommonPanel panel)
    {
        new ManagePlayerPanel(panel).build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This enum holds all possible actions in current GUI.
     */
    private enum Tab
    {
        /**
         * Shows islands with online users.
         */
        IS_ONLINE,
        /**
         * Shows islands with data.
         */
        ALL_PLAYERS
    }


    /**
     * This list contains currently displayed island list.
     */
    private List<User> elementList;

    /**
     * This list contains currently displayed island list.
     */
    private List<User> filterElements;

    /**
     * Indicate current active tab.
     */
    private Tab activeTab = Tab.IS_ONLINE;
}
