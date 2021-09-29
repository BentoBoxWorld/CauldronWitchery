//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.cauldronwitchery.panels.utils;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import world.bentobox.bentobox.api.panels.Panel;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.EntityRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.ItemRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.utils.Constants;


/**
 * This class creates GUI that allows to select challenge type.
 */
public record RecipeTypeSelector(User user, Consumer<Recipe> consumer)
{
	/**
	 * This method opens GUI that allows to select challenge type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user, Consumer<Recipe> consumer)
	{
		new RecipeTypeSelector(user, consumer).build();
	}


	/**
	 * This method builds GUI that allows to select challenge type.
	 */
	private void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().
			user(this.user).
			type(Panel.Type.HOPPER).
			name(this.user.getTranslation(Constants.TITLE + "type-selector"));

		panelBuilder.item(0, this.getButton(Type.ENTITY));
		panelBuilder.item(2, this.getButton(Type.ITEM));
		panelBuilder.item(4, this.getButton(Type.BOOK));

		panelBuilder.build();
	}


	/**
	 * Creates ChallengeType button.
	 *
	 * @param type Challenge type which button must be created.
	 * @return PanelItem button.
	 */
	private PanelItem getButton(Type type)
	{
		final String reference = Constants.BUTTON + type.name().toLowerCase() + ".";

		final String name = this.user.getTranslation(reference + "name");
		final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));
		description.add("");
		description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));

		ItemStack icon;
		PanelItem.ClickHandler clickHandler;

		switch (type)
		{
			case ENTITY -> {
				icon = new ItemStack(Material.CREEPER_HEAD);
				clickHandler = (
					(panel, user1, clickType, slot) ->
					{
						this.consumer.accept(new EntityRecipe());
						return true;
					});
			}
			case ITEM -> {
				icon = new ItemStack(Material.GRASS_BLOCK);
				clickHandler = (
					(panel, user1, clickType, slot) ->
					{
						this.consumer.accept(new ItemRecipe());
						return true;
					});
			}
			case BOOK -> {
				icon = new ItemStack(Material.KNOWLEDGE_BOOK);
				clickHandler = (
					(panel, user1, clickType, slot) ->
					{
						this.consumer.accept(new BookRecipe());
						return true;
					});
			}
			default -> {
				icon = new ItemStack(Material.PAPER);
				clickHandler = null;
			}
		}

		return new PanelItemBuilder().
			icon(icon).
			name(name).
			description(description).
			clickHandler(clickHandler).
			build();
	}


	/**
	 * Type of recipe
	 */
	private enum Type
	{
		ITEM,
		ENTITY,
		BOOK
	}
}
