package world.bentobox.cauldronwitchery.configs;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;



@StoreAt(filename="config.yml", path="addons/CauldronWitchery")
public class Settings implements ConfigObject
{
	/**
	 * Is drop in cauldron boolean.
	 *
	 * @return the boolean
	 */
	public boolean isMixInCauldron()
	{
		return mixInCauldron;
	}


	/**
	 * Sets drop in cauldron.
	 *
	 * @param mixInCauldron the drop in cauldron
	 */
	public void setMixInCauldron(boolean mixInCauldron)
	{
		this.mixInCauldron = mixInCauldron;
	}


	/**
	 * Is exact extra count boolean.
	 *
	 * @return the boolean
	 */
	public boolean isExactExtraCount()
	{
		return exactExtraCount;
	}


	/**
	 * Sets exact extra count.
	 *
	 * @param exactExtraCount the exact extra count
	 */
	public void setExactExtraCount(boolean exactExtraCount)
	{
		this.exactExtraCount = exactExtraCount;
	}


	/**
	 * Is remove left overs boolean.
	 *
	 * @return the boolean
	 */
	public boolean isRemoveLeftOvers()
	{
		return removeLeftOvers;
	}


	/**
	 * Sets remove left overs.
	 *
	 * @param removeLeftOvers the remove left overs
	 */
	public void setRemoveLeftOvers(boolean removeLeftOvers)
	{
		this.removeLeftOvers = removeLeftOvers;
	}


	/**
	 * Is successful damage boolean.
	 *
	 * @return the boolean
	 */
	public boolean isSuccessfulDamage()
	{
		return successfulDamage;
	}


	/**
	 * Sets successful damage.
	 *
	 * @param successfulDamage the successful damage
	 */
	public void setSuccessfulDamage(boolean successfulDamage)
	{
		this.successfulDamage = successfulDamage;
	}


	/**
	 * Is successful hit player boolean.
	 *
	 * @return the boolean
	 */
	public boolean isSuccessfulHitPlayer()
	{
		return successfulHitPlayer;
	}


	/**
	 * Sets successful hit player.
	 *
	 * @param successfulHitPlayer the successful hit player
	 */
	public void setSuccessfulHitPlayer(boolean successfulHitPlayer)
	{
		this.successfulHitPlayer = successfulHitPlayer;
	}


	/**
	 * Is successful destroy cauldron boolean.
	 *
	 * @return the boolean
	 */
	public boolean isSuccessfulDestroyCauldron()
	{
		return successfulDestroyCauldron;
	}


	/**
	 * Sets successful destroy cauldron.
	 *
	 * @param successfulDestroyCauldron the successful destroy cauldron
	 */
	public void setSuccessfulDestroyCauldron(boolean successfulDestroyCauldron)
	{
		this.successfulDestroyCauldron = successfulDestroyCauldron;
	}


	/**
	 * Gets successful timings.
	 *
	 * @return the successful timings
	 */
	public List<Long> getSuccessfulTimings()
	{
		return successfulTimings;
	}


	/**
	 * Sets successful timings.
	 *
	 * @param successfulTimings the successful timings
	 */
	public void setSuccessfulTimings(List<Long> successfulTimings)
	{
		this.successfulTimings = successfulTimings;
	}


	/**
	 * Is error damage boolean.
	 *
	 * @return the boolean
	 */
	public boolean isErrorDamage()
	{
		return errorDamage;
	}


	/**
	 * Sets error damage.
	 *
	 * @param errorDamage the error damage
	 */
	public void setErrorDamage(boolean errorDamage)
	{
		this.errorDamage = errorDamage;
	}


	/**
	 * Is error hit player boolean.
	 *
	 * @return the boolean
	 */
	public boolean isErrorHitPlayer()
	{
		return errorHitPlayer;
	}


