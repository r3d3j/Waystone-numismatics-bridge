# Waystones Coin Cost — NeoForge 1.21.1

A NeoForge bridge mod that replaces Waystones' XP teleport costs with
**Numismatic Overhaul** coins (by gliscowo / wisp-forest).

Because gliscowo's Numismatic Overhaul is a Fabric-only mod, this setup uses
**Sinytra Connector** to run the Fabric jar inside a NeoForge modpack.

---

## Required mods (all must be in your `mods/` folder)

| Mod | Loader | Where to get |
|-----|--------|--------------|
| NeoForge 21.1.x | — | neoforged.net |
| **Sinytra Connector** | NeoForge | modrinth.com/mod/connector |
| **Forgified Fabric API** | NeoForge (via Connector) | modrinth.com/mod/forgified-fabric-api |
| **Waystones** | NeoForge | modrinth.com/mod/waystones |
| **Balm** | NeoForge | modrinth.com/mod/balm |
| **Numismatic Overhaul** `0.3.5+1.21` | Fabric (loaded via Connector) | modrinth.com/mod/numismatic-overhaul |
| **oωo-lib** | Fabric (loaded via Connector) | modrinth.com/mod/owo-lib |
| **This mod** (`waystonecoins-1.0.0.jar`) | NeoForge — **server only** | (build yourself, see below) |

---

## Server-side only

This mod only needs to be installed on the **server** (or in a singleplayer world's mods folder). Clients do not need it. All coin deduction, balance checking, and warp gating happens server-side. Players receive feedback via hotbar messages sent from the server.

## How it works

1. Sinytra Connector maps the Fabric Numismatic Overhaul jar into the NeoForge
   class loader at startup.
2. This mod listens for Waystones' `WarpRequirementsCreatedEvent` on the NeoForge
   game event bus.
3. When the event fires, all XP-based warp requirements are removed and replaced
   with a `CoinCostRequirement` that charges the player's Numismatic purse.
4. Coin reads/writes use reflection so this mod compiles without a direct
   dependency on the Fabric jar.

---

## Configuration

Edit `config/waystonecoins-common.toml` (generated on first launch):

```toml
# Cost in bronze coins per waystone teleport.
# 100 bronze = 1 silver | 10,000 bronze = 1 gold | 1,000,000 bronze = 1 platinum
# Set to 0 to disable the coin cost entirely.
coin_cost = 10
```

---

## Building from source

### Prerequisites
- JDK 21
- Internet connection (Gradle downloads dependencies automatically)

### Steps

```bash
# 1. Clone / unzip this project
cd waystones-numismatic-bridge

# 2. (Recommended) Download Numismatic Overhaul Fabric jar manually from Modrinth
#    and place it at:  libs/numismatic-overhaul-0.3.5+1.21.jar
#    Then in build.gradle switch the dependency to:
#      compileOnly files('libs/numismatic-overhaul-0.3.5+1.21.jar')

# 3. Build
./gradlew build

# 4. The output jar will be at:
#    build/libs/waystonecoins-1.0.0.jar
```

Copy `waystonecoins-1.0.0.jar` into your instance's `mods/` folder alongside
all the required mods listed above.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `[WaystoneCoinsMod] Numismatic Overhaul not loaded via Connector` in chat | Numismatic or oωo is missing from mods/. Make sure both Fabric jars are present. |
| Waystones still charges XP | Check that `coin_cost` > 0 in the config and that the mod loaded (look for the mod in the mods list). |
| `ClassNotFoundException: com.glisco.numismaticoverhaul...` in logs | Connector didn't map Numismatic. Check your Connector and Forgified Fabric API versions match 1.21.1. |
| Coin balance goes negative | Should not happen — `setBalance` clamps to 0. File an issue. |

---

## License

MIT — do whatever you like with this code.
