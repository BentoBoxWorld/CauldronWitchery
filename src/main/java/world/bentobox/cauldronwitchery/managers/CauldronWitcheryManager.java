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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.util.Util;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.PlayerDataObject;
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

        this.playerDatabase = new Database<>(addon, PlayerDataObject.class);
        this.playerDataCache = new ConcurrentHashMap<>();

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
        this.assignRecipeIds(magicStickObject);
        this.magicStickDatabase.saveObjectAsync(magicStickObject);
        this.addon.registerRecipePlaceholders(magicStickObject);
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
        this.assignMissingRecipeIds();
        this.playerDataCache.clear();
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
        this.assignMissingRecipeIds();
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
        return this.magicStickDataCache.values().
            stream().
            filter(key -> key.getUniqueId().startsWith(Utils.getGameMode(user.getWorld()).toLowerCase())).
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
            filter(key -> key.getUniqueId().startsWith(Utils.getGameMode(user.getWorld()).toLowerCase())).
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
// Section: Recipe ids
// ---------------------------------------------------------------------


    /**
     * Returns the GameMode prefix of given magic stick id. Magic stick ids are built as
     * {@code <gamemode>_<id>}.
     *
     * @param stickId Magic stick unique id.
     * @return GameMode prefix in lower case.
     */
    public static String getGameModePrefix(String stickId)
    {
        int index = stickId.indexOf('_');
        return (index < 0 ? stickId : stickId.substring(0, index)).toLowerCase(Locale.ENGLISH);
    }


    /**
     * Converts given text into a placeholder-friendly recipe id.
     *
     * @param text the text
     * @return sanitized recipe id.
     */
    public static String sanitizeRecipeId(String text)
    {
        String id = text == null ? "" : text.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9_]", "_");
        return id.isBlank() ? "recipe" : id;
    }


    /**
     * Assigns recipe ids to all cached magic sticks that have recipes without id and saves changed sticks.
     * Existing ids are always kept, so recipe ids are stable once assigned.
     */
    private void assignMissingRecipeIds()
    {
        this.magicStickDataCache.values().stream().
            sorted(Comparator.comparingInt(MagicStickObject::getOrder).
                thenComparing(MagicStickObject::getUniqueId)).
            filter(this::assignRecipeIds).
            forEach(this.magicStickDatabase::saveObjectAsync);
    }


    /**
     * Assigns ids for recipes in given magic stick that does not have one yet. Recipe ids are unique inside
     * single GameMode.
     *
     * @param magicStick the magic stick
     * @return {@code true} if any recipe id was assigned, {@code false} otherwise.
     */
    public boolean assignRecipeIds(MagicStickObject magicStick)
    {
        return assignRecipeIds(magicStick, this.magicStickDataCache.values());
    }


    /**
     * Assigns ids for recipes in given magic stick that does not have one yet. Ids that are used by other
     * magic sticks in the same GameMode are avoided by adding {@code _2}, {@code _3}, ... suffix.
     *
     * @param magicStick the magic stick
     * @param allSticks All known magic sticks (may include given magic stick).
     * @return {@code true} if any recipe id was assigned, {@code false} otherwise.
     */
    public static boolean assignRecipeIds(MagicStickObject magicStick, Collection<MagicStickObject> allSticks)
    {
        if (magicStick.getRecipeList().stream().allMatch(recipe -> !recipe.getRecipeId().isBlank()))
        {
            return false;
        }

        String prefix = getGameModePrefix(magicStick.getUniqueId());

        // Collect all ids that are already used in this gamemode.
        Set<String> usedIds = new HashSet<>();

        allSticks.stream().
            filter(stick -> !stick.getUniqueId().equals(magicStick.getUniqueId())).
            filter(stick -> getGameModePrefix(stick.getUniqueId()).equals(prefix)).
            flatMap(stick -> stick.getRecipeList().stream()).
            map(Recipe::getRecipeId).
            filter(id -> !id.isBlank()).
            forEach(usedIds::add);

        magicStick.getRecipeList().stream().
            map(Recipe::getRecipeId).
            filter(id -> !id.isBlank()).
            forEach(usedIds::add);

        for (Recipe recipe : magicStick.getRecipeList())
        {
            if (recipe.getRecipeId().isBlank())
            {
                String base = sanitizeRecipeId(recipe.getDefaultRecipeId());
                String id = base;
                int counter = 2;

                while (usedIds.contains(id))
                {
                    id = base + "_" + counter++;
                }

                recipe.setRecipeId(id);
                usedIds.add(id);
            }
        }

        return true;
    }


    /**
     * Returns all recipe ids for given GameMode.
     *
     * @param gameMode GameMode name.
     * @return Set of recipe ids.
     */
    public Set<String> getRecipeIds(String gameMode)
    {
        String prefix = gameMode.toLowerCase(Locale.ENGLISH);

        return this.magicStickDataCache.values().stream().
            filter(stick -> getGameModePrefix(stick.getUniqueId()).equals(prefix)).
            flatMap(stick -> stick.getRecipeList().stream()).
            map(Recipe::getRecipeId).
            filter(id -> !id.isBlank()).
            collect(Collectors.toCollection(LinkedHashSet::new));
    }


