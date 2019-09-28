package world.bentobox.cauldronwitchery.listeners;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.configs.Settings;


/**
 * This is main listener that checks for clicks on cauldrons.
 */
public class MainCauldronListener implements Listener
{
	/**
	 * Default constructor
	 * @param addon MagicSummonAddon object.
	 */
	public MainCauldronListener(CauldronWitcheryAddon addon)
	{
		this.addon = addon;
	}


	/**
	 * This method check interact event on cauldron
	 * @param event PlayerInteractEvent
	 */
	@SuppressWarnings("SuspiciousMethodCalls")
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onCauldronClick(PlayerInteractEvent event)
	{
		// Pass events only if:
		// event involves item.
		// player is using right click
		// player hand is not empty
		// clicked block is cauldron
		// If player clicks on cauldron with bucket, water bucket, bottle,
		// potion or another cauldron, then do nothing.

		if (event.hasItem() &&
			event.getAction() == Action.RIGHT_CLICK_BLOCK &&
			event.getHand() != null &&
			event.getHand().equals(EquipmentSlot.HAND) &&
			event.getClickedBlock() != null &&
			event.getClickedBlock().getType().equals(Material.CAULDRON) &&
			!IGNORED_MATERIALS.contains(event.getItem().getType()))
		{
			// We have checked that player clicked on cauldron with an item. Now check if
			// he has permissions to spawn an animal.

			Block block = event.getClickedBlock();

			if (CauldronWitcheryAddon.MAGIC_SUMMON_ENABLE_FLAG.isSetForWorld(block.getWorld()))
			{
				// Cancel event
				event.setCancelled(true);

				// Transform player to BentoBox object.
				User user = User.getInstance(event.getPlayer());

				// Magic Summon is enabled in current world.
				// Now check island permissions.
				Optional<Island> island = this.addon.getIslands().getIslandAt(block.getLocation());

				if (island.isPresent())
				{
					// Island is found. Check if player has permission to do anything here.
					if (island.get().isAllowed(user, CauldronWitcheryAddon.MAGIC_SUMMON_ISLAND_PROTECTION))
					{
						// Now work on summoning.

						Settings.LightningEffect effect;

						if (!block.getBlockData().getAsString().equals("minecraft:cauldron[level=3]"))
						{
							user.sendMessage("cauldronwitchery.messages.cauldron-not-full");
							effect = this.addon.getSettings().getMissingWater();
						}
						else if (this.addon.getMagicSummon().isSummonSuccessful(user, block.getLocation()))
						{
							user.sendMessage("cauldronwitchery.messages.it-is-alive");
							effect = this.addon.getSettings().getSuccessful();
						}
						else
						{
							user.sendMessage("cauldronwitchery.messages.something-went-wrong");
							effect = this.addon.getSettings().getError();
						}

						if (effect.hasTimings())
						{
							Location location = effect.isHitPlayer() ? user.getLocation() : block.getLocation();

							for (Long time : effect.getTimings())
							{
								if (effect.isDamage())
								{
									Bukkit.getScheduler().scheduleSyncDelayedTask(BentoBox.getInstance(),
										() -> user.getWorld().strikeLightning(location),
										time);
								}
								else
								{
									Bukkit.getScheduler().scheduleSyncDelayedTask(BentoBox.getInstance(),
										() -> user.getWorld().strikeLightningEffect(location),
										time);
								}
							}

							if (effect.isDestroyCauldron())
							{
								block.setType(Material.AIR);
							}
							else
							{
								block.setType(Material.CAULDRON);
							}
						}
					}
					else
					{
						user.sendMessage("cauldronwitchery.messages.missing-rank");
					}
				}
				else
				{
					user.sendMessage("cauldronwitchery.messages.missing-island");
				}
			}
		}
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	/**
	 * This variable stores magic summon addon object.
	 */
	private CauldronWitcheryAddon addon;


	// ---------------------------------------------------------------------
	// Section: Constants
	// ---------------------------------------------------------------------


	/**
	 * This final set contains elements that should be ignored by right click.
	 */
	private static final Set<Material> IGNORED_MATERIALS;


	static {
		IGNORED_MATERIALS = new HashSet<>(6);

		IGNORED_MATERIALS.add(Material.CAULDRON);
		IGNORED_MATERIALS.add(Material.WATER);
		IGNORED_MATERIALS.add(Material.WATER_BUCKET);
		IGNORED_MATERIALS.add(Material.BUCKET);
		IGNORED_MATERIALS.add(Material.GLASS_BOTTLE);
		IGNORED_MATERIALS.add(Material.POTION);
	}
}
