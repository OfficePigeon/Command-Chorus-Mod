package fun.wich;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;

public class ChorusCommand implements ModInitializer {
	@Override public void onInitialize() { CommandRegistrationCallback.EVENT.register(ChorusCommand::register); }
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
		dispatcher.register(CommandManager.literal("chorus")
				.requires(source -> source.hasPermissionLevel(2))
				.executes(context -> execute(context.getSource(), ImmutableList.of(context.getSource().getEntityOrThrow()), 1))
				.then(CommandManager.argument("targets", EntityArgumentType.entities())
						.executes(context -> execute(context.getSource(), EntityArgumentType.getEntities(context, "targets"), 1))
						.then(CommandManager.argument("chance", FloatArgumentType.floatArg(0, 1))
								.executes(context -> execute(context.getSource(), EntityArgumentType.getEntities(context, "targets"), FloatArgumentType.getFloat(context, "chance"))))));
	}
	private static int execute(ServerCommandSource source, Collection<? extends Entity> targets, float chance) {
		if (chance > 0) {
			int count = 0;
			for (Entity entity : targets) {
				if (entity instanceof LivingEntity) {
					if (((LivingEntity)entity).getRandom().nextFloat() <= chance) {
						TeleportEntity((LivingEntity)entity);
						count++;
					}
				}
			}
			if (targets.size() == 1 && count > 0) source.sendFeedback(new TranslatableText("commands.chorus.success.single", targets.iterator().next().getDisplayName()), true);
			else source.sendFeedback(new TranslatableText("commands.chorus.success.multiple", count), true);
		}
		return targets.size();
	}
	public static void TeleportEntity(LivingEntity entity) {
		if (entity.getEntityWorld() instanceof ServerWorld) {
			ServerWorld world = (ServerWorld)entity.getEntityWorld();
			double x = entity.getX(), y = entity.getY(), z = entity.getZ();
			for (int i = 0; i < 16; ++i) {
				double g = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 16;
				double h = MathHelper.clamp(entity.getY() + (double)(entity.getRandom().nextInt(16) - 8), 0.0F, world.getEffectiveHeight() - 1);
				double j = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 16;
				if (entity.hasVehicle()) entity.stopRiding();
				if (entity.teleport(g, h, j, true)) {
					world.playSound(null, x, y, z, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1, 1);
					entity.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
					break;
				}
			}
		}
	}
}