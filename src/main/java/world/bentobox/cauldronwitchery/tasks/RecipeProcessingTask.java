//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.tasks;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.database.object.MagicStickObject;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.EntityRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.ItemRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.utils.Constants;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * The Recipe Processing task.
 */
public class RecipeProcessingTask implements Runnable
{
    /**
     * Instantiates a new Recipe processing task.
     *
     * @param addon the addon
     * @param user the user
     * @param block the block
     * @param magicStick the magic stick
     * @param cauldronEntities List of dropped items inside cauldron.
     */
    public RecipeProcessingTask(CauldronWitcheryAddon addon,
        User user,
        Block block,
        MagicStickObject magicStick,
        Collection<Entity> cauldronEntities)
    {
        this.addon = addon;
        this.user = user;
        this.block = block;
        this.magicStick = magicStick;

        this.cauldronEntities = cauldronEntities;

        // Transform entities into item stacks.
        this.cauldronItems = Utils.groupEqualItems(
            cauldronEntities.
                stream().
                // Filter is not necessary, but just in case.
                filter(Item.class::isInstance).
                map(entity -> ((Item) entity).getItemStack()).
                toList());
    }


    /**
     * Runnable task for the recipe processing.
     */
    @Override
    public void run()
    {
        // Get items in player hands.
        ItemStack itemInOffHand = Objects.requireNonNull(this.user.getInventory()).getItemInOffHand();
        ItemStack itemInMainHand = Objects.requireNonNull(this.user.getInventory()).getItemInMainHand();

        // This list will contain last error message for the failure.
        StringBuilder lastErrorMessage = new StringBuilder();

        // Filter available recipes, based on cauldron type, item in main offhand and recipes that contains
        // at least 1 of the ingredient type.
        List<Recipe> recipeList = this.findCandidateRecipes(itemInOffHand, lastErrorMessage);

        // Select any recipe that matches outcome.
        Optional<Recipe> recipeOptional;

        if (this.missingPermissions(this.magicStick.getPermissions()))
        {
            lastErrorMessage = new StringBuilder();
            lastErrorMessage.append(this.user.getTranslation(Constants.MESSAGES + "missing-permissions"));

            // Set empty as recipe fails.
            recipeOptional = Optional.empty();
        }
        else
        {
            // Find any from remaining.
            recipeOptional = recipeList.stream().findAny();
        }

        if (recipeOptional.isPresent() &&
            this.didMagicWorked(recipeOptional.get(), itemInOffHand, lastErrorMessage))
        {
            this.onSuccess(recipeOptional.get(), itemInOffHand, itemInMainHand);
        }
        else
        {
            this.onFailure(itemInOffHand, itemInMainHand, lastErrorMessage.toString());
        }
    }


    /**
     * Filters magic stick recipes down to those that match the offhand item, cauldron type and contain
     * at least one of each extra ingredient. Error messages for every failed filter step are appended.
     *
     * @param itemInOffHand Item in player offhand.
     * @param lastErrorMessage Collector for error messages.
     * @return Mutable list of candidate recipes.
     */
    private List<Recipe> findCandidateRecipes(ItemStack itemInOffHand, StringBuilder lastErrorMessage)
    {
        List<Recipe> recipeList = this.magicStick.getRecipeList().stream().
            filter(Recipe::isValid).
            filter(recipe -> recipe.getMainIngredient().isSimilar(itemInOffHand)).
            collect(Collectors.toCollection(ArrayList::new));

        if (recipeList.isEmpty())
        {
            // Missing main ingredient
            lastErrorMessage.append(this.user.getTranslation(Constants.MESSAGES + "incorrect-main-ingredient"));
        }
        else
        {
            // Filter by cauldron type.
            recipeList.removeIf(recipe -> !recipe.getCauldronType().equals(this.block.getType()));
        }

        if (recipeList.isEmpty())
        {
            // Missing main ingredient
            lastErrorMessage.append(this.user.getTranslation(Constants.MESSAGES + "incorrect-cauldron"));
        }
        else
        {
            // Filter by extra ingredients.
            recipeList.removeIf(recipe -> !recipe.getExtraIngredients().stream().allMatch(
                ingredient -> this.containsAtLeast(ingredient, 1)));
        }

        if (recipeList.isEmpty())
        {
            // Missing extra ingredients
            lastErrorMessage.append(this.user.getTranslation(Constants.MESSAGES + "incorrect-extra-ingredients"));
        }

        return recipeList;
    }


