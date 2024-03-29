//
// Created by BONNe
// Copyright - 2020
//


package world.bentobox.cauldronwitchery.utils;


import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.LangUtilsHook;
import world.bentobox.bentobox.util.Util;


/**
 * This class contains some useful methods for addon.
 */
public class Utils
{
    /**
     * This method checks if 2 given item stacks are similar without durability check.
     * @param input First item.
     * @param stack Second item.
     * @return {@code true} if items are equal, {@code false} otherwise.
     */
    public static boolean isSimilarNoDurability(@Nullable ItemStack input, @Nullable ItemStack stack)
    {
        if (stack == null || input == null)
        {
            return false;
        }
        else if (stack == input)
        {
            return true;
        }
        else
        {
            return input.getType() == stack.getType() &&
                input.hasItemMeta() == stack.hasItemMeta() &&
                (!input.hasItemMeta() || Bukkit.getItemFactory().equals(input.getItemMeta(), stack.getItemMeta()));
        }
    }


    /**
     * This method groups equal elements into single stack. It uses ItemStack#isSimilar method.
     * @param requiredItems Items that should be grouped.
     * @return List of grouped item stacks.
     */
    public static List<ItemStack> groupEqualItems(Collection<ItemStack> requiredItems)
    {
        List<ItemStack> returnItems = new ArrayList<>(requiredItems.size());

        // Group all equal items in single stack, as otherwise it will be too complicated to check if all
        // items are in players inventory.
        for (ItemStack item : requiredItems)
        {
            boolean isUnique = item != null;

            int i = 0;
            final int requiredSize = returnItems.size();

            while (i < requiredSize && isUnique)
            {
                ItemStack required = returnItems.get(i);

                // Merge items which meta can be ignored or is similar to item in required list.
                if (item.isSimilar(required))
                {
                    required.setAmount(required.getAmount() + item.getAmount());
                    isUnique = false;
                }

                i++;
            }

            if (isUnique)
            {
                // The same issue as in other places. Clone prevents from changing original item.
                returnItems.add(item.clone());
            }
        }

        return returnItems;
    }


    /**
     * This method gets string value of given permission prefix. If user does not have given permission or it have all
     * (*), then return default value.
     *
     * @param user User who's permission should be checked.
     * @param permissionPrefix Prefix that need to be found.
     * @param defaultValue Default value that will be returned if permission not found.
     * @return String value that follows permissionPrefix.
     */
    public static String getPermissionValue(User user, String permissionPrefix, String defaultValue)
    {
        if (user.isPlayer())
        {
            if (permissionPrefix.endsWith("."))
            {
                permissionPrefix = permissionPrefix.substring(0, permissionPrefix.length() - 1);
            }

            String permPrefix = permissionPrefix + ".";

            List<String> permissions = user.getEffectivePermissions().stream().
                map(PermissionAttachmentInfo::getPermission).
                filter(permission -> permission.startsWith(permPrefix)).
                collect(Collectors.toList());

            for (String permission : permissions)
            {
                if (permission.contains(permPrefix + "*"))
                {
                    // * means all. So continue to search more specific.
                    continue;
                }

                String[] parts = permission.split(permPrefix);

                if (parts.length > 1)
                {
                    return parts[1];
                }
            }
        }

        return defaultValue;
    }


    /**
     * This method gets integer value of given permission prefix. If user does not have given permission or it have all
     * (*), then return default value.
     *
     * @param user User who's permission should be checked.
     * @param permissionPrefix Prefix that need to be found.
     * @param defaultValue Default value that will be returned if permission not found.
     * @return Integer value that follows permissionPrefix.
     */
    public static int getPermissionValue(User user, String permissionPrefix, int defaultValue)
    {
        return user.getPermissionValue(permissionPrefix, defaultValue);
    }