	/**
	 * Sets error hit player.
	 *
	 * @param errorHitPlayer the error hit player
	 */
	public void setErrorHitPlayer(boolean errorHitPlayer)
	{
		this.errorHitPlayer = errorHitPlayer;
	}


	/**
	 * Is error destroy cauldron boolean.
	 *
	 * @return the boolean
	 */
	public boolean isErrorDestroyCauldron()
	{
		return errorDestroyCauldron;
	}


	/**
	 * Sets error destroy cauldron.
	 *
	 * @param errorDestroyCauldron the error destroy cauldron
	 */
	public void setErrorDestroyCauldron(boolean errorDestroyCauldron)
	{
		this.errorDestroyCauldron = errorDestroyCauldron;
	}


	/**
	 * Gets error timings.
	 *
	 * @return the error timings
	 */
	public List<Long> getErrorTimings()
	{
		return errorTimings;
	}


	/**
	 * Sets error timings.
	 *
	 * @param errorTimings the error timings
	 */
	public void setErrorTimings(List<Long> errorTimings)
	{
		this.errorTimings = errorTimings;
	}


	/**
	 * Gets disabled game modes.
	 *
	 * @return the disabled game modes
	 */
	public Set<String> getDisabledGameModes()
	{
		return disabledGameModes;
	}


	/**
	 * Sets disabled game modes.
	 *
	 * @param disabledGameModes the disabled game modes
	 */
	public void setDisabledGameModes(Set<String> disabledGameModes)
	{
		this.disabledGameModes = disabledGameModes;
	}


	/**
	 * Gets success damage amount.
	 *
	 * @return the success damage amount
	 */
	public int getSuccessDamageAmount()
	{
		return successDamageAmount;
	}


	/**
	 * Sets success damage amount.
	 *
	 * @param successDamageAmount the success damage amount
	 */
	public void setSuccessDamageAmount(int successDamageAmount)
	{
		this.successDamageAmount = successDamageAmount;
	}


	/**
	 * Gets error damage amount.
	 *
	 * @return the error damage amount
	 */
	public int getErrorDamageAmount()
	{
		return errorDamageAmount;
	}


	/**
	 * Sets error damage amount.
	 *
	 * @param errorDamageAmount the error damage amount
	 */
	public void setErrorDamageAmount(int errorDamageAmount)
	{
		this.errorDamageAmount = errorDamageAmount;
	}


	/**
	 * Is remove on fail boolean.
	 *
	 * @return the boolean
	 */
	public boolean isRemoveOnFail()
	{
		return removeOnFail;
	}


