package net.example.waystonecoins;

import net.blay09.mods.waystones.api.WarpRequirement;
import net.blay09.mods.waystones.api.WarpContext;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CoinCostRequirement
 *
 * A Waystones {@link WarpRequirement} that deducts a configurable number of
 * Numismatic Overhaul bronze coins from the player's purse on teleport.
 *
 * The check phase ({@link #canBeMet}) verifies the player has enough coins and
 * sends them a hotbar message if not.  The fulfil phase ({@link #meet}) deducts
 * the coins immediately after the warp is approved.
 *
 * Creative-mode players are always exempt.
 */
public class CoinCostRequirement implements WarpRequirement {

    private static final Logger LOGGER = LogManager.getLogger(WaystoneCoinsMod.MODID);

    /** Cost expressed in bronze coins (smallest Numismatic denomination). */
    private final int bronzeCost;

    public CoinCostRequirement(int bronzeCost) {
        this.bronzeCost = bronzeCost;
    }

    // ── WarpRequirement implementation ─────────────────────────────────────────

    @Override
    public boolean canBeMet(WarpContext context) {
        Player player = context.getPlayer();

        // Creative / spectator players bypass all costs
        if (player.getAbilities().instabuild) return true;

        // Client-side: optimistically allow; server enforces
        if (!(player instanceof ServerPlayer serverPlayer)) return true;

        if (!NumismaticBridge.isAvailable()) {
            serverPlayer.displayClientMessage(
                    Component.literal("§c[WaystoneCoinsMod] ERROR: Numismatic Overhaul not loaded via Connector."),
                    false
            );
            return false;
        }

        try {
            long balance = NumismaticBridge.getBalance(serverPlayer);
            if (balance < bronzeCost) {
                serverPlayer.displayClientMessage(
                        Component.translatable(
                                "waystonecoins.not_enough_coins",
                                bronzeCost,
                                formatCoins(bronzeCost),
                                formatCoins(balance)
                        ),
                        true
                );
                return false;
            }
            return true;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("[WaystoneCoinsMod] Failed to read player balance.", e);
            return false;
        }
    }

    @Override
    public void meet(WarpContext context) {
        Player player = context.getPlayer();
        if (player.getAbilities().instabuild) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!NumismaticBridge.isAvailable()) return;

        try {
            long balance = NumismaticBridge.getBalance(serverPlayer);
            NumismaticBridge.setBalance(serverPlayer, balance - bronzeCost);
            serverPlayer.displayClientMessage(
                    Component.translatable(
                            "waystonecoins.coins_charged",
                            bronzeCost,
                            formatCoins(bronzeCost)
                    ),
                    true
            );
        } catch (ReflectiveOperationException e) {
            LOGGER.error("[WaystoneCoinsMod] Failed to deduct coins from player.", e);
        }
    }

    @Override
    public Component getDisplayText() {
        return Component.translatable("waystonecoins.cost_display", bronzeCost, formatCoins(bronzeCost));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Converts a raw bronze value to a human-readable denomination string.
     * e.g. 10350 bronze → "1g 3s 50b"
     */
    public static String formatCoins(long bronze) {
        long platinum = bronze / 1_000_000L;
        bronze %= 1_000_000L;
        long gold    = bronze / 10_000L;
        bronze %= 10_000L;
        long silver  = bronze / 100L;
        bronze %= 100L;

        StringBuilder sb = new StringBuilder();
        if (platinum > 0) sb.append(platinum).append("p ");
        if (gold     > 0) sb.append(gold).append("g ");
        if (silver   > 0) sb.append(silver).append("s ");
        if (bronze   > 0 || sb.length() == 0) sb.append(bronze).append("b");
        return sb.toString().trim();
    }
}
