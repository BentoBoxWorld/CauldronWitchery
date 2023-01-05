//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.commands;


import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.panels.user.MagicStickPanel;
import world.bentobox.cauldronwitchery.utils.Constants;


public class WitcheryPlayerCommand extends CompositeCommand
{
    /**
     * Player command for Witchery Addon
     *
     * @param parent Parent CMD
     */
    public WitcheryPlayerCommand(CauldronWitcheryAddon addon, CompositeCommand parent)
    {
        super(addon,
            parent,
            addon.getSettings().getPlayerMainCommand().split(" ")[0],
            addon.getSettings().getPlayerMainCommand().split(" "));
    }


    @Override
    public void setup()
    {
        this.setOnlyPlayer(true);
        this.setPermission("witchery");
        this.setParametersHelp(Constants.PLAYER_COMMANDS + "main.parameters");
        this.setDescription(Constants.PLAYER_COMMANDS + "main.description");
    }


    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        // Open up the admin challenges GUI
        if (user.isPlayer())
        {
            MagicStickPanel.open(this.getAddon(),
                this.getWorld(),
                user,
                this.getTopLabel(),
                this.getPermissionPrefix());

            return true;
        }
        return false;
    }
}
