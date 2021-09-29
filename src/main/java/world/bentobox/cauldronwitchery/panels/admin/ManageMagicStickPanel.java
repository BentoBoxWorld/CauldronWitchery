package world.bentobox.cauldronwitchery.panels.admin;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.panels.CommonPagedPanel;
import world.bentobox.cauldronwitchery.panels.CommonPanel;
import world.bentobox.cauldronwitchery.panels.ConversationUtils;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * This class opens GUI that allows to manage all generators for admin.
 */
public class ManageMagicStickPanel extends CommonPagedPanel<MagicStickObject>
{
    // ---------------------------------------------------------------------
    // Section: Internal Constructor
    // ---------------------------------------------------------------------


    /**
     * This is internal constructor. It is used internally in current class to avoid creating objects everywhere.
     *
     * @param parentPanel Parent Panel object.
     */
    private ManageMagicStickPanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        // Store bundles in local list to avoid building it every time.
        this.magicStickList = this.manager.getAllMagicSticks(this.world);
        this.filterElements = this.magicStickList;

        // Init set with selected bundles.
        this.selectedMagicSticks = new HashSet<>(this.magicStickList.size());
    }


    /**
     * This method is used to open GeneratorManagePanel outside this class. It will be much easier to open panel with
     * single method call then initializing new object.
     *
     * @param parentPanel Parent Panel object.
     */
    public static void open(CommonPanel parentPanel)
    {
        new ManageMagicStickPanel(parentPanel).build();
    }


    /**
     * This method builds this GUI.
     */
    @Override
    public void build()
    {
        // PanelBuilder is a BentoBox API that provides ability to easy create Panels.
        PanelBuilder panelBuilder = new PanelBuilder().
            user(this.user).
            name(this.user.getTranslation(Constants.TITLE + "manage-magic-sticks"));

        PanelUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(1, this.createButton(Action.CREATE_MAGIC_STICK));
        panelBuilder.item(2, this.createButton(Action.DELETE_MAGIC_STICK));

        this.populateElements(panelBuilder, this.filterElements);

        panelBuilder.item(44, this.returnButton);

        // Build panel.
        panelBuilder.build();
    }


    @Override
    protected void updateFilters()
    {
        if (this.searchString == null || this.searchString.isBlank())
        {
            this.filterElements = this.magicStickList;
        }
        else
        {
            this.filterElements = this.magicStickList.stream().
                filter(element -> {
                    // If element name is set and name contains search field, then do not filter out.
                    return element.getMagicStick().getType().name().toLowerCase().
                        contains(this.searchString.toLowerCase()) ||
                        element.getMagicStick().getItemMeta() != null &&
                            element.getMagicStick().getItemMeta().getDisplayName().toLowerCase().
                                contains(this.searchString.toLowerCase());
                }).
                distinct().
                collect(Collectors.toList());
        }
    }


