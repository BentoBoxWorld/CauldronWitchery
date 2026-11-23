package world.bentobox.cauldronwitchery.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.panels.CommonPanel;
import world.bentobox.cauldronwitchery.panels.ConversationUtils;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * This class contains Main
 */
public class AdminPanel extends CommonPanel
{
    private static final String CLICK_TO_OPEN = Constants.TIPS + "click-to-open";


    // ---------------------------------------------------------------------
    // Section: Constructor
    // ---------------------------------------------------------------------

    /**
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     */
    private AdminPanel(CauldronWitcheryAddon addon,
        World world,
        User user,
        String topLabel,
        String permissionPrefix)
    {
        super(addon, user, world, topLabel, permissionPrefix);
    }


    /**
     * Open the Challenges Admin GUI.
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
        new AdminPanel(addon, world, user, topLabel, permissionPrefix).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * {@inheritDoc}
     */
    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation(Constants.TITLE + "admin-panel"));

        PanelUtils.fillBorder(panelBuilder, Material.MAGENTA_STAINED_GLASS_PANE);

        // User management, bundles and web library buttons are not shown yet.
        panelBuilder.item(12, this.createButton(Action.MANAGE_MAGIC_STICKS));

        panelBuilder.item(14, this.createButton(Action.IMPORT_TEMPLATE));

        panelBuilder.item(24, this.createButton(Action.EXPORT_DATABASE));
        panelBuilder.item(33, this.createButton(Action.IMPORT_DATABASE));

        panelBuilder.item(16, this.createButton(Action.EDIT_SETTINGS));
        panelBuilder.item(34, this.createButton(Action.WIPE_DATABASE));

        panelBuilder.item(44, this.returnButton);

        panelBuilder.build();
    }


    /**
     * This method is used to create PanelItem for each button type.
     * @param button Button which must be created.
     * @return PanelItem with necessary functionality.
     */
    private PanelItem createButton(Action button)
    {
        final String name = this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".name");
        List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(Constants.BUTTON + button.name().toLowerCase() + ".description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (button)
        {
            case MANAGE_USERS -> {
                icon = new ItemStack(Material.PLAYER_HEAD);
                clickHandler = (panel, user1, clickType, slot) -> {
                    ManagePlayerPanel.open(this);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(CLICK_TO_OPEN));
            }
            case MANAGE_MAGIC_STICKS -> {
                icon = new ItemStack(Material.STICK);
                clickHandler = (panel, user1, clickType, slot) -> {
                    ManageMagicStickPanel.open(this);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(CLICK_TO_OPEN));
            }
            case MANAGE_BUNDLES -> {
                icon = new ItemStack(Material.CHEST);
                // Bundle management panel is not implemented yet.
                clickHandler = (panel, user1, clickType, slot) -> true;
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(CLICK_TO_OPEN));
            }
            case EDIT_SETTINGS -> {
                icon = new ItemStack(Material.CRAFTING_TABLE);
                clickHandler = (panel, user, clickType, slot) -> {
                    SettingsPanel.open(this);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(CLICK_TO_OPEN));
            }
            case WEB_LIBRARY -> {
                icon = new ItemStack(Material.PAPER);
                clickHandler = null;
                glow = false;
            }
            case EXPORT_DATABASE -> {
                icon = new ItemStack(Material.HOPPER);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.askExportFileName();
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-export"));
            }
            case IMPORT_TEMPLATE -> {
                icon = new ItemStack(Material.BOOKSHELF);
                clickHandler = (panel, user, clickType, slot) -> {
                    LibraryPanel.open(this, LibraryPanel.Library.TEMPLATE);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(CLICK_TO_OPEN));
            }
            case IMPORT_DATABASE -> {
                icon = new ItemStack(Material.BOOKSHELF);
                clickHandler = (panel, user, clickType, slot) -> {
                    LibraryPanel.open(this, LibraryPanel.Library.DATABASE);
                    return true;
                };
                glow = true;

                description.add("");
                description.add(this.user.getTranslationOrNothing(CLICK_TO_OPEN));
            }
            case WIPE_DATABASE -> {
                icon = new ItemStack(Material.TNT);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.askWipeConfirmation(this.addon.getAddonManager()::wipeDatabase, "confirm-all-data-deletion", "all-data-removed");
                    return true;
                };
                glow = true;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-wipe"));
            }
            case WIPE_USER_DATABASE -> {
                icon = new ItemStack(Material.TNT);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.askWipeConfirmation(this.addon.getAddonManager()::wipePlayers, "confirm-user-data-deletion", "user-data-removed");
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-wipe"));
            }
            default -> {
                icon = new ItemStack(Material.PAPER);
                clickHandler = null;
                glow = false;
            }
        }

        return new PanelItemBuilder().
            icon(icon).
            name(name).
            description(description).
            glow(glow).
            clickHandler(clickHandler).
            build();
    }


    /**
     * Asks user for export file name in chat and exports database into that file.
     */
    private void askExportFileName()
    {
        // This consumer process file exporting after user input is returned.
        Consumer<String> fileNameConsumer = value -> {
            if (value != null)
            {
                this.addon.getImportManager().generateDatabaseFile(this.user,
                    this.world,
                    Utils.sanitizeInput(value));
            }

            this.build();
        };

        // This function checks if file can be created.
        Predicate<String> validationFunction = fileName ->
        {
            String sanitizedName = Utils.sanitizeInput(fileName);
            return !new File(this.addon.getDataFolder(),
                sanitizedName.endsWith(".json") ? sanitizedName : sanitizedName + ".json").exists();
        };

        // Call a conversation API to get input string.
        ConversationUtils.createIDStringInput(fileNameConsumer,
            validationFunction,
            this.user,
            this.user.getTranslation(Constants.CONVERSATIONS + "exported-file-name"),
            this.user.getTranslation(Constants.CONVERSATIONS + "database-export-completed",
                Constants.PARAMETER_WORLD, this.world.getName()),
            Constants.CONVERSATIONS + "file-name-exist");
    }


    /**
     * Asks user to confirm data wipe in chat and runs it on confirmation.
     *
     * @param wipe Wipe action that accepts game mode name.
     * @param confirmKey Conversation translation key for the question.
     * @param removedKey Conversation translation key for the success message.
     */
    private void askWipeConfirmation(Consumer<String> wipe, String confirmKey, String removedKey)
    {
        String gameMode = Utils.getGameMode(this.world);

        Consumer<Boolean> consumer = value -> {
            if (Boolean.TRUE.equals(value) && gameMode != null)
            {
                wipe.accept(gameMode);
            }

            this.build();
        };

        // Create conversation that gets user acceptance to delete data.
        ConversationUtils.createConfirmation(
            consumer,
            this.user,
            this.user.getTranslation(Constants.CONVERSATIONS + confirmKey,
                Constants.PARAMETER_GAMEMODE, gameMode),
            this.user.getTranslation(Constants.CONVERSATIONS + removedKey,
                Constants.PARAMETER_GAMEMODE, gameMode));
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum contains all button variations. Just for cleaner code.
     */
    private enum Action
    {
        MANAGE_USERS,
        MANAGE_MAGIC_STICKS,
        MANAGE_BUNDLES,

        EDIT_SETTINGS,

        WEB_LIBRARY,
        EXPORT_DATABASE,

        IMPORT_TEMPLATE,
        IMPORT_DATABASE,

        WIPE_DATABASE,
        WIPE_USER_DATABASE
    }
}