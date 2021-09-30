//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.cauldronwitchery.panels.admin;


import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.cauldronwitchery.configs.Settings;
import world.bentobox.cauldronwitchery.panels.CommonPanel;
import world.bentobox.cauldronwitchery.panels.ConversationUtils;
import world.bentobox.cauldronwitchery.utils.Constants;


/**
 * This class manages settings for Magic Cobblestone Generator Addon.
 */
public class SettingsPanel extends CommonPanel
{
    /**
     * This is default constructor for all classes that extends CommonPanel.
     *
     * @param parentPanel Parent panel of current panel.
     */
    protected SettingsPanel(CommonPanel parentPanel)
    {
        super(parentPanel);
        this.settings = this.addon.getSettings();
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
            name(this.user.getTranslation(Constants.TITLE + "settings"));

        PanelUtils.fillBorder(panelBuilder, 5, Material.MAGENTA_STAINED_GLASS_PANE);

        panelBuilder.item(10, this.createButton(Action.SUCCESS_DAMAGE));
        panelBuilder.item(19, this.createButton(Action.FAIL_DAMAGE));

        panelBuilder.item(30, this.createButton(Action.FAIL_DESTROY_CAULDRON));
        panelBuilder.item(31, this.createButton(Action.CORRECT_FAIL_MESSAGE));

        panelBuilder.item(12, this.createButton(Action.MIX_IN_CAULDRON));

        if (this.settings.isMixInCauldron())
        {
            panelBuilder.item(13, this.createButton(Action.EXACT_COUNT));

            if (!this.settings.isExactExtraCount())
            {
                panelBuilder.item(14, this.createButton(Action.REMOVE_LEFT_OVERS));
            }

            panelBuilder.item(15, this.createButton(Action.REMOVE_ON_FAIL));
        }

        panelBuilder.item(44, this.returnButton);
        panelBuilder.build();
    }


    /**
     * Create button panel item with a given button type.
     *
     * @param button the button
     * @return the panel item
     */
    private PanelItem createButton(Action button)
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
            case SUCCESS_DAMAGE -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getSuccessDamageAmount())));

                icon = new ItemStack(Material.STICK, Math.max(1, this.settings.getSuccessDamageAmount()));
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.settings.setSuccessDamageAmount(number.intValue());
                            this.addon.saveSettings();
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        2000);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case FAIL_DAMAGE -> {
                description.add(this.user.getTranslation(reference + "value",
                    Constants.PARAMETER_NUMBER, String.valueOf(this.settings.getErrorDamageAmount())));

                icon = new ItemStack(Material.BLAZE_ROD, Math.max(1, this.settings.getErrorDamageAmount()));
                clickHandler = (panel, user, clickType, i) -> {
                    Consumer<Number> numberConsumer = number -> {
                        if (number != null)
                        {
                            this.settings.setErrorDamageAmount(number.intValue());
                            this.addon.saveSettings();
                        }

                        // reopen panel
                        this.build();
                    };

                    ConversationUtils.createNumericInput(numberConsumer,
                        this.user,
                        this.user.getTranslation(Constants.CONVERSATIONS + "input-number"),
                        0,
                        2000);

                    return true;
                };
                glow = false;

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));
            }
            case MIX_IN_CAULDRON -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isMixInCauldron() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.CAULDRON);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setMixInCauldron(!this.settings.isMixInCauldron());
                    // More buttons are affected by this button.
                    this.build();
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isMixInCauldron();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case EXACT_COUNT -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isMixInCauldron() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.COMPARATOR);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setExactExtraCount(!this.settings.isExactExtraCount());
                    // More buttons are affected by this button.
                    this.build();
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isExactExtraCount();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case REMOVE_LEFT_OVERS -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isRemoveLeftOvers() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.LAVA_BUCKET);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setRemoveLeftOvers(!this.settings.isRemoveLeftOvers());
                    panel.getInventory().setItem(i, this.createButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isRemoveLeftOvers();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case REMOVE_ON_FAIL -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isRemoveOnFail() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.CAMPFIRE);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setRemoveOnFail(!this.settings.isRemoveOnFail());
                    panel.getInventory().setItem(i, this.createButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isRemoveOnFail();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case FAIL_DESTROY_CAULDRON -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isErrorDestroyCauldron() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.TNT);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setErrorDestroyCauldron(!this.settings.isErrorDestroyCauldron());
                    panel.getInventory().setItem(i, this.createButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isErrorDestroyCauldron();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            case CORRECT_FAIL_MESSAGE -> {
                description.add(this.user.getTranslation(reference +
                    (this.settings.isCorrectErrorMessage() ? "enabled" : "disabled")));

                icon = new ItemStack(Material.MAP);
                clickHandler = (panel, user1, clickType, i) -> {
                    this.settings.setCorrectErrorMessage(!this.settings.isCorrectErrorMessage());
                    panel.getInventory().setItem(i, this.createButton(button).getItem());
                    this.addon.saveSettings();
                    return true;
                };
                glow = this.settings.isCorrectErrorMessage();

                description.add("");
                description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));
            }
            default -> {
                icon = new ItemStack(Material.PAPER);
                clickHandler = null;
                glow = false;
            }
        }

        return new PanelItemBuilder().
            name(name).
            description(description).
            icon(icon).
            clickHandler(clickHandler).
            glow(glow).
            build();
    }


    /**
     * This method saves current settings.
     */
    private void saveSettings()
    {
        new Config<>(this.addon, Settings.class).saveConfigObject(this.settings);
    }


    /**
     * This method build settings panel from parent panel.
     *
     * @param panel ParentPanel.
     */
    public static void open(CommonPanel panel)
    {
        new SettingsPanel(panel).build();
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * This enum holds all possible actions in current GUI.
     */
    private enum Action
    {
        SUCCESS_DAMAGE,
        FAIL_DAMAGE,

        MIX_IN_CAULDRON,
        EXACT_COUNT,
        REMOVE_LEFT_OVERS,
        REMOVE_ON_FAIL,

        FAIL_DESTROY_CAULDRON,
        CORRECT_FAIL_MESSAGE
    }

    /**
     * Settings object to allow to change settings.
     */
    private final Settings settings;
}