    /**
     * This method replaces "[gamemode] and [number] in permission template with a requested gamemode and empty space
     * accordantly.
     *
     * @param world World where permission is operating.
     * @param permissionTemplate permission template.
     * @return Parsed permission String.
     */
    public static String getPermissionString(World world, String permissionTemplate)
    {
        String permissionPrefix = BentoBox.getInstance().getIWM().getPermissionPrefix(world);

        return permissionPrefix.isEmpty() ? permissionTemplate :
            permissionTemplate.replace("[gamemode].", permissionPrefix);
    }


    /**
     * This method returns if given user has all required permissions.
     *
     * @param user User who must be checked.
     * @param permissions List of permissions that must be checked.
     * @return {@code true} if player has all required permissions, {@code flase} otherwise.
     */
    public static boolean matchAllPermissions(User user, Collection<String> permissions)
    {
        return permissions.isEmpty() ||
            user.isOp() ||
            permissions.stream().allMatch(user::hasPermission);
    }


    /**
     * This method transforms given World into GameMode name. If world is not a GameMode world then it returns null.
     *
     * @param world World which gameMode name must be found out.
     * @return GameMode name or null.
     */
    public static String getGameMode(World world)
    {
        return BentoBox.getInstance().getIWM().getAddon(world).
            map(gameModeAddon -> gameModeAddon.getDescription().getName()).
            orElse(null);
    }


    /**
     * This method transforms given GameMode into name.
     *
     * @param gameModeAddon GameMode which name must be returned.
     * @return GameMode name.
     */
    public static String getGameMode(GameModeAddon gameModeAddon)
    {
        return gameModeAddon.getDescription().getName();
    }


    /**
     * This method allows to get next value from array list after given value.
     *
     * @param values Array that should be searched for given value.
     * @param currentValue Value which next element should be found.
     * @param <T> Instance of given object.
     * @return Next value after currentValue in values array.
     */
    public static <T> T getNextValue(T[] values, T currentValue)
    {
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].equals(currentValue))
            {
                if (i + 1 == values.length)
                {
                    return values[0];
                }
                else
                {
                    return values[i + 1];
                }
            }
        }

        return currentValue;
    }


    /**
     * This method allows to get previous value from array list after given value.
     *
     * @param values Array that should be searched for given value.
     * @param currentValue Value which previous element should be found.
     * @param <T> Instance of given object.
     * @return Previous value before currentValue in values array.
     */
    public static <T> T getPreviousValue(T[] values, T currentValue)
    {
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].equals(currentValue))
            {
                if (i > 0)
                {
                    return values[i - 1];
                }
                else
                {
                    return values[values.length - 1];
                }
            }
        }

        return currentValue;
    }



    /**
     * Sanitizes the provided input. It replaces spaces and hyphens with underscores and lower cases the input.
     * This code also removes all color codes from the input.
     * @param input input to sanitize
     * @return sanitized input
     */
    public static String sanitizeInput(String input)
    {
        return ChatColor.stripColor(
            Util.translateColorCodes(input.toLowerCase(Locale.ENGLISH).
                replace(" ", "_").
                replace("-", "_")));
    }


    /**
     * Send given message to user and add prefix to the start of the message.
     *
     * @param user User who need to receive message.
     * @param message String of message that must be send.
     */
    public static void sendMessage(User user, String message)
    {
        user.sendMessage(user.getTranslation(Constants.CONVERSATIONS + "prefix") + message);
    }



