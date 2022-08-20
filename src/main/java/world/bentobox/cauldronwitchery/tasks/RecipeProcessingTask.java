//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.tasks;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                filter(entity -> entity instanceof Item).
                map(entity -> ((Item) entity).getItemStack()).
                collect(Collectors.toList()));
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

        // Filter available recipes, based on cauldron type, item in main offhand and recipes that contains
        // at least 1 of the ingredient type.

        // This list will contain last error message for the failure.
        StringBuilder lastErrorMessage = new StringBuilder();

        List<Recipe> recipeList = this.magicStick.getRecipeList().stream().
            filter(recipe -> recipe.getMainIngredient() != null).
            filter(recipe -> recipe.getMainIngredient().isSimilar(itemInOffHand)).
            collect(Collectors.toList());

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
            // Fulfill requirements
            this.fulFillRecipe(recipeOptional.get(), itemInOffHand);

            // Damage magic stick
            if (this.addon.getSettings().getSuccessDamageAmount() > 0)
            {
                if (itemInMainHand.getItemMeta() instanceof Damageable damageable)
                {
                    damageable.setDamage(this.addon.getSettings().getSuccessDamageAmount());
                    itemInMainHand.setItemMeta(damageable);
                }
                else
                {
                    // Remove item.
                    itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);
                }
            }

            // Remove items that are inside cauldron
            if (this.addon.getSettings().isMixInCauldron())
            {
                if (this.addon.getSettings().isRemoveLeftOvers())
                {
                    // Remove all cauldron items.
                    Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                        () -> this.cauldronEntities.forEach(Entity::remove));
                }
            }

            // Run consumer.
            Bukkit.getScheduler().runTaskTimer(this.addon.getPlugin(),
                new SuccessConsumer(this.block, this.user),
                0,
                1);
        }
        else
        {
            // Remove items that are inside cauldron
            if (this.addon.getSettings().isMixInCauldron() && this.addon.getSettings().isRemoveOnFail())
            {
                // Remove all cauldron items on fail.
                Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                    () -> this.cauldronEntities.forEach(Entity::remove));
            }

            // Damage magic stick
            if (this.addon.getSettings().getErrorDamageAmount() > 0)
            {
                if (itemInMainHand.getItemMeta() instanceof Damageable damageable)
                {
                    damageable.setDamage(this.addon.getSettings().getErrorDamageAmount());
                    itemInMainHand.setItemMeta(damageable);
                }
                else
                {
                    // Remove item.
                    itemInMainHand.setAmount(itemInMainHand.getAmount() - 1);
                }
            }

            // Reduce main ingredient count by 1 on fail.
            itemInOffHand.setAmount(itemInOffHand.getAmount() - 1);

            // Check which message should be sent.
            String errorMessage = this.addon.getSettings().isCorrectErrorMessage() ?
                lastErrorMessage.toString() :
                this.user.getTranslation(Constants.MESSAGES + "something-went-wrong");

            // Run task every tick.
            Bukkit.getScheduler().runTaskTimer(this.addon.getPlugin(),
                new ErrorConsumer(this.block, this.user, errorMessage, this.addon.getSettings().isErrorDestroyCauldron()),
                0,
                1);
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
        BlockData blockData = this.block.getBlockData();

        if (blockData instanceof Levelled)
        {
            if (recipe.getCauldronLevel() > ((Levelled) blockData).getLevel())
            {
                // Recipe cannot be fulfilled.
                errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "not-filled-cauldron"));
                return false;
            }
        }

        // Check cauldron temperature based on block below.
        if (recipe.getTemperature() == Recipe.Temperature.COOL)
        {
            Material type = this.block.getRelative(BlockFace.DOWN).getType();

            if (type != Material.ICE &&
                type != Material.PACKED_ICE &&
                type != Material.BLUE_ICE &&
                type != Material.FROSTED_ICE &&
                type != Material.SNOW_BLOCK &&
                type != Material.POWDER_SNOW)
            {
                // Recipe cannot be fulfilled.
                errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "too-hot-cauldron"));
                return false;
            }
        }
        else if (recipe.getTemperature() == Recipe.Temperature.HEAT)
        {
            Block relative = this.block.getRelative(BlockFace.DOWN);
            Material type = relative.getType();

            boolean heat = false;

            if (type == Material.CAMPFIRE ||
                type == Material.SOUL_CAMPFIRE ||
                type == Material.FURNACE ||
                type == Material.BLAST_FURNACE ||
                type == Material.SMOKER ||
                type.name().contains("CANDLE"))
            {
                if (relative.getBlockData() instanceof Lightable data)
                {
                    heat = data.isLit();
                }
            }
            else if (type == Material.FIRE ||
                type == Material.LAVA ||
                type == Material.LAVA_CAULDRON)
            {
                heat = true;
            }

            if (!heat)
            {
                // Recipe cannot be fulfilled.
                errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "too-cold-cauldron"));
                return false;
            }
        }
        else
        {
            Block relative = this.block.getRelative(BlockFace.DOWN);
            Material type = relative.getType();

            boolean heat = false;
            boolean cool = false;

            if (type == Material.CAMPFIRE ||
                type == Material.SOUL_CAMPFIRE ||
                type == Material.FURNACE ||
                type == Material.BLAST_FURNACE ||
                type == Material.SMOKER ||
                type.name().contains("CANDLE"))
            {
                if (relative.getBlockData() instanceof Lightable data)
                {
                    heat = data.isLit();
                }
            }
            else if (type == Material.FIRE ||
                type == Material.LAVA ||
                type == Material.LAVA_CAULDRON)
            {
                heat = true;
            }
            else if (type == Material.ICE ||
                type == Material.PACKED_ICE ||
                type == Material.BLUE_ICE ||
                type == Material.FROSTED_ICE ||
                type == Material.SNOW_BLOCK ||
                type == Material.POWDER_SNOW)
            {
                cool = true;
            }

            if (heat)
            {
                // Recipe cannot be fulfilled.
                errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "too-hot-cauldron"));
                return false;
            }
            else if (cool)
            {
                // Recipe cannot be fulfilled.
                errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "too-cold-cauldron"));
                return false;
            }
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

        if (this.addon.getSettings().isMixInCauldron() && this.addon.getSettings().isExactExtraCount())
        {
            if (!this.exactIngredients(recipe.getExtraIngredients()))
            {
                // Requires exact items but something was wrong.
                errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "too-much-ingredients"));
                return false;
            }
        }
        else if (this.missingIngredients(recipe.getExtraIngredients()))
        {
            // Missing ingredients.
            errorMessages.append(this.user.getTranslation(Constants.MESSAGES + "missing-extra-ingredients"));
            return false;
        }

        // Return true.
        return true;
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
        else if (amount <= 0)
        {
            return true;
        }
        else
        {
            if (this.addon.getSettings().isMixInCauldron())
            {
                // Check dropped items in cauldron.

                for (ItemStack dropped : this.cauldronItems)
                {
                    if (item.isSimilar(dropped) && (amount -= dropped.getAmount()) <= 0)
                    {
                        return true;
                    }
                }
            }
            else
            {
                // Check player inventory.
                return Objects.requireNonNull(this.user.getInventory()).
                    containsAtLeast(item, amount);
            }

            return false;
        }
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
            collect(Collectors.toList());

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
        if (recipe.getCauldronType() == Material.WATER_CAULDRON ||
            recipe.getCauldronType() == Material.POWDER_SNOW_CAULDRON ||
            recipe.getCauldronType() == Material.LAVA_CAULDRON)
        {
            BlockData blockData = this.block.getBlockData();

            if (blockData instanceof Levelled levelled)
            {
                int level = levelled.getLevel() - recipe.getCauldronLevel();

                if (level <= 0)
                {
                    // Empty cauldron type.
                    Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                        () -> this.block.setType(Material.CAULDRON));
                }
                else
                {
                    // Apply level change
                    levelled.setLevel(level);
                    this.block.setBlockData(levelled);
                }
            }
            else
            {
                // Empty cauldron type.
                Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                    () -> this.block.setType(Material.CAULDRON));
            }
        }

        // Remove items.
        if (this.addon.getSettings().isMixInCauldron())
        {
            // Check only if remove left-overs are disabled. Otherwise, items will be removed
            // at the end.
            if (!this.addon.getSettings().isRemoveLeftOvers())
            {
                // Remove extra ingredients from player inventory.
                recipe.getExtraIngredients().forEach(ingredient -> {
                    int amount = ingredient.getAmount();

                    for (Entity entity : this.cauldronEntities)
                    {
                        if (entity instanceof Item itemEntity)
                        {
                            if (amount > 0)
                            {
                                ItemStack itemStack = itemEntity.getItemStack();

                                if (ingredient.isSimilar(itemStack))
                                {
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

                                        // Remove all cauldron items on fail.
                                        Bukkit.getScheduler().runTask(this.addon.getPlugin(),
                                            itemEntity::remove);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
        else
        {
            // Remove extra ingredients from player inventory.
            recipe.getExtraIngredients().forEach(ingredient -> {
                PlayerInventory inventory = Objects.requireNonNull(this.user.getInventory());

                List<ItemStack> itemsInInventory = Arrays.stream(inventory.getContents()).
                    filter(Objects::nonNull).
                    filter(ingredient::isSimilar).
                    collect(Collectors.toList());

                int amount = ingredient.getAmount();

                for (ItemStack itemStack : itemsInInventory)
                {
                    if (amount > 0)
                    {
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
            });
        }

        // Produce outcome.
        if (recipe instanceof BookRecipe bookRecipe)
        {
            this.produceRecipe(bookRecipe);
        }
        else if (recipe instanceof EntityRecipe entityRecipe)
        {
            this.produceRecipe(entityRecipe);
        }
        else if (recipe instanceof ItemRecipe itemStackRecipe)
        {
            this.produceRecipe(itemStackRecipe);
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
                this.block.getWorld().spawnParticle(Particle.SPELL_WITCH,
                    this.center, 5, 0.10D, 0.2D, 0.10D, 2);
            }
            else if (this.counter < 70)
            {
                if (this.counter % 6 == 0)
                {
                    this.block.getWorld().spawnParticle(Particle.EXPLOSION_LARGE,
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
                this.block.getWorld().spawnParticle(Particle.SPELL_WITCH,
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
                this.block.getWorld().spawnParticle(Particle.SPELL,
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
