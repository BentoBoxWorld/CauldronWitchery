//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.cauldronwitchery.panels.admin;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import lv.id.bonne.panelutils.PanelUtils;
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
    private static final String ENABLED = "enabled";
    private static final String DISABLED = "disabled";


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
        return switch (button)
        {
            case SUCCESS_DAMAGE -> this.createNumberButton(button, Material.STICK,
                this.settings.getSuccessDamageAmount(), this.settings::setSuccessDamageAmount);
            case FAIL_DAMAGE -> this.createNumberButton(button, Material.BLAZE_ROD,
                this.settings.getErrorDamageAmount(), this.settings::setErrorDamageAmount);
            // These toggles affect other buttons, so the whole panel is rebuilt.
            case MIX_IN_CAULDRON -> this.createToggleButton(button, Material.CAULDRON,
                this.settings.isMixInCauldron(), this.settings::setMixInCauldron, true);
            case EXACT_COUNT -> this.createToggleButton(button, Material.COMPARATOR,
                this.settings.isExactExtraCount(), this.settings::setExactExtraCount, true);
            case REMOVE_LEFT_OVERS -> this.createToggleButton(button, Material.LAVA_BUCKET,
                this.settings.isRemoveLeftOvers(), this.settings::setRemoveLeftOvers, false);
            case REMOVE_ON_FAIL -> this.createToggleButton(button, Material.CAMPFIRE,
                this.settings.isRemoveOnFail(), this.settings::setRemoveOnFail, false);
            case FAIL_DESTROY_CAULDRON -> this.createToggleButton(button, Material.TNT,
                this.settings.isErrorDestroyCauldron(), this.settings::setErrorDestroyCauldron, false);
            case CORRECT_FAIL_MESSAGE -> this.createToggleButton(button, Material.MAP,
                this.settings.isCorrectErrorMessage(), this.settings::setCorrectErrorMessage, false);
        };
    }


    /**
     * Creates a button that asks for a number in chat and stores it in settings.
     *
     * @param button Button type, used for translations.
     * @param material Icon material.
     * @param value Current value.
     * @param setter Settings setter for the new value.
     * @return the panel item
     */
    private PanelItem createNumberButton(Action button, Material material, int value, IntConsumer setter)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslation(reference + "description"));
        description.add(this.user.getTranslation(reference + "value",
            Constants.PARAMETER_NUMBER, String.valueOf(value)));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-change"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            Consumer<Number> numberConsumer = number -> {
                if (number != null)
                {
                    setter.accept(number.intValue());
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

        return new PanelItemBuilder().
            name(this.user.getTranslation(reference + "name")).
            description(description).
            icon(new ItemStack(material, Math.max(1, value))).
            clickHandler(clickHandler).
            glow(false).
            build();
    }


    /**
     * Creates a button that toggles a boolean setting.
     *
     * @param button Button type, used for translations.
     * @param material Icon material.
     * @param enabled Current value.
     * @param setter Settings setter for the new value.
     * @param rebuildPanel {@code true} if whole panel must be rebuilt, {@code false} to update only this button.
     * @return the panel item
     */
    private PanelItem createToggleButton(Action button,
        Material material,
        boolean enabled,
        Consumer<Boolean> setter,
        boolean rebuildPanel)
    {
        final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

        final List<String> description = new ArrayList<>(4);
        description.add(this.user.getTranslation(reference + "description"));
        description.add(this.user.getTranslation(reference + (enabled ? ENABLED : DISABLED)));
        description.add("");
        description.add(this.user.getTranslation(Constants.TIPS + "click-to-toggle"));

        PanelItem.ClickHandler clickHandler = (panel, user, clickType, i) -> {
            setter.accept(!enabled);

            if (rebuildPanel)
            {
                this.build();
            }
            else
            {
                panel.getInventory().setItem(i, this.createButton(button).getItem());
            }

            this.addon.saveSettings();
            return true;
        };

        return new PanelItemBuilder().
            name(this.user.getTranslation(reference + "name")).
            description(description).
            icon(new ItemStack(material)).
            clickHandler(clickHandler).
            glow(enabled).
            build();
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
