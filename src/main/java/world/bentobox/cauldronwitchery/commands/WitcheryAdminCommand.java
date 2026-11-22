package world.bentobox.cauldronwitchery.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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


    /**
     * Permission required by all admin commands.
     */
    private static final String ADMIN_PERMISSION = "admin.witchery";


    @Override
    public void setup()
    {
        this.setOnlyPlayer(true);
        this.setPermission(ADMIN_PERMISSION);
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
            this.setPermission(ADMIN_PERMISSION);

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
            this.setOnlyPlayer(false);
            this.setPermission(ADMIN_PERMISSION);
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "give.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "give.description");
        }


        @Override
        public boolean execute(User user, String s, List<String> list)
        {
            String gamemode = Utils.getGameMode(this.getWorld());

            if (list.isEmpty() || list.size() > 2 || gamemode == null)
            {
                this.showHelp(this, user);
                return false;
            }

            // Stick id is always the last argument; optional player name comes before it.
            String stickId = list.getLast();

            MagicStickObject object = this.<CauldronWitcheryAddon>getAddon().
                getAddonManager().getMagicStickById(gamemode.toLowerCase() + "_" + stickId);

            User target;

            if (list.size() == 1)
            {
                target = user;
            }
            else
            {
                UUID uuid = Util.getUUID(list.getFirst());
                target = uuid == null ? null : User.getInstance(uuid);
            }

            if (object == null)
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "no-magic-stick",
                    "[id]", stickId,
                    "[gamemode]", gamemode));
                return false;
            }

            if (target == null || !target.isPlayer())
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "player-not-found"));
                return false;
            }

            Utils.giveItem(target, object.getMagicStick());
            return true;
        }


        @Override
        public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
        {
            if (args.isEmpty()) return Optional.empty();

            String lastString = args.get(args.size() - 1).toLowerCase();

            List<String> returnList;
            final int size = args.size();

            String gameModeName = Utils.getGameMode(this.getWorld());

            if ((size == 4 || size == 5) && gameModeName != null)
            {
                String gamemode = gameModeName.toLowerCase() + "_";

                returnList = this.<CauldronWitcheryAddon>getAddon().getAddonManager().
                    getAllMagicSticks(this.getWorld()).
                    stream().
                    map(MagicStickObject::getUniqueId).
                    map(name -> name.replaceFirst(gamemode, "")).
                    collect(Collectors.toCollection(ArrayList::new));

                if (size == 4)
                {
                    // Add all players to the first list.
                    returnList.addAll(Bukkit.getOnlinePlayers().stream().
                        map(Player::getName).
                        toList());
                }
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
            this.setPermission(ADMIN_PERMISSION);
            this.setParametersHelp(Constants.ADMIN_COMMANDS + "book.parameters");
            this.setDescription(Constants.ADMIN_COMMANDS + "book.description");
        }


        @Override
        public boolean execute(User user, String s, List<String> list)
        {
            if (list.size() != 2)
            {
                this.showHelp(this, user);
                return false;
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
                    return false;
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
                return false;
            }

            ItemStack itemStack =
                this.<CauldronWitcheryAddon>getAddon().getAddonManager().craftBook(list.get(1), target);

            if (itemStack == null)
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "no-magic-book",
                    "[book]", list.get(1)));
                return false;
            }

            Utils.giveItem(user, itemStack);
            return true;
        }


        @Override
        public Optional<List<String>> tabComplete(User user, String alias, List<String> args)
        {
            if (args.isEmpty()) return Optional.empty();

            String lastString = args.get(args.size() - 1);

            List<String> returnList = switch (args.size())
            {
                case 1 -> this.getPlugin().getPlayers().getPlayers().stream().
                    map(Players::getPlayerName).
                    filter(playerName -> playerName.toLowerCase().startsWith(args.get(0).toLowerCase())).
                    toList();
                case 2 -> this.<CauldronWitcheryAddon>getAddon().getAddonManager().getAllBookNames().stream().
                    filter(bookName -> bookName.toLowerCase().startsWith(args.get(1).toLowerCase())).
                    toList();
                default -> Collections.emptyList();
            };

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
            this.setPermission(ADMIN_PERMISSION);

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
            this.setPermission(ADMIN_PERMISSION);
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
            this.setPermission(ADMIN_PERMISSION);

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
