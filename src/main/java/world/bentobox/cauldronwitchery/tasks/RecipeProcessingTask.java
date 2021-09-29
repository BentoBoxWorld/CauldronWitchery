//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.tasks;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
                filter(entity -> entity instanceof Item).
                map(entity -> ((Item) entity).getItemStack()).
                collect(Collectors.toList()));

        this.error = new LightningEffect(addon.getSettings().isErrorDamage(),
            addon.getSettings().isErrorHitPlayer(),
            addon.getSettings().isErrorDestroyCauldron(),
            addon.getSettings().getErrorTimings());
        this.success = new LightningEffect(addon.getSettings().isSuccessfulDamage(),
            addon.getSettings().isSuccessfulHitPlayer(),
            addon.getSettings().isSuccessfulDestroyCauldron(),
            addon.getSettings().getSuccessfulTimings());
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

        Optional<Recipe> recipeOptional = this.magicStick.getRecipeList().stream().
            filter(recipe -> recipe.getMainIngredient().isSimilar(itemInOffHand)).
            filter(recipe -> recipe.getCauldronType().equals(this.block.getType())).
            filter(recipe -> recipe.getExtraIngredients().stream().allMatch(
                ingredient -> this.containsAtLeast(ingredient, 1))).
            findFirst();

        // LightningEffect initialization.
        LightningEffect effect;

        if (recipeOptional.isPresent() && this.didMagicWorked(recipeOptional.get(), itemInOffHand))
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
                    this.cauldronEntities.forEach(Entity::remove);
                }
            }

            // Recipe worked.
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "it-is-alive"));
            effect = this.success;
        }
        else
        {
            // Remove items that are inside cauldron
            if (this.addon.getSettings().isMixInCauldron() && this.addon.getSettings().isRemoveOnFail())
            {
                // Remove all cauldron items on fail.
                this.cauldronEntities.forEach(Entity::remove);
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

            // If user does not have an island, return message.
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "something-went-wrong"));
            effect = this.error;
        }

        // Trigger Lightning effects
        if (effect.hasTimings())
        {
            // Get lightning target location
            Location location = effect.hitPlayer() ? this.user.getLocation() : this.block.getLocation();

            if (location == null)
            {
                // Just a null-check that cannot be triggered.
                return;
            }

            // Strike the lightning.
            for (Long time : effect.timings())
            {
                if (effect.damage())
                {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(BentoBox.getInstance(),
                        () -> this.user.getWorld().strikeLightning(location),
                        time);
                }
                else
                {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(BentoBox.getInstance(),
                        () -> this.user.getWorld().strikeLightningEffect(location),
                        time);
                }
            }

            // Destroy the cauldron
            if (effect.destroyCauldron())
            {
                this.block.setType(Material.AIR);
            }
        }
    }


    /**
     * Did magic worked boolean.
     *
     * @param recipe the recipe list
     * @param mainIngredient the main ingredient
     * @return the boolean
     */
    private boolean didMagicWorked(Recipe recipe, ItemStack mainIngredient)
    {
        BlockData blockData = this.block.getBlockData();

        if (blockData instanceof Levelled)
        {
            if (recipe.getCauldronLevel() > ((Levelled) blockData).getLevel())
            {
                // Recipe cannot be fulfilled.
                Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "not-filled-cauldron"));
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
                type != Material.FROSTED_ICE)
            {
                // Recipe cannot be fulfilled.
                Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "too-hot-cauldron"));
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
                Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "too-cold-cauldron"));
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
                type == Material.FROSTED_ICE)
            {
                cool = true;
            }

            if (heat)
            {
                // Recipe cannot be fulfilled.
                Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "too-hot-cauldron"));
                return false;
            }
            else if (cool)
            {
                // Recipe cannot be fulfilled.
                Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "too-cold-cauldron"));
                return false;
            }
        }

        if (recipe.getMainIngredient().getAmount() > mainIngredient.getAmount())
        {
            // Missing main ingredient
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "missing-main-ingredient"));
            return false;
        }

        if (recipe.getExperience() > this.user.getPlayer().getTotalExperience())
        {
            // Not enough experience
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "missing-knowledge"));
            return false;
        }

        if (this.missingPermissions(recipe.getPermissions()))
        {
            // Missing permissions.
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "missing-permissions"));
            return false;
        }

        if (this.addon.getSettings().isMixInCauldron() && this.addon.getSettings().isExactExtraCount())
        {
            if (!this.exactIngredients(recipe.getExtraIngredients()))
            {
                // Requires exact items but something was wrong.
                Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "missing-extra-ingredients"));
                return false;
            }
        }
        else if (this.missingIngredients(recipe.getExtraIngredients()))
        {
            // Missing ingredients.
            Utils.sendMessage(this.user, this.user.getTranslation(Constants.CONVERSATIONS + "missing-extra-ingredients"));
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
            for (ItemStack dropped : this.cauldronItems)
            {
                if (item.isSimilar(dropped) && (amount -= dropped.getAmount()) <= 0)
                {
                    return true;
                }
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
        this.user.getPlayer().setExp(recipe.getExperience());

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
                    this.block.setType(Material.CAULDRON);
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
                this.block.setType(Material.CAULDRON);
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

                                // Remove either the full amount or the remaining amount
                                if (itemStack.getAmount() >= amount)
                                {
                                    itemStack.setAmount(itemStack.getAmount() - amount);
                                    // Update entity item stack.
                                    itemEntity.setItemStack(itemStack);
                                    amount = 0;
                                }
                                else
                                {
                                    amount -= itemStack.getAmount();
                                    // Remove entity.
                                    itemEntity.remove();
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
// Section: Class
// ---------------------------------------------------------------------


    /**
     * The type Lightning effect.
     * @param damage damage effect
     * @param hitPlayer target player or cauldron
     * @param destroyCauldron destroy cauldron
     * @param timings List of lightning delays
     */
    private record LightningEffect(boolean damage, boolean hitPlayer, boolean destroyCauldron, List<Long> timings)
    {
        /**
         * Has timings.
         *
         * @return the boolean
         */
        public boolean hasTimings()
        {
            return !timings.isEmpty();
        }
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

    /**
     * The error lightning effect.
     */
    private final LightningEffect error;

    /**
     * The success lightning effect.
     */
    private final LightningEffect success;
}
