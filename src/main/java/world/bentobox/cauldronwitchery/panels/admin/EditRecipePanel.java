package world.bentobox.cauldronwitchery.panels.admin;


import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.PanelListener;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.EntityRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.ItemRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.panels.CommonPanel;
import world.bentobox.cauldronwitchery.panels.ConversationUtils;
import world.bentobox.cauldronwitchery.panels.utils.ItemSelector;
import world.bentobox.cauldronwitchery.panels.utils.SingleBlockSelector;
import world.bentobox.cauldronwitchery.panels.utils.SingleEntitySelector;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * This class contains all necessary elements to create Levels Edit GUI.
 */
public class EditRecipePanel extends CommonPanel
{
    // ---------------------------------------------------------------------
    // Section: Constructors
    // ---------------------------------------------------------------------


    /**
     * @param recipe ChallengeLevel that must be edited.
     */
    private EditRecipePanel(CommonPanel parentGUI, MagicStickObject magicStick, Recipe recipe)
    {
        super(parentGUI);
        this.recipe = recipe;
        this.magicStick = magicStick;
    }


    /**
     * Open the Challenges Level Edit GUI.
     *
     * @param panel - Parent Panel
     * @param recipe - recipe that needs editing
     */
    public static void open(CommonPanel panel, MagicStickObject magicStick, Recipe recipe)
    {
        new EditRecipePanel(panel, magicStick, recipe).build();
    }


    // ---------------------------------------------------------------------
    // Section: Methods
    // ---------------------------------------------------------------------


    /**
     * This method builds all necessary elements in GUI panel.
     */
    @Override
    protected void build()
    {
        PanelBuilder panelBuilder = new PanelBuilder().user(this.user).name(
            this.user.getTranslation(Constants.TITLE + "edit-recipe"));

        PanelUtils.fillBorder(panelBuilder);
        panelBuilder.listener(new IconChanger());

        if (this.recipe instanceof BookRecipe bookRecipe)
        {
            panelBuilder.item(10, this.createElementButton(bookRecipe));
        }
        else if (this.recipe instanceof ItemRecipe itemRecipe)
        {
            panelBuilder.item(10, this.createElementButton(itemRecipe));
        }
        else if (this.recipe instanceof EntityRecipe entityRecipe)
        {
            panelBuilder.item(10, this.createElementButton(entityRecipe));
        }

        panelBuilder.item(19, this.createButton(Button.ORDER));
        panelBuilder.item(28, this.createButton(Button.NAME));


        // Main Ingredients.
        panelBuilder.item(12, this.createButton(Button.MAIN_INGREDIENT));
        panelBuilder.item(21, this.createButton(Button.EXTRA_INGREDIENTS));
        panelBuilder.item(30, this.createButton(Button.EXPERIENCE));

        // Cauldron settings.
        panelBuilder.item(14, this.createButton(Button.CAULDRON_TYPE));

        if (this.recipe.getCauldronType() == Material.WATER_CAULDRON ||
            this.recipe.getCauldronType() == Material.POWDER_SNOW_CAULDRON)
        {
            panelBuilder.item(23, this.createButton(Button.CAULDRON_LEVEL));
        }

        panelBuilder.item(32, this.createButton(Button.TEMPERATURE));

        // Non implemented things
        //panelBuilder.item(15, this.createButton(Button.COMPLEXITY));
        //panelBuilder.item(24, this.createButton(Button.OUTCOME_POINTS));


        // Permissions.
        panelBuilder.item(34, this.createButton(Button.PERMISSIONS));

        panelBuilder.item(44, this.returnButton);

        // Save challenge level every time this gui is build.
        // It will ensure that changes are stored in database.
        this.addon.getAddonManager().saveMagicStick(this.magicStick);

        panelBuilder.build();
    }


    // ---------------------------------------------------------------------
    // Section: Other methods
    // ---------------------------------------------------------------------