// ---------------------------------------------------------------------
// Section: User sections
// ---------------------------------------------------------------------


    /**
     * Returns player data object for given player in given GameMode. Creates new object if it does not exist.
     *
     * @param playerId Player UUID.
     * @param gameMode GameMode name.
     * @return PlayerDataObject.
     */
    public PlayerDataObject getPlayerData(UUID playerId, String gameMode)
    {
        String uniqueId = gameMode.toLowerCase(Locale.ENGLISH) + "_" + playerId;

        return this.playerDataCache.computeIfAbsent(uniqueId, id -> {
            PlayerDataObject data = this.playerDatabase.objectExists(id) ? this.playerDatabase.loadObject(id) : null;

            if (data == null)
            {
                data = new PlayerDataObject();
                data.setUniqueId(id);
            }

            return data;
        });
    }


    /**
     * Returns player data object for given user in the GameMode of the user's world.
     *
     * @param user User.
     * @param world World that is used to detect GameMode.
     * @return PlayerDataObject or null if world is not a GameMode world.
     */
    @Nullable
    private PlayerDataObject getPlayerData(User user, World world)
    {
        String gameMode = world == null ? null : Utils.getGameMode(world);

        if (user == null || user.getUniqueId() == null || gameMode == null)
        {
            return null;
        }

        return this.getPlayerData(user.getUniqueId(), gameMode);
    }


    /**
     * Records magic stick purchase for the user.
     *
     * @param user User who purchased magic stick.
     */
    public void recordStickPurchase(User user)
    {
        PlayerDataObject data = this.getPlayerData(user, user.getWorld());

        if (data != null)
        {
            data.addStickPurchase();
            this.playerDatabase.saveObjectAsync(data);
        }
    }


    /**
     * Records successful recipe usage for the user.
     *
     * @param user User who used recipe.
     * @param world World where recipe was used.
     * @param recipe Recipe that was used.
     */
    public void recordRecipeUse(User user, World world, Recipe recipe)
    {
        PlayerDataObject data = this.getPlayerData(user, world);

        if (data != null)
        {
            data.addRecipeUsage(recipe.getRecipeId());

            if (recipe instanceof EntityRecipe)
            {
                data.addSummon();
            }

            this.playerDatabase.saveObjectAsync(data);
        }
    }


    /**
     * Allows removing all player data from gamemode.
     * @param gameMode Gamemode which data must be removed
     */
    public void wipePlayers(String gameMode)
    {
        String prefix = gameMode.toLowerCase(Locale.ENGLISH) + "_";

        this.playerDataCache.keySet().removeIf(id -> id.startsWith(prefix));

        this.playerDatabase.loadObjects().stream().
            map(PlayerDataObject::getUniqueId).
            filter(id -> id.startsWith(prefix)).
            forEach(this.playerDatabase::deleteID);
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
            orElse(Collections.emptyList()).
            stream().
            filter(Recipe::isValid).
            toList();

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
                    this.recordStickPurchase(user);

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
                this.recordStickPurchase(user);

                String name = magicStick.getFriendlyName().isBlank() ?
                    Utils.prettifyObject(magicStick.getMagicStick(), user) :
                    magicStick.getFriendlyName();

                Utils.sendMessage(user, user.getTranslation(Constants.MESSAGES + "free-stick",
                    Constants.PARAMETER_NAME, name));
            }
        }
    }


    /**
     * This method returns the book name.
     *
     * @param user User who will see the book.
     * @param bookId The book ID.
     * @param backup The backup name.
     * @return The name of given book.
     */
    public String craftBookName(User user, String bookId, String backup)
    {
        YamlConfiguration bookConfiguration =
            this.getBookConfiguration(bookId, user.getLocale().toLanguageTag());

        String name;

        if (bookConfiguration != null)
        {
            name = bookConfiguration.getString(bookId + ".title", backup);
        }
        else
        {
            // Cannot add this book.
            name = backup;
        }

        return name;
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
     * Variable stores database of player data objects.
     */
    private final Database<PlayerDataObject> playerDatabase;

    /**
     * Variable stores loaded player data objects.
     */
    private final Map<String, PlayerDataObject> playerDataCache;

    /**
     * Variable stores and links locales with translated book config.
     */
    private final Map<String, YamlConfiguration> translatedBooks;
}
