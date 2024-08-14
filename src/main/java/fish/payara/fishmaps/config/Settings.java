package fish.payara.fishmaps.config;

import com.google.gson.stream.JsonReader;
import fish.payara.fishmaps.FishMapsMain;
import fish.payara.fishmaps.messaging.Messenger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Settings {
    private static final String SERVER_ADDRESS = "server_address";
    private static final String CONFIG_FILE = "config/fishmaps.json";

    private static String address = "http://localhost:8080/fishmaps";

    public static void read () {
        try {
            FileReader fileReader = new FileReader(CONFIG_FILE);
            JsonReader jsonReader = new JsonReader(fileReader);

            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (SERVER_ADDRESS.equals(name)) {
                    address = jsonReader.nextString();
                    break;
                }
            }
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
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n")
            .append("\t\"").append(SERVER_ADDRESS).append("\": \"").append(address).append("\"")
            .append("\n}");

        try {
            FileWriter writer = new FileWriter(CONFIG_FILE);
            writer.write(jsonBuilder.toString());
            writer.close();
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
}