    /**
     * Create BookRecipe button.
     * @param recipe Book Recipe.
     * @return BookRecipe button.
     */
    private PanelItem createElementButton(BookRecipe recipe)
    {
        List<String> description = new ArrayList<>();
        description.add(this.generateRecipeDescription(recipe, null));
        description.add("");

        description.add(this.user.getTranslation(Constants.TIPS + "click-to-edit"));

        if (!recipe.getBookName().isEmpty())
        {
            description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-clear"));
        }

        return new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "recipe.name",
                "[value]", recipe.getRecipeName(this.user))).
            description(description).
            icon(recipe.getIcon()).
            clickHandler((panel, user, clickType, slot) -> {
                // Open challenges edit screen.

                if (clickType.isShiftClick())
                {
                    // Clear string.
                    recipe.setBookName("");
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
                            recipe.setBookName(value);
                        }

                        this.build();
                    };

                    // start conversation
                    ConversationUtils.createStringInput(consumer,
                        user,
                        user.getTranslation(Constants.CONVERSATIONS + "write-book-name"),
                        user.getTranslation(Constants.CONVERSATIONS + "book-name-changed"));
                }

                return true;
            }).
            build();
    }


    /**
     * Create ItemRecipe button.
     * @param recipe Item Recipe.
     * @return ItemRecipe button.
     */
    private PanelItem createElementButton(ItemRecipe recipe)
    {
        return new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "recipe.name",
                "[value]", recipe.getRecipeName(this.user))).
            description(this.generateRecipeDescription(recipe, null)).
            description("").
            description(this.selectedButton != Button.REWARD_ITEM ?
                this.user.getTranslation(Constants.TIPS + "click-to-change") :
                this.user.getTranslation(Constants.TIPS + "click-on-item")).
            icon(recipe.getIcon()).
            clickHandler((panel, user, clickType, slot) -> {
                // Selects and deselects item
                this.selectedButton = this.selectedButton == Button.REWARD_ITEM ? null : Button.REWARD_ITEM;
                this.build();

                return true;
            }).
            glow(this.selectedButton == Button.REWARD_ITEM).
            build();
    }


    /**
     * Create EntityRecipe button.
     * @param recipe Entity Recipe.
     * @return EntityRecipe button.
     */
    private PanelItem createElementButton(EntityRecipe recipe)
    {
        return new PanelItemBuilder().
            name(this.user.getTranslation(Constants.BUTTON + "recipe.name",
                "[value]", recipe.getRecipeName(this.user))).
            description(this.generateRecipeDescription(recipe, null)).
            description("").
            description(this.user.getTranslation(Constants.TIPS + "click-to-edit")).
            icon(recipe.getIcon()).
            clickHandler((panel, user, clickType, slot) -> {
                // Open challenges edit screen.
                SingleEntitySelector.open(this.user,
                    false,
                    SingleEntitySelector.Mode.ALIVE,
                    (status, entity) -> {
                        if (status)
                        {
                            recipe.setEntityType(entity);
                        }

                        this.build();
                    });

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
            case REWARD_POINTS -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.recipe.getRewardPoints())));

                icon = new ItemStack(Material.REPEATER);
                clickHandler = (panel, user, clickType, i) -> {
                    // deselects item
                    this.selectedButton = null;

                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.recipe.setRewardPoints(number.intValue());
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Integer.MAX_VALUE);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case COMPLEXITY -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.recipe.getComplexity())));

                icon = new ItemStack(Material.REPEATER);
                clickHandler = (panel, user, clickType, i) -> {
                    // deselects item
                    this.selectedButton = null;

                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.recipe.setComplexity(number.floatValue());
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
            case MAIN_INGREDIENT -> {
                icon = this.recipe.getMainIngredient();

                clickHandler = (panel, user, clickType, i) ->
                {
                    // selects / deselects the button.
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
            case EXTRA_INGREDIENTS -> {
                if (this.recipe.getExtraIngredients().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "title"));

                    Utils.groupEqualItems(this.recipe.getExtraIngredients()).
                        stream().
                        sorted(Comparator.comparing(ItemStack::getType)).
                        forEach(itemStack ->
                            description.add(this.user.getTranslationOrNothing(reference + "value",
                                "[item]", Utils.prettifyObject(itemStack, this.user))));
                }

                icon = new ItemStack(Material.CHEST);
                clickHandler = (panel, user, clickType, slot) -> {
                    // deselects item
                    this.selectedButton = null;

                    ItemSelector.open(this.user,
                        this.recipe.getExtraIngredients(),
                        (status, value) -> {
                            if (status)
                            {
                                this.recipe.setExtraIngredients(value);
                            }

                            this.build();
                        });
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case EXPERIENCE -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.recipe.getExperience())));

                icon = new ItemStack(Material.EXPERIENCE_BOTTLE);
                clickHandler = (panel, user, clickType, i) -> {
                    // deselects item
                    this.selectedButton = null;

                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.recipe.setExperience(number.intValue());
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        Integer.MAX_VALUE);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case CAULDRON_TYPE -> {
                description.add(this.user.getTranslation(reference + "value",
                    "[type]", Utils.prettifyObject(this.recipe.getCauldronType(), this.user)));
                icon = new ItemStack(Material.CAULDRON);
                clickHandler = (panel, user, clickType, slot) -> {
                    // deselects item
                    this.selectedButton = null;

                    Set<Material> validBlocks = new HashSet<>(Arrays.stream(Material.values()).toList());
                    validBlocks.remove(Material.CAULDRON);
                    validBlocks.remove(Material.WATER_CAULDRON);
                    validBlocks.remove(Material.LAVA_CAULDRON);
                    validBlocks.remove(Material.POWDER_SNOW_CAULDRON);
                    validBlocks.add(this.recipe.getCauldronType());

                    SingleBlockSelector.open(this.user,
                        SingleBlockSelector.Mode.BLOCKS,
                        validBlocks,
                        (status, block) -> {
                            if (status)
                            {
                                this.recipe.setCauldronType(block);
                            }

                            this.build();
                        });

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case CAULDRON_LEVEL -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.recipe.getCauldronLevel())));

                icon = new ItemStack(Material.LEAD);
                clickHandler = (panel, user, clickType, i) -> {
                    // deselects item
                    this.selectedButton = null;

                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.recipe.setCauldronLevel(number.intValue());
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        3);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case TEMPERATURE -> {
                description.add(this.user.getTranslation(reference +
                    this.recipe.getTemperature().toString().toLowerCase()));

                if (this.recipe.getTemperature() == Recipe.Temperature.COOL)
                {
                    icon = new ItemStack(Material.ICE);
                }
                else if (this.recipe.getTemperature() == Recipe.Temperature.HEAT)
                {
                    icon = new ItemStack(Material.CAMPFIRE);
                }
                else
                {
                    icon = new ItemStack(Material.DIRT);
                }

                clickHandler = (panel, user, clickType, slot) -> {
                    // deselects item
                    this.selectedButton = null;

                    if (clickType.isRightClick())
                    {
                        this.recipe.setTemperature((Utils.getPreviousValue(Recipe.Temperature.values(),
                            this.recipe.getTemperature())));
                    }
                    else
                    {
                        this.recipe.setTemperature((Utils.getNextValue(Recipe.Temperature.values(),
                            this.recipe.getTemperature())));
                    }

                    // Rebuild just this icon
                    panel.getInventory().setItem(slot, this.createButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-cycle"));
                description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-cycle"));
            }
            case PERMISSIONS -> {
                if (this.recipe.getPermissions().isEmpty())
                {
                    description.add(this.user.getTranslation(reference + "none"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "title"));

                    this.recipe.getPermissions().forEach(permission ->
                        description.add(this.user.getTranslation(reference + "value",
                            "[permission]", permission)));
                }

                icon = new ItemStack(Material.REDSTONE_LAMP);

                clickHandler = (panel, user, clickType, i) ->
                {
                    // deselects item
                    this.selectedButton = null;

                    // Create consumer that process description change
                    Consumer<List<String>> consumer = value ->
                    {
                        if (value != null)
                        {
                            this.recipe.setPermissions(new HashSet<>(value));
                        }

                        this.build();
                    };

                    if (!this.recipe.getPermissions().isEmpty() &&
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

                if (!this.recipe.getPermissions().isEmpty())
                {
                    description.add(this.user.getTranslation(Constants.TIPS + "shift-click-to-reset"));
                }
            }
            case ORDER -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.recipe.getOrder())));

                icon = new ItemStack(Material.HOPPER);
                clickHandler = (panel, user, clickType, i) -> {
                    this.selectedButton = null;

                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.recipe.setOrder(number.intValue());
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case NAME -> {
                if (this.recipe.getRecipeDisplayName().isBlank())
                {
                    description.add(this.user.getTranslation(reference + "no-value"));
                }
                else
                {
                    description.add(this.user.getTranslation(reference + "value",
                        Constants.PARAMETER_NAME, this.recipe.getRecipeDisplayName()));
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
                            this.recipe.setRecipeDisplayName(value);
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
            if (EditRecipePanel.this.selectedButton != null &&
                event.getCurrentItem() != null &&
                !event.getCurrentItem().getType().equals(Material.AIR) &&
                event.getRawSlot() > 44)
            {
                if (EditRecipePanel.this.selectedButton == Button.MAIN_INGREDIENT)
                {
                    // set material and amount only. Other data should be removed.
                    EditRecipePanel.this.recipe.setMainIngredient(event.getCurrentItem().clone());
                    // Deselect icon
                    EditRecipePanel.this.selectedButton = null;
                    // Rebuild icon
                    EditRecipePanel.this.build();
                }
                else if (EditRecipePanel.this.selectedButton == Button.REWARD_ITEM &&
                    EditRecipePanel.this.recipe instanceof ItemRecipe itemRecipe)
                {
                    // set material and amount only. Other data should be removed.
                    itemRecipe.setItemStack(event.getCurrentItem().clone());
                    // Deselect icon
                    EditRecipePanel.this.selectedButton = null;
                    // Rebuild icon
                    EditRecipePanel.this.build();
                }
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
        REWARD_POINTS,
        COMPLEXITY,

        MAIN_INGREDIENT,
        EXTRA_INGREDIENTS,

        EXPERIENCE,

        CAULDRON_TYPE,
        CAULDRON_LEVEL,
        TEMPERATURE,

        PERMISSIONS,

        REWARD_ITEM,

        ORDER,
        NAME
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable holds current challenge level that is in editing GUI.
     */
    private final Recipe recipe;

    /**
     * This variable holds current challenge level that is in editing GUI.
     */
    private final MagicStickObject magicStick;

    /**
     * Selected button that allows to detect icon changing.
     * Lazy way :D
     */
    private Button selectedButton;
}