    /**
     * Processes successful recipe: consumes requirements, damages stick, clears cauldron and plays effects.
     */
    private void onSuccess(Recipe recipe, ItemStack itemInOffHand, ItemStack itemInMainHand)
    {
        // Fulfill requirements
        this.fulFillRecipe(recipe, itemInOffHand);

        // Damage magic stick
        this.damageMagicStick(itemInMainHand, this.addon.getSettings().getSuccessDamageAmount());

        // Remove items that are inside cauldron
        if (this.addon.getSettings().isMixInCauldron() && this.addon.getSettings().isRemoveLeftOvers())
        {
            // Remove all cauldron items.
            Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                () -> this.cauldronEntities.forEach(Entity::remove));
        }

        // Run consumer.
        Bukkit.getScheduler().runTaskTimer(this.addon.getPlugin(),
            new SuccessConsumer(this.block, this.user),
            0,
            1);
    }


    /**
     * Processes failed recipe: removes items if configured, damages stick, consumes one main ingredient
     * and plays error effects.
     */
    private void onFailure(ItemStack itemInOffHand, ItemStack itemInMainHand, String lastErrorMessage)
    {
        // Remove items that are inside cauldron
        if (this.addon.getSettings().isMixInCauldron() && this.addon.getSettings().isRemoveOnFail())
        {
            // Remove all cauldron items on fail.
            Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                () -> this.cauldronEntities.forEach(Entity::remove));
        }

        // Damage magic stick
        this.damageMagicStick(itemInMainHand, this.addon.getSettings().getErrorDamageAmount());

        // Reduce main ingredient count by 1 on fail.
        itemInOffHand.setAmount(itemInOffHand.getAmount() - 1);

        // Check which message should be sent.
        String errorMessage = this.addon.getSettings().isCorrectErrorMessage() ?
            lastErrorMessage :
            this.user.getTranslation(Constants.MESSAGES + "something-went-wrong");

        // Run task every tick.
        Bukkit.getScheduler().runTaskTimer(this.addon.getPlugin(),
            new ErrorConsumer(this.block, this.user, errorMessage, this.addon.getSettings().isErrorDestroyCauldron()),
            0,
            1);
    }


    /**
     * Applies damage to the magic stick. Items that cannot be damaged lose one from their stack instead.
     *
     * @param magicStick Magic stick item.
     * @param damage Damage amount; nothing happens if it is not positive.
     */
    private void damageMagicStick(ItemStack magicStick, int damage)
    {
        if (damage <= 0)
        {
            return;
        }

        if (magicStick.getItemMeta() instanceof Damageable damageable)
        {
            damageable.setDamage(damage);
            magicStick.setItemMeta(damageable);
        }
        else
        {
            // Remove item.
            magicStick.setAmount(magicStick.getAmount() - 1);
        }
    }


    /**
     * Did magic worked boolean.
     *
     * @param recipe the recipe list
     * @param mainIngredient the main ingredient
     * @return the boolean
     */
    private boolean didMagicWorked(Recipe recipe, ItemStack mainIngredient, StringBuilder errorMessages)
    {
        if (this.block.getBlockData() instanceof Levelled level && recipe.getCauldronLevel() > level.getLevel())
        {
            // Recipe cannot be fulfilled.
            errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "not-filled-cauldron"));
            return false;
        }

        // Check cauldron temperature based on block below.
        String temperatureError = this.getTemperatureError(recipe.getTemperature());

        if (temperatureError != null)
        {
            // Recipe cannot be fulfilled.
            errorMessages.append(this.user.getTranslation(Constants.MESSAGES + temperatureError));
            return false;
        }

        if (recipe.getMainIngredient().getAmount() > mainIngredient.getAmount())
        {
            // Missing main ingredient
            errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "missing-main-ingredient"));
            return false;
        }

        if (recipe.getExperience() > Utils.getTotalExperience(this.user.getPlayer()))
        {
            // Not enough experience
            errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "missing-knowledge"));
            return false;
        }

        if (this.missingPermissions(recipe.getPermissions()))
        {
            // Missing permissions.
            errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "missing-permissions"));
            return false;
        }

        boolean exactCount = this.addon.getSettings().isMixInCauldron() && this.addon.getSettings().isExactExtraCount();

        if (exactCount && !this.exactIngredients(recipe.getExtraIngredients()))
        {
            // Requires exact items but something was wrong.
            errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "too-much-ingredients"));
            return false;
        }

        if (!exactCount && this.missingIngredients(recipe.getExtraIngredients()))
        {
            // Missing ingredients.
            errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "missing-extra-ingredients"));
            return false;
        }

        // Return true.
        return true;
    }


    /**
     * Checks whether the block below the cauldron provides the temperature that recipe requires.
     *
     * @param temperature Required temperature.
     * @return Message key suffix for the temperature error, or null if temperature is correct.
     */
    private String getTemperatureError(Recipe.Temperature temperature)
    {
        Block relative = this.block.getRelative(BlockFace.DOWN);

        boolean heat = isHeatSource(relative);
        boolean cool = isCoolSource(relative.getType());

        return switch (temperature)
        {
            case COOL -> cool ? null : "too-hot-cauldron";
            case HEAT -> heat ? null : "too-cold-cauldron";
            default -> {
                if (heat)
                {
                    yield "too-hot-cauldron";
                }

                yield cool ? "too-cold-cauldron" : null;
            }
        };
    }


    /**
     * Returns if given block is an active heat source.
     * @param block Block below cauldron.
     * @return {@code true} if block provides heat.
     */
    private static boolean isHeatSource(Block block)
    {
        Material type = block.getType();

        if (type == Material.CAMPFIRE ||
            type == Material.SOUL_CAMPFIRE ||
            type == Material.FURNACE ||
            type == Material.BLAST_FURNACE ||
            type == Material.SMOKER ||
            type.name().contains("CANDLE"))
        {
            return block.getBlockData() instanceof Lightable data && data.isLit();
        }

        return type == Material.FIRE ||
            type == Material.LAVA ||
            type == Material.LAVA_CAULDRON;
    }


    /**
     * Returns if given material is a cold source.
     * @param type Material below cauldron.
     * @return {@code true} if material provides cold.
     */
    private static boolean isCoolSource(Material type)
    {
        return type == Material.ICE ||
            type == Material.PACKED_ICE ||
            type == Material.BLUE_ICE ||
            type == Material.FROSTED_ICE ||
            type == Material.SNOW_BLOCK ||
            type == Material.POWDER_SNOW;
    }


    /**
     * Returns if user miss any of given permissions.
     * @param permissions List of permissions
     * @return {@code true} if some permissions is missing, {@code false} otherwise.
     */
    private boolean missingPermissions(Collection<String> permissions)
    {
        return !permissions.stream().allMatch(this.user::hasPermission);
    }


    /**
     * Returns if user miss any of given itemStacks.
     * @param itemStacks List of itemStacks
     * @return {@code true} if some itemStacks is missing, {@code false} otherwise.
     */
    private boolean missingIngredients(List<ItemStack> itemStacks)
    {
        if (this.addon.getSettings().isMixInCauldron())
        {
            // Check dropped items in cauldron.
            return !itemStacks.stream().allMatch(this::containsAtLeast);
        }
        else
        {
            // Check player inventory.
            return !itemStacks.stream().allMatch(itemStack ->
                Objects.requireNonNull(this.user.getInventory()).
                    containsAtLeast(itemStack, itemStack.getAmount()));
        }
    }


    /**
     * This method checks if there is at least amount of items inside cauldron.
     * @param item Item that must be searched.
     * @return {@code true} it there is at least amount of items, {@code false} otherwise.
     */
    private boolean containsAtLeast(ItemStack item)
    {
        return this.containsAtLeast(item, item.getAmount());
    }


    /**
     * This method checks if there is at least amount of items inside cauldron.
     * @param item Item that must be searched.
     * @param amount Amount of items.
     * @return {@code true} it there is at least amount of items, {@code false} otherwise.
     */
    private boolean containsAtLeast(ItemStack item, int amount)
    {
        if (item == null)
        {
            return false;
        }

        if (amount <= 0)
        {
            return true;
        }

        if (!this.addon.getSettings().isMixInCauldron())
        {
            // Check player inventory.
            return Objects.requireNonNull(this.user.getInventory()).
                containsAtLeast(item, amount);
        }

        // Check dropped items in cauldron.
        int remaining = amount;

        for (ItemStack dropped : this.cauldronItems)
        {
            if (item.isSimilar(dropped))
            {
                remaining -= dropped.getAmount();

                if (remaining <= 0)
                {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * This method returns if cauldronItems has exactly the same count and type as recipe ingredients.
     * @param extraIngredients Recipe Ingredients.
     * @return {@code true} if recipe has exact items dropped in cauldron, {@code false} otherwise.
     */
    private boolean exactIngredients(List<ItemStack> extraIngredients)
    {
        // Clone cauldron items.
        List<ItemStack> clonedList = this.cauldronItems.stream().
            map(ItemStack::clone).
            collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        for (ItemStack extra : extraIngredients)
        {
            int amount = extra.getAmount();

            for (Iterator<ItemStack> iterator = clonedList.iterator(); iterator.hasNext();)
            {
                ItemStack dropped = iterator.next();

                if (extra.isSimilar(dropped))
                {
                    amount -= dropped.getAmount();
                    iterator.remove();
                }
            }

            if (amount != 0)
            {
                return false;
            }
        }

        return clonedList.isEmpty();
    }


    /**
     * This method fulfills the recipe. It removes requirements and produces recipes outcome.
     * @param recipe Recipe that is fulfilled.
     * @param offhandItem Offhand item.
     */
    private void fulFillRecipe(Recipe recipe, ItemStack offhandItem)
    {
        // Reduce main ingredient
        offhandItem.setAmount(offhandItem.getAmount() - recipe.getMainIngredient().getAmount());

        // Reduce player xp.
        int experience = Utils.getTotalExperience(this.user.getPlayer()) - (int) recipe.getExperience();
        Utils.setTotalExperience(this.user.getPlayer(), experience);

        // Empty the cauldron
        this.emptyCauldron(recipe);

        // Remove items.
        if (!this.addon.getSettings().isMixInCauldron())
        {
            // Remove extra ingredients from player inventory.
            recipe.getExtraIngredients().forEach(this::removeFromInventory);
        }
        else if (!this.addon.getSettings().isRemoveLeftOvers())
        {
            // Remove only used items, if left-overs are kept. Otherwise, all items will be removed
            // at the end.
            recipe.getExtraIngredients().forEach(this::removeFromCauldron);
        }

        // Produce outcome.
        switch (recipe)
        {
            case BookRecipe bookRecipe -> this.produceRecipe(bookRecipe);
            case EntityRecipe entityRecipe -> this.produceRecipe(entityRecipe);
            case ItemRecipe itemStackRecipe -> this.produceRecipe(itemStackRecipe);
            default -> {
                // Unknown recipe types produce nothing.
            }
        }

        // Record statistics. Make sure the recipe has an id before it is counted.
        if (recipe.getRecipeId().isBlank() && this.addon.getAddonManager().assignRecipeIds(this.magicStick))
        {
            this.addon.getAddonManager().saveMagicStick(this.magicStick);
        }

        this.addon.getAddonManager().recordRecipeUse(this.user, this.block.getWorld(), recipe);
    }


    /**
     * Lowers cauldron level by the amount recipe consumes, or empties it.
     * @param recipe Recipe that is fulfilled.
     */
    private void emptyCauldron(Recipe recipe)
    {
        if (recipe.getCauldronType() != Material.WATER_CAULDRON &&
            recipe.getCauldronType() != Material.POWDER_SNOW_CAULDRON &&
            recipe.getCauldronType() != Material.LAVA_CAULDRON)
        {
            return;
        }

        if (this.block.getBlockData() instanceof Levelled levelled &&
            levelled.getLevel() - recipe.getCauldronLevel() > 0)
        {
            // Apply level change
            levelled.setLevel(levelled.getLevel() - recipe.getCauldronLevel());
            Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                () -> this.block.setBlockData(levelled));
        }
        else
        {
            // Empty cauldron type.
            Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                () -> this.block.setType(Material.CAULDRON));
        }
    }


    /**
     * Removes given ingredient amount from items dropped inside the cauldron.
     * @param ingredient Ingredient that must be removed.
     */
    private void removeFromCauldron(ItemStack ingredient)
    {
        int amount = ingredient.getAmount();

        for (Entity entity : this.cauldronEntities)
        {
            if (amount <= 0)
            {
                return;
            }

            if (entity instanceof Item itemEntity && ingredient.isSimilar(itemEntity.getItemStack()))
            {
                ItemStack itemStack = itemEntity.getItemStack();

                // Remove either the full amount or the remaining amount
                if (itemStack.getAmount() > amount)
                {
                    itemStack.setAmount(itemStack.getAmount() - amount);
                    // Update entity item stack.
                    itemEntity.setItemStack(itemStack);
                    amount = 0;
                }
                else
                {
                    amount -= itemStack.getAmount();
                    Bukkit.getScheduler().runTask(this.addon.getPlugin(), itemEntity::remove);
                }
            }
        }
    }


    /**
     * Removes given ingredient amount from player inventory.
     * @param ingredient Ingredient that must be removed.
     */
    private void removeFromInventory(ItemStack ingredient)
    {
        PlayerInventory inventory = Objects.requireNonNull(this.user.getInventory());

        List<ItemStack> itemsInInventory = Arrays.stream(inventory.getContents()).
            filter(Objects::nonNull).
            filter(ingredient::isSimilar).
            toList();

        int amount = ingredient.getAmount();

        for (ItemStack itemStack : itemsInInventory)
        {
            if (amount <= 0)
            {
                return;
            }

            // Remove either the full amount or the remaining amount
            if (itemStack.getAmount() >= amount)
            {
                itemStack.setAmount(itemStack.getAmount() - amount);
                amount = 0;
            }
            else
            {
                amount -= itemStack.getAmount();
                itemStack.setAmount(0);
            }
        }
    }


    /**
     * This method fulfills book recipe and tries to summon a book.
     * @param recipe Book Recipe
     */
    private void produceRecipe(BookRecipe recipe)
    {
        ItemStack book = this.addon.getAddonManager().craftBook(recipe.getBookName(), this.user);

        if (book != null)
        {
            // Drop only if book exists.
            Bukkit.getScheduler().scheduleSyncDelayedTask(BentoBox.getInstance(),
                () -> this.user.getWorld().dropItemNaturally(this.block.getLocation().add(0, 2, 0), book),
                70L);
        }
    }


    /**
     * This method fulfills entity recipe and tries to summon an entity.
     * @param recipe Entity Recipe
     */
    private void produceRecipe(EntityRecipe recipe)
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(BentoBox.getInstance(),
            () -> this.user.getWorld().spawnEntity(this.block.getLocation().add(0, 2, 0), recipe.getEntityType()),
            70L);
    }


    /**
     * This method fulfills item recipe and tries to summon an item.
     * @param recipe Item Recipe
     */
    private void produceRecipe(ItemRecipe recipe)
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(BentoBox.getInstance(),
            () -> this.user.getWorld().dropItemNaturally(this.block.getLocation().add(0, 2, 0), recipe.getItemStack()),
            70L);
    }


