package world.bentobox.cauldronwitchery.panels.admin;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.panels.CommonPagedPanel;
import world.bentobox.cauldronwitchery.panels.CommonPanel;
import world.bentobox.cauldronwitchery.panels.ConversationUtils;
import world.bentobox.cauldronwitchery.panels.utils.RecipeSelector;
import world.bentobox.cauldronwitchery.panels.utils.RecipeTypeSelector;
import world.bentobox.cauldronwitchery.utils.Constants;


/**
 * This class contains all necessary elements to create Levels Edit GUI.
 */
public class EditMagicStickPanel extends CommonPagedPanel<Recipe>
{
    // ---------------------------------------------------------------------
    // Section: Constructors
    // ---------------------------------------------------------------------


    /**
     * @param addon Addon where panel operates.
     * @param world World from which panel was created.
     * @param user User who created panel.
     * @param topLabel Command top label which creates panel (f.e. island or ai)
     * @param permissionPrefix Command permission prefix (f.e. bskyblock.)
     * @param magicStick ChallengeLevel that must be edited.
     */
    private EditMagicStickPanel(CauldronWitcheryAddon addon,
        User user,
        World world,
        String topLabel,
        String permissionPrefix,
        MagicStickObject magicStick)
    {
        super(addon, user, world, topLabel, permissionPrefix);
        this.magicStick = magicStick;
        this.currentMenuType = MenuType.PROPERTIES;
    }


    /**
     * @param magicStick ChallengeLevel that must be edited.
     */
    private EditMagicStickPanel(CommonPanel parentGUI, MagicStickObject magicStick)
    {
        super(parentGUI);
        this.magicStick = magicStick;
        this.currentMenuType = MenuType.PROPERTIES;
    }


    /**
     * Open the Challenges Level Edit GUI.
     *
     * @param addon the addon
     * @param world the world
     * @param user the user
     * @param topLabel the top label
     * @param permissionPrefix the permission prefix
     * @param level - level that needs editing
     */
    public static void open(CauldronWitcheryAddon addon,
        User user,
        World world,
        String topLabel,
        String permissionPrefix,
        MagicStickObject level)
    {
        new EditMagicStickPanel(addon, user, world, topLabel, permissionPrefix, level).build();
    }


    /**
     * Open the Challenges Level Edit GUI.
     *
     * @param panel - Parent Panel
     * @param level - level that needs editing
     */
    public static void open(CommonPanel panel,  MagicStickObject level)
    {
        new EditMagicStickPanel(panel, level).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method is called when filter value is updated.
     */
    @Override
    protected void updateFilters()
    {
        // Do nothing here.
    }


    /**
     * This method builds all necessary elements in GUI panel.
     */
    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation(Constants.TITLE + "edit-stick",
                "[stick]", this.magicStick.getFriendlyName()));

        PanelUtils.fillBorder(panelBuilder);

        panelBuilder.item(2, this.createMenuButton(MenuType.PROPERTIES));
        panelBuilder.item(6, this.createMenuButton(MenuType.RECIPES));

        if (this.currentMenuType.equals(MenuType.PROPERTIES))
        {
            this.buildMainPropertiesPanel(panelBuilder);
        }
        else if (this.currentMenuType.equals(MenuType.RECIPES))
        {
            this.buildRecipePanel(panelBuilder);
        }

        panelBuilder.item(44, this.returnButton);

        // Save challenge level every time this gui is build.
        // It will ensure that changes are stored in database.
        this.addon.getAddonManager().saveMagicStick(this.magicStick);

