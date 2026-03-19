package net.example.waystonecoins;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Config file: config/waystonecoins-common.toml
 *
 * coin_cost — number of bronze coins charged per waystone teleport.
 *
 * Numismatic Overhaul denomination ladder (all values stored as bronze internally):
 *   100 bronze  = 1 silver
 *   10 000 bronze = 1 gold   (100 silver)
 *   1 000 000 bronze = 1 platinum (100 gold)
 *
 * Default: 10 bronze coins per teleport.
 */
@EventBusSubscriber(modid = WaystoneCoinsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class WaystoneCoinsConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue COIN_COST = BUILDER
            .comment(
                "Cost in bronze coins charged per waystone teleport.",
                "100 bronze = 1 silver | 10,000 bronze = 1 gold | 1,000,000 bronze = 1 platinum.",
                "Set to 0 to disable the coin cost entirely."
            )
            .defineInRange("coin_cost", 10, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        WaystoneCoinsMod.LOGGER.info("[WaystoneCoinsMod] Config (re)loaded — coin_cost = {} bronze.", COIN_COST.get());
    }
}
