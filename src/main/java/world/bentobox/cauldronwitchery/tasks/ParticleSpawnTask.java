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

        // Update entity list every 5 * 20 ticks.
        if (this.runCounter++ > 20)
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

            if (!block.getType().name().contains("CAULDRON"))
            {
                // Remove block from iterator, as it is not cauldron.
                blockIterator.remove();
                continue;
            }

            this.spawnCauldronEffects(block);
        }
    }


    /**
     * Spawns particles and sounds for a single cauldron, based on its contents and the block below it.
     * @param block Cauldron block.
     */
    private void spawnCauldronEffects(Block block)
    {
        World world = block.getWorld();
        Material type = block.getType();
        Location center = block.getLocation().add(0.5D, 0.9D, 0.5D);

        if (type == Material.LAVA_CAULDRON || this.isHeatBelow(block))
        {
            this.spawnHeatParticles(world, type, center);
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
            this.spawnEnchantParticles(world, center);
        }

        if (this.runCounter % 40 == 1)
        {
            // Play sound lava poping
            world.playSound(center, Sound.BLOCK_LAVA_POP, 0.5f, 0.5f);
        }
    }


    /**
     * Spawns particles for a heated cauldron.
     */
    private void spawnHeatParticles(World world, Material type, Location center)
    {
        switch (type)
        {
            case WATER_CAULDRON, POWDER_SNOW_CAULDRON ->
                world.spawnParticle(Particle.SPLASH, center, 12, 0.20D, 0.0D, 0.20D);
            case LAVA_CAULDRON ->
                world.spawnParticle(Particle.DRIPPING_LAVA, center, 12, 0.20D, 0.0D, 0.20D);
            default -> {
                // Empty cauldrons do not spawn heat particles.
            }
        }
    }


    /**
     * Spawns two enchant particles around the cauldron center.
     */
    private void spawnEnchantParticles(World world, Location center)
    {
        double x = this.runCounter % 3 == 0 ? 0 : this.random.nextFloat() * 0.4;
        double z = this.runCounter % 3 == 1 ? 0 : this.random.nextFloat() * 0.4;

        for (int i = 0; i < 2; i++)
        {
            x = this.random.nextBoolean() ? x : -x;
            z = this.random.nextBoolean() ? z : -z;

            world.spawnParticle(Particle.ENCHANT,
                center.add(x, 0, z),
                0,
                0,
                127,
                0,
                127);
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
            return relative.getBlockData() instanceof Lightable data && data.isLit();
        }

        return type == Material.FIRE ||
            type == Material.LAVA ||
            type == Material.LAVA_CAULDRON;
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
