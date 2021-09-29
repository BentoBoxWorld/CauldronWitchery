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


	/**
	 * Gets admin main command.
	 *
	 * @return the admin main command
	 */
	public String getAdminMainCommand()
    {
        return adminMainCommand;
    }


	/**
	 * Sets admin main command.
	 *
	 * @param adminMainCommand the admin main command
	 */
	public void setAdminMainCommand(String adminMainCommand)
    {
        this.adminMainCommand = adminMainCommand;
    }


	/**
	 * Is correct error message boolean.
	 *
	 * @return the boolean
	 */
	public boolean isCorrectErrorMessage()
	{
		return correctErrorMessage;
	}


	/**
	 * Sets correct error message.
	 *
	 * @param correctErrorMessage the correct error message
	 */
	public void setCorrectErrorMessage(boolean correctErrorMessage)
	{
		this.correctErrorMessage = correctErrorMessage;
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

	@ConfigComment("Allows to toggle failing should destroy the cauldron.")
	@ConfigComment("Setting it to `true` will destroy cauldron.")
	@ConfigComment("Setting it to `false` will not destroy cauldron.")
	@ConfigEntry(path = "cauldron.error.destroy-cauldron")
	private boolean errorDestroyCauldron = false;

	@ConfigComment("Allows to toggle if failing recipe should display correct reason or")
	@ConfigComment("generic message about failing.")
	@ConfigEntry(path = "cauldron.error.correct-message")
	private boolean correctErrorMessage = true;

	@ConfigComment("Allows to change label for admin command.")
	@ConfigEntry(path = "command.admin-command")
	private String adminMainCommand = "witchery";

	@ConfigComment("")
	@ConfigComment("This list stores GameModes in which CauldronWitchery addon should not work.")
	@ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
	@ConfigComment("disabled-gamemodes:")
	@ConfigComment(" - BSkyBlock")
	@ConfigEntry(path = "disabled-gamemodes")
	private Set<String> disabledGameModes = new HashSet<>();
}
