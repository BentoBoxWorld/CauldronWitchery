package world.bentobox.cauldronwitchery;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.flags.clicklisteners.CycleClick;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;
import world.bentobox.cauldronwitchery.commands.WitcheryAdminCommand;
import world.bentobox.cauldronwitchery.commands.WitcheryPlayerCommand;
import world.bentobox.cauldronwitchery.configs.Settings;
import world.bentobox.cauldronwitchery.listeners.CauldronClickListener;
import world.bentobox.cauldronwitchery.listeners.ItemsInsideCauldronListener;
import world.bentobox.cauldronwitchery.managers.CauldronWitcheryImportManager;
import world.bentobox.cauldronwitchery.managers.CauldronWitcheryManager;
import world.bentobox.cauldronwitchery.tasks.ParticleSpawnTask;


public class CauldronWitcheryAddon extends Addon
{
	/**
	 * Executes code when loading the addon. This is called before {@link #onEnable()}. This <b>must</b> be
	 * used to setup configuration, worlds and commands.
	 */
	@Override
	public void onLoad()
	{
		super.onLoad();
		// Save default config.yml
		this.saveDefaultConfig();
		// Load the plugin's config
		this.loadSettings();

		// Save template
		this.saveResource("template.yml",false);
		// Save all books
		this.loadBooks();
		// Save Panels
		this.saveResource("panels/recipe_panel.yml", false);
		this.saveResource("panels/stick_panel.yml",false);
	}


	/**
	 * Executes code when enabling the addon. This is called after {@link #onLoad()}. <br/> Note that commands
	 * and worlds registration <b>must</b> be done in {@link #onLoad()}, if need be. Failure to do so
	 * <b>will</b> result in issues such as tab-completion not working for commands.
	 */
	@Override
	public void onEnable()
	{
		// Check if it is enabled - it might be loaded, but not enabled.
		if (this.getPlugin() == null || !this.getPlugin().isEnabled())
		{
			Bukkit.getLogger().severe("BentoBox is not available or disabled!");
			this.setState(State.DISABLED);
			return;
		}

		// Check if addon is not disabled before.
		if (this.getState().equals(State.DISABLED))
		{
			Bukkit.getLogger().severe("CauldronWitchery is not available or disabled!");
			return;
		}

		// Init manager.
		this.manager = new CauldronWitcheryManager(this);
		this.manager.load();

		this.importManager = new CauldronWitcheryImportManager(this);

		// Init flag.
		CauldronWitcheryAddon.CAULDRON_WITCHERY_ISLAND_PROTECTION =
			new Flag.Builder("CAULDRON_WITCHERY_ISLAND_PROTECTION", Material.DIAMOND_PICKAXE).
				type(Flag.Type.PROTECTION).
				defaultRank(this.settings.getDefaultFlagPermission()).
				clickHandler(new CycleClick("CAULDRON_WITCHERY_ISLAND_PROTECTION",
					RanksManager.VISITOR_RANK,
					RanksManager.OWNER_RANK)).
				build();

		List<World> worldList = new ArrayList<>();

		this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon -> {
			if (!this.settings.getDisabledGameModes().contains(gameModeAddon.getDescription().getName()))
			{
				if (gameModeAddon.isEnabled())
				{
					CAULDRON_WITCHERY_ISLAND_PROTECTION.addGameModeAddon(gameModeAddon);

					gameModeAddon.getAdminCommand().ifPresent(command ->
						new WitcheryAdminCommand(this, command));
					gameModeAddon.getPlayerCommand().ifPresent(command ->
						new WitcheryPlayerCommand(this, command));
				}

				worldList.add(gameModeAddon.getOverWorld());

				if (gameModeAddon.getWorldSettings().isNetherGenerate())
				{
					worldList.add(gameModeAddon.getNetherWorld());
				}

				if (gameModeAddon.getWorldSettings().isEndGenerate())
				{
					worldList.add(gameModeAddon.getEndWorld());
				}

				this.hooked = true;
			}
		});

