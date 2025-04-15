package fish.payara.fishmaps.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fish.payara.fishmaps.FishMapsMain;
import fish.payara.fishmaps.messaging.Messenger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Settings {
    private static final String KEY_SERVER_ADDRESS = "server_address";
    private static final String KEY_BASIC_AUTH = "basic_auth";

    private static final String CONFIG_FILE = "config/fishmaps.json";

    private static String address = "http://localhost:8080/fishmaps";
    private static String auth = "Basic ZmlzaG1hcHNfYWRtaW46ZmlzaG1hcHM=";

    public static void read () {
        try {
            JsonElement element = JsonParser.parseReader(new FileReader(CONFIG_FILE));
            if (element instanceof JsonObject json) {
                if (json.has(KEY_SERVER_ADDRESS)) {
                    address = json.getAsJsonPrimitive(KEY_SERVER_ADDRESS).getAsString();
                }

                if (json.has(KEY_BASIC_AUTH)) {
                    auth = json.getAsJsonPrimitive(KEY_BASIC_AUTH).getAsString();
                }
            }
            write();
        }
        catch (FileNotFoundException e) {
            FishMapsMain.LOGGER.info("Could not find FishMaps config file, creating one now.");
            try {
                new File("config/").mkdirs();
            }
            catch (Exception ignored) {

            }
            write();
        }
        catch (Exception e) {
            FishMapsMain.LOGGER.error("Error occurred whilst parsing FishMaps config: ", e);
        }

        Messenger.updateAddresses();
    }

    public static void write () {
        JsonObject json = new JsonObject();
        json.addProperty(KEY_SERVER_ADDRESS, address);
        json.addProperty(KEY_BASIC_AUTH, auth);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
        }
        catch (IOException e) {
            FishMapsMain.LOGGER.error("Error occurred whilst writing FishMaps config file: ", e);
        }
    }

    public static String getAddress () {
        return address;
    }

    public static String getAddress (String path) {
        return address + path;
    }

    public static String getAuthentication () {
        return auth;
    }
}
