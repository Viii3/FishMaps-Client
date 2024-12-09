package fish.payara.fishmaps.messaging.payload;

import com.google.gson.Gson;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public record EventRequest (EventData event, List<EventParticipation> participation) {
    private static final Gson gson = new Gson();

    public static EventRequest createDeath (PlayerEntity player, String message) {
        return new EventRequest(
            EventData.death(
                player.getBlockX(),
                player.getBlockY(),
                player.getBlockZ(),
                player.getWorld().getDimensionEntry().getIdAsString(),
                message
            ),
            List.of(EventParticipation.fromPlayer(player, EventParticipation.VICTIM))
        );
    }

    public static EventRequest createPvP (PlayerEntity killer, PlayerEntity victim, String message) {
        return new EventRequest(
            EventData.combat(
                victim.getBlockX(),
                victim.getBlockY(),
                victim.getBlockZ(),
                victim.getWorld().getDimensionEntry().getIdAsString(),
                message
            ),
            List.of(
                EventParticipation.fromPlayer(killer, EventParticipation.ATTACKER),
                EventParticipation.fromPlayer(victim, EventParticipation.VICTIM)
            )
        );
    }

    public String toJSON () {
        return gson.toJson(this);
    }

    public record EventData (int x, int y, int z, String dimension, String icon, String message) {
        private static final String COMBAT_ICON = "COMBAT";
        private static final String DEATH_ICON = "DEATH";

        public static EventData combat (int x, int y, int z, String dimension, String message) {
            return new EventData(x, y, z, dimension, COMBAT_ICON, message);
        }

        public static EventData death (int x, int y, int z, String dimension, String message) {
            return new EventData(x, y, z, dimension, DEATH_ICON, message);
        }
    }

    public record EventParticipation (String player, String role) {
        private static final String VICTIM = "victim";
        private static final String ATTACKER = "attacker";

        public static EventParticipation fromPlayer (PlayerEntity player, String role) {
            return new EventParticipation(player.getName().getString(), role);
        }
    }
}