		if (this.hooked)
		{
			// Init ParticleSpawnRunnable.
			// Store task that will be running.
			this.particleTask = Bukkit.getScheduler().runTaskTimer(this.getPlugin(),
				new ParticleSpawnTask(this, worldList),
				20,
				5);

			// Register the listener.
			this.registerListener(new CauldronClickListener(this));
			this.registerListener(new ItemsInsideCauldronListener(this));

			// Register Flags
			this.registerFlag(CAULDRON_WITCHERY_ISLAND_PROTECTION);

			// Register Request Handlers
//			this.registerRequestHandler(REQUEST_HANDLER);
		}
		else
		{
			this.logError("CauldronWitchery could not hook into any GameMode so will not do anything!");
			this.setState(State.DISABLED);
		}
	}


	/**
	 * Processes that are running after everything is loaded.
	 */
	@Override
	public void allLoaded()
	{
		super.allLoaded();

		// Try to find Vault Plugin and if it does not exist, display a warning
		this.getPlugin().getVault().ifPresentOrElse(hook ->
		{
			this.vaultHook = hook;

			if (this.vaultHook.hook())
			{
				this.log("Cauldron Witchery Addon hooked into Economy.");
			}
			else
			{
				this.logWarning("Cauldron Witchery Addon could not hook into valid Economy.");
			}
		}, () ->
		{
			this.vaultHook = null;
			this.logWarning("Vault plugin not found. Economy will not work!");
		});
	}


	/**
	 * Executes code when disabling the addon.
	 */
	@Override
	public void onDisable()
	{
		// Stop particle task.
		if (this.particleTask != null)
		{
			this.particleTask.cancel();
		}
	}


	/**
	 * Executes code when reloading the addon.
	 */
	@Override
	public void onReload()
	{
		super.onReload();

		if (this.hooked)
		{
			this.loadSettings();
			this.getAddonManager().reload();
			this.log("CauldronWitchery reloaded.");
		}

		// Stop particle task.
		if (this.particleTask != null)
		{
			this.particleTask.cancel();
		}
	}


	/**
	 * This method saves addon settings into file.
	 */
	public void saveSettings()
	{
		if (this.settings != null)
		{
			new Config<>(this, Settings.class).saveConfigObject(this.settings);
		}
	}


	/**
	 * This method loads addon configuration settings in memory.
	 */
	private void loadSettings()
	{
		this.settings = new Config<>(this, Settings.class).loadConfigObject();

		if (this.settings == null)
		{
			// Disable
			this.logError("CauldronWitchery settings could not load! Addon disabled.");
			this.setState(State.DISABLED);
		}
	}


	/**
	 * This method saves books from jar to addon data folder.
	 */
	private void loadBooks()
	{
		try (JarFile jar = new JarFile(this.getFile()))
		{
			File bookDir = new File(this.getDataFolder(), "books");

			if (!bookDir.exists())
			{
				bookDir.mkdirs();
			}

			// Obtain any locale files, save them and update
			Util.listJarFiles(jar, "books", ".yml").forEach(lf ->
				this.saveResource(lf, bookDir, false, true));
		}
		catch (Exception e)
		{
			this.logError(e.getMessage());
		}
	}


	// ---------------------------------------------------------------------
	// Section: Getters
	// ---------------------------------------------------------------------


	/**
	 * This method returns the settings object.
	 * @return the settings object.
	 */
	public Settings getSettings()
	{
		return this.settings;
	}


	/**
	 * This method returns stone manager.
	 *
	 * @return Stone Generator Manager
	 */
	public CauldronWitcheryManager getAddonManager()
	{
		return this.manager;
	}


	/**
	 * Gets import manager.
	 *
	 * @return the import manager
	 */
	public CauldronWitcheryImportManager getImportManager()
	{
		return this.importManager;
	}


	/**
	 *
	 * @return economyProvided variable.
	 */
	public boolean isEconomyProvided()
	{
		return this.vaultHook != null && this.vaultHook.hook();
	}


	/**
	 * Returns VaultHook. Used to get easier access to Economy. NEVER USE WITHOUT isEconomyProvided or null
	 * check.
	 * @return VaultHook or null.
	 */
	public VaultHook getEconomyProvider()
	{
		return vaultHook;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	/**
	 * Variable holds settings object.
	 */
	private Settings settings;

	/**
	 * Variable indicates if addon is hooked in any game mode
	 */
	private boolean hooked;

	/**
	 * Variable that stores CauldronWitcheryManager.
	 */
	private CauldronWitcheryManager manager;

	/**
	 * Variable that stores CauldronWitcheryManager.
	 */
	private CauldronWitcheryImportManager importManager;

	/**
	 * Stores the id of the particle task.
	 */
	private BukkitTask particleTask;

	/**
	 * VaultHook that process economy.
	 */
	private VaultHook vaultHook;


// ---------------------------------------------------------------------
// Section: Flags
// ---------------------------------------------------------------------

	/**
	 * This flag allows to define which users can summon mobs using magic.
	 */
	public static Flag CAULDRON_WITCHERY_ISLAND_PROTECTION;
}
