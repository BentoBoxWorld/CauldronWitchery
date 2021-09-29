package world.bentobox.cauldronwitchery.panels.utils;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import lv.id.bonne.panelutils.PanelUtils;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.builders.PanelBuilder;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;
import world.bentobox.cauldronwitchery.database.object.recipe.BookRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.EntityRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.ItemRecipe;
import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;
import world.bentobox.cauldronwitchery.utils.Constants;


/**
 * This class creates new GUI that allows to select single Recipe, which is returned via consumer.
 */
public class RecipeSelector extends PagedSelector<Recipe>
{
	private RecipeSelector(User user,
		Material border,
		Map<Recipe, String> objectDescriptionMap,
		BiConsumer<Boolean, Set<Recipe>> consumer)
	{
		super(user);
		this.consumer = consumer;
		this.objectDescriptionMap = objectDescriptionMap;
		this.border = border;

		this.elements = objectDescriptionMap.keySet().stream().toList();
		this.selectedElements = new HashSet<>(this.elements.size());
		this.filterElements = this.elements;
	}


	/**
	 * This method opens GUI that allows to select Recipe type.
	 *
	 * @param user User who opens GUI.
	 * @param consumer Consumer that allows to get clicked type.
	 */
	public static void open(User user,
		Material border,
		Map<Recipe, String> elementDescriptionMap,
		BiConsumer<Boolean, Set<Recipe>> consumer)
	{
		new RecipeSelector(user, border, elementDescriptionMap, consumer).build();
	}


	/**
	 * This method builds all necessary elements in GUI panel.
	 */
	@Override
	protected void build()
	{
		PanelBuilder panelBuilder = new PanelBuilder().user(this.user);
		panelBuilder.name(this.user.getTranslation(Constants.TITLE + "recipe-selector"));

		PanelUtils.fillBorder(panelBuilder, this.border);

		this.populateElements(panelBuilder, this.filterElements);

		panelBuilder.item(3, this.createButton(Button.ACCEPT_SELECTED));
		panelBuilder.item(5, this.createButton(Button.CANCEL));

		panelBuilder.build();
	}


	/**
	 * This method is called when filter value is updated.
	 */
	@Override
	protected void updateFilters()
	{
		if (this.searchString == null || this.searchString.isBlank())
		{
			this.filterElements = this.elements;
		}
		else
		{
			this.filterElements = this.elements.stream().
				filter(element -> {
					String name = "";

					if (element instanceof BookRecipe bookRecipe)
					{
						name = bookRecipe.getBookName().toLowerCase();
					}
					else if (element instanceof ItemRecipe itemRecipe)
					{
						name = itemRecipe.getItemStack().getType().name().toLowerCase();
					}
					else if (element instanceof EntityRecipe entityRecipe)
					{
						name = entityRecipe.getEntityType().name().toLowerCase();
					}

					// If element name is set and name contains search field, then do not filter out.
					return name.contains(this.searchString.toLowerCase()) ||
						element.getCauldronType().name().toLowerCase().
							contains(this.searchString.toLowerCase()) ||
						element.getMainIngredient().getType().name().toLowerCase().
							contains(this.searchString.toLowerCase());
				}).
				distinct().
				collect(Collectors.toList());
		}
	}


	/**
	 * This method creates PanelItem button of requested type.
	 * @param button Button which must be created.
	 * @return new PanelItem with requested functionality.
	 */
	private PanelItem createButton(Button button)
	{
		final String reference = Constants.BUTTON + button.name().toLowerCase() + ".";

		final String name = this.user.getTranslation(reference + "name");
		final List<String> description = new ArrayList<>(3);
		description.add(this.user.getTranslation(reference + "description"));

		ItemStack icon;
		PanelItem.ClickHandler clickHandler;

		switch (button)
		{
			case ACCEPT_SELECTED -> {
				if (!this.selectedElements.isEmpty())
				{
					description.add(this.user.getTranslation(reference + "title"));
					this.selectedElements.forEach(recipe -> {
						description.add(this.user.getTranslation(reference + "element",
							"[element]", recipe.getRecipeName(this.user)));
					});
				}

				icon = new ItemStack(Material.COMMAND_BLOCK);
				clickHandler = (panel, user1, clickType, slot) ->
				{
					this.consumer.accept(true, this.selectedElements);
					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-save"));
			}
			case CANCEL -> {

				icon = new ItemStack(Material.IRON_DOOR);

				clickHandler = (panel, user1, clickType, slot) ->
				{
					this.consumer.accept(false, null);
					return true;
				};

				description.add("");
				description.add(this.user.getTranslation(Constants.TIPS + "click-to-cancel"));
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
	 * This method creates button for given recipe.
	 * @param recipe recipe which button must be created.
	 * @return new Button for recipe.
	 */
	@Override
	protected PanelItem createElementButton(Recipe recipe)
	{
		final String reference = Constants.BUTTON + "recipe.";

		List<String> description = new ArrayList<>();
		description.add(this.objectDescriptionMap.get(recipe));
		description.add("");

		if (this.selectedElements.contains(recipe))
		{
			description.add(this.user.getTranslation(reference + "selected"));
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-deselect"));
		}
		else
		{
			description.add(this.user.getTranslation(Constants.TIPS + "click-to-select"));
		}

		return new PanelItemBuilder().
			name(Util.translateColorCodes(recipe.getRecipeName(this.user))).
			icon(recipe.getIcon()).
			description(description).
			clickHandler((panel, user1, clickType, slot) -> {
				// On right click change which entities are selected for deletion.
				if (!this.selectedElements.add(recipe))
				{
					// Remove recipe if it is already selected
					this.selectedElements.remove(recipe);
				}

				this.build();
				return true;
			}).
			glow(this.selectedElements.contains(recipe)).
			build();
	}


	/**
	 * Functional buttons in current GUI.
	 */
	private enum Button
	{
		ACCEPT_SELECTED,
		CANCEL
	}


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


	/**
	 * This variable stores consumer.
	 */
	private final BiConsumer<Boolean, Set<Recipe>> consumer;

	/**
	 * Current value.
	 */
	private final List<Recipe> elements;

	/**
	 * Selected challenges that will be returned to consumer.
	 */
	private final Set<Recipe> selectedElements;

	/**
	 * Map that contains all object descriptions
	 */
	private final Map<Recipe, String> objectDescriptionMap;

	/**
	 * Border Material.
	 */
	private final Material border;

	/**
	 * Current value.
	 */
	private List<Recipe> filterElements;
}
