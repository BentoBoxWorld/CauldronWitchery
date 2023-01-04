//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.managers;


import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.eclipse.jdt.annotation.Nullable;
import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.stream.Collectors;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.util.Util;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.EntityRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.ItemRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * The type Cauldron witchery manager.
 */
public class CauldronWitcheryManager
{
    /**
     * Instantiates a new Cauldron witchery manager.
     *
     * @param addon the addon
     */
    public CauldronWitcheryManager(CauldronWitcheryAddon addon)
    {
        this.addon = addon;

        this.magicStickDatabase = new Database<>(addon, MagicStickObject.class);
        this.magicStickDataCache = new HashMap<>();

        this.translatedBooks = new HashMap<>();
    }



// ---------------------------------------------------------------------
// Section: Savers
// ---------------------------------------------------------------------


    /**
     * Save generator tiers from cache into database
     */
    public void save()
    {
        this.magicStickDataCache.values().forEach(this::saveMagicStick);
    }


    /**
     * Save magic stick.
     *
     * @param magicStickObject the magic stick object
     */
    public void saveMagicStick(MagicStickObject magicStickObject)
    {
        this.magicStickDatabase.saveObjectAsync(magicStickObject);
    }


// ---------------------------------------------------------------------
// Section: Loaders
// ---------------------------------------------------------------------


    /**
     * Loads Magic Sticks into cache.
     */
    public void load()
    {
        this.magicStickDataCache.clear();
        this.translatedBooks.clear();
        this.addon.log("Loading magic sticks from database...");
        this.magicStickDatabase.loadObjects().forEach(this::loadMagicStick);
        this.addon.log("Loading book translations...");
        this.loadBookTranslations();
        this.addon.log("Done");
    }


    /**
     * Reloads everything.
     */
    public void reload()
    {
        // TODO: Implement
    }


    /**
     * This method reload sticks.
     */
    public void reloadSticks()
    {
        this.magicStickDataCache.clear();
        this.addon.log("Loading magic sticks from database...");
        this.magicStickDatabase.loadObjects().forEach(this::loadMagicStick);
    }


    /**
     * Load magic stick boolean.
     *
     * @param magicStick the magic stick
     * @return the boolean if stick was loaded or not.
     */
    private boolean loadMagicStick(MagicStickObject magicStick)
    {
        return this.loadMagicStick(magicStick, true, null);
    }


    /**
     * Load magic stick in the cache.
     *
     * @param magicStick - magic stick that must be stored.
     * @param overwrite - true if previous object should be overwritten
     * @param user - user making the request
     * @return - true if imported
     */
    public boolean loadMagicStick(MagicStickObject magicStick, boolean overwrite, User user)
    {
        if (this.magicStickDataCache.containsKey(magicStick.getUniqueId()))
        {
            if (!overwrite)
            {
                return false;
            }
            else
            {
                this.magicStickDataCache.replace(magicStick.getUniqueId(), magicStick);
                return true;
            }
        }

        if (user != null)
        {
            Utils.sendMessage(user,
                user.getTranslation(Constants.CONVERSATIONS + "magic-stick-loaded",
                    "[stick]", magicStick.getMagicStick().getType().name()));
        }

        this.magicStickDataCache.put(magicStick.getUniqueId(), magicStick);
        return true;
    }


    /**
     * This method wipes database for selected gamemode.
     * @param gamemode GameMode which data must be removed.
     */
    public void wipeDatabase(String gamemode)
    {
        List<MagicStickObject> magicSticks = this.magicStickDatabase.loadObjects();

        magicSticks.stream().
            filter(magicStick -> magicStick.getUniqueId().startsWith(gamemode.toLowerCase()) ||
                magicStick.getUniqueId().startsWith(gamemode)).
            forEach(magicStick -> {
                this.magicStickDatabase.deleteID(magicStick.getUniqueId());
                this.magicStickDataCache.remove(magicStick.getUniqueId());
            });
    }


// ---------------------------------------------------------------------
// Section: General Stick methods
// ---------------------------------------------------------------------


    /**
     * Boolean that returns if given item is a magic stick.
     *
     * @param item the item
     * @param user User who uses a stick. Unused currently.
     * @return the boolean
     */
    public boolean isMagicStick(ItemStack item, User user)
    {
        // Search an item from magic stick item cache.
        return this.magicStickDataCache.values().stream().
            anyMatch(key -> Utils.isSimilarNoDurability(key.getMagicStick(), item));
    }


    /**
     * MagicStickObject for given item stick.
     *
     * @param item the item
     * @param user User who uses a stick. Unused currently.
     * @return the MagicStickObject.
     */
    @Nullable
    public MagicStickObject getMagicStick(ItemStack item, User user)
    {
        return this.magicStickDataCache.values().stream().
            filter(key -> Utils.isSimilarNoDurability(key.getMagicStick(), item)).
            findFirst().
            orElse(null);
    }


