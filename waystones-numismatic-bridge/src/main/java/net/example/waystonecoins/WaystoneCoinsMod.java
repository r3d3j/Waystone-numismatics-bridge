package net.example.waystonecoins;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * WaystoneCoinsMod — NeoForge 1.21.1
 *
 * Bridges Waystones (NeoForge) and Numismatic Overhaul (gliscowo / wisp-forest,
 * loaded as a Fabric mod via Sinytra Connector) so that waystone teleportation
 * costs coins instead of experience.
 *
 * Required mods in the mods folder:
 *   • NeoForge 21.1.x
 *   • Sinytra Connector (+ Forgified Fabric API)
 *   • Waystones (NeoForge build) + Balm (NeoForge build)
 *   • Numismatic Overhaul 0.3.5+1.21 (Fabric jar — loaded via Connector)
 *   • oωo-lib (Fabric jar — Numismatic dependency, also loaded via Connector)
 */
@Mod(WaystoneCoinsMod.MODID)
public class WaystoneCoinsMod {

    public static final String MODID = "waystonecoins";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public WaystoneCoinsMod(IEventBus modEventBus, ModContainer container) {
        // Register config
        container.registerConfig(ModConfig.Type.COMMON, WaystoneCoinsConfig.SPEC);

        // Listen for common setup on the mod bus
        modEventBus.addListener(this::commonSetup);

        // Register game-event listener on the NeoForge game bus
        // (Waystones fires its events here)
        NeoForge.EVENT_BUS.register(WaystoneCoinsEventHandler.class);

        LOGGER.info("[WaystoneCoinsMod] Registered. Waystone costs will be charged in Numismatic coins.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[WaystoneCoinsMod] Common setup — cost per teleport: {} bronze coins.",
                WaystoneCoinsConfig.COIN_COST.get());
    }
}
