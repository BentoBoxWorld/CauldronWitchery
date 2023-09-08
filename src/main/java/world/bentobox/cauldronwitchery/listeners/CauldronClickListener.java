package world.bentobox.cauldronwitchery.listeners;


import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import world.bentobox.bentobox.api.flags.FlagListener;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.tasks.RecipeProcessingTask;
import world.bentobox.cauldronwitchery.utils.Utils;


/**
 * This is main listener that checks for clicks on cauldrons.
 */
public class CauldronClickListener extends FlagListener implements Listener
{
    /**
     * Default constructor
     * @param addon CauldronWitcheryAddon object.
     */
    public CauldronClickListener(CauldronWitcheryAddon addon)
    {
        this.addon = addon;
    }


    /**
     * This method check interact event on cauldron
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onCauldronClick(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK ||
            event.getPlayer().isSneaking())
        {
            // Return if action is not right-click or player is sneaking.
            return;
        }

        if (event.getClickedBlock() == null ||
            !event.getClickedBlock().getType().name().contains("CAULDRON"))
        {
            // Return if clocked block is not any type of cauldron.
            return;
        }

        if (!event.hasItem() ||
            event.getHand() == null ||
            !(event.getHand().equals(EquipmentSlot.HAND) ||
                event.getHand().equals(EquipmentSlot.OFF_HAND)))
        {
            // Return if event is not produced with item or player hand is null
            return;
        }

        // Gets the user who clicks.
        User user = User.getInstance(event.getPlayer());

        if (user == null)
        {
            // This cannot never happen. The only reason it is here, is because in 1.17.3 it is annotated
            // with @Nullable.
            return;
        }

        if (!this.addon.getAddonManager().isMagicStick(event.getPlayer().getInventory().getItemInMainHand(), user))
        {
            // Return if event item is not a magic stick.
            return;
        }

        // All other cases are processed.
        Block block = event.getClickedBlock();

        // Cancel event is cancelled.
        event.setCancelled(true);

        if (event.getHand().equals(EquipmentSlot.OFF_HAND))
        {
            // If event is produced with off-hand, return as action is processed in HAND click.
            // Just cancel to prevent block placement.
            return;
        }

        // Now check the island.
        Optional<Island> islandOptional = this.addon.getIslands().getIslandAt(block.getLocation());

        if (islandOptional.isEmpty())
        {
            // If user does not have an island, return message.
            Utils.sendMessage(user, user.getTranslation("general.errors.no-island"));
            return;
        }

        if (!this.checkIsland(event,
            event.getPlayer(),
            block.getLocation(),
            CauldronWitcheryAddon.CAULDRON_WITCHERY_ISLAND_PROTECTION))
        {
            // Check protection flag for island.
            return;
        }

        Collection<Entity> nearbyEntities =
            block.getWorld().getNearbyEntities(block.getBoundingBox(),
                entity -> EntityType.DROPPED_ITEM.equals(entity.getType()));

        // Run the recipe processing task in next tick.
        Bukkit.getScheduler().runTaskAsynchronously(this.addon.getPlugin(),
            () -> new RecipeProcessingTask(this.addon,
                user,
                block,
                this.addon.getAddonManager().getMagicStick(event.getItem(), user),
                nearbyEntities).run());
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * This variable store cauldron witchery addon object.
     */
    private final CauldronWitcheryAddon addon;
}