    /**
     * Gets all magic sticks.
     *
     * @param world the world
     * @return the all magic sticks
     */
    public List<MagicStickObject> getAllMagicSticks(World world)
    {
        String name = Utils.getGameMode(world).toLowerCase();

        return this.magicStickDataCache.values().stream().
            filter(stick -> stick.getUniqueId().startsWith(name)).
            sorted(Comparator.comparingInt(MagicStickObject::getOrder).
                thenComparing(MagicStickObject::getFriendlyName)).
            collect(Collectors.toList());
    }


    /**
     * Has magic sticks boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean hasMagicSticks(String name)
    {
        return this.magicStickDataCache.keySet().stream().anyMatch(key ->
            key.startsWith(name) || key.startsWith(name.toLowerCase()));
    }


    /**
     * Method returns Magic Stick based on its id.
     * @param objectId Magic Stick ID.
     * @return MagicStickObject or null.
     */
    public MagicStickObject getMagicStickById(String objectId)
    {
        return this.magicStickDataCache.get(objectId);
    }


    /**
     * This method deletes magic stick object.
     * @param stickObject Object that must be deleted.
     */
    public void delete(MagicStickObject stickObject)
    {
        this.magicStickDataCache.remove(stickObject.getUniqueId());
        this.magicStickDatabase.deleteObject(stickObject);
    }


// ---------------------------------------------------------------------
// Section: User sections
// ---------------------------------------------------------------------


    /**
     * Allows removing all player data from gamemode.
     * @param gameMode Gamemode which data must be removed
     */
    public void wipePlayers(String gameMode)
    {
        // TODO: wipe data for players.
    }


// ---------------------------------------------------------------------
// Section: Book Section
// ---------------------------------------------------------------------


    /**
     * This method reloads books.
     */
    public void reloadBooks()
    {
        this.translatedBooks.clear();
        this.addon.log("Loading book translations...");
        this.loadBookTranslations();
    }


    /**
     * This method tries to load all book translations.
     */
    private void loadBookTranslations()
    {
        // Filter for files ending with .yml with a name whose length is >= 6 (xx.yml)
        FilenameFilter ymlFilter = (dir, name) ->
            name.toLowerCase(java.util.Locale.ENGLISH).endsWith(".yml") && name.length() >= 6;

        // Get the folder
        File localeDir = new File(this.addon.getDataFolder(), "books");

        if (!localeDir.exists())
        {
            // If there is no locale folder, then return
            return;
        }

        // Run through the files and store the locales
        for (File bookTranslation : Objects.requireNonNull(localeDir.listFiles(ymlFilter)))
        {
            // Drop YML at the end.
            String bookName = bookTranslation.getName().substring(0, bookTranslation.getName().length() - 4);

            try
            {
                YamlConfiguration languageYaml = YamlConfiguration.loadConfiguration(bookTranslation);

                if (!this.translatedBooks.containsKey(bookName))
                {
                    // Merge into current bookTranslation
                    this.translatedBooks.put(bookName, languageYaml);
                }
            }
            catch (Exception e)
            {
                this.addon.logError("Could not load '" + bookTranslation.getName() + "' : " + e.getMessage()
                    + " with the following cause '" + e.getCause() + "'." +
                    " The file has likely an invalid YML format or has been made unreadable during the process.");
            }
        }
    }


    /**
     * This method crafts book for a given user.
     * @param bookName Name of the book.
     * @param user User who crafts it.
     * @return ItemStack with a book data.
     */
    public ItemStack craftBook(String bookName, User user)
    {
        if (this.translatedBooks.isEmpty())
        {
            // There are no loaded books.
            return null;
        }

        // Get translation.
        YamlConfiguration bookTranslation = this.getBookConfiguration(bookName,
            user.getLocale().toLanguageTag());

        if (bookTranslation == null)
        {
            // Error: Book translation is missing.
            return null;
        }

        ConfigurationSection dataSection = bookTranslation.getConfigurationSection(bookName);

        if (dataSection == null)
        {
            // Get translation data.
            return null;
        }

        ItemStack magicBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) magicBook.getItemMeta();

        if (bookMeta == null)
        {
            // Book meta can never be null, but well, that is how it is implemented.
            return null;
        }

        bookMeta.setTitle(Util.translateColorCodes(dataSection.getString("title", "")));
        bookMeta.setAuthor(Util.translateColorCodes(dataSection.getString("author", "")));
        bookMeta.setDisplayName(Util.translateColorCodes(dataSection.getString("display-name", "")));