	/**
	 * Sets remove on fail.
	 *
	 * @param removeOnFail the remove on fail
	 */
	public void setRemoveOnFail(boolean removeOnFail)
	{
		this.removeOnFail = removeOnFail;
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

	@ConfigComment("Allows to set amount of damage on successful magic.")
	@ConfigComment("Items that cannot be damaged, like STICK, will be removed.")
	@ConfigComment("0 means that damage will not be applied. ")
	@ConfigEntry(path = "stick.success-damage")
	private int successDamageAmount = 0;

	@ConfigComment("Allows to set amount of damage on failed magic.")
	@ConfigComment("Items that cannot be damaged, like STICK, will be removed.")
	@ConfigComment("0 means that damage will not be applied. ")
	@ConfigEntry(path = "stick.error-damage")
	private int errorDamageAmount = 0;

	@ConfigComment("Allows to toggle if extra ingredients should be dropped in cauldron.")
	@ConfigComment("Setting it to `true` will allow to perform magic only if items are dropped in cauldron.")
	@ConfigEntry(path = "cauldron.mix-in-cauldron")
	private boolean mixInCauldron = true;

	@ConfigComment("Allows to toggle if extra ingredients should be in correct count.")
	@ConfigComment("Setting it to `true` will fail any recipe where extras in cauldron are more then required.")
	@ConfigComment("This option works only if `mix-in-cauldron` is enabled.")
	@ConfigEntry(path = "cauldron.require-exact")
	private boolean exactExtraCount = true;

	@ConfigComment("Allows to toggle if left-overs for extra ingredients should be removed.")
	@ConfigComment("Setting it to `true` will remove all left-over ingredients that were dropped in cauldron.")
	@ConfigComment("This option works only if `mix-in-cauldron` is enabled and `require-exact` is disabled.")
	@ConfigEntry(path = "cauldron.remove-left-overs")
	private boolean removeLeftOvers = false;

	@ConfigComment("Allows to toggle if items inside cauldron should be removed on failed magic.")
	@ConfigComment("Setting it to `true` will remove all items inside cauldron if magic fails.")
	@ConfigComment("This option works only if `mix-in-cauldron` is enabled.")
	@ConfigEntry(path = "cauldron.remove-on-fail")
	private boolean removeOnFail = false;

	@ConfigComment("Allows to toggle lightning between visual and damage mode when recipe worked.")
	@ConfigComment("Setting it to `true` will summon actual lightning that will damage and set things on fire")
	@ConfigComment("Setting it to `false` will just make visual lightning and sound.")
	@ConfigEntry(path = "lightning.successful.damage")
	private boolean successfulDamage = false;
	
	@ConfigComment("Allows to toggle lightning should target cauldron or player who performed successful magic.")
	@ConfigComment("Setting it to `true` will force lightning to target player.")
	@ConfigComment("Setting it to `false` will force lightning to target cauldron.")
	@ConfigEntry(path = "lightning.successful.hit-player")
	private boolean successfulHitPlayer = false;

	@ConfigComment("Allows to toggle lightning should destroy the cauldron after successful magic.")
	@ConfigComment("Setting it to `true` will force lightning to destroy cauldron.")
	@ConfigComment("Setting it to `false` will not allow lightning to destroy cauldron.")
	@ConfigEntry(path = "lightning.successful.destroy-cauldron")
	private boolean successfulDestroyCauldron = false;

	@ConfigComment("Allows to set the ticks daley between lightning strikes.")
	@ConfigComment("If left empty, lightning will not strike.")
	@ConfigEntry(path = "lightning.successful.timings")
	private List<Long> successfulTimings = new ArrayList<>(4);
	
	@ConfigComment("Allows to toggle lightning between visual and damage mode when recipe did worked.")
	@ConfigComment("Setting it to `true` will summon actual lightning that will damage and set things on fire")
	@ConfigComment("Setting it to `false` will just make visual lightning and sound.")
	@ConfigEntry(path = "lightning.error.damage")
	private boolean errorDamage = false;

	@ConfigComment("Allows to toggle lightning should target cauldron or player when magic failed.")
	@ConfigComment("Setting it to `true` will force lightning to target player.")
	@ConfigComment("Setting it to `false` will force lightning to target cauldron.")
	@ConfigEntry(path = "lightning.error.hit-player")
	private boolean errorHitPlayer = false;

	@ConfigComment("Allows to toggle lightning should destroy the cauldron after failed magic.")
	@ConfigComment("Setting it to `true` will force lightning to destroy cauldron.")
	@ConfigComment("Setting it to `false` will not allow lightning to destroy cauldron.")
	@ConfigEntry(path = "lightning.error.destroy-cauldron")
	private boolean errorDestroyCauldron = false;

	@ConfigComment("Allows to set the ticks daley between lightning strikes.")
	@ConfigComment("If left empty, lightning will not strike.")
	@ConfigEntry(path = "lightning.error.timings")
	private List<Long> errorTimings = new ArrayList<>(4);

	@ConfigComment("")
	@ConfigComment("This list stores GameModes in which CauldronWitchery addon should not work.")
	@ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
	@ConfigComment("disabled-gamemodes:")
	@ConfigComment(" - BSkyBlock")
	@ConfigEntry(path = "disabled-gamemodes")
	private Set<String> disabledGameModes = new HashSet<>();	
}
