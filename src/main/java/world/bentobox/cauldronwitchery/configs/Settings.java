package world.bentobox.cauldronwitchery.configs;


import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import java.util.*;

import world.bentobox.cauldronwitchery.CauldronWitcheryAddon;


public class Settings
{
	public Settings(CauldronWitcheryAddon addon)
	{
		addon.saveDefaultConfig();

		// Get disabled GameModes
		this.disabledGameModes = new HashSet<>(addon.getConfig().getStringList("disabled-gamemodes"));

		if (addon.getConfig().isSet("magic-sticks"))
		{
			ConfigurationSection section = addon.getConfig().getConfigurationSection("magic-sticks");

			this.magicStickMap = new HashMap<>(4);

			for (String key : section.getKeys(false))
			{
				ConfigurationSection stickSection = section.getConfigurationSection(key);

				Material stick = Material.getMaterial(key);

				if (stick != null)
				{
					MagicStick magicStick = new MagicStick(stick);
					magicStick.setEnvironment(World.Environment.valueOf(stickSection.getString("environment")));

					ConfigurationSection recipesSection = stickSection.getConfigurationSection("recipes");

					for (String recipeKey : recipesSection.getKeys(false))
					{
						ConfigurationSection singleRecipeSection = recipesSection.getConfigurationSection(recipeKey);
						MagicRecipe magicRecipe;

						if (recipeKey.equals("BOOK"))
						{
							magicRecipe = new MagicBook(
								Material.getMaterial(singleRecipeSection.getString("offhand")),
								singleRecipeSection.getString("name"));
						}
						else
						{
							magicRecipe = new MagicMob(
								Material.getMaterial(singleRecipeSection.getString("offhand")),
								EntityType.valueOf(recipeKey));
						}

						magicRecipe.setRequiredLevel(singleRecipeSection.getInt("levels"));

						ConfigurationSection materialsSection = singleRecipeSection.getConfigurationSection("materials");

						if (materialsSection != null)
						{
							for (String materialKey : materialsSection.getKeys(false))
							{
								magicRecipe.addRequiredItem(Material.valueOf(materialKey),
									materialsSection.getInt(materialKey));
							}
						}

						magicStick.addRecipe(magicRecipe);
					}

					this.magicStickMap.put(magicStick.getMainMaterial(), magicStick);
				}
				else
				{
					addon.logError("Cannot parse " + key + " as magic stick!");
				}
			}
		}
		else
		{
			addon.logError("No magic sticks installed!");
		}

		// Set up lightning effects
		this.successful = new LightningEffect();
		this.successful.damage = addon.getConfig().getBoolean("lightning.successful.damage", false);
		this.successful.hitPlayer = addon.getConfig().getBoolean("lightning.successful.hit-player", false);
		this.successful.destroyCauldron = addon.getConfig().getBoolean("lightning.successful.destroy-cauldron", false);

		if (addon.getConfig().contains("lightning.successful.timings"))
		{
			this.successful.timings.addAll(addon.getConfig().getLongList("lightning.successful.timings"));
		}
		else
		{
			this.successful.timings.add(20L);
			this.successful.timings.add(30L);
			this.successful.timings.add(40L);
			this.successful.timings.add(50L);
		}

		this.error = new LightningEffect();
		this.error.damage = addon.getConfig().getBoolean("lightning.error.damage", true);
		this.error.hitPlayer = addon.getConfig().getBoolean("lightning.error.hit-player", false);
		this.error.destroyCauldron = addon.getConfig().getBoolean("lightning.error.destroy-cauldron", false);

		if (addon.getConfig().contains("lightning.error.timings"))
		{
			this.error.timings.addAll(addon.getConfig().getLongList("lightning.error.timings"));
		}
		else
		{
			this.error.timings.add(20L);
			this.error.timings.add(40L);
			this.error.timings.add(60L);
		}

		this.missingWater = new LightningEffect();
		this.missingWater.damage = addon.getConfig().getBoolean("lightning.no-water.damage", true);
		this.missingWater.hitPlayer = addon.getConfig().getBoolean("lightning.no-water.hit-player", true);
		this.missingWater.destroyCauldron = addon.getConfig().getBoolean("lightning.no-water.destroy-cauldron", false);

		if (addon.getConfig().contains("lightning.no-water.timings"))
		{
			this.missingWater.timings.addAll(addon.getConfig().getLongList("lightning.no-water.timings"));
		}
		else
		{
			this.missingWater.timings.add(0L);
			this.missingWater.timings.add(20L);
			this.missingWater.timings.add(40L);
		}
	}

	// ---------------------------------------------------------------------
	// Section: Getters and Setters
	// ---------------------------------------------------------------------


	/**
	 * This method returns the disabledGameModes value.
	 * @return the value of disabledGameModes.
	 */
	public Set<String> getDisabledGameModes()
	{
		return disabledGameModes;
	}