        panelBuilder.build();
    }


    /**
     * This class populate LevelsEditGUI with main level settings.
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildMainPropertiesPanel(PanelBuilder panelBuilder)
    {
        panelBuilder.listener(new IconChanger());

        panelBuilder.item(10, this.createButton(Button.STICK));

        // Alternative to anvil usage.
        panelBuilder.item(19, this.createButton(Button.NAME));
        panelBuilder.item(20, this.createButton(Button.DESCRIPTION));

        panelBuilder.item(13, this.createButton(Button.BOOK));
        //panelBuilder.item(22, this.createButton(Button.COMPLEXITY));
        panelBuilder.item(31, this.createButton(Button.PERMISSIONS));
    }


    /**
     * This class populate LevelsEditGUI with level challenges.
     * @param panelBuilder PanelBuilder where icons must be added.
     */
    private void buildRecipePanel(PanelBuilder panelBuilder)
    {
        this.populateElements(panelBuilder, this.magicStick.getRecipeList());

        panelBuilder.item(39, this.createButton(Button.ADD_RECIPE));
        panelBuilder.item(41, this.createButton(Button.REMOVE_RECIPE));
    }


    // ---------------------------------------------------------------------
    // Section: Other methods
    // ---------------------------------------------------------------------


    /**
     * This method creates top menu buttons, that allows to switch "tabs".
     * @param menuType Menu Type which button must be constructed.
     * @return PanelItem that represents given menu type.
     */
    private PanelItem createMenuButton(MenuType menuType)
    {
        final String reference = Constants.BUTTON + menuType.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (menuType)
        {
            case PROPERTIES -> {
                icon = new ItemStack(Material.CRAFTING_TABLE);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.currentMenuType = MenuType.PROPERTIES;
                    this.build();

                    return true;
                };
                glow = this.currentMenuType.equals(MenuType.PROPERTIES);
            }
            case RECIPES -> {
                icon = new ItemStack(Material.BREWING_STAND);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.currentMenuType = MenuType.RECIPES;
                    this.build();

                    return true;
                };
                glow = this.currentMenuType.equals(MenuType.RECIPES);
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
     * This method creates given challenge icon. On click it should open Edit Challenge GUI.
     * @param recipe Challenge which icon must be created.
     * @return PanelItem that represents given challenge.
     */
    @Override
    protected PanelItem createElementButton(Recipe recipe)
    {
        return new PanelItemBuilder().
            name(recipe.getRecipeName(this.user)).
            icon(recipe.getIcon()).
            description(this.generateRecipeDescription(recipe, null)).
            description("").
            description(this.user.getTranslation(Constants.TIPS + "click-to-edit")).
            clickHandler((panel, user, clickType, slot) -> {
                // Open challenges edit screen.
                EditRecipePanel.open(this, this.magicStick, recipe);
                return true;
            }).
            build();
    }


    /**
     * This method creates buttons for default main menu.
     * @param button Button which panel item must be created.
     * @return PanelItem that represents given button.
     */
    private PanelItem createButton(Button button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final String name = this.user.getTranslation(reference + "name");
        final List<String> description = new ArrayList<>(3);
        description.add(this.user.getTranslation(reference + "description"));

        ItemStack icon;
        boolean glow;
        PanelItem.ClickHandler clickHandler;

        switch (button)
        {
            case NAME -> {

                if (this.magicStick.getFriendlyName().isBlank())
                {
                    description.add(this.user.getTranslation(reference + "no-value"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "value",
                        Constants.PARAMETER_NAME, this.magicStick.getFriendlyName()));
                }

                icon = new ItemStack(Material.NAME_TAG);

                clickHandler = (panel, user, clickType, i) ->
                {
                    this.selectedButton = null;

                    // Create consumer that process description change
                    Consumer<String> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.magicStick.setFriendlyName(value);

                            ItemMeta itemMeta = this.magicStick.getMagicStick().getItemMeta();

                            if (itemMeta != null)
                            {
                                // Set new display name
                                itemMeta.setDisplayName(Util.translateColorCodes(value));
                                // Update meta.
                                this.magicStick.getMagicStick().setItemMeta(itemMeta);
                            }
                        }

                        this.build();
                    };

                    // start conversation
                    ConversationUtils.createStringInput(consumer,
                        user,
                        user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        user.getTranslation(Constants.CONVERSATIONS + "name-changed"));

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case DESCRIPTION -> {
                icon = new ItemStack(Material.WRITTEN_BOOK);

                if (this.magicStick.getDescription().isBlank())
                {
                    description.add(this.user.getTranslation(reference + "no-value"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "value"));
                    description.add(Util.translateColorCodes(this.magicStick.getDescription()));
                }

                clickHandler = (panel, user, clickType, i) ->
                {
                    this.selectedButton = null;

                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.magicStick.setDescription(String.join("\n", value));

                            ItemMeta itemMeta = this.magicStick.getMagicStick().getItemMeta();

                            if (itemMeta != null)
                            {
                                // Set new description
                                itemMeta.setLore(Arrays.stream(
                                    Util.translateColorCodes(this.magicStick.getDescription()).
                                        split("\n")).
                                    toList());
                                // Update meta.
                                this.magicStick.getMagicStick().setItemMeta(itemMeta);
                            }
                        }

                        this.build();
                    };

                    if (!this.magicStick.getDescription().isEmpty() && clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept(Collections.emptyList());
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-description"),
                            user.getTranslation(Constants.CONVERSATIONS + "description-changed"));
                    }

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                if (!this.magicStick.getDescription().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }
            }
            case STICK -> {
                icon = this.magicStick.getMagicStick();

                clickHandler = (panel, user, clickType, i) ->
                {
                    // selects and deselects the element
                    this.selectedButton = this.selectedButton == button ? null : button;

                    this.build();
                    return true;
                };

                if (this.selectedButton != button)
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
                }
                else
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-on-item"));
                }

                glow = this.selectedButton == button;
            }

            case PERMISSIONS -> {
                if (this.magicStick.getPermissions().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "title"));

                    this.magicStick.getPermissions().forEach(permission ->
                        description.add(this.user.getTranslation(reference + "value",
                            "[permission]", permission)));
                }

                icon = new ItemStack(Material.REDSTONE_LAMP);

                clickHandler = (panel, user, clickType, i) ->
                {
                    this.selectedButton = null;

                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.magicStick.setPermissions(new HashSet<>(value));
                        }

                        this.build();
                    };

                    if (!this.magicStick.getPermissions().isEmpty() &&
                        clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept(Collections.emptyList());
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringListInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-permissions"),
                            user.getTranslation(Constants.CONVERSATIONS + "permissions-changed"));
                    }

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                if (!this.magicStick.getPermissions().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }
            }
            case COMPLEXITY -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.magicStick.getComplexity())));

                icon = new ItemStack(Material.REPEATER);
                clickHandler = (panel, user, clickType, i) -> {
                    this.selectedButton = null;

                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.magicStick.setComplexity(number.floatValue());
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        1);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case BOOK -> {
                icon = new ItemStack(Material.WRITTEN_BOOK);

                if (this.magicStick.getBookName().isBlank())
                {
                    description.add(this.user.getTranslation(reference + "no-value"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "value",
                        "[book]", Util.translateColorCodes(this.magicStick.getBookName())));
                }

                clickHandler = (panel, user, clickType, i) ->
                {
                    this.selectedButton = null;

                    // Create consumer that process description change
                    Consumer<String> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.magicStick.setBookName(value);
                        }

                        this.build();
                    };

                    if (!this.magicStick.getBookName().isEmpty() && clickType.isShiftClick())
                    {
                        // Reset to the empty value
                        consumer.accept("");
                    }
                    else
                    {
                        // start conversation
                        ConversationUtils.createStringInput(consumer,
                            user,
                            user.getTranslation(Constants.CONVERSATIONS + "write-book-name"),
                            user.getTranslation(Constants.CONVERSATIONS + "book-name-changed"));
                    }


                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

                if (!this.magicStick.getBookName().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }
            }

            case ADD_RECIPE -> {
                icon = new ItemStack(Material.WATER_BUCKET);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.selectedButton = null;

                    RecipeTypeSelector.open(this.user,
                        type -> {
                            this.magicStick.getRecipeList().add(type);
                            EditRecipePanel.open(this, this.magicStick, type);
                        });

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-add"));
            }
            case REMOVE_RECIPE -> {
                icon = new ItemStack(Material.LAVA_BUCKET);
                clickHandler = (panel, user, clickType, slot) -> {
                    this.selectedButton = null;

                    // Generate descriptions for these challenges
                    Map<Recipe, String> elementDescriptionMap = this.magicStick.getRecipeList().stream().
                        collect(Collectors.toMap(recipe -> recipe,
                            recipe -> this.generateRecipeDescription(recipe, null),
                            (a, b) -> b,
                            () -> new LinkedHashMap<>(this.magicStick.getRecipeList().size())));

                    // Open select gui
                    RecipeSelector.open(this.user,
                        Material.RED_STAINED_GLASS_PANE,
                        elementDescriptionMap,
                        (status, valueSet) -> {
                            if (status)
                            {
                                this.magicStick.getRecipeList().removeAll(valueSet);
                            }

                            this.build();
                        });

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));
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
    // Section: Classes
    // ---------------------------------------------------------------------


    /**
     * This class allows changing icon for Generator Tier
     */
    private class IconChanger implements PanelListener
    {
        /**
         * Process inventory click. If generator icon is selected and user clicks on item in his inventory, then change
         * icon to the item from inventory.
         *
         * @param user the user
         * @param event the event
         */
        @Override
        public void onInventoryClick(User user, InventoryClickEvent event)
        {
            // Handle icon changing
            if (EditMagicStickPanel.this.selectedButton == Button.STICK &&
                event.getCurrentItem() != null &&
                !event.getCurrentItem().getType().equals(Material.AIR) &&
                event.getRawSlot() > 44)
            {
                ItemStack itemStack = event.getCurrentItem().clone();
                itemStack.setAmount(1);

                // set material and amount only. Other data should be removed.
                EditMagicStickPanel.this.magicStick.setMagicStick(itemStack);
                // Deselect icon
                EditMagicStickPanel.this.selectedButton = null;
                // Update name and description
                ItemMeta itemMeta = EditMagicStickPanel.this.magicStick.getMagicStick().getItemMeta();

                if (itemMeta != null)
                {
                    // Set new display name
                    itemMeta.setDisplayName(Util.translateColorCodes(
                        EditMagicStickPanel.this.magicStick.getFriendlyName()));
                    itemMeta.setLore(Arrays.stream(
                        Util.translateColorCodes(EditMagicStickPanel.this.magicStick.getDescription()).
                            split("\n")).
                        toList());
                    // Update meta.
                    EditMagicStickPanel.this.magicStick.getMagicStick().setItemMeta(itemMeta);
                }
                // Rebuild icon
                EditMagicStickPanel.this.build();
            }
        }


        /**
         * On inventory close.
         *
         * @param event the event
         */
        @Override
        public void onInventoryClose(InventoryCloseEvent event)
        {
            // Do nothing
        }


        /**
         * Setup current listener.
         */
        @Override
        public void setup()
        {
            // Do nothing
        }
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * Represents different buttons that could be in menus.
     */
    private enum Button
    {
        NAME,
        STICK,

        DESCRIPTION,
        BOOK,

        PERMISSIONS,
        COMPLEXITY,

        ADD_RECIPE,
        REMOVE_RECIPE
    }


    /**
     * Represents different types of menus
     */
    private enum MenuType
    {
        PROPERTIES,
        RECIPES
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable holds current challenge level that is in editing GUI.
     */
    private final MagicStickObject magicStick;

    /**
     * Variable holds current active menu.
     */
    private MenuType currentMenuType;

    /**
     * Selected button that allows to detect icon changing.
     * Lazy way :D
     */
    private Button selectedButton;
}