// ---------------------------------------------------------------------
// Section: Classes
// ---------------------------------------------------------------------


    /**
     * This class contains consumer for failing the recipe.
     */
    private static class ErrorConsumer implements Consumer<BukkitTask>
    {
        /**
         * Instantiates a new Error consumer.
         *
         * @param block the block
         * @param user the user
         * @param errorMessage the error message
         * @param destroyCauldron the destroy cauldron
         */
        public ErrorConsumer(Block block, User user, String errorMessage, boolean destroyCauldron)
        {
            this.block = block;
            this.user = user;
            this.errorMessage = errorMessage;
            this.destroyCauldron = destroyCauldron;

            // Put particles in the middle and above water.
            this.center = this.block.getLocation().add(0.5D, 0.9D, 0.5D);
        }


        /**
         * Consumer acceptor.
         * @param bukkitTask BukkitTask.
         */
        @Override
        public void accept(BukkitTask bukkitTask)
        {
            if (this.counter == 0)
            {
                // Play brewing sound effect
                this.block.getWorld().playEffect(this.block.getLocation(),
                    Effect.BREWING_STAND_BREW,
                    10);
            }
            else if (this.counter < 50)
            {
                // All next ticks spawn witch particles
                this.block.getWorld().spawnParticle(Particle.WITCH,
                    this.center, 5, 0.10D, 0.2D, 0.10D, 2);
            }
            else if (this.counter < 70)
            {
                if (this.counter % 6 == 0)
                {
                    this.block.getWorld().spawnParticle(Particle.EXPLOSION,
                        this.center, 5, 0.10D, 0.2D, 0.10D, 2);

                    this.block.getWorld().playSound(this.block.getLocation(),
                        Sound.ENTITY_GENERIC_EXPLODE,
                        0.5f,
                        0.5f);
                }
            }
            else
            {
                // Display Error Message
                Utils.sendMessage(this.user, this.errorMessage);

                // Destroy Cauldron.
                if (this.destroyCauldron)
                {
                    this.block.getWorld().createExplosion(this.center,
                        3,
                        true,
                        true);
                }

                // Stop task after 50 ticks.
                bukkitTask.cancel();
            }

            // Increase counter.
            this.counter++;
        }


        /**
         * Tick counter.
         */
        private int counter;

        /**
         * Block that is targeted.
         */
        private final Block block;

        /**
         * Stores the particle spawn location.
         */
        private final Location center;

        /**
         * The user who will receive message.
         */
        private final User user;

        /**
         * Indicates that cauldron will be removed.
         */
        private final boolean destroyCauldron;

        /**
         * Error message that will be displayed.
         */
        private final String errorMessage;
    }


    /**
     * This class contains consumer for successful recipe.
     */
    private static class SuccessConsumer implements  Consumer<BukkitTask>
    {
        /**
         * Instantiates a new Success consumer.
         *
         * @param block the block
         * @param user the user
         */
        public SuccessConsumer(Block block, User user)
        {
            this.user = user;
            this.block = block;

            // Put particles in the middle and above water.
            this.center = this.block.getLocation().add(0.5D, 0.9D, 0.5D);
        }


        /**
         * Consumer acceptor.
         * @param bukkitTask BukkitTask.
         */
        @Override
        public void accept(BukkitTask bukkitTask)
        {
            if (this.counter == 0)
            {
                // Play brewing sound effect
                this.block.getWorld().playEffect(this.block.getLocation(),
                    Effect.BREWING_STAND_BREW,
                    10);
            }
            else if (this.counter < 50)
            {
                // All next ticks spawn witch particles
                this.block.getWorld().spawnParticle(Particle.WITCH,
                    this.center, 5, 0.10D, 0.2D, 0.10D, 2);
            }
            else if (this.counter < 70)
            {
                if (this.counter % 6 == 0)
                {
                    // Send 3 smokes up.
                    this.block.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,
                        this.center, 0, 0, 0.1, 0);
                }

                this.block.getWorld().playSound(this.block.getLocation(),
                    Sound.BLOCK_AMETHYST_CLUSTER_HIT,
                    0.5f,
                    0.5f);
                // All next ticks spawn witch particles
                this.block.getWorld().spawnParticle(Particle.ENCHANT,
                    this.center, 5, 0.10D, 0.2D, 0.10D, 2);
            }
            else
            {
                // Display Error Message
                this.user.getTranslation(Constants.MESSAGES + "it-worked");

                // Stop task after 50 ticks.
                bukkitTask.cancel();
            }

            this.counter++;
        }


        /**
         * Tick counter.
         */
        private int counter;

        /**
         * Stores the particle spawn location.
         */
        private final Location center;

        /**
         * Target block.
         */
        private final Block block;

        /**
         * User who receives message.
         */
        private final User user;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * Addon instance.
     */
    private final CauldronWitcheryAddon addon;

    /**
     * User instance.
     */
    private final User user;

    /**
     * Block instance.
     */
    private final Block block;

    /**
     * Magic Stick object instance.
     */
    private final MagicStickObject magicStick;

    /**
     * List of entities inside cauldron.
     */
    private final Collection<Entity> cauldronEntities;

    /**
     * List of items inside cauldron.
     */
    private final List<ItemStack> cauldronItems;
}
