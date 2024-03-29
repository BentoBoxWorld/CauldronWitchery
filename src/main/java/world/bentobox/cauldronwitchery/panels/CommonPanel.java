//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.panels;


import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.EntityRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.ItemRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.managers.CauldronWitcheryManager;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


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


    /**
     * This method generates book recipe description.
     * @param recipe Book Recipe.
     * @param target User who will view recipe.
     * @return String that contains info about recipe.
     */
    protected String generateRecipeDescription(BookRecipe recipe, User target)
    {
        final String reference = Constants.DESCRIPTIONS + "recipe.";

        String bookName = !recipe.getBookName().isBlank() ?
            "" :
            this.user.getTranslation(reference + "book.no-book");

        String cauldron = this.generateCauldronText(recipe);
        String knowledge = this.generateKnowledgeText(recipe);
        String mainIngredient = this.generateMainIngredientText(recipe);
        String extraItems = this.generateExtraIngredientText(recipe);
        String temperature = this.generateTemperatureText(recipe);
        String permissions = this.generatePermissionText(recipe, target);

        String returnString = this.user.getTranslationOrNothing(reference + "book.lore",
            "[no-book]", bookName,
            "[cauldron]", cauldron,
            "[knowledge]", knowledge,
            "[ingredient]", mainIngredient,
            "[extra]", extraItems,
            "[temperature]", temperature,
            "[permissions]", permissions);

        // Remove empty lines and returns as a list.

        return returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "");
    }


    /**
     * This method generates item recipe description.
     * @param recipe Book Recipe.
     * @param target User who will view recipe.
     * @return String that contains info about recipe.
     */
    protected String generateRecipeDescription(ItemRecipe recipe, User target)
    {
        final String reference = Constants.DESCRIPTIONS + "recipe.";

        String itemName = recipe.getItemStack() != null ? "" :
            this.user.getTranslation(reference + "item.no-item");

        String cauldron = this.generateCauldronText(recipe);
        String knowledge = this.generateKnowledgeText(recipe);
        String mainIngredient = this.generateMainIngredientText(recipe);
        String extraItems = this.generateExtraIngredientText(recipe);
        String temperature = this.generateTemperatureText(recipe);
        String permissions = this.generatePermissionText(recipe, target);

        String returnString = this.user.getTranslationOrNothing(reference + "item.lore",
            "[no-item]", itemName,
            "[cauldron]", cauldron,
            "[knowledge]", knowledge,
            "[ingredient]", mainIngredient,
            "[extra]", extraItems,
            "[temperature]", temperature,
            "[permissions]", permissions);

        // Remove empty lines and returns as a list.

        return returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "");
    }


    /**
     * This method generates entity recipe description.
     * @param recipe Book Recipe.
     * @param target User who will view recipe.
     * @return String that contains info about recipe.
     */
    protected String generateRecipeDescription(EntityRecipe recipe, User target)
    {
        final String reference = Constants.DESCRIPTIONS + "recipe.";

        String entityName = recipe.getEntityType() != null ?
            "" :
            this.user.getTranslation(reference + "entity.no-entity");

        String cauldron = this.generateCauldronText(recipe);
        String knowledge = this.generateKnowledgeText(recipe);
        String mainIngredient = this.generateMainIngredientText(recipe);
        String extraItems = this.generateExtraIngredientText(recipe);
        String temperature = this.generateTemperatureText(recipe);
        String permissions = this.generatePermissionText(recipe, target);

        String returnString = this.user.getTranslationOrNothing(reference + "entity.lore",
            "[no-entity]", entityName,
            "[cauldron]", cauldron,
            "[knowledge]", knowledge,
            "[ingredient]", mainIngredient,
            "[extra]", extraItems,
            "[temperature]", temperature,
            "[permissions]", permissions);

        // Remove empty lines and returns as a list.

        return returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "");
    }


    /**
     * This method generates recipe description.
     * @param recipe Recipe.
     * @param target User who will view recipe.
     * @return String that contains info about recipe.
     */
    protected String generateRecipeDescription(Recipe recipe, User target)
    {
        if (recipe instanceof BookRecipe bookRecipe)
        {
            return this.generateRecipeDescription(bookRecipe, target);
        }
        else if (recipe instanceof ItemRecipe itemRecipe)
        {
            return this.generateRecipeDescription(itemRecipe, target);
        }
        else if (recipe instanceof EntityRecipe entityRecipe)
        {
            return this.generateRecipeDescription(entityRecipe, target);
        }

        return "";
    }


    /**
     * Generate stick description string.
     *
     * @param magicStick the magic stick
     * @param target the target
     * @return the string
     */
    protected String generateStickDescription(MagicStickObject magicStick, User target)
    {
        final String reference = Constants.DESCRIPTIONS + "stick.";

        String costString;

        if (this.addon.isEconomyProvided())
        {
            costString = this.user.getTranslationOrNothing(reference + "cost",
                Constants.PARAMETER_NUMBER, String.valueOf(magicStick.getPurchaseCost()));
        }
        else
        {
            costString = "";
        }

        String permissions;

        if (!magicStick.getPermissions().isEmpty())
        {
            // Yes list duplication for complete menu.
            List<String> missingPermissions = magicStick.getPermissions().stream().
                filter(permission -> target == null || !target.hasPermission(permission)).
                sorted().
                collect(Collectors.toList());

            StringBuilder permissionBuilder = new StringBuilder();

            if (missingPermissions.size() == 1)
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permission-single",
                    Constants.PARAMETER_PERMISSION, missingPermissions.get(0)));
            }
            else if (!missingPermissions.isEmpty())
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permissions-title"));
                missingPermissions.forEach(permission ->
                {
                    permissionBuilder.append("\n");
                    permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permissions-list",
                        Constants.PARAMETER_PERMISSION, permission));
                });
            }

            permissions = permissionBuilder.toString();
        }
        else
        {
            permissions = "";
        }

        String returnString = this.user.getTranslation(reference + "lore",
            "[description]", magicStick.getDescription(),
            "[cost]", costString,
            "[permissions]", permissions);

        return returnString.replaceAll("(?m)^[ \\t]*\\r?\\n", "");
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
// Section: Private methods
// ---------------------------------------------------------------------

    /**
     * This method generates cauldron text.
     * @param recipe Recipe that is generated.
     * @return Cauldron message.
     */
    private String generateCauldronText(Recipe recipe)
    {
        final String reference = Constants.DESCRIPTIONS + "recipe.";

        int level = switch (recipe.getCauldronType()) {
            case LAVA_CAULDRON -> 3;
            case CAULDRON -> 0;
            default -> recipe.getCauldronLevel();
        };

        return this.user.getTranslation(reference + "cauldron-level-" + level,
            "[cauldron]", Utils.prettifyObject(recipe.getCauldronType(), this.user));
    }


    /**
     * This method generates knowledge text.
     * @param recipe Recipe that is generated.
     * @return Knowledge message.
     */
    private String generateKnowledgeText(Recipe recipe)
    {
        final String reference = Constants.DESCRIPTIONS + "recipe.";

        return recipe.getExperience() == 0 ? "" :
            this.user.getTranslation(reference + "knowledge",
                "[number]", String.valueOf(recipe.getExperience()));
    }


    /**
     * This method generates main ingredient text.
     * @param recipe Recipe that is generated.
     * @return Main ingredient message.
     */
    private String generateMainIngredientText(Recipe recipe)
    {
        final String reference = Constants.DESCRIPTIONS + "recipe.";

        return this.user.getTranslation(reference + "main-ingredient",
            "[count]", String.valueOf(recipe.getMainIngredient().getAmount()),
            "[item]", Utils.prettifyObject(recipe.getMainIngredient(), this.user));
    }


    /**
     * This method generates extra ingredient text.
     * @param recipe Recipe that is generated.
     * @return Extra ingredient message.
     */
    private String generateExtraIngredientText(Recipe recipe)
    {
        final String reference = Constants.DESCRIPTIONS + "recipe.";

        StringBuilder extraItems = new StringBuilder();

        if (recipe.getExtraIngredients().isEmpty())
        {
            extraItems.append(this.user.getTranslation(reference + "no-extra"));
        }
        else
        {
            extraItems.append(this.user.getTranslation(reference + "extra-title"));

            recipe.getExtraIngredients().forEach(item -> {
                extraItems.append("\n");
                extraItems.append(this.user.getTranslation(reference + "extra-element",
                    "[count]", String.valueOf(item.getAmount()),
                    "[item]", Utils.prettifyObject(item, this.user)));
            });
        }

        return extraItems.toString();
    }


    /**
     * This method generates temperature text.
     * @param recipe Recipe that is generated.
     * @return Temperature message.
     */
    private String generateTemperatureText(Recipe recipe)
    {
        final String reference = Constants.DESCRIPTIONS + "recipe.";

        return this.user.getTranslation(reference + "temperature-" +
            recipe.getTemperature().name().toLowerCase());
    }


    /**
     * This method generates permission text.
     * @param recipe Recipe that is generated.
     * @param target User who permission are checked.
     * @return Permission message.
     */
    private String generatePermissionText(Recipe recipe, User target)
    {
        final String reference = Constants.DESCRIPTIONS + "recipe.";

        String permissions;

        if (!recipe.getPermissions().isEmpty())
        {
            // Yes list duplication for complete menu.
            List<String> missingPermissions = recipe.getPermissions().stream().
                filter(permission -> target == null || !target.hasPermission(permission)).
                sorted().
                toList();

            StringBuilder permissionBuilder = new StringBuilder();

            if (missingPermissions.size() == 1)
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permission-single",
                    Constants.PARAMETER_PERMISSION, missingPermissions.get(0)));
            }
            else if (!missingPermissions.isEmpty())
            {
                permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permissions-title"));
                missingPermissions.forEach(permission ->
                {
                    permissionBuilder.append("\n");
                    permissionBuilder.append(this.user.getTranslationOrNothing(reference + "permissions-list",
                        Constants.PARAMETER_PERMISSION, permission));
                });
            }

            permissions = permissionBuilder.toString();
        }
        else
        {
            permissions = "";
        }

        return permissions;
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
