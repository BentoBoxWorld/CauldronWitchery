//
// Created by BONNe
// Copyright - 2019
//


package world.bentobox.cauldronwitchery.database.object.adapters;


import com.google.gson.*;
import java.lang.reflect.Type;

import world.bentobox.cauldronwitchery.database.object.recipe.Recipe;



/**
 * This is a generic JSON serializer and deserializer for abstract classes.
 * It store target class in class object, and instance variables in variables object.
 */
public class RecipeAdapter implements JsonSerializer<Recipe>, JsonDeserializer<Recipe>
{
	/**
	 * This class allows to serialize all Requirements classes.
	 */
	@Override
	public JsonElement serialize(Recipe src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();
		result.add("class", new JsonPrimitive(src.getClass().getSimpleName()));
		result.add("parameters", context.serialize(src, src.getClass()));

		return result;
	}


	/**
	 * This class allows to deserialize json element to correct Requirements class.
	 */
	@Override
	public Recipe deserialize(JsonElement json,
		Type typeOfT,
		JsonDeserializationContext context)
		throws JsonParseException
	{
		JsonObject jsonObject = json.getAsJsonObject();
		String type = jsonObject.get("class").getAsString();
		JsonElement element = jsonObject.get("parameters");

		try
		{
			return context.deserialize(element, Class.forName(PACKAGE + type));
		}
		catch (ClassNotFoundException e)
		{
			throw new JsonParseException("Unknown element type: " + type, e);
		}
	}


	/**
	 * Package location of all requirement classes.
	 */
	private static final String PACKAGE = "world.bentobox.cauldronwitchery.database.object.recipe.";
}