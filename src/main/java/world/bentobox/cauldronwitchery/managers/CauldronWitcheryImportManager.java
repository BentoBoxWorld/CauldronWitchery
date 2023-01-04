package world.bentobox.cauldronwitchery.managers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.json.BentoboxTypeAdapterFactory;
import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.util.ItemParser;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.EntityRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.ItemRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * Imports CauldronWitchery Addon data.
 * @author BONNe1704
 *
 */
public class CauldronWitcheryImportManager
{
    /**
     * Import recipes from file or link.
     * @param addon CauldronWitchery addon.
     */
    public CauldronWitcheryImportManager(CauldronWitcheryAddon addon)
    {
        this.addon = addon;
    }


    // ---------------------------------------------------------------------
    // Section: YAML Importers
    // ---------------------------------------------------------------------


    /**
     * This method imports template
     *
     * @param user - user
     * @param world - world to import into
     * @param file - file that must be imported
     */
    public void importFile(@Nullable User user, World world, String file)
    {
        File generatorFile = new File(this.addon.getDataFolder(), file.endsWith(".yml") ? file : file + ".yml");

        if (!generatorFile.exists())
        {
            if (user != null)
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "no-file", Constants.PARAMETER_FILE, file));
            }

            return;
        }

        YamlConfiguration config = new YamlConfiguration();

        try
        {
            config.load(generatorFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
            if (user != null)
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "no-load",
                    Constants.PARAMETER_FILE, file, TextVariables.DESCRIPTION, e.getMessage()));
            }
            else
            {
                this.addon.logError("Exception when loading file. " + e.getMessage());
            }

            return;
        }

        Optional<GameModeAddon> optional = this.addon.getPlugin().getIWM().getAddon(world);

        if (optional.isEmpty())
        {
            if (user != null)
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "not-a-gamemode-world",
                        Constants.PARAMETER_WORLD, world.getName()));
            }
            else
            {
                this.addon.logWarning("Given world is not a gamemode world.");
            }

            return;
        }

        this.addon.getAddonManager().wipeDatabase(optional.get().getDescription().getName().toLowerCase());
        this.createMagicSticks(config, user, optional.get());
    }


    /**
     * This method creates generator tier object from config file.
     *
     * @param config YamlConfiguration that contains all generators.
     * @param user User who calls reading.
     * @param gameMode GameMode in which generator tiers must be imported
     */
    private void createMagicSticks(YamlConfiguration config, @Nullable User user, GameModeAddon gameMode)
    {
        final String prefix = gameMode.getDescription().getName().toLowerCase() + "_";

        long objectCount = 0;

        if (config.contains("magic-sticks"))
        {
            ConfigurationSection reader = config.getConfigurationSection("magic-sticks");

            if (reader != null)
            {
                objectCount = reader.getKeys(false).stream().
                    mapToInt(levelId -> this.createMagicStick(levelId,
                        prefix,
                        reader.getConfigurationSection(levelId))).
                    sum();
            }
        }

        if (user != null)
        {
            Utils.sendMessage(user,
                user.getTranslation(Constants.MESSAGES + "import-count",
                    "[number]", String.valueOf(objectCount)));
        }

        this.addon.log("Imported " + objectCount + " sticks into database.");
    }


    /**
     * This method creates magic stick
     * @param objectId Object Id
     * @param prefix Gamemode prefix
     * @param section Section that contains magic stick info.
     * @return 1 if stick was created, 0 otherwise.
     */
    private int createMagicStick(String objectId,
        String prefix,
        @Nullable ConfigurationSection section)
    {
        if (section == null)
        {
            return 0;
        }

        try
        {
            MagicStickObject magicStick = new MagicStickObject();
            magicStick.setUniqueId(prefix + objectId);

            magicStick.setMagicStick(matchItem(section.getString("stick"),
                new ItemStack(Material.STICK)));

            // Add name and description only if requested.
            if (section.contains("name"))
            {
                magicStick.setFriendlyName(section.getString("name"));
            }

            if (section.contains("description"))
            {
                if (section.isString("description"))
                {
                    magicStick.setDescription(section.getString("description"));
                }
                else if (section.isList("description"))
                {
                    StringBuilder builder = new StringBuilder();

                    section.getStringList("description").forEach(line -> {
                        builder.append(line);
                        builder.append("\n");
                    });

                    magicStick.setDescription(builder.toString());
                }
            }

            magicStick.setBookName(section.getString("book", ""));
            // Set recipes.
            magicStick.setRecipeList(this.populateRecipes(section.getList("recipes")));
            // Set cost
            magicStick.setPurchaseCost(section.getDouble("cost", 0));
            // Set order.
            magicStick.setOrder(section.getInt("order", 0));

            this.addon.getAddonManager().saveMagicStick(magicStick);
            this.addon.getAddonManager().loadMagicStick(magicStick, true, null);
        }
        catch (Exception ignored)
        {
            return 0;
        }

        return 1;
    }


    private List<Recipe> populateRecipes(List<?> section)
    {
        List<Recipe> recipeList = new ArrayList<>(section.size());

        for (Object object : section)
        {
            if (object instanceof LinkedHashMap hashMap)
            {
                Recipe recipe;

                if (hashMap.containsKey("mob"))
                {
                    recipe = new EntityRecipe(matchEntity((String) hashMap.get("mob")));
                }
                else if (hashMap.containsKey("item"))
                {
                    recipe = new ItemRecipe(matchItem((String) hashMap.get("item")));
                }
                else if (hashMap.containsKey("book"))
                {
                    recipe = new BookRecipe((String) hashMap.get("book"));
                }
                else
                {
                    // Skip as unknown recipe.
                    continue;
                }

                recipe.setCauldronType(matchMaterial((String) hashMap.get("cauldron"), Material.WATER_CAULDRON));
                recipe.setCauldronLevel((int) hashMap.getOrDefault("level", 0));
                recipe.setExperience((int) hashMap.getOrDefault("experience", 0));
                recipe.setPermissions(new HashSet<>());
                recipe.getPermissions().addAll((List<String>) hashMap.getOrDefault("permissions", Collections.emptyList()));

                // Set order.
                recipe.setOrder((int) hashMap.getOrDefault("order", 0));

                recipe.setMainIngredient(matchItem((String) hashMap.get("ingredient"), new ItemStack(Material.PAPER)));

                List<ItemStack> extra = new ArrayList<>();
                ((List<String>) hashMap.getOrDefault("extra", Collections.emptyList())).forEach(key -> {
                    ItemStack itemStack = matchItem(key);

                    if (itemStack != null)
                    {
                        extra.add(itemStack);
                    }
                });

                recipe.setExtraIngredients(extra);
                recipeList.add(recipe);
            }
        }

        return recipeList;
    }


    // ---------------------------------------------------------------------
    // Section: JSON Importers
    // ---------------------------------------------------------------------


    /**
     * Import database file from local storage.
     *
     * @param user the user
     * @param world the world
     * @param fileName the file name
     */
    public void importDatabaseFile(User user, World world, String fileName)
    {
        CauldronWitcheryManager manager = this.addon.getAddonManager();

        // If exist any sticks that is bound to current world, then wipe existing data.
        if (manager.hasMagicSticks(Utils.getGameMode(world).toLowerCase()))
        {
            manager.wipeDatabase(Utils.getGameMode(world).toLowerCase());
        }

        try
        {
            // This prefix will be used to all sticks. That is a unique way how to separate sticks for
            // each game mode.
            String uniqueIDPrefix = Utils.getGameMode(world).toLowerCase() + "_";
            DefaultDataHolder databaseFile = new DefaultJSONHandler(this.addon).loadObject(fileName);

            if (databaseFile == null)
            {
                return;
            }

            // All new sticks should get correct ID. So we need to map it to loaded should.
            databaseFile.getMagicStickList().forEach(magicStick -> {
                // Set correct sticks ID
                magicStick.setUniqueId(uniqueIDPrefix + magicStick.getUniqueId());
                // Load sticks in memory
                manager.loadMagicStick(magicStick, false, user);
            });
        }
        catch (Exception e)
        {
            this.addon.getPlugin().logStacktrace(e);
            return;
        }

        manager.save();
    }


    /**
     * This method loads downloaded file into memory.
     * @param user User who calls downloaded file loading
     * @param world Target world.
     * @param downloadString String that need to be loaded via DefaultDataHolder.
     */
    public void loadDownloadedFile(User user, World world, String downloadString)
    {
        CauldronWitcheryManager manager = this.addon.getAddonManager();

        // If exist any data or level that is bound to current world, then do not load default data.
        if (manager.hasMagicSticks(Utils.getGameMode(world).toLowerCase()))
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user, user.getTranslation(Constants.ERRORS + "exist-magic-sticks"));
            }
            else
            {
                this.addon.logWarning(Constants.ERRORS + "exist-magic-sticks");
            }

            return;
        }

        try
        {
            // This prefix will be used to all sticks. That is a unique way how to separate sticks for
            // each game mode.
            String uniqueIDPrefix = Utils.getGameMode(world).toLowerCase() + "_";
            DefaultDataHolder downloadedData = new DefaultJSONHandler(this.addon).loadWebObject(downloadString);

            // All new sticks should get correct ID. So we need to map it to loaded sticks.
            downloadedData.getMagicStickList().forEach(magicStick -> {
                // Set correct sticks ID
                magicStick.setUniqueId(uniqueIDPrefix + magicStick.getUniqueId());
                // Load sticks in memory
                manager.loadMagicStick(magicStick, false, user);
            });
        }
        catch (Exception e)
        {
            this.addon.getPlugin().logStacktrace(e);
            return;
        }

        this.addon.getAddonManager().save();
    }


    // ---------------------------------------------------------------------
    // Section: Default generation
    // ---------------------------------------------------------------------


    public void generateDatabaseFile(User user, World world, String fileName)
    {
        File defaultFile = new File(this.addon.getDataFolder(),
            fileName.endsWith(".json") ? fileName : fileName + ".json");

        if (defaultFile.exists())
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "file-exist",
                        Constants.PARAMETER_FILE, fileName));
            }
            else
            {
                this.addon.logWarning(Constants.ERRORS + "file-exist");
            }

            return;
        }

        try
        {
            if (defaultFile.createNewFile())
            {
                String replacementString = Utils.getGameMode(world).toLowerCase() + "_";
                CauldronWitcheryManager manager = this.addon.getAddonManager();

                List<MagicStickObject> challengeList = manager.getAllMagicSticks(world).
                    stream().
                    map(object -> {
                        // Use clone to avoid any changes in existing object.
                        MagicStickObject clone = object.copy();
                        // Remove gamemode name from object id.
                        clone.setUniqueId(object.getUniqueId().replaceFirst(replacementString, ""));

                        return clone;
                    }).
                    collect(Collectors.toList());

                DefaultDataHolder defaultDataHolder = new DefaultDataHolder();
                defaultDataHolder.setMagicStickList(challengeList);

                defaultDataHolder.setVersion(this.addon.getDescription().getVersion());

                try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(defaultFile), StandardCharsets.UTF_8))) {
                    writer.write(Objects.requireNonNull(
                        new DefaultJSONHandler(this.addon).toJsonString(defaultDataHolder)));
                }
            }
        }
        catch (IOException e)
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.ERRORS + "no-load",
                        Constants.PARAMETER_FILE, fileName,
                        TextVariables.DESCRIPTION, e.getMessage()));
            }

            this.addon.logError("Could not save json file: " + e.getMessage());
        }
        finally
        {
            if (user.isPlayer())
            {
                Utils.sendMessage(user,
                    user.getTranslation(Constants.CONVERSATIONS + "database-export-completed",
                        Constants.PARAMETER_WORLD, world.getName(),
                        Constants.PARAMETER_FILE, fileName));
            }
            else
            {
                this.addon.logWarning("Database Export Completed");
            }
        }
    }


    // ---------------------------------------------------------------------
    // Section: Static Methods
    // ---------------------------------------------------------------------


    /**
     * Match item stack.
     *
     * @param text the text
     * @return the item stack
     */
    @Nullable
    private static ItemStack matchItem(@Nullable String text)
    {
        if (text == null || text.isBlank())
        {
            return new ItemStack(Material.PAPER);
        }
        else
        {
            return ItemParser.parse(text, new ItemStack(Material.PAPER));
        }
    }


    /**
     * Match item stack.
     *
     * @param text the text
     * @param defaultItem the default item
     * @return the item stack
     */
    @NonNull
    private static ItemStack matchItem(@Nullable String text, ItemStack defaultItem)
    {
        ItemStack item = matchItem(text);
        return item == null ? defaultItem : item;
    }


    /**
     * Match material.
     *
     * @param text the text
     * @return the material
     */
    @Nullable
    private static Material matchMaterial(@Nullable String text)
    {
        if (text == null || text.isBlank())
        {
            return null;
        }
        else
        {
            return Material.getMaterial(text.toUpperCase());
        }
    }


    /**
     * Match material.
     *
     * @param text the text
     * @param defaultItem the default item
     * @return the material
     */
    @NonNull
    private static Material matchMaterial(@Nullable String text, Material defaultItem)
    {
        Material item = matchMaterial(text);
        return item == null ? defaultItem : item;
    }


    /**
     * Match entity type.
     *
     * @param text the text
     * @return the entity type
     */
    @Nullable
    private static EntityType matchEntity(@Nullable String text)
    {
        if (text == null || text.isBlank())
        {
            return null;
        }
        else
        {
            try
            {
                return EntityType.valueOf(text.toUpperCase());
            }
            catch (Exception e)
            {
                return null;
            }
        }
    }


    /**
     * Match entity type.
     *
     * @param text the text
     * @param defaultItem the default item
     * @return the entity type
     */
    @NonNull
    private static EntityType matchEntity(@Nullable String text, EntityType defaultItem)
    {
        EntityType item = matchEntity(text);
        return item == null ? defaultItem : item;
    }


    // ---------------------------------------------------------------------
    // Section: Private classes for export file
    // ---------------------------------------------------------------------


    /**
     * This Class allows default json exporter and their levels as objects much easier.
     */
    private static final class DefaultJSONHandler
    {
        /**
         * This constructor inits JSON builder that will be used to parse object.
         * @param addon Adddon
         */
        DefaultJSONHandler(Addon addon)
        {
            GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization();
            // Register adapters
            builder.registerTypeAdapterFactory(new BentoboxTypeAdapterFactory(addon.getPlugin()));
            // Keep null in the database
            builder.serializeNulls();
            // Allow characters like < or > without escaping them
            builder.disableHtmlEscaping();

            this.addon = addon;
            this.gson = builder.setPrettyPrinting().create();
        }


        /**
         * This method returns json object that is parsed to string. Json object is made from given instance.
         * @param instance Instance that must be parsed to json string.
         * @return String that contains JSON information from instance object.
         */
        String toJsonString(DefaultDataHolder instance)
        {
            // Null check
            if (instance == null)
            {
                this.addon.logError("JSON database request to store a null. ");
                return null;
            }

            return this.gson.toJson(instance);
        }


        /**
         * This method creates and adds to list all objects from default.json file.
         * @return List of all objects from default.json that is with T instance.
         */
        DefaultDataHolder loadObject(String fileName)
        {
            if (!fileName.endsWith(".json"))
            {
                fileName = fileName + ".json";
            }

            File defaultFile = new File(this.addon.getDataFolder(), fileName);

            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(defaultFile), StandardCharsets.UTF_8))
            {
                DefaultDataHolder object = this.gson.fromJson(reader, DefaultDataHolder.class);

                reader.close(); // NOSONAR Required to keep OS file handlers low and not rely on GC

                return object;
            }
            catch (FileNotFoundException e)
            {
                this.addon.logError("Could not load file '" + defaultFile.getName() + "': File not found.");
            }
            catch (Exception e)
            {
                this.addon.logError("Could not load objects " + defaultFile.getName() + " " + e.getMessage());
            }

            return null;
        }


        /**
         * This method creates and adds to list all objects from default.json file.
         * @return List of all objects from default.json that is with T instance.
         */
        DefaultDataHolder loadWebObject(String downloadedObject)
        {
            return this.gson.fromJson(downloadedObject, DefaultDataHolder.class);
        }


        // ---------------------------------------------------------------------
        // Section: Variables
        // ---------------------------------------------------------------------


        /**
         * Holds JSON builder object.
         */
        private final Gson gson;

        /**
         * Holds Addon object.
         */
        private final Addon addon;
    }


    /**
     * This is simple object that will allow to store all current sticks
     * in single file.
     */
    private static final class DefaultDataHolder implements DataObject
    {
        /**
         * Default constructor. Creates object with empty lists.
         */
        DefaultDataHolder()
        {
            this.magicStickList = Collections.emptyList();
            this.version = "";
        }


        /**
         * This method returns stored sticks list.
         * @return list that contains default sticks.
         */
        List<MagicStickObject> getMagicStickList()
        {
            return magicStickList;
        }


        /**
         * This method sets given list as default sticks list.
         * @param magicStickList new default sticks list.
         */
        void setMagicStickList(List<MagicStickObject> magicStickList)
        {
            this.magicStickList = magicStickList;
        }


        /**
         * This method returns the version value.
         * @return the value of version.
         */
        public String getVersion()
        {
            return version;
        }


        /**
         * This method sets the version value.
         * @param version the version new value.
         *
         */
        public void setVersion(String version)
        {
            this.version = version;
        }


        /**
         * @return default.json
         */
        @Override
        public String getUniqueId()
        {
            return "default.json";
        }


        /**
         * @param uniqueId - unique ID the uniqueId to set
         */
        @Override
        public void setUniqueId(String uniqueId)
        {
            // method not used.
        }


        // ---------------------------------------------------------------------
        // Section: Variables
        // ---------------------------------------------------------------------


        /**
         * Holds a list with default sticks.
         */
        @Expose
        private List<MagicStickObject> magicStickList;

        /**
         * Holds a variable that stores in which addon version file was made.
         */
        @Expose
        private String version;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    private final CauldronWitcheryAddon addon;
}