// ---------------------------------------------------------------------
// Section: Methods
// ---------------------------------------------------------------------


    /**
     * This method creates panel item for given button type.
     *
     * @param button Button type.
     * @return Clickable PanelItem button.
     */
    private PanelItem createButton(Action button)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase();
        String name = this.user.getTranslation(reference + ".name");
        List<String> description = new ArrayList<>();

        PanelItem.ClickHandler clickHandler;
        boolean glow = false;

        Material icon = Material.PAPER;
        int count = 1;

        switch (button)
        {
            case CREATE_MAGIC_STICK -> {
                description.add(this.user.getTranslationOrNothing(reference + ".description"));
                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-create"));

                icon = Material.WRITABLE_BOOK;
                clickHandler = (panel, user1, clickType, slot) ->
                {
                    String gameModePrefix = Utils.getGameMode(this.world).toLowerCase() + "_";

                    // This consumer process new bundle creating with a name and id from given
                    // consumer value.
                    Consumer<String> bundleIdConsumer = value ->
                    {
                        if (value != null)
                        {
                            MagicStickObject newObject = new MagicStickObject();
                            newObject.setFriendlyName(value);
                            newObject.setUniqueId(gameModePrefix + Utils.sanitizeInput(value));
                            // Add PAPER as new icon.
                            newObject.setMagicStick(new ItemStack(Material.STICK));
                            this.manager.saveMagicStick(newObject);
                            this.manager.loadMagicStick(newObject, false, this.user);

                            // Add new generator to generatorList.
                            this.magicStickList.add(newObject);
                            // Open bundle edit panel.
                            EditMagicStickPanel.open(this, newObject);
                        }
                        else
                        {
                            // Operation is canceled. Open this panel again.
                            this.build();
                        }
                    };

                    // This function checks if generator with a given ID already exist.
                    Function<String, Boolean> validationFunction = magicStick ->
                        this.manager.getMagicStickById(gameModePrefix + Utils.sanitizeInput(magicStick)) == null;

                    // Call a conversation API to get input string.
                    ConversationUtils.createIDStringInput(bundleIdConsumer,
                        validationFunction,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "write-name"),
                        this.user.getTranslation(Constants.CONVERSATIONS + "new-object-created",
                            Constants.PARAMETER_WORLD, world.getName()),
                        Constants.ERRORS + "object-already-exists");

                    return true;
                };
            }
            case DELETE_MAGIC_STICK -> {
                icon = this.selectedMagicSticks.isEmpty() ? Material.BARRIER : Material.LAVA_BUCKET;
                glow = !this.selectedMagicSticks.isEmpty();

                description.add(this.user.getTranslationOrNothing(reference + ".description"));

                if (!this.selectedMagicSticks.isEmpty())
                {
                    description.add(this.user.getTranslation(reference + ".list"));
                    this.selectedMagicSticks.forEach(bundle ->
                        description.add(this.user.getTranslation(reference + ".value",
                            "[stick]", bundle.getFriendlyName())));

                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "click-to-remove"));

                    clickHandler = (panel, user1, clickType, slot) ->
                    {

                        // Create consumer that accepts value from conversation.
                        Consumer<Boolean> consumer = value ->
                        {
                            if (value)
                            {
                                this.selectedMagicSticks.forEach(stickObject ->
                                {
                                    this.manager.delete(stickObject);
                                    this.magicStickList.remove(stickObject);
                                });

                                this.selectedMagicSticks.clear();
                            }

                            this.build();
                        };

                        String stickString;

                        if (!this.selectedMagicSticks.isEmpty())
                        {
                            Iterator<MagicStickObject> iterator = this.selectedMagicSticks.iterator();

                            StringBuilder builder = new StringBuilder();
                            builder.append(iterator.next().getFriendlyName());

                            while (iterator.hasNext())
                            {
                                builder.append(", ").append(iterator.next().getFriendlyName());
                            }

                            stickString = builder.toString();
                        }
                        else
                        {
                            stickString = "";
                        }

                        // Create conversation that gets user acceptance to delete selected generator data.
                        ConversationUtils.createConfirmation(
                            consumer,
                            this.user,
                            this.user.getTranslation(Constants.CONVERSATIONS + "confirm-deletion",
                                TextVariables.NUMBER, String.valueOf(this.selectedMagicSticks.size()),
                                "[value]", stickString),
                            this.user.getTranslation(Constants.CONVERSATIONS + "data-removed",
                                "[gamemode]", Utils.getGameMode(this.world)));


                        return true;
                    };
                }
                else
                {
                    description.add("");
                    description.add(this.user.getTranslation(Constants.TIPS + "select-before"));

                    // Do nothing as no generators are selected.
                    clickHandler = (panel, user1, clickType, slot) -> true;
                }
            }
            default -> clickHandler = (panel, user1, clickType, slot) -> true;
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(icon).
            amount(count).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method creates button for magic stick.
     *
     * @param magicStick magic stick which button must be created.
     * @return PanelItem for magic stick.
     */
    protected PanelItem createElementButton(MagicStickObject magicStick)
    {
        boolean glow = this.selectedMagicSticks.contains(magicStick);

        List<String> description = new ArrayList<>();
        description.add(magicStick.getDescription());

        if (this.selectedMagicSticks.contains(magicStick))
        {
            description.add(this.user.getTranslation(Constants.DESCRIPTIONS + "selected"));
        }

        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "left-click-to-edit"));

        if (this.selectedMagicSticks.contains(magicStick))
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-deselect"));
        }
        else
        {
            description.add(this.user.getTranslation(Constants.TIPS + "right-click-to-select"));
        }


        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            // Click handler should work only if user has a permission to change anything.
            // Otherwise just to view.

            if (clickType.isRightClick())
            {
                // Open edit panel.
                if (this.selectedMagicSticks.contains(magicStick))
                {
                    this.selectedMagicSticks.remove(magicStick);
                }
                else
                {
                    this.selectedMagicSticks.add(magicStick);
                }

                // Build necessary as multiple icons are changed.
                this.build();
            }
            else
            {
                EditMagicStickPanel.open(this, magicStick);
            }

            // Always return true.
            return true;
        };

        return new PanelItemBuilder().
            name(magicStick.getFriendlyName()).
            description(description).
            icon(magicStick.getMagicStick().clone()).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    // ---------------------------------------------------------------------
    // Section: Enums
    // ---------------------------------------------------------------------


    /**
     * This enum holds variable that allows to switch between button creation.
     */
    private enum Action
    {
        /**
         * Allows to add new stick.
         */
        CREATE_MAGIC_STICK,
        /**
         * Allows to delete selected stick.
         */
        DELETE_MAGIC_STICK
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable stores all sticks in the given world.
     */
    private final List<MagicStickObject> magicStickList;

    /**
     * This variable stores all sticks in the given world.
     */
    private List<MagicStickObject> filterElements;

    /**
     * This variable stores all selected sticks.
     */
    private final Set<MagicStickObject> selectedMagicSticks;
}
