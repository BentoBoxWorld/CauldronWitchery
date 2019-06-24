package world.bentobox.cauldronwitchery.tasks;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;
import world.bentobox.cauldronwitchery.configs.Settings;


public class CauldronSummon
{
	public CauldronSummon(CauldronWitcheryAddon addon)
	{
		this.addon = addon;
	}


	/**
	 * This method tries to summon mob and returns if it was successful.
	 * @param user User who wants to summon mob
	 * @param location Location of cauldron
	 * @return {@code true} if the summoning was successful, {@code false} otherwise.
	 */
	public boolean isSummonSuccessful(User user, Location location)
	{
		Material mainHandItem = user.getInventory().getItemInMainHand().getType();

		Map<Material, Settings.MagicStick> magicStickMap = this.addon.getSettings().getMagicStickMap();

		if (!magicStickMap.containsKey(mainHandItem))
		{
			// Main Hand Item Is not a valid magic stick
			user.sendMessage("cauldronwitchery.messages.not-a-stick");
			return false;
		}

		Settings.MagicStick magicStick = magicStickMap.get(mainHandItem);

		if (!location.getWorld().getEnvironment().equals(magicStick.getEnvironment()))
		{
			// Wrong Workspace
			user.sendMessage("cauldronwitchery.messages.wrong-workspace");
			return false;
		}

		Material offHandItem = user.getInventory().getItemInOffHand().getType();

		if (!magicStick.containsRecipe(offHandItem))
		{
			// Not a recipe item
			user.sendMessage("cauldronwitchery.messages.missing-offhand-item");
			return false;
		}

		Settings.MagicRecipe recipe = magicStick.getRecipe(offHandItem);

		if (!this.hasAllRequirements(user, recipe))
		{
			// Required materials not met
			user.sendMessage("cauldronwitchery.messages.something-is-missing");
			return false;
		}

		// Remove materials
		this.removeRequirements(user, recipe);

		if (recipe instanceof Settings.MagicBook)
		{
			// Create Book
			this.summonBook(user, location, (Settings.MagicBook) recipe, magicStick);
		}
		else
		{
			// Summon mob
			this.summonMob(user, location, (Settings.MagicMob) recipe);
		}

		return true;
	}


	/**
	 * Check recipe requirements.
	 * @param user User
	 * @param recipe Summon Recipe
	 * @return {@code true} if requirements are met, {@code false} otherwise.
	 */
	private boolean hasAllRequirements(User user, Settings.MagicRecipe recipe)
	{
		// If recipe requires higher level, then return false.

		if (user.getPlayer().getLevel() < recipe.getRequiredLevel())
		{
			// TODO: Send about missing levels?
			return false;
		}

		// Check all required materials.

		for (Map.Entry<Material, Integer> entry : recipe.getRequiredMaterials().entrySet())
		{
			if (!user.getInventory().contains(entry.getKey(), entry.getValue()))
			{
				// TODO: Send about missing materials?
				return false;
			}
		}

		return true;
	}


	private boolean removeRequirements(User user, Settings.MagicRecipe recipe)
	{
		// Remove level
		user.getPlayer().setLevel(user.getPlayer().getLevel() - recipe.getRequiredLevel());

		// Remove offhand item

		user.getPlayer().getInventory().getItemInOffHand().setAmount(
			user.getPlayer().getInventory().getItemInOffHand().getAmount() - 1);

		// Remove required materials

		for (Map.Entry<Material, Integer> entry : recipe.getRequiredMaterials().entrySet())
		{
			int removeAmount = entry.getValue();

			while (removeAmount <= 0)
			{
				ItemStack itemStack = user.getInventory().getItem(user.getInventory().first(entry.getKey()));

				if (itemStack == null)
				{
					return false;
				}
				else if (itemStack.getAmount() >= removeAmount)
				{
					itemStack.setAmount(itemStack.getAmount() - removeAmount);
				}
				else
				{
					removeAmount -= itemStack.getAmount();
					itemStack.setAmount(0);
				}
			}
		}

		return true;
	}


	private void summonMob(User user, Location location, Settings.MagicMob recipe)
	{
		// Summon Mob.

		Bukkit.getScheduler().scheduleSyncDelayedTask(BentoBox.getInstance(),
			() -> user.getWorld().spawnEntity(location.add(0, 2, 0), recipe.getEntity()),
			70L);
	}


	@SuppressWarnings("ConstantConditions")
	private void summonBook(User user,
		Location location,
		Settings.MagicBook book,
		Settings.MagicStick magicStick)
	{
		String stringCode = book.getName();

		ItemStack magicBook = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) magicBook.getItemMeta();

		bookMeta.setDisplayName(user.getTranslation("cauldronwitchery.books." + stringCode + ".title"));
		bookMeta.setTitle(user.getTranslation("cauldronwitchery.books." + stringCode + ".title"));
		bookMeta.setAuthor(user.getTranslation("cauldronwitchery.books." + stringCode + ".author"));

		List<String> pages = new ArrayList<>();

		// Check and add up to nine intro pages.

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-one").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-one"));
		}

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-two").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-two"));
		}

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-three").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-three"));
		}

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-four").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-four"));
		}

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-five").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-five"));
		}

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-six").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-six"));
		}

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-seven").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-seven"));
		}

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-eight").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-eight"));
		}

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-nine").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-nine"));
		}

		// Add All recipes
		// This will add extra spaciness... books will be in random order. Intentional!

		for (Settings.MagicRecipe recipe : magicStick.getRecipes().values())
		{
			// Do not add book recipe
			if (recipe instanceof Settings.MagicMob)
			{
				EntityType entityType = ((Settings.MagicMob) recipe).getEntity();

				StringBuilder builder = new StringBuilder();

				builder.append(user.getTranslation("cauldronwitchery.books." + stringCode + ".recipe-header",
					"[mob]", user.getTranslation("cauldronwitchery.mobs." + entityType.name()),
					"[offhand]", user.getTranslation("cauldronwitchery.materials." + recipe.getOffhandItem().name()),
					"[levels]", Integer.toString(recipe.getRequiredLevel())));

				builder.append(user.getTranslation("cauldronwitchery.books." + stringCode + ".recipe-extra-list"));

				for (Map.Entry<Material, Integer> entry : recipe.getRequiredMaterials().entrySet())
				{
					builder.append(user.getTranslation("cauldronwitchery.books." + stringCode + ".recipe-extra-element",
						"[material]", user.getTranslation("cauldronwitchery.materials." + entry.getKey().name()),
						"[count]", Integer.toString(entry.getValue())));
				}

				pages.add(builder.toString());
			}
		}

		// Add Last Page

		if (!user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-last").equals(""))
		{
			pages.add(user.getTranslationOrNothing("cauldronwitchery.books." + stringCode + ".page-last"));
		}

		bookMeta.setPages(pages);
		magicBook.setItemMeta(bookMeta);


		Bukkit.getScheduler().scheduleSyncDelayedTask(BentoBox.getInstance(),
			() -> user.getWorld().dropItem(location.add(0, 2, 0), magicBook),
			70L);
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------


	private CauldronWitcheryAddon addon;
}
