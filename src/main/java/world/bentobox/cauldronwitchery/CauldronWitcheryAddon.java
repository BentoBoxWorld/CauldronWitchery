package world.bentobox.cauldronwitchery;


import org.bukkit.Bukkit;
import org.bukkit.Material;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.cauldronwitchery.configs.Settings;
import world.bentobox.cauldronwitchery.listeners.MainCauldronListener;
import world.bentobox.cauldronwitchery.tasks.CauldronSummon;


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
//		// Save default config.yml
		this.saveDefaultConfig();
		// Load Addon Settings
		this.settings = new Settings(this);
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
			Bukkit.getLogger().severe("CauldronWitchery Addon is not available or disabled!");
			return;
		}


		this.getPlugin().getAddonsManager().getGameModeAddons().forEach(gameModeAddon -> {
			if (!this.settings.getDisabledGameModes().contains(gameModeAddon.getDescription().getName()))
			{
				if (gameModeAddon.isEnabled())
				{
					MAGIC_SUMMON_ENABLE_FLAG.addGameModeAddon(gameModeAddon);
					MAGIC_SUMMON_ISLAND_PROTECTION.addGameModeAddon(gameModeAddon);
				}

				this.hooked = true;
			}
		});

		if (this.hooked)
		{
			this.summon = new CauldronSummon(this);

			// Register the listener.
			this.registerListener(new MainCauldronListener(this));

			// Register Flags
			this.registerFlag(MAGIC_SUMMON_ENABLE_FLAG);
			this.registerFlag(MAGIC_SUMMON_ISLAND_PROTECTION);

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
	 * Executes code when disabling the addon.
	 */
	@Override
	public void onDisable()
	{
		// Do some staff...
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
			this.settings = new Settings(this);
			this.getLogger().info("CauldronWitchery addon reloaded.");
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
	 * This method returns Magic Generator.
	 * @return Magic Generator object.
	 */
	public CauldronSummon getMagicSummon()
	{
		return this.summon;
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
	 * Variable holds MagicSummon object.
	 */
	private CauldronSummon summon;


// ---------------------------------------------------------------------
// Section: Flags
// ---------------------------------------------------------------------


	/**
	 * This flag allows to enable or disable Magic Summon addon in particular world.
	 */
	public static Flag MAGIC_SUMMON_ENABLE_FLAG =
		new Flag.Builder("MAGIC_SUMMON_ENABLE_FLAG", Material.PIG_SPAWN_EGG).
			type(Flag.Type.WORLD_SETTING).defaultSetting(true).build();

	/**
	 * This flag allows to define which users can summon mobs using magic.
	 */
	public static Flag MAGIC_SUMMON_ISLAND_PROTECTION =
		new Flag.Builder("MAGIC_SUMMON_ISLAND_PROTECTION", Material.CHICKEN_SPAWN_EGG).
			defaultRank(RanksManager.VISITOR_RANK).build();
}