        // Add lang to the persistent data.
        bookMeta.getPersistentDataContainer().set(
            new NamespacedKey(this.addon.getPlugin(), "lang"),
            PersistentDataType.STRING,
            dataSection.getString("lang", "unknown"));

        // Add user-name to the persistent data.
        bookMeta.getPersistentDataContainer().set(
            new NamespacedKey(this.addon.getPlugin(), "user"),
            PersistentDataType.STRING,
            user.getName());

        List<String> pages = new ArrayList<>(100);

        ConfigurationSection pagesSection = dataSection.getConfigurationSection("pages");

        int lastFilledPage = 0;

        // Fill predefined pages in book.
        if (pagesSection != null)
        {
            // There are max 100 pages per book.
            for (int i = 0; i < 100; i++)
            {
                String pageContent = pagesSection.getString(String.valueOf(i));

                if (pageContent != null)
                {
                    pages.add(i, Util.translateColorCodes(pageContent));
                    lastFilledPage = i;
                }
                else
                {
                    // Add empty page.
                    pages.add(i, "");
                }
            }
        }

        // Add one page?
        lastFilledPage += 1;

        // Add All recipes
        List<Recipe> recipeList = this.magicStickDataCache.values().stream().
            filter(magicStickObject -> magicStickObject.getBookName().equalsIgnoreCase(bookName)).
            findFirst().
            map(MagicStickObject::getRecipeList).
            orElse(Collections.emptyList());

        ConfigurationSection recipeSection = dataSection.getConfigurationSection("recipe");

        if (recipeSection != null)
        {
            for (Recipe recipe : recipeList)
            {
                StringBuilder recipePage;

                int level = switch (recipe.getCauldronType()) {
                    case LAVA_CAULDRON -> 3;
                    case CAULDRON -> 0;
                    default -> recipe.getCauldronLevel();
                };

                String cauldronLevel;

                ConfigurationSection cauldronSection = recipeSection.getConfigurationSection("cauldron_level");

                if (cauldronSection != null)
                {
                    cauldronLevel = cauldronSection.getString(String.valueOf(level), "");
                }
                else
                {
                    cauldronLevel = "";
                }

                if (recipe instanceof EntityRecipe entityRecipe)
                {
                    recipePage = new StringBuilder(recipeSection.getString("mob-header", "").
                        replace("[mob]", Utils.prettifyObject(entityRecipe.getEntityType(), user)).
                        replace("[level]", String.valueOf(recipe.getExperience())).
                        replace("[cauldron_level]", cauldronLevel).
                        replace("[cauldron]", Utils.prettifyObject(recipe.getCauldronType(), user)).
                        replace("[offhand]", Utils.prettifyObject(recipe.getMainIngredient(), user)));
                }
                else if (recipe instanceof ItemRecipe itemRecipe)
                {
                    recipePage = new StringBuilder(recipeSection.getString("item-header", "").
                        replace("[item]", Utils.prettifyObject(itemRecipe.getItemStack(), user)).
                        replace("[level]", String.valueOf(recipe.getExperience())).
                        replace("[cauldron_level]", cauldronLevel).
                        replace("[cauldron]", Utils.prettifyObject(recipe.getCauldronType(), user)).
                        replace("[offhand]", Utils.prettifyObject(recipe.getMainIngredient(), user)));
                }
                else if (recipe instanceof BookRecipe bookRecipe)
                {
                    String recipeBookName = bookRecipe.getBookName();

                    YamlConfiguration bookConfiguration =
                        this.getBookConfiguration(recipeBookName, user.getLocale().toLanguageTag());

                    String name;

                    if (bookConfiguration != null)
                    {
                        name = bookConfiguration.getString(recipeBookName + ".title", "");
                    }
                    else
                    {
                        // Cannot add this book.
                        continue;
                    }

                    recipePage = new StringBuilder(recipeSection.getString("book-header", "").
                        replace("[book]", name).
                        replace("[level]", String.valueOf(recipe.getExperience())).
                        replace("[cauldron_level]", cauldronLevel).
                        replace("[cauldron]", Utils.prettifyObject(recipe.getCauldronType(), user)).
                        replace("[offhand]", Utils.prettifyObject(recipe.getMainIngredient(), user)));
                }
                else
                {
                    continue;
                }

                recipePage.append("\n");
                recipePage.append(recipeSection.getString("extra-title", ""));

                for (ItemStack extraIngredient : recipe.getExtraIngredients())
                {
                    recipePage.append("\n");
                    recipePage.append(recipeSection.getString("extra-element", "").
                        replace("[item]", Utils.prettifyObject(extraIngredient, user)));
                }

                if (recipe.getTemperature() == Recipe.Temperature.COOL)
                {
                    recipePage.append("\n");
                    recipePage.append(recipeSection.getString("cooling", ""));
                }

                if (recipe.getTemperature() == Recipe.Temperature.HEAT)
                {
                    recipePage.append("\n");
                    recipePage.append(recipeSection.getString("heating", ""));
                }

                pages.set(lastFilledPage++, Util.translateColorCodes(recipePage.toString()));
            }
        }

