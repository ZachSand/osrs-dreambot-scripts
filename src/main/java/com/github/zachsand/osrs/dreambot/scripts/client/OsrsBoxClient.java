package com.github.zachsand.osrs.dreambot.scripts.client;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.net.URIBuilder;
import org.dreambot.api.methods.MethodProvider;
import org.springframework.util.CollectionUtils;

import com.github.zachsand.osrs.dreambot.scripts.model.ItemModel;
import com.github.zachsand.osrs.dreambot.scripts.model.ItemsModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class OsrsBoxClient {

	private OsrsBoxClient() {}

	private static final URI BASE_URI = URI.create("https://api.osrsbox.com");
	private static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
			.setLenient()
			.create();

	private static final String EQUIPMENT_ENDPOINT = "equipment";
	private static final String WHERE_PARAM = "where";
	private static final String PROJECTION_PARAM = "projection";
	private static final String EQUIPMENT_STATS_FIELD = "equipment";
	private static final String ID_FIELD = "id";
	private static final String NAME_FIELD = "name";

	public static Optional<ItemModel> getItemModel(final long id) {
		return retrieveEquipmentStatsModel(getEquipmentStatsURI(id));
	}

	private static URI getEquipmentStatsURI(final long id) {
		JsonObject whereParamJson = new JsonObject();
		whereParamJson.addProperty("id", String.valueOf(id));
		whereParamJson.addProperty("equipable_by_player", true);
		whereParamJson.addProperty("duplicate", false);

		/* Only interested in the equipment field which contains the bonus to equipment */
		JsonObject projectionParam = new JsonObject();
		projectionParam.addProperty(EQUIPMENT_STATS_FIELD, 1);
		projectionParam.addProperty(ID_FIELD, 1);
		projectionParam.addProperty(NAME_FIELD, 1);

		try {
			return new URIBuilder(BASE_URI)
					.appendPath(EQUIPMENT_ENDPOINT)
					.addParameter(WHERE_PARAM, whereParamJson.toString())
					.addParameter(PROJECTION_PARAM, projectionParam.toString())
					.build();
		} catch (final URISyntaxException e) {
			MethodProvider.logError("Unable to create URI for OSRS Box " + e.getMessage());
			throw new IllegalStateException("Unable to create URI for OSRS Box", e);
		}
	}

	private static Optional<ItemModel> retrieveEquipmentStatsModel(final URI equipmentUri) {
		Optional<ItemModel> equipmentStatsModel = Optional.empty();
		try {
			String response = Request.get(equipmentUri)
					.execute()
					.returnContent()
					.asString();

			ItemsModel itemsModel = GSON.fromJson(response, ItemsModel.class);
			if (CollectionUtils.isEmpty(itemsModel.getItems()) || itemsModel.getItems().get(0) == null) {
				return equipmentStatsModel;
			}

			return Optional.of(itemsModel.getItems().get(0));
		} catch (IOException e) {
			MethodProvider.logError("Encountered error when trying to retrieve item from OSRS Box " + equipmentUri);
			return equipmentStatsModel;
		}
	}
}
