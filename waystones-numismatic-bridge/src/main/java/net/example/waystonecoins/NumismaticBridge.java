package net.example.waystonecoins;

import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * NumismaticBridge
 *
 * Accesses gliscowo's Numismatic Overhaul (Fabric mod) through reflection at runtime.
 * Because Numismatic Overhaul is loaded via Sinytra Connector, its classes ARE present
 * in the JVM classpath — we just cannot link against them at compile time.
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * Numismatic Overhaul internal structure (0.3.x / 1.21 branch):
 *
 *   com.glisco.numismaticoverhaul.NumismaticOverhaul
 *       public static final EntityComponentType<PlayerPurse> PURSE;
 *
 *   com.glisco.numismaticoverhaul.PlayerPurse   (implements ComponentV3 / owo EntityComponent)
 *       public long getBalance()       -- total balance expressed in bronze coins
 *       public void setBalance(long)   -- set balance in bronze coins
 *
 * The EntityComponent is retrieved via:
 *   PlayerPurse purse = ComponentAccess.get(player, NumismaticOverhaul.PURSE);
 *   -- OR --
 *   PlayerPurse purse = NumismaticOverhaul.PURSE.get(player);   // using oωo ComponentType
 *
 * oωo's EntityComponentType.get(LivingEntity) is the canonical access path.
 * ──────────────────────────────────────────────────────────────────────────────
 *
 * If the class names ever shift (mod update), only the constants below need updating.
 */
public final class NumismaticBridge {

    private static final Logger LOGGER = LogManager.getLogger(WaystoneCoinsMod.MODID);

    // ── Fully-qualified class names inside Numismatic Overhaul ──────────────────
    private static final String MAIN_CLASS   = "com.glisco.numismaticoverhaul.NumismaticOverhaul";
    private static final String PURSE_FIELD  = "PURSE";          // static EntityComponentType<PlayerPurse>
    private static final String GET_BALANCE  = "getBalance";     // () -> long
    private static final String SET_BALANCE  = "setBalance";     // (long) -> void

    // ── Cached reflection objects (initialised once, on first use) ──────────────
    private static volatile boolean initialised = false;
    private static volatile boolean available   = false;

    private static Object  purseComponentType; // EntityComponentType<PlayerPurse>
    private static Method  componentTypeGet;   // EntityComponentType#get(LivingEntity)
    private static Method  purseGetBalance;    // PlayerPurse#getBalance()
    private static Method  purseSetBalance;    // PlayerPurse#setBalance(long)

    private NumismaticBridge() {}

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Returns true if Numismatic Overhaul is present and its reflection bridge is ready.
     */
    public static boolean isAvailable() {
        ensureInit();
        return available;
    }

    /**
     * Returns the player's purse balance in bronze coins.
     * Throws if Numismatic is unavailable — always call {@link #isAvailable()} first.
     */
    public static long getBalance(ServerPlayer player) throws ReflectiveOperationException {
        Object purse = componentTypeGet.invoke(purseComponentType, player);
        return (long) purseGetBalance.invoke(purse);
    }

    /**
     * Sets the player's purse balance in bronze coins.
     * The value is clamped to >= 0.
     * Throws if Numismatic is unavailable — always call {@link #isAvailable()} first.
     */
    public static void setBalance(ServerPlayer player, long newBalance) throws ReflectiveOperationException {
        Object purse = componentTypeGet.invoke(purseComponentType, player);
        purseSetBalance.invoke(purse, Math.max(0L, newBalance));
    }

    // ── Initialisation ─────────────────────────────────────────────────────────

    private static void ensureInit() {
        if (initialised) return;
        synchronized (NumismaticBridge.class) {
            if (initialised) return;
            try {
                init();
                available = true;
                LOGGER.info("[WaystoneCoinsMod] Numismatic Overhaul bridge initialised successfully.");
            } catch (Exception e) {
                available = false;
                LOGGER.warn("[WaystoneCoinsMod] Numismatic Overhaul not found or incompatible. " +
                        "Make sure the Fabric jar is installed and Sinytra Connector is loaded. Error: {}", e.getMessage());
            } finally {
                initialised = true;
            }
        }
    }

    private static void init() throws ReflectiveOperationException {
        // 1. Load the main Numismatic class
        Class<?> mainClass = Class.forName(MAIN_CLASS);

        // 2. Get the static PURSE field (EntityComponentType<PlayerPurse>)
        Field purseField = mainClass.getField(PURSE_FIELD);
        purseComponentType = purseField.get(null);

        // 3. Find EntityComponentType#get(LivingEntity) on the component type's class
        //    oωo's EntityComponentType uses a get(T entity) method
        for (Method m : purseComponentType.getClass().getMethods()) {
            if (m.getName().equals("get") && m.getParameterCount() == 1) {
                componentTypeGet = m;
                break;
            }
        }
        if (componentTypeGet == null) {
            throw new NoSuchMethodException("Could not find EntityComponentType#get(Entity) on " + purseComponentType.getClass());
        }

        // 4. Resolve PlayerPurse methods via a dummy get on the component type.
        //    We need the actual PlayerPurse class — get it from the return type.
        Class<?> purseClass = componentTypeGet.getReturnType();

        // If return type is Object (generic erasure), derive from the field's generic signature
        if (purseClass == Object.class) {
            purseClass = Class.forName("com.glisco.numismaticoverhaul.PlayerPurse");
        }

        purseGetBalance = purseClass.getMethod(GET_BALANCE);
        purseSetBalance = purseClass.getMethod(SET_BALANCE, long.class);
    }
}