        // Add last page.
        if (pagesSection != null)
        {
            String pageContent = pagesSection.getString("last");

            if (pageContent != null)
            {
                pages.set(lastFilledPage, Util.translateColorCodes(pageContent));
            }
        }

        // Clear empty pages.
        if (pages.size() >= lastFilledPage + 1)
        {
            pages.subList(lastFilledPage + 1, pages.size()).clear();
        }

        bookMeta.setPages(pages);
        magicBook.setItemMeta(bookMeta);

        return magicBook;
    }


    /**
     * This method returns book config section with the requested locale tag.
     * @param bookName Book name.
     * @param localeTag Locale tag.
     * @return Translated book configuration or english translation.
     */
    @Nullable
    private YamlConfiguration getBookConfiguration(String bookName, String localeTag)
    {
        if (this.translatedBooks.containsKey(bookName + "-" + localeTag))
        {
            return this.translatedBooks.get(bookName + "-" + localeTag);
        }
        else
        {
            return this.translatedBooks.get(bookName + "-en-US");
        }
    }


    /**
     * This method returns all book names based on en-US language.
     * en-US is considered as default books, and if someone removes them...
     * well... it will just not return book names.
     * @return List of book names.
     */
    public List<String> getAllBookNames()
    {
        return this.translatedBooks.keySet().stream().
            filter(name -> name.endsWith("-en-US")).
            map(name -> name.replace("-en-US", "")).
            collect(Collectors.toList());
    }


    /**
     * Returns if given user can buy this magic stick.
     *
     * @param magicStick the magic stick
     * @param user the user
     * @return the boolean true, if user has permissions to buy magic stick.
     */
    public boolean canPurchase(MagicStickObject magicStick, User user)
    {
        return magicStick.getPermissions().isEmpty() ||
            user.isOp() ||
            magicStick.getPermissions().stream().allMatch(user::hasPermission);
    }


    /**
     * This method process magic stick purchase.
     * @param user User who buys magic stick.
     * @param magicStick Magic Stick object that is purchased.
     */
    public void purchaseStick(User user, MagicStickObject magicStick)
    {
        if (this.addon.isEconomyProvided() && magicStick.getPurchaseCost() > 0.0)
        {
            if (this.addon.getEconomyProvider().has(user, magicStick.getPurchaseCost()) && user.getLocation() != null)
            {
                EconomyResponse withdraw = this.addon.getEconomyProvider().withdraw(user, magicStick.getPurchaseCost());

                if (withdraw.transactionSuccess())
                {
                    user.getWorld().dropItemNaturally(user.getLocation(), magicStick.getMagicStick());

                    String name = magicStick.getFriendlyName().isBlank() ?
                        Utils.prettifyObject(magicStick.getMagicStick(), user) :
                        magicStick.getFriendlyName();

                    Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "purchased-stick",
                        Constants.PARAMETER_NAME, name,
                        Constants.PARAMETER_VALUE, String.valueOf(magicStick.getPurchaseCost())));
                }
                else
                {
                    Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "could-not-withdraw",
                        Constants.PARAMETER_VALUE, withdraw.errorMessage));
                }
            }
            else
            {
                String name = magicStick.getFriendlyName().isBlank() ?
                    Utils.prettifyObject(magicStick.getMagicStick(), user) :
                    magicStick.getFriendlyName();

                Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "not-enough-money",
                    Constants.PARAMETER_NAME, name));
            }
        }
        else
        {
            if (user.getLocation() != null)
            {
                user.getWorld().dropItemNaturally(user.getLocation(), magicStick.getMagicStick());

                String name = magicStick.getFriendlyName().isBlank() ?
                    Utils.prettifyObject(magicStick.getMagicStick(), user) :
                    magicStick.getFriendlyName();

                Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "free-stick",
                    Constants.PARAMETER_NAME, name));
            }
        }
    }


// ---------------------------------------------------------------------
// Section: Variable
// ---------------------------------------------------------------------

    /**
     * Addon instance
     */
    private final CauldronWitcheryAddon addon;

    /**
     * Variable stores map that links String to loaded magic sticks data object.
     */
    private final Map<String, MagicStickObject> magicStickDataCache;

    /**
     * Variable stores database of magic sticks data objects.
     */
    private final Database<MagicStickObject> magicStickDatabase;

    /**
     * Variable stores and links locales with translated book config.
     */
    private final Map<String, YamlConfiguration> translatedBooks;
}
