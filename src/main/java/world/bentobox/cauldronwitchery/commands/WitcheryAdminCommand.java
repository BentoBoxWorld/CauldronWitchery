package world.bentobox.cauldronwitchery.commands;

import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Players;
import world.bentobox.bentobox.util.Util;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.panels.admin.AdminPanel;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * Main admin command.
 */
public class WitcheryAdminCommand extends CompositeCommand
{

    /**
     * Admin command for challenges
     *
     * @param parent Parent CMD
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
        this.setOnlyPlayer(true);
        this.setPermission("admin.witchery");
        this.setParametersHelp(Constants.ADMIN_COMMANDS + "main.parameters");
        this.setDescription(Constants.ADMIN_COMMANDS + "main.description");

        new GetCommand(this.getAddon(), this);
        new ReloadCommand(this.getAddon(), this);
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


// ---------------------------------------------------------------------
// Section: Sub commands
// ---------------------------------------------------------------------


    /**
     * Generic get wrapper. This command is just a wrapper around `get book` or `get stick`
     */
    public static class GetCommand extends CompositeCommand
    {
        public GetCommand(CauldronWitcheryAddon addon, CompositeCommand parent)
        {
            super(addon, parent, "get");
        }


        @Override
        public void setup()
        {
            this.setOnlyPlayer(true);
            this.setPermission("admin.witchery");

            new GenerateMagicStickCommand(this.getAddon(), this);
            new CraftBookCommand(this.getAddon(), this);
        }


        @Override
        public boolean execute(User user, String s, List<String> list)
        {
            this.showHelp(this, user);
            return true;
        }
    }


    /**
     * Command that generates magic stick.
     */
    public static class GenerateMagicStickCommand extends CompositeCommand
    {
        public GenerateMagicStickCommand(CauldronWitcheryAddon addon, CompositeCommand parent)
        {
            super(addon, parent, "stick");
        }


        @Override
        public void setup()
        {
            this.setOnlyPlayer(true);
            this.setPermission("admin.witchery");
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "give.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "give.description");
        }