// ---------------------------------------------------------------------
// Section: User experience related methods
// Code is taken from https://hub.spigotmc.org/stash/projects/PLUGIN/repos/essentials/browse/Essentials/src/com/earth2me/essentials/craftbukkit/SetExpFix.java
// ---------------------------------------------------------------------


    /**
     * This method sets player total level to the new value.
     * @param player Player whose EXP must be changed.
     * @param experience The new value for experience.
     */
    public static void setTotalExperience(final Player player, final int experience)
    {
        // Reset value to 0.
        player.setExp(0);
        player.setLevel(0);

        int amount = experience;

        while (amount > 0)
        {
            final int expToLevel = Utils.getExpAtLevel(player);
            amount -= expToLevel;

            if (amount >= 0)
            {
                // give until next level
                player.giveExp(expToLevel);
            }
            else
            {
                // give the rest
                amount += expToLevel;
                player.giveExp(amount);
                amount = 0;
            }
        }
    }


    /**
     * This method returns experience at player level.
     * @param player Player whose EXP must be returned.
     * @return Experience points at player leve.
     */
    private static int getExpAtLevel(final Player player)
    {
        return Utils.getExpAtLevel(player.getLevel());
    }


    /**
     * This method calculates experience at the given level.
     * @param level Level that must be reached.
     * @return Experience for the level.
     */
    public static int getExpAtLevel(final int level)
    {
        if (level <= 15)
        {
            return 2 * level + 7;
        }

        if (level <=30)
        {
            return 5 * level - 38;
        }

        return 9 * level - 158;
    }


    /**
     * This method returns total player experience calculated from player level.
     * @param player Player whose total experience must be returned.
     * @return Total experience points for given player.
     */
    public static int getTotalExperience(final Player player)
    {
        int exp = Math.round(Utils.getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0)
        {
            currentLevel--;
            exp += Utils.getExpAtLevel(currentLevel);
        }

        if (exp < 0)
        {
            exp = Integer.MAX_VALUE;
        }

        return exp;
    }


