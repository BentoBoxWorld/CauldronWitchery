//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.listeners;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;


/**
 * The type Items inside cauldron events.
 */
public class ItemsInsideCauldronListener implements Listener
{
    /**
     * Instantiates a new Items inside cauldron listener.
     *
     * @param addon the addon
     */
    public ItemsInsideCauldronListener(CauldronWitcheryAddon addon)
    {
        this.addon = addon;
    }


    /**
     * On item burn in lava cauldron.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemBurnInLavaCauldron(EntityDamageEvent event)
    {
        if (!this.addon.getSettings().isMixInCauldron())
        {
            // Mixing in cauldron disabled.
            return;
        }

        if (!this.addon.getPlugin().getIWM().inWorld(event.getEntity().getWorld()))
        {
            // Not a gamemode world. CauldronWitchery does not operate there.
            return;
        }

        // Dropped items inside lava cauldrons should not burn.
        if (EntityType.DROPPED_ITEM.equals(event.getEntityType()) &&
            (EntityDamageEvent.DamageCause.LAVA.equals(event.getCause()) ||
                EntityDamageEvent.DamageCause.FIRE_TICK.equals(event.getCause())))
        {
            Block block = event.getEntity().getLocation().getBlock();

            if (Material.LAVA_CAULDRON.equals(block.getType()))
            {
                event.setCancelled(true);
            }
        }
    }


    /**
     * On item despawn in cauldron.
     *
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDespawnInCauldron(ItemDespawnEvent event)
    {
        if (!this.addon.getSettings().isMixInCauldron())
        {
            // Mixing in cauldron disabled.
            return;
        }

        if (!this.addon.getPlugin().getIWM().inWorld(event.getEntity().getWorld()))
        {
            // Not a gamemode world. CauldronWitchery does not operate there.
            return;
        }

        // Dropped items inside cauldrons should not despawn.
        if (EntityType.DROPPED_ITEM.equals(event.getEntityType()))
        {
            Block block = event.getEntity().getLocation().getBlock();

            if (Material.LAVA_CAULDRON.equals(block.getType()) ||
                Material.WATER_CAULDRON.equals(block.getType()) ||
                Material.POWDER_SNOW_CAULDRON.equals(block.getType()) ||
                Material.CAULDRON.equals(block.getType()))
            {
                event.setCancelled(true);
            }
        }
    }


    /**
     * Addon instance.
     */
    private final CauldronWitcheryAddon addon;
}
