package world.bentobox.cauldronwitchery.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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

        //panelBuilder.item(10, this.createButton(Action.MANAGE_USERS));
        //panelBuilder.item(28, this.createButton(Action.WIPE_USER_DATABASE));

        panelBuilder.item(12, this.createButton(Action.MANAGE_MAGIC_STICKS));
        //panelBuilder.item(21, this.createButton(Action.MANAGE_BUNDLES));

        panelBuilder.item(14, this.createButton(Action.IMPORT_TEMPLATE));

        //panelBuilder.item(15, this.createButton(Action.WEB_LIBRARY));
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
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));
            }
            case MANAGE_MAGIC_STICKS -> {
                icon = new ItemStack(Material.STICK);
                clickHandler = (panel, user1, clickType, slot) -> {
                    ManageMagicStickPanel.open(this);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));
            }
            case MANAGE_BUNDLES -> {
                icon = new ItemStack(Material.CHEST);
                clickHandler = (panel, user1, clickType, slot) -> {
                    // BundleManagePanel.open(this);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-open"));
            }
            case EDIT_SETTINGS -> {
                icon = new ItemStack(Material.CRAFTING_TABLE);
                clickHandler = (panel, user, clickType, slot) -> {
                    SettingsPanel.open(this);
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case WEB_LIBRARY -> {
                icon = new ItemStack(Material.PAPER);
                clickHandler = null;
                glow = false;
            }
            case EXPORT_DATABASE -> {
                icon = new ItemStack(Material.HOPPER);
                clickHandler = (panel, user, clickType, slot) -> {

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
                    Function<String, Boolean> validationFunction = fileName ->
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
                            Constants.PARAMETER_WORLD, world.getName()),
                        Constants.CONVERSATIONS + "file-name-exist");

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
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case IMPORT_DATABASE -> {
                icon = new ItemStack(Material.BOOKSHELF);
                clickHandler = (panel, user, clickType, slot) -> {
                    LibraryPanel.open(this, LibraryPanel.Library.DATABASE);
                    return true;
                };
                glow = true;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-open"));
            }
            case WIPE_DATABASE -> {
                icon = new ItemStack(Material.TNT);
                clickHandler = (panel, user, clickType, slot) -> {

                    Consumer<Boolean> consumer = value -> {
                        if (value)
                        {
                            this.addon.getAddonManager().wipeDatabase(Utils.getGameMode(this.world));
                        }

                        this.build();
                    };

                    // Create conversation that gets user acceptance to delete generator data.
                    ConversationUtils.createConfirmation(
                        consumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "confirm-all-data-deletion",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)),
                        this.user.getTranslation(Constants.CONVERSATIONS + "all-data-removed",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));

                    return true;
                };
                glow = true;

                description.add("");
                description.add(this.user.getTranslationOrNothing(Constants.TIPS + "click-to-wipe"));
            }
            case WIPE_USER_DATABASE -> {
                icon = new ItemStack(Material.TNT);
                clickHandler = (panel, user, clickType, slot) -> {

                    Consumer<Boolean> consumer = value -> {
                        if (value)
                        {
                            this.addon.getAddonManager().wipePlayers(Utils.getGameMode(this.world));
                        }

                        this.build();
                    };

                    // Create conversation that gets user acceptance to delete generator data.
                    ConversationUtils.createConfirmation(
                        consumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "confirm-user-data-deletion",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)),
                        this.user.getTranslation(Constants.CONVERSATIONS + "user-data-removed",
                            Constants.PARAMETER_GAMEMODE, Utils.getGameMode(this.world)));

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