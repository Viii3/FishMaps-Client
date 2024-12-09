package fish.payara.fishmaps.mixin;

import com.mojang.authlib.GameProfile;
import fish.payara.fishmaps.messaging.Messenger;
import fish.payara.fishmaps.messaging.payload.EventRequest;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    protected ServerPlayerEntityMixin (World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method="onDeath", at=@At("HEAD"))
    private void emitDeath (DamageSource damageSource, CallbackInfo info) {
        EventRequest request;
        String deathMessage = damageSource.getDeathMessage(this).getString();
        if (damageSource.getAttacker() instanceof PlayerEntity attacker) {
            request = EventRequest.createPvP(attacker, this, deathMessage);
        }
        else {
            request = EventRequest.createDeath(this, deathMessage);
        }

        Messenger.cacheEvent(request);
    }
}