	/**
	 * This method returns loaded Magic Sticks from Config file.
	 * @return Map that contains all magic sticks.
	 */
	public Map<Material, MagicStick> getMagicStickMap()
	{
		return this.magicStickMap;
	}


	public LightningEffect getSuccessful()
	{
		return successful;
	}


	public LightningEffect getError()
	{
		return error;
	}


	public LightningEffect getMissingWater()
	{
		return missingWater;
	}


	// ---------------------------------------------------------------------
	// Section: Variables
	// ---------------------------------------------------------------------

	/**
	 * This holds disabled game modes.
	 */
	private Set<String> disabledGameModes;

	private Map<Material, MagicStick> magicStickMap;

	/**
	 * Effect that will be triggered when everything works correctly.
	 */
	private LightningEffect successful;

	/**
	 * Effect that will be triggered when something is missing in players inventory.
	 */
	private LightningEffect error;

	/**
	 * Effect that will be triggered when player clicks on cauldron without a water.
	 */
	private LightningEffect missingWater;


	// ---------------------------------------------------------------------
	// Section: Object
	// ---------------------------------------------------------------------


	public class MagicStick
	{
		private MagicStick(Material mainMaterial)
		{
			this.mainMaterial = mainMaterial;
			this.recipes = new HashMap<>();
		}


		private void setEnvironment(World.Environment environment)
		{
			this.environment = environment;
		}


		private void addRecipe(MagicRecipe recipe)
		{
			this.recipes.put(recipe.getOffhandItem(), recipe);
		}


		// ---------------------------------------------------------------------
		// Section: Getters
		// ---------------------------------------------------------------------


		public Material getMainMaterial()
		{
			return mainMaterial;
		}


		public World.Environment getEnvironment()
		{
			return environment;
		}


		public Map<Material, MagicRecipe> getRecipes()
		{
			return recipes;
		}


		public boolean containsRecipe(Material offHandItem)
		{
			return this.recipes.containsKey(offHandItem);
		}


		public MagicRecipe getRecipe(Material offHandItem)
		{
			return this.recipes.get(offHandItem);
		}


		// ---------------------------------------------------------------------
		// Section: Variables
		// ---------------------------------------------------------------------

		/**
		 * Magic stick item.
		 */
		private Material mainMaterial;

		/**
		 * Environment where current stick works
		 */
		private World.Environment environment;

		/**
		 * List of recipes that can be spawned.
		 */
		private Map<Material, MagicRecipe> recipes;
	}


	public class MagicRecipe
	{
		private MagicRecipe(Material offhandItem)
		{
			this.offhandItem = offhandItem;
			this.requiredMaterials = new HashMap<>(2);
		}


		private void setRequiredLevel(int requiredLevel)
		{
			this.requiredLevel = requiredLevel;
		}


		private void setOffhandItem(Material offhandItem)
		{
			this.offhandItem = offhandItem;
		}


		private void addRequiredItem(Material material, int count)
		{
			this.requiredMaterials.put(material, count);
		}


		public int getRequiredLevel()
		{
			return this.requiredLevel;
		}


		public Material getOffhandItem()
		{
			return this.offhandItem;
		}


		public Map<Material, Integer> getRequiredMaterials()
		{
			return this.requiredMaterials;
		}


		// ---------------------------------------------------------------------
		// Section: Variables
		// ---------------------------------------------------------------------

		/**
		 * Required player experience level.
		 */
		private int requiredLevel;

		/**
		 * Offhand item
		 */
		private Material offhandItem;

		/**
		 * List of required materials.
		 */
		private Map<Material, Integer> requiredMaterials;
	}


	public class MagicMob extends MagicRecipe
	{
		private MagicMob(Material offhandItem, EntityType entity)
		{
			super(offhandItem);
			this.entity = entity;
		}


		public EntityType getEntity()
		{
			return this.entity;
		}


		// ---------------------------------------------------------------------
		// Section: Variables
		// ---------------------------------------------------------------------

		/**
		 * Entity type that will be spawned by current recipe
		 */
		private EntityType entity;
	}


	public class MagicBook extends MagicRecipe
	{
		private MagicBook(Material offhandItem, String name)
		{
			super(offhandItem);
			this.name = name;
		}


		public String getName()
		{
			return this.name;
		}


		// ---------------------------------------------------------------------
		// Section: Variables
		// ---------------------------------------------------------------------

		/**
		 * Name of the book.
		 */
		private String name;
	}


	public class LightningEffect
	{
		public boolean isDestroyCauldron()
		{
			return destroyCauldron;
		}


		public boolean isHitPlayer()
		{
			return hitPlayer;
		}


		public boolean isDamage()
		{
			return damage;
		}


		public List<Long> getTimings()
		{
			return timings;
		}


		public boolean hasTimings()
		{
			return timings != null && !timings.isEmpty();
		}


		// ---------------------------------------------------------------------
		// Section: Variables
		// ---------------------------------------------------------------------

		private boolean hitPlayer;

		private boolean damage;

		private List<Long> timings = new ArrayList<>(3);

		private boolean destroyCauldron;
	}
}
