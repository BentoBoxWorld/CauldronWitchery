//
// Created by BONNe
// Copyright - 2021
//


package world.bentobox.cauldronwitchery.tasks;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Item;

import java.util.*;

import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;


/**
 * This class spawns particles for cauldrons that contains items.
 */
public class ParticleSpawnTask implements Runnable
{
    /**
     * Default constructor.
     * @param worldList List of worlds where particles must be summoned.
     */
    public ParticleSpawnTask(CauldronWitcheryAddon addon, List<World> worldList)
    {
        this.addon = addon;
        this.worldList = worldList;
    }


    /**
     * Default runner.
     */
    @Override
    public void run()
    {
        if (!this.addon.getSettings().isMixInCauldron())
        {
            // Do nothing if cauldrons do not contain items.
            return;
        }

        if (this.runCounter++ > 100)
        {
            // Update blocklist
            this.blockSet.clear();
            // Collect all cauldrons.
            this.worldList.stream().
                flatMap(world -> world.getEntitiesByClasses(Item.class).stream()).
                map(item -> item.getLocation().getBlock()).
                filter(block -> block.getType().name().contains("CAULDRON")).
                forEach(this.blockSet::add);
            this.runCounter = 0;
        }

        // Iterate through blocks to spawn particles.
        for (Iterator<Block> blockIterator = this.blockSet.iterator(); blockIterator.hasNext(); )
        {
            Block block = blockIterator.next();
            World world = block.getWorld();
            Material type = block.getType();

            if (!block.getType().name().contains("CAULDRON"))
            {
                // Remove block from iterator, as it is not cauldron.
                blockIterator.remove();
            }

            Location center = block.getLocation().add(0.5D, 0.9D, 0.5D);

            if (type == Material.LAVA_CAULDRON || this.isHeatBelow(block))
            {
                if (type == Material.WATER_CAULDRON)
                {
                    world.spawnParticle(Particle.WATER_SPLASH, center, 12, 0.20D, 0.0D, 0.20D);
                }
                else if (type == Material.POWDER_SNOW_CAULDRON)
                {
                    world.spawnParticle(Particle.WATER_SPLASH, center, 12, 0.20D, 0.0D, 0.20D);
                }
                else if (type == Material.LAVA_CAULDRON)
                {
                    world.spawnParticle(Particle.DRIP_LAVA, center, 12, 0.20D, 0.0D, 0.20D);
                }
            }
            else if (type == Material.POWDER_SNOW_CAULDRON || this.isIceBelow(block))
            {
                if (type == Material.WATER_CAULDRON || type == Material.POWDER_SNOW_CAULDRON)
                {
                    world.spawnParticle(Particle.SNOWFLAKE, center, 12, 0.20D, 0.0D, 0.20D, 0.01);
                }
            }
            else
            {
                double x = this.runCounter % 3 == 0 ? 0 : this.random.nextFloat() * 0.4;
                double z = this.runCounter % 3 == 1 ? 0 : this.random.nextFloat() * 0.4;

                x = this.random.nextBoolean() ? x : -x;
                z = this.random.nextBoolean() ? z : -z;

                world.spawnParticle(Particle.SPELL_MOB,
                    center.add(x, 0, z),
                    0,
                    0,
                    127,
                    0,
                    127);

                x = this.random.nextBoolean() ? x : -x;
                z = this.random.nextBoolean() ? z : -z;

                world.spawnParticle(Particle.SPELL_MOB,
                    center.add(x, 0, z),
                    0,
                    0,
                    127,
                    0,
                    127);
            }

            if (this.runCounter % 40 == 1)
            {
                // Play sound lava poping
                world.playSound(center, Sound.BLOCK_LAVA_POP, 0.5f, 0.5f);
            }
        }
    }


    /**
     * This method returns if below current block is a could source.
     * @param block Block that must be checked.
     * @return {@code true} if could source is below, {@code false} otherwise.
     */
    private boolean isIceBelow(Block block)
    {
        Material type = block.getRelative(BlockFace.DOWN).getType();

        return type == Material.ICE ||
            type == Material.PACKED_ICE ||
            type == Material.BLUE_ICE ||
            type == Material.FROSTED_ICE ||
            type == Material.SNOW_BLOCK ||
            type == Material.POWDER_SNOW;
    }


    /**
     * This method returns if below current block is a heat source.
     * @param block Block that must be checked.
     * @return {@code true} if heat source is below, {@code false} otherwise.
     */
    private boolean isHeatBelow(Block block)
    {
        Block relative = block.getRelative(BlockFace.DOWN);
        Material type = relative.getType();

        if (type == Material.CAMPFIRE ||
            type == Material.SOUL_CAMPFIRE ||
            type == Material.FURNACE ||
            type == Material.BLAST_FURNACE ||
            type == Material.SMOKER ||
            type.name().contains("CANDLE"))
        {
            if (relative.getBlockData() instanceof Lightable data)
            {
                return data.isLit();
            }
            else
            {
                return false;
            }
        }
        else
        {
            return type == Material.FIRE ||
                type == Material.LAVA ||
                type == Material.LAVA_CAULDRON;
        }
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Run Counter.
     */
    private int runCounter = 0;

    /**
     * Addon instance.
     */
    private final CauldronWitcheryAddon addon;

    /**
     * List of worlds where addon operates.
     */
    private final List<World> worldList;

    /**
     * Set of cauldrons with items inside.
     */
    private final Set<Block> blockSet = new HashSet<>();

    /**
     * Random instance.
     */
    private final Random random = new Random(0);
}