        @Override
        public boolean execute(User user, String s, List<String> list)
        {
            if (list.size() != 1)
            {
                this.showHelp(this, user);
                return true;
            }

            String gamemode = Utils.getGameMode(this.getWorld());

            MagicStickObject object = this.<CauldronWitcheryAddon>getAddon().
                getAddonManager().getMagicStickById(gamemode.toLowerCase() + "_" + list.get(0));

            if (object == null)
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "no-magic-stick",
                    "[id]", list.get(0),
                    "[gamemode]", Utils.getGameMode(this.getWorld())));
            }
            else if (user.isPlayer() && user.getLocation() != null)
            {
                this.getWorld().dropItemNaturally(user.getLocation(), object.getMagicStick());
            }

            return true;
        }


        @Override
        public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
        {
            if (args.isEmpty()) return Optional.empty();

            String lastString = args.get(args.size() - 1);

            List<String> returnList;
            final int size = args.size();

            if (size == 1)
            {
                String gamemode = Utils.getGameMode(this.getWorld()).toLowerCase() + "_";

                returnList = this.<CauldronWitcheryAddon>getAddon().getAddonManager().
                    getAllMagicSticks(this.getWorld()).
                    stream().
                    map(MagicStickObject::getUniqueId).
                    map(name -> name.replaceFirst(gamemode, "")).
                    filter(magicStick -> magicStick.toLowerCase().startsWith(args.get(1).toLowerCase())).
                    collect(Collectors.toList());
            }
            else
            {
                returnList = Collections.emptyList();
            }

            return Optional.of(Util.tabLimit(returnList, lastString));
        }
    }


    /**
     * This method allows admin to craft a book that is directed to a player.
     */
    public static class CraftBookCommand extends CompositeCommand
    {
        public CraftBookCommand(CauldronWitcheryAddon addon, CompositeCommand parent)
        {
            super(addon, parent, "book");
        }


        @Override
        public void setup()
        {
            this.setOnlyPlayer(true);
            this.setPermission("admin.witchery");
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "book.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "book.description");
        }


        @Override
        public boolean execute(User user, String s, List<String> list)
        {
            if (list.size() != 2)
            {
                this.showHelp(this, user);
                return true;
            }

            User target;

            if (list.get(0).length() < 17)
            {
                Optional<Players> first = this.getAddon().getPlayers().getPlayers().stream().
                    filter(player -> player.getPlayerName().equalsIgnoreCase(list.get(0))).
                    findFirst();

                if (first.isPresent())
                {
                    target = User.getInstance(first.get().getPlayerUUID());
                }
                else
                {
                    this.showHelp(this, user);
                    return true;
                }
            }
            else if (list.get(0).length() == 32)
            {
                target = User.getInstance(UUID.fromString(list.get(0).
                    replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                        "$1-$2-$3-$4-$5")));
            }
            else if (list.get(0).length() == 36)
            {
                target = User.getInstance(UUID.fromString(list.get(0)));
            }
            else
            {
                this.showHelp(this, user);
                return true;
            }

            ItemStack itemStack =
                this.<CauldronWitcheryAddon>getAddon().getAddonManager().craftBook(list.get(1), target);

            if (itemStack == null)
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "no-magic-book",
                    "[book]", list.get(1)));
            }
            else if (user.isPlayer() && user.getLocation() != null)
            {
                this.getWorld().dropItemNaturally(user.getLocation(), itemStack);
            }

            return true;
        }


        @Override
        public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
        {
            if (args.isEmpty()) return Optional.empty();

            String lastString = args.get(args.size() - 1);

            List<String> returnList;
            final int size = args.size();

            if (size == 1)
            {
                returnList = this.getPlugin().getPlayers().getPlayers().stream().
                    map(Players::getPlayerName).
                    filter(playerName -> playerName.toLowerCase().startsWith(args.get(0).toLowerCase())).
                    collect(Collectors.toList());
            }
            else if (size == 2)
            {
                returnList = this.<CauldronWitcheryAddon>getAddon().getAddonManager().getAllBookNames().stream().
                    filter(bookName -> bookName.toLowerCase().startsWith(args.get(1).toLowerCase())).
                    collect(Collectors.toList());
            }
            else
            {
                returnList = Collections.emptyList();
            }

            return Optional.of(Util.tabLimit(returnList, lastString));
        }
    }


    /**
     * Generic reload wrapper. This command is just a wrapper around `reload books` or `reload sticks`
     */
    public static class ReloadCommand extends CompositeCommand
    {
        public ReloadCommand(CauldronWitcheryAddon addon, CompositeCommand parent)
        {
            super(addon, parent, "reload");
        }


        @Override
        public void setup()
        {
            this.setOnlyPlayer(true);
            this.setPermission("admin.witchery");

            new ReloadMagicStickCommand(this.getAddon(), this);
            new ReloadBookCommand(this.getAddon(), this);
        }


        @Override
        public boolean execute(User user, String s, List<String> list)
        {
            this.showHelp(this, user);
            return true;
        }
    }


    /**
     * This method reloads magic sticks from database.
     */
    public static class ReloadMagicStickCommand extends CompositeCommand
    {
        public ReloadMagicStickCommand(CauldronWitcheryAddon addon, CompositeCommand parent)
        {
            super(addon, parent, "sticks");
        }


        @Override
        public void setup()
        {
            this.setOnlyPlayer(true);
            this.setPermission("admin.witchery");
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "reload-sticks.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "reload-sticks.description");
        }


        @Override
        public boolean execute(User user, String s, List<String> list)
        {
            this.<CauldronWitcheryAddon>getAddon().getAddonManager().reloadSticks();
            Utils.sendMessage(user, user.getTranslation(Constants.CONVERSATIONS + "sticks-reload"));
            return true;
        }
    }


    /**
     * This method reloads books from database.
     */
    public static class ReloadBookCommand extends CompositeCommand
    {
        public ReloadBookCommand(CauldronWitcheryAddon addon, CompositeCommand parent)
        {
            super(addon, parent, "books");
        }


        @Override
        public void setup()
        {
            this.setOnlyPlayer(true);
            this.setPermission("admin.witchery");

            this.setParametersHelp(Constants.ADMIN_COMMANDS + "reload-books.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "reload-books.description");
        }


        @Override
        public boolean execute(User user, String s, List<String> list)
        {
            this.<CauldronWitcheryAddon>getAddon().getAddonManager().reloadBooks();
            Utils.sendMessage(user, user.getTranslation(Constants.CONVERSATIONS + "books-reload"));
            return true;
        }
    }
}