// ---------------------------------------------------------------------
// Section: Translation related methods
// ---------------------------------------------------------------------


    /**
     * Prettify World.Environment object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for World.Environment.
     */
    public static String prettifyObject(World.Environment object, User user)
    {
        // Find addon structure with:
        // [addon]:
        //   environments:
        //     [environment]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.ENVIRONMENTS + object.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   environments:
        //     [environment]: [name]

        translation = user.getTranslationOrNothing(Constants.ENVIRONMENTS + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // environments:
        //   [environment]: [name]

        translation = user.getTranslationOrNothing("environments." + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Lang Utils do not have Environment :(
        //LangUtilsHook.getEnvrionmentName(object, user);

        // Nothing was found. Use just a prettify text function.
        return Util.prettifyText(object.name());
    }


    /**
     * Prettify World.Environment object description for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified description string for World.Environment.
     */
    public static String prettifyDescription(World.Environment object, User user)
    {
        // Find addon structure with:
        // [addon]:
        //   environments:
        //     [environment]:
        //       description: [text]
        String translation = user.getTranslationOrNothing(Constants.ENVIRONMENTS + object.name().toLowerCase() + ".description");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // No text to return.
        return "";
    }


    /**
     * Prettify Material object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for Material.
     */
    public static String prettifyObject(@Nullable Material object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]: [name]

        translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // materials:
        //   [material]: [name]

        translation = user.getTranslationOrNothing("materials." + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Use Lang Utils Hook to translate material
        return LangUtilsHook.getMaterialName(object, user);
    }


    /**
     * Prettify Material object description for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified description string for Material.
     */
    public static String prettifyDescription(@Nullable Material object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]:
        //       description: [text]
        String translation = user.getTranslationOrNothing(Constants.MATERIALS + object.name().toLowerCase() + ".description");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // No text to return.
        return "";
    }


    /**
     * Prettify EntityType object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for EntityType.
     */
    public static String prettifyObject(@Nullable EntityType object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   entities:
        //     [entity]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.ENTITIES + object.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   entities:
        //     [entity]: [name]

        translation = user.getTranslationOrNothing(Constants.ENTITIES + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // entities:
        //   [entity]: [name]

        translation = user.getTranslationOrNothing("entities." + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Use Lang Utils Hook to translate material
        return LangUtilsHook.getEntityName(object, user);
    }


    /**
     * Prettify EntityType object description for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified description string for EntityType.
     */
    public static String prettifyDescription(@Nullable EntityType object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   entities:
        //     [entity]:
        //       description: [text]
        String translation = user.getTranslationOrNothing(Constants.ENTITIES + object.name().toLowerCase() + ".description");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // No text to return.
        return "";
    }


    /**
     * Prettify Statistic object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for Statistic.
     */
    public static String prettifyObject(@Nullable Statistic object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   statistics:
        //     [statistic]:
        //       name: [name]
        String translation = user.getTranslationOrNothing(Constants.STATISTICS + object.name().toLowerCase() + ".name");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   statistics:
        //     [statistic]: [name]

        translation = user.getTranslationOrNothing(Constants.STATISTICS + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // statistics:
        //   [statistic]: [name]

        translation = user.getTranslationOrNothing("statistics." + object.name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Use Lang Utils Hook to translate material
        //return LangUtilsHook.getStatisticName(object, user);
        return Util.prettifyText(object.name());
    }


    /**
     * Prettify Statistic object description for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified description string for Statistic.
     */
    public static String prettifyDescription(@Nullable Statistic object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   statistics:
        //     [statistic]:
        //       description: [text]
        String translation = user.getTranslationOrNothing(Constants.STATISTICS + object.name().toLowerCase() + ".description");

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // No text to return.
        return "";
    }


    /**
     * Prettify ItemStack object for user.
     * @param object Object that must be pretty.
     * @param user User who will see the object.
     * @return Prettified string for ItemStack.
     */
    public static String prettifyObject(@Nullable ItemStack object, User user)
    {
        // Nothing to translate
        if (object == null)
        {
            return "";
        }

        // Find addon structure with:
        // [addon]:
        //   item-stacks:
        //     [material]: ...
        //     meta:
        //       potion-type: ...
        //       ...
        //     generic: [amount] [name] [meta]
        String translation;
        switch (object.getType())
        {
            case POTION, SPLASH_POTION, LINGERING_POTION, TIPPED_ARROW -> {
                // Get Potion Meta
                translation = prettifyObject(object, (PotionMeta) object.getItemMeta(), user);
            }
            case PLAYER_HEAD, PLAYER_WALL_HEAD -> {
                translation = prettifyObject(object, (SkullMeta) object.getItemMeta(), user);
            }
            case ENCHANTED_BOOK -> {
                translation = prettifyObject(object, (EnchantmentStorageMeta) object.getItemMeta(), user);
            }
            case WRITTEN_BOOK, WRITABLE_BOOK -> {
                translation = prettifyObject(object, (BookMeta) object.getItemMeta(), user);
            }
            case LEATHER_BOOTS,LEATHER_CHESTPLATE,LEATHER_HELMET,LEATHER_LEGGINGS,LEATHER_HORSE_ARMOR,
                TRIDENT,CROSSBOW,CHAINMAIL_HELMET,CHAINMAIL_CHESTPLATE,CHAINMAIL_LEGGINGS,CHAINMAIL_BOOTS,IRON_HELMET,
                IRON_CHESTPLATE,IRON_LEGGINGS,IRON_BOOTS,DIAMOND_HELMET,DIAMOND_CHESTPLATE,DIAMOND_LEGGINGS,DIAMOND_BOOTS,
                GOLDEN_HELMET,GOLDEN_CHESTPLATE,GOLDEN_LEGGINGS,GOLDEN_BOOTS,NETHERITE_HELMET,NETHERITE_CHESTPLATE,
                NETHERITE_LEGGINGS,NETHERITE_BOOTS,WOODEN_SWORD,WOODEN_SHOVEL,WOODEN_PICKAXE,WOODEN_AXE,WOODEN_HOE,
                STONE_SWORD,STONE_SHOVEL,STONE_PICKAXE,STONE_AXE,STONE_HOE,GOLDEN_SWORD,GOLDEN_SHOVEL,GOLDEN_PICKAXE,
                GOLDEN_AXE,GOLDEN_HOE,IRON_SWORD,IRON_SHOVEL,IRON_PICKAXE,IRON_AXE,IRON_HOE,DIAMOND_SWORD,DIAMOND_SHOVEL,
                DIAMOND_PICKAXE,DIAMOND_AXE,DIAMOND_HOE,NETHERITE_SWORD,NETHERITE_SHOVEL,NETHERITE_PICKAXE,NETHERITE_AXE,
                NETHERITE_HOE,TURTLE_HELMET,SHEARS,SHIELD,FLINT_AND_STEEL,BOW -> {
                translation = prettifyObject(object, object.getItemMeta(), user);
            }
            default ->
                translation = "";
        }

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find addon structure with:
        // [addon]:
        //   materials:
        //     [material]: [name]

        translation = user.getTranslationOrNothing(Constants.MATERIALS + object.getType().name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Find general structure with:
        // materials:
        //   [material]: [name]

        translation = user.getTranslationOrNothing("materials." + object.getType().name().toLowerCase());

        if (!translation.isEmpty())
        {
            // We found our translation.
            return translation;
        }

        // Use Lang Utils
        return LangUtilsHook.getItemDisplayName(object, user);
    }


    /**
     * Prettify enchant string.
     *
     * @param enchantment the enchantment
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(Enchantment enchantment, User user)
    {
        if (enchantment == null)
        {
            return "";
        }

        String type = user.getTranslationOrNothing(Constants.ITEM_STACKS + "enchant." + enchantment.getKey().getKey());

        if (type.isEmpty())
        {
            type = LangUtilsHook.getEnchantName(enchantment, user);
        }

        return type;
    }


    /**
     * Prettify effect string.
     *
     * @param effect the enchantment
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(PotionEffectType effect, User user)
    {
        if (effect == null)
        {
            return "";
        }

        String type = user.getTranslationOrNothing(Constants.ITEM_STACKS + "potion-effect." + effect.getName().toLowerCase());

        if (type.isEmpty())
        {
            type = LangUtilsHook.getPotionEffectName(effect, user);
        }

        return type;
    }


    /**
     * Prettify potion item string.
     *
     * @param item the item
     * @param potionMeta the potion meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable PotionMeta potionMeta, User user)
    {
        if (potionMeta == null)
        {
            return "";
        }

        Material itemType = item.getType();

        final String itemReference = Constants.ITEM_STACKS + itemType.name().toLowerCase() + ".";
        final String metaReference = Constants.ITEM_STACKS + "meta.";

        PotionData potionData = potionMeta.getBasePotionData();

        // Check custom translation for potions.
        String type = user.getTranslationOrNothing(itemReference + potionData.getType().name().toLowerCase());

        if (type.isEmpty())
        {
            // Check potion types translation.
            type = prettifyObject(potionData.getType().getEffectType(), user);
        }

        String upgraded = user.getTranslationOrNothing(metaReference + "upgraded");
        String extended = user.getTranslationOrNothing(metaReference + "extended");

        // Get item specific translation.
        String specific = user.getTranslationOrNothing(itemReference + "name",
            "[type]", type,
            "[upgraded]", (potionData.isUpgraded() ? upgraded : ""),
            "[extended]", (potionData.isExtended() ? extended : ""));

        if (specific.isEmpty())
        {
            // Use generic translation.
            String meta = user.getTranslationOrNothing(metaReference + "potion-meta",
                "[type]", type,
                "[upgraded]", (potionData.isUpgraded() ? upgraded : ""),
                "[extended]", (potionData.isExtended() ? extended : ""));

            specific = user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
                "[type]", prettifyObject(itemType, user),
                "[meta]", meta);
        }

        return specific;
    }


    /**
     * Prettify skull item string.
     *
     * @param item the item
     * @param skullMeta the skull meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable SkullMeta skullMeta, User user)
    {
        if (skullMeta == null)
        {
            return "";
        }

        Material itemType = item.getType();
        final String metaReference = Constants.ITEM_STACKS + "meta.";

        String meta = user.getTranslationOrNothing(metaReference + "skull-meta",
            "[player-name]", skullMeta.getDisplayName());

        return user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
            "[type]", prettifyObject(itemType, user),
            "[meta]", meta);
    }


    /**
     * Prettify item string.
     *
     * @param item the item
     * @param itemMeta the item meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable ItemMeta itemMeta, User user)
    {
        if (itemMeta == null)
        {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        itemMeta.getEnchants().forEach((enchantment, level) -> {
            builder.append("\n");
            builder.append(user.getTranslationOrNothing(Constants.ITEM_STACKS + "meta.enchant-meta",
                "[type]", prettifyObject(enchantment, user),
                "[level]", String.valueOf(level)));
        });


        Material itemType = item.getType();
        final String itemReference = Constants.ITEM_STACKS + itemType.name().toLowerCase() + ".";

        String translation = user.getTranslationOrNothing(itemReference + "name",
            "[type]", prettifyObject(itemType, user),
            "[enchant]", builder.toString());

        if (translation.isEmpty())
        {
            translation = user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
                "[type]", prettifyObject(itemType, user),
                "[meta]", builder.toString());
        }

        return translation;
    }


    /**
     * Prettify enchantment storage string.
     *
     * @param item the item
     * @param enchantmentMeta the enchantment storage meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable EnchantmentStorageMeta enchantmentMeta, User user)
    {
        if (enchantmentMeta == null)
        {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        enchantmentMeta.getEnchants().forEach((enchantment, level) -> {
            builder.append("\n");
            builder.append(user.getTranslationOrNothing(Constants.ITEM_STACKS + "meta.enchant-meta",
                "[type]", prettifyObject(enchantment, user),
                "[level]", String.valueOf(level)));
        });


        Material itemType = item.getType();
        final String itemReference = Constants.ITEM_STACKS + itemType.name().toLowerCase() + ".";

        String translation = user.getTranslationOrNothing(itemReference + "name",
            "[type]", prettifyObject(itemType, user),
            "[enchant]", builder.toString());

        if (translation.isEmpty())
        {
            translation = user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
                "[type]", prettifyObject(itemType, user),
                "[meta]", builder.toString());
        }

        return translation;
    }


    /**
     * Prettify book item string.
     *
     * @param item the item
     * @param bookMeta the book meta
     * @param user the user
     * @return the string
     */
    public static String prettifyObject(ItemStack item, @Nullable BookMeta bookMeta, User user)
    {
        if (bookMeta == null)
        {
            return "";
        }

        Material itemType = item.getType();
        final String metaReference = Constants.ITEM_STACKS + "meta.";

        String meta = user.getTranslationOrNothing(metaReference + "book-meta",
            "[title]", bookMeta.getTitle(),
            "[author]", bookMeta.getAuthor());

        return user.getTranslationOrNothing(Constants.ITEM_STACKS + "generic",
            "[type]", prettifyObject(itemType, user),
            "[meta]", meta);
    }


    /**
     * This method parses duration to a readable format.
     * @param duration that needs to be parsed.
     * @return parsed duration string.
     */
    public static String parseDuration(Duration duration, User user)
    {
        final String reference = Constants.DESCRIPTIONS + "challenge.cooldown.";

        String returnString = "";

        if (duration.toDays() > 0)
        {
            returnString += user.getTranslationOrNothing(reference + "in-days",
                Constants.PARAMETER_NUMBER, String.valueOf(duration.toDays()));
        }

        if (duration.toHoursPart() > 0)
        {
            returnString += user.getTranslationOrNothing(reference + "in-hours",
                Constants.PARAMETER_NUMBER, String.valueOf(duration.toHoursPart()));
        }

        if (duration.toMinutesPart() > 0)
        {
            returnString += user.getTranslationOrNothing(reference + "in-minutes",
                Constants.PARAMETER_NUMBER, String.valueOf(duration.toMinutesPart()));
        }

        if (duration.toSecondsPart() > 0 || returnString.isBlank())
        {
            returnString += user.getTranslationOrNothing(reference + "in-seconds",
                Constants.PARAMETER_NUMBER, String.valueOf(duration.toSecondsPart()));
        }

        return returnString;
    }
}
