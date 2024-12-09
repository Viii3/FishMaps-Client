package fish.payara.fishmaps.messaging.payload;

import com.google.gson.Gson;
import net.minecraft.entity.player.PlayerEntity;

public record PlayerData (String name, int x, int z, String dimension) {
    private static final Gson gson = new Gson();

    public static PlayerData fromPlayer (PlayerEntity player) {
        return new PlayerData(player.getName().getString(), player.getBlockX(), player.getBlockZ(), player.getWorld().getDimensionEntry().getIdAsString());
    }

    public String toJSON () {
        return gson.toJson(this);
    }
}
