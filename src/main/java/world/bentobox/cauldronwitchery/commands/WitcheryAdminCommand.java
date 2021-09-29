package world.bentobox.cauldronwitchery.commands;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.panels.admin.AdminPanel;


public class WitcheryAdminCommand extends CompositeCommand
{

    /**
     * Admin command for challenges
     *
     * @param parent
     */
    public WitcheryAdminCommand(CauldronWitcheryAddon addon, CompositeCommand parent)
    {
        super(addon,
            parent,
            addon.getSettings().getAdminMainCommand().split(" ")[0],
            addon.getSettings().getAdminMainCommand().split(" "));
    }


    @Override
    public void setup()
    {
        this.setPermission("admin.witchery");
        this.setParametersHelp("witchery.commands.admin.main.parameters");
        this.setDescription("witchery.commands.admin.main.description");
    }


    @Override
    public boolean execute(User user, String label, List<String> args)
    {
        // Open up the admin challenges GUI
        if (user.isPlayer())
        {
            AdminPanel.open(this.getAddon(),
                this.getWorld(),
                user,
                this.getTopLabel(),
                this.getPermissionPrefix());

            return true;
        }
        return false;
    }
}
