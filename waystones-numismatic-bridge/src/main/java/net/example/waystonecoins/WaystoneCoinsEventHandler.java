package net.example.waystonecoins;

import net.blay09.mods.waystones.api.WarpRequirement;
import net.blay09.mods.waystones.api.event.WarpRequirementsCreatedEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

/**
 * WaystoneCoinsEventHandler
 *
 * Listens on the NeoForge GAME event bus for the Waystones
 * {@link WarpRequirementsCreatedEvent}, strips all XP-based requirements,
 * and substitutes a {@link CoinCostRequirement} charged in Numismatic coins.
 *
 * The event is fired server-side every time a player initiates a warp,
 * before cost validation begins — so replacing requirements here is safe.
 *
 * dist = DEDICATED_SERVER: this mod is server-side only. Clients do not need
 * it installed; coin deduction and warp gating happen entirely on the server.
 */
@EventBusSubscriber(modid = WaystoneCoinsMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.DEDICATED_SERVER)
public class WaystoneCoinsEventHandler {

    @SubscribeEvent
    public static void onWarpRequirementsCreated(WarpRequirementsCreatedEvent event) {
        List<WarpRequirement> requirements = event.getRequirements();

        // Remove every XP / experience based requirement.
        // Waystones' built-in XP cost types identify themselves with class names
        // containing "xp" or "experience" (case-insensitive).  We also remove any
        // requirement whose display text translation key contains those tokens.
        requirements.removeIf(req -> {
            String simpleName = req.getClass().getSimpleName().toLowerCase();
            return simpleName.contains("xp") || simpleName.contains("experience");
        });

        // Substitute our coin cost (0 = disabled via config)
        int cost = WaystoneCoinsConfig.COIN_COST.get();
        if (cost > 0) {
            requirements.add(new CoinCostRequirement(cost));
        }
    }
}
