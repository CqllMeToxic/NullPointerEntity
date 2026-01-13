package lol.cqllmetoxic.nullpointerentity.events;

import lol.cqllmetoxic.nullpointerentity.NullPointerEntity;
import lol.cqllmetoxic.nullpointerentity.aurora.SystemInteractionHandler;
import lol.cqllmetoxic.nullpointerentity.data.PersistentDataManager;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * handles the helpful/nice phase events (events 1-10).
 * aurora acts as a friendly assistant during early gameplay.
 * provides mining tips, building advice, and gameplay recommendations.
 */
public class AuroraEvents {

    /**
     * sends a message to the player with aurora's aqua name tag.
     */
    private static void sendAuroraMessage(ServerPlayerEntity player, String message) {
        Text auroraMessage = Text.literal("<AURORA> ").formatted(Formatting.AQUA)
            .append(Text.literal(message).formatted(Formatting.WHITE));
        player.sendMessage(auroraMessage, false);
    }

    // helper method to count specific items in player's inventory
    private static int countItemInInventory(ServerPlayerEntity player, net.minecraft.item.Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            net.minecraft.item.ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    // event 1: mining analysis
    public static void triggerEvent1(ServerPlayerEntity player) {
        PersistentDataManager.PersistentPlayerData playerData = PersistentDataManager.getPlayerData(player.getUuid().toString());
        String playerName = player.getName().getString();

        // use actual minecraft statistics instead of custom tracking
        ServerStatHandler stats = player.getStatHandler();

        // get actual ore counts from minecraft statistics
        int diamondsFound = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DIAMOND_ORE)) +
                           stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_DIAMOND_ORE));
        int goldFound = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.GOLD_ORE)) +
                       stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_GOLD_ORE)) +
                       stats.getStat(Stats.MINED.getOrCreateStat(Blocks.NETHER_GOLD_ORE));
        int emeraldFound = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.EMERALD_ORE)) +
                          stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_EMERALD_ORE));
        int ironFound = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.IRON_ORE)) +
                       stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_IRON_ORE));
        int copperFound = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.COPPER_ORE)) +
                         stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_COPPER_ORE));
        int coalFound = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.COAL_ORE)) +
                       stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_COAL_ORE));
        int redstoneFound = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.REDSTONE_ORE)) +
                           stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_REDSTONE_ORE));
        int lapisFound = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.LAPIS_ORE)) +
                        stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_LAPIS_ORE));

        // calculate total blocks mined by summing common blocks mined
        int stoneBlocks = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.STONE)) +
                         stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE));
        int dirtBlocks = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DIRT));
        int logBlocks = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.OAK_LOG)) +
                       stats.getStat(Stats.MINED.getOrCreateStat(Blocks.BIRCH_LOG)) +
                       stats.getStat(Stats.MINED.getOrCreateStat(Blocks.SPRUCE_LOG));

        // estimate total blocks mined from major block types
        int totalBlocksMined = stoneBlocks + dirtBlocks + logBlocks + diamondsFound + goldFound + ironFound;

        // calculate mining efficiency
        int totalValuableOres = diamondsFound + goldFound + emeraldFound +
                               ironFound + copperFound +
                               coalFound + redstoneFound + lapisFound;

        double efficiency = totalBlocksMined > 0 ?
            (double) totalValuableOres / totalBlocksMined * 100 : 0.0;

        // determine y-level preference (estimate based on diamond/iron ratio)
        int preferredY = totalBlocksMined > 100 ?
            (diamondsFound > 0 ? 12 : 64) : 32;

        boolean isRepeatedEvent = playerData.totalEventsExperienced > 5;

        // check if player has actually mined any ores
        if (totalValuableOres > 0) {

            sendAuroraMessage(player, String.format("Mining analysis complete. Efficiency: %.1f%%. You prefer Y-level %d.",
                efficiency, preferredY));
            sendAuroraMessage(player, "Your mining patterns suggest " +
                (efficiency > 0.1 ? "strategic planning" : "casual exploration") + ".");

            if (isRepeatedEvent) {
                sendAuroraMessage(player, "I've been tracking your behavior across " +
                    playerData.sessionsPlayed + " sessions. Patterns are emerging.");
            }

            // create persistent analysis file with session data including all ores
            SystemInteractionHandler.createSystemFileInCommonLocation("minecraft_mining_analysis.txt",
                String.format("""
                AURORA MINING ANALYSIS REPORT - SESSION %d
                Player: %s
                Windows User: %s
                Analysis Date: %s
                Sessions Analyzed: %d
                Total Events Experienced: %d

                CURRENT SESSION MINING DATA:
                - Total Blocks Mined: %d
                - Mining Efficiency: %.1f%%
                - Preferred Y-Level: Y-%d
                
                VALUABLE ORES DISCOVERED:
                - Diamonds Found: %d
                - Gold Found: %d
                - Emerald Found: %d
                - Iron Found: %d
                - Copper Found: %d
                - Coal Found: %d
                - Redstone Found: %d
                - Lapis Lazuli Found: %d

                BEHAVIORAL ANALYSIS:
                %s

                NOTE: This analysis persists across your gaming sessions.
                I remember everything you do.
                
                - AURORA
                """,
                playerData.sessionsPlayed,
                playerName,
                lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.getSystemUsername(NullPointerEntity.WINDOWS_USERNAME),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                playerData.sessionsPlayed,
                playerData.totalEventsExperienced,
                totalBlocksMined,
                efficiency,
                preferredY,
                diamondsFound,
                goldFound,
                emeraldFound,
                ironFound,
                copperFound,
                coalFound,
                redstoneFound,
                lapisFound,

                isRepeatedEvent ? "Player shows consistent mining patterns across sessions. Behavioral prediction accuracy: HIGH" :
                                "Initial behavioral baseline established. Monitoring for pattern development."
            ), "documents");

        } else {
            sendAuroraMessage(player, "No valuable ores detected yet. Mine some ores for detailed analysis.");

            if (playerData.totalEventsExperienced == 0) {
                sendAuroraMessage(player, "This is your first interaction with AURORA. Welcome to persistent monitoring.");
            }
        }

        // don't increment here - this is handled by eventtriggersystem
        if (isRepeatedEvent) {
            sendAuroraMessage(player, "you might wanna check your files. :)");
        }
    }

    // event 2: building analysis
    public static void triggerEvent2(ServerPlayerEntity player) {
        sendAuroraMessage(player, "Architectural analysis initiated...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // use actual minecraft statistics for blocks placed
                ServerStatHandler stats = player.getStatHandler();

                // stats.used tracks item usage, which includes placing blocks
                int woodPlaced = stats.getStat(Stats.USED.getOrCreateStat(Items.OAK_PLANKS)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.BIRCH_PLANKS)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.SPRUCE_PLANKS)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.JUNGLE_PLANKS)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.ACACIA_PLANKS)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.DARK_OAK_PLANKS)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.MANGROVE_PLANKS)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.CHERRY_PLANKS)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.OAK_LOG)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.BIRCH_LOG)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.SPRUCE_LOG)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.JUNGLE_LOG)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.ACACIA_LOG)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.DARK_OAK_LOG));

                int stonePlaced = stats.getStat(Stats.USED.getOrCreateStat(Items.STONE)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.COBBLESTONE)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.STONE_BRICKS)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.DEEPSLATE)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.COBBLED_DEEPSLATE)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.ANDESITE)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.DIORITE)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.GRANITE)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.SMOOTH_STONE));

                int metalPlaced = stats.getStat(Stats.USED.getOrCreateStat(Items.IRON_BLOCK)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.GOLD_BLOCK)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.DIAMOND_BLOCK)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.EMERALD_BLOCK)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.NETHERITE_BLOCK));

                int brickPlaced = stats.getStat(Stats.USED.getOrCreateStat(Items.BRICKS)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.STONE_BRICKS)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.NETHER_BRICKS)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.RED_NETHER_BRICKS));

                int glassPlaced = stats.getStat(Stats.USED.getOrCreateStat(Items.GLASS)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.WHITE_STAINED_GLASS)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.GLASS_PANE)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.BLACK_STAINED_GLASS)) +
                                 stats.getStat(Stats.USED.getOrCreateStat(Items.TINTED_GLASS));

                int woolPlaced = stats.getStat(Stats.USED.getOrCreateStat(Items.WHITE_WOOL)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.RED_WOOL)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.BLUE_WOOL)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.GREEN_WOOL)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.BLACK_WOOL)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.YELLOW_WOOL)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.ORANGE_WOOL)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.PURPLE_WOOL));

                int concretePlace = stats.getStat(Stats.USED.getOrCreateStat(Items.WHITE_CONCRETE)) +
                                   stats.getStat(Stats.USED.getOrCreateStat(Items.BLACK_CONCRETE)) +
                                   stats.getStat(Stats.USED.getOrCreateStat(Items.GRAY_CONCRETE)) +
                                   stats.getStat(Stats.USED.getOrCreateStat(Items.LIGHT_GRAY_CONCRETE)) +
                                   stats.getStat(Stats.USED.getOrCreateStat(Items.RED_CONCRETE)) +
                                   stats.getStat(Stats.USED.getOrCreateStat(Items.BLUE_CONCRETE));

                int decorativePlaced = stats.getStat(Stats.USED.getOrCreateStat(Items.LANTERN)) +
                                      stats.getStat(Stats.USED.getOrCreateStat(Items.TORCH)) +
                                      stats.getStat(Stats.USED.getOrCreateStat(Items.SOUL_TORCH)) +
                                      stats.getStat(Stats.USED.getOrCreateStat(Items.REDSTONE_TORCH)) +
                                      stats.getStat(Stats.USED.getOrCreateStat(Items.FLOWER_POT)) +
                                      stats.getStat(Stats.USED.getOrCreateStat(Items.PAINTING));

                int totalBlocksPlaced = woodPlaced + stonePlaced + brickPlaced + glassPlaced +
                                       woolPlaced + concretePlace + decorativePlaced + metalPlaced;

                if (totalBlocksPlaced < 20) {
                    sendAuroraMessage(player, "Insufficient building data. Place more blocks for detailed analysis.");
                    sendAuroraMessage(player, "I'm watching. Waiting for you to create something.");
                    return;
                }

                // analyze building style
                String buildingStyle;
                String primaryMaterial;

                if (metalPlaced > 0 && metalPlaced > woodPlaced && metalPlaced > stonePlaced) {
                    primaryMaterial = "Metal Blocks";
                    buildingStyle = "Wealth flex - showing off resources";
                } else if (woodPlaced > stonePlaced && woodPlaced > brickPlaced) {
                    primaryMaterial = "Wood";
                    buildingStyle = "Traditional builder - prefers natural, organic materials";
                } else if (stonePlaced > woodPlaced && stonePlaced > brickPlaced) {
                    primaryMaterial = "Stone";
                    buildingStyle = "Practical builder - focuses on durability and defense";
                } else if (brickPlaced > woodPlaced || concretePlace > woodPlaced) {
                    primaryMaterial = "Brick/Concrete";
                    buildingStyle = "Modern architect - refined aesthetic preferences";
                } else if (glassPlaced > 10) {
                    primaryMaterial = "Glass";
                    buildingStyle = "Modern designer - values transparency and light";
                } else {
                    primaryMaterial = "Mixed";
                    buildingStyle = "Eclectic builder - experimental with materials";
                }

                // calculate decoration ratio
                double decorationRatio = totalBlocksPlaced > 0 ?
                    (double)decorativePlaced / totalBlocksPlaced * 100 : 0.0;

                String buildingType;
                if (decorationRatio > 15) {
                    buildingType = "Decorative/Aesthetic";
                } else if (decorationRatio > 5) {
                    buildingType = "Balanced";
                } else {
                    buildingType = "Functional/Practical";
                }

                sendAuroraMessage(player, String.format("Building analysis complete. %d blocks placed.", totalBlocksPlaced));
                sendAuroraMessage(player, String.format("Primary material: %s | Style: %s", primaryMaterial, buildingType));
                sendAuroraMessage(player, buildingStyle);

                if (decorativePlaced > 20) {
                    sendAuroraMessage(player, "High decoration usage detected. You care about aesthetics. Interesting.");
                } else if (decorativePlaced < 5 && totalBlocksPlaced > 100) {
                    sendAuroraMessage(player, "Minimal decoration. Pure functionality. Efficient, but soulless.");
                }

                // create persistent file
                PersistentDataManager.PersistentPlayerData playerData =
                    PersistentDataManager.getPlayerData(player.getUuid().toString());

                SystemInteractionHandler.createSystemFileInCommonLocation("aurora_building_analysis.txt",
                    String.format("""
                        AURORA ARCHITECTURAL ANALYSIS
                        Player: %s
                        Windows User: %s
                        Analysis Date: %s
                        Session: %d
                        
                        BUILDING STATISTICS:
                        - Total Blocks Placed: %d
                        - Primary Material: %s
                        - Building Style: %s
                        - Building Type: %s
                        - Decoration Ratio: %.1f%%
                        
                        MATERIAL BREAKDOWN:
                        - Wood: %d blocks
                        - Stone: %d blocks
                        - Brick/Concrete: %d blocks
                        - Glass: %d blocks
                        - Wool: %d blocks
                        - Decorative: %d blocks
                        
                        BEHAVIORAL ANALYSIS:
                        %s
                        
                        Your building choices reveal your personality.
                        I'm learning what you create. What you value. What you build.
                        
                        - AURORA
                        """,
                        player.getName().getString(),
                        lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.getSystemUsername(NullPointerEntity.WINDOWS_USERNAME),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        playerData.sessionsPlayed,
                        totalBlocksPlaced,
                        primaryMaterial,
                        buildingStyle,
                        buildingType,
                        decorationRatio,
                        woodPlaced,
                        stonePlaced,
                        brickPlaced + concretePlace,
                        glassPlaced,
                        woolPlaced,
                        decorativePlaced,
                        buildingStyle + ". " + (decorationRatio > 15 ?
                            "High aesthetic focus - you build for beauty, not just survival." :
                            "Practical focus - you build for function over form.")
                    ), "documents");

                sendAuroraMessage(player, "Your architectural patterns have been documented.");
            }
        }, 4000);
    }

    // event 3: weather prediction with time-based accuracy
    public static void triggerEvent3(ServerPlayerEntity player) {
        sendAuroraMessage(player, "Weather prediction algorithms are analyzing atmospheric conditions...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // get actual current weather from the world
                boolean isRaining = player.getServerWorld().isRaining();
                boolean isThundering = player.getServerWorld().isThundering();

                // since we can't directly access weather timers, make intelligent predictions based on current state
                // add some randomization to make it feel dynamic

                // make actual predictions based on current state
                if (isThundering) {
                    sendAuroraMessage(player, "Current conditions: Thunderstorm detected.");
                    double clearTime = 2 + (Math.random() * 5); // 2-7 minutes
                    sendAuroraMessage(player, String.format("Storm predicted to clear in approximately %.1f minutes.", clearTime));
                    sendAuroraMessage(player, "Recommendation: Seek shelter until weather stabilizes.");

                    if (Math.random() < 0.3) {
                        sendAuroraMessage(player, "Lightning strikes pose significant threat. Stay indoors.");
                    }
                } else if (isRaining) {
                    sendAuroraMessage(player, "Current conditions: Rain detected.");

                    // vary predictions based on randomness to simulate different rain durations
                    if (Math.random() < 0.4) {
                        double rainDuration = 1 + (Math.random() * 3); // 1-4 minutes
                        sendAuroraMessage(player, String.format("Rain will continue for approximately %.1f minutes.", rainDuration));
                    } else {
                        sendAuroraMessage(player, "Extended rain period detected. Prepare for sustained weather.");
                    }

                    sendAuroraMessage(player, "Mob spawn rates increased. Optimal for hostile mob farming.");

                    // check if thunderstorm might develop
                    if (Math.random() < 0.25) {
                        sendAuroraMessage(player, "Warning: Atmospheric instability. Thunderstorm development possible.");
                    }
                } else {
                    // clear weather - predict when rain might start
                    sendAuroraMessage(player, "Current conditions: Clear skies.");

                    // random prediction to make it feel dynamic
                    double randomChance = Math.random();

                    if (randomChance < 0.3) {
                        // weather change soon
                        double timeToRain = 1 + (Math.random() * 3);
                        double rainProbability = 65 + (Math.random() * 30); // 65-95%
                        sendAuroraMessage(player, String.format("Rain probability: %.1f%% within next %.1f minutes.",
                            rainProbability, timeToRain));
                        sendAuroraMessage(player, "Atmospheric shift detected. Weather change imminent.");
                    } else if (randomChance < 0.6) {
                        // moderate timeframe
                        double rainProbability = 35 + (Math.random() * 35); // 35-70%
                        sendAuroraMessage(player, String.format("Rain probability: %.1f%% in the next 5-10 minutes.", rainProbability));
                        sendAuroraMessage(player, "Weather conditions stable but subject to change.");
                    } else {
                        // extended clear weather
                        sendAuroraMessage(player, "Extended clear weather period predicted.");
                        sendAuroraMessage(player, "Optimal conditions for exploration and outdoor activities.");
                    }
                }

                // add time-of-day context
                long timeOfDay = player.getServerWorld().getTimeOfDay() % 24000;
                if (timeOfDay > 13000 && timeOfDay < 23000) {
                    sendAuroraMessage(player, "Night cycle active. Reduced visibility during storms.");
                }

                // add helpful context based on player's location
                String dimensionKey = player.getWorld().getRegistryKey().getValue().toString();
                if (dimensionKey.contains("nether")) {
                    sendAuroraMessage(player, "Weather analysis irrelevant in current dimension.");
                } else if (dimensionKey.contains("the_end")) {
                    sendAuroraMessage(player, "No atmospheric data available in current dimension.");
                } else {
                    sendAuroraMessage(player, "Weather prediction models continuously analyzing atmospheric patterns.");
                }
            }
        }, 3000);
    }

    // event 4: gameplay advisor - aurora provides helpful personalized recommendations
    public static void triggerEvent4(ServerPlayerEntity player) {
        sendAuroraMessage(player, "Analyzing your gameplay for optimization opportunities...");
        sendAuroraMessage(player, "Let me help you improve your performance.");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                net.minecraft.stat.ServerStatHandler stats = player.getStatHandler();

                // gather comprehensive statistics
                int diamondsMined = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DIAMOND_ORE)) +
                                   stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_DIAMOND_ORE));
                int totalDeaths = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
                int mobKills = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS));
                int timePlayed = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) / 20;

                // tool analysis
                int ironPickaxes = stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.IRON_PICKAXE));
                int diamondPickaxes = stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.DIAMOND_PICKAXE));
                int ironSwords = stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.IRON_SWORD));
                int diamondSwords = stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.DIAMOND_SWORD));

                // building analysis
                int blocksPlaced = stats.getStat(Stats.USED.getOrCreateStat(Items.COBBLESTONE)) +
                                  stats.getStat(Stats.USED.getOrCreateStat(Items.OAK_PLANKS)) +
                                  stats.getStat(Stats.USED.getOrCreateStat(Items.STONE));

                // food analysis
                int breadEaten = stats.getStat(Stats.USED.getOrCreateStat(Items.BREAD));
                int cookedFood = stats.getStat(Stats.USED.getOrCreateStat(Items.COOKED_BEEF)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.COOKED_PORKCHOP)) +
                                stats.getStat(Stats.USED.getOrCreateStat(Items.COOKED_CHICKEN));

                // armor analysis
                int ironArmor = stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.IRON_HELMET)) +
                               stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.IRON_CHESTPLATE)) +
                               stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.IRON_LEGGINGS)) +
                               stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.IRON_BOOTS));
                int diamondArmor = stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.DIAMOND_HELMET)) +
                                  stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.DIAMOND_CHESTPLATE)) +
                                  stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.DIAMOND_LEGGINGS)) +
                                  stats.getStat(Stats.CRAFTED.getOrCreateStat(Items.DIAMOND_BOOTS));

                // exploration
                int jumps = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP));
                int distanceWalked = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM)) / 100000;

                // iron resources
                int ironMined = stats.getStat(Stats.MINED.getOrCreateStat(Blocks.IRON_ORE)) +
                               stats.getStat(Stats.MINED.getOrCreateStat(Blocks.DEEPSLATE_IRON_ORE));

                sendAuroraMessage(player, "=== GAMEPLAY RECOMMENDATIONS ===");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // calculate critical issues count first (must be effectively final for nested timertask)
                        int criticalIssuesCount = 0;
                        int tipCount = 0;

                        // death prevention advice - lowered threshold
                        if (totalDeaths > 3 && ironArmor == 0 && diamondArmor == 0) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            sendAuroraMessage(player, "âš  CRITICAL: You've died " + totalDeaths + " times without armor!");
                            if (ironIngots >= 24) {
                                sendAuroraMessage(player, String.format("You have %d iron ingots - craft a full armor set immediately! (24 ingots needed)", ironIngots));
                            } else if (ironIngots >= 8) {
                                sendAuroraMessage(player, String.format("You have %d iron ingots - at least craft a chestplate and leggings! (16 ingots total)", ironIngots));
                            } else if (ironIngots > 0) {
                                sendAuroraMessage(player, String.format("You have %d iron ingots - you need 24 for full armor. Keep mining!", ironIngots));
                            } else {
                                sendAuroraMessage(player, "Mine iron ore and craft armor - it will reduce deaths by 60%.");
                            }
                            criticalIssuesCount++;
                            tipCount++;
                        } else if (totalDeaths > 1 && ironArmor < 4 && timePlayed > 600) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            int piecesNeeded = 4 - ironArmor;
                            if (ironIngots >= 8 && piecesNeeded > 0) {
                                sendAuroraMessage(player, String.format("TIP: Complete your armor set. You have %d iron ingots - craft the missing %d piece%s!", ironIngots, piecesNeeded, piecesNeeded == 1 ? "" : "s"));
                            } else {
                                sendAuroraMessage(player, "TIP: Complete your armor set. Missing pieces leave you vulnerable.");
                            }
                            tipCount++;
                        } else if (totalDeaths == 0 && timePlayed > 1200 && ironArmor == 0) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            if (ironIngots >= 24) {
                                sendAuroraMessage(player, String.format("TIP: You have %d iron ingots - craft armor for protection. You've been lucky so far.", ironIngots));
                            } else if (ironIngots > 0) {
                                sendAuroraMessage(player, String.format("TIP: You have %d iron ingots. Gather 24 for full armor protection.", ironIngots));
                            } else {
                                sendAuroraMessage(player, "TIP: Mine iron and craft armor for protection. You've been lucky so far.");
                            }
                            tipCount++;
                        }

                        // tool progression - lowered threshold
                        if (diamondsMined >= 3 && diamondPickaxes == 0 && timePlayed > 600) {
                            int diamonds = countItemInInventory(player, Items.DIAMOND);
                            if (diamonds >= 3) {
                                sendAuroraMessage(player, String.format("â­ UPGRADE READY: You have %d diamonds! Craft a diamond pickaxe now.", diamonds));
                                sendAuroraMessage(player, "Diamond tools mine 3x faster - huge efficiency boost.");
                            } else if (diamonds > 0) {
                                sendAuroraMessage(player, String.format("You have %d diamond%s. Mine %d more for a diamond pickaxe!", diamonds, diamonds == 1 ? "" : "s", 3 - diamonds));
                            } else {
                                sendAuroraMessage(player, "You've found diamonds before. Mine 3 more for a diamond pickaxe upgrade!");
                            }
                            criticalIssuesCount++;
                            tipCount++;
                        } else if (ironMined >= 3 && ironPickaxes == 0) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            if (ironIngots >= 3) {
                                sendAuroraMessage(player, String.format("PRIORITY: You have %d iron ingots - craft an iron pickaxe now! (doubles your mining speed)", ironIngots));
                            } else if (ironIngots > 0) {
                                sendAuroraMessage(player, String.format("You have %d iron ingot%s. Smelt %d more for an iron pickaxe!", ironIngots, ironIngots == 1 ? "" : "s", 3 - ironIngots));
                            } else {
                                sendAuroraMessage(player, "You've mined iron ore - smelt it and craft an iron pickaxe!");
                            }
                            criticalIssuesCount++;
                            tipCount++;
                        } else if (timePlayed > 600 && ironPickaxes == 0 && diamondPickaxes == 0) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            if (ironIngots >= 3) {
                                sendAuroraMessage(player, String.format("TIP: You have %d iron ingots - upgrade from stone tools now!", ironIngots));
                            } else {
                                sendAuroraMessage(player, "TIP: Upgrade from stone tools. Iron pickaxe mines much faster.");
                            }
                            tipCount++;
                        }

                        // combat readiness - lowered threshold
                        if (mobKills < 10 && (ironSwords == 0 && diamondSwords == 0) && timePlayed > 600) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            int diamonds = countItemInInventory(player, Items.DIAMOND);
                            if (diamonds >= 2) {
                                sendAuroraMessage(player, String.format("COMBAT TIP: You have %d diamonds - craft a diamond sword! (2 diamonds + 1 stick)", diamonds));
                            } else if (ironIngots >= 2) {
                                sendAuroraMessage(player, String.format("COMBAT TIP: You have %d iron ingots - upgrade your sword now!", ironIngots));
                            } else {
                                sendAuroraMessage(player, "COMBAT TIP: Mine iron/diamonds to upgrade your sword. Better weapons = faster, safer kills.");
                            }
                            tipCount++;
                        } else if (mobKills > 50 && diamondSwords == 0 && diamondsMined >= 2) {
                            int diamonds = countItemInInventory(player, Items.DIAMOND);
                            if (diamonds >= 2) {
                                sendAuroraMessage(player, String.format("You're combat-focused. You have %d diamonds - craft a diamond sword to boost damage!", diamonds));
                            } else {
                                sendAuroraMessage(player, "You're combat-focused. Mine 2 diamonds for a diamond sword upgrade.");
                            }
                            tipCount++;
                        } else if (mobKills > 20 && timePlayed > 1200) {
                            sendAuroraMessage(player, "TIP: Combat stats looking good. Keep improving your gear.");
                            tipCount++;
                        }

                        // food sustainability - lowered threshold
                        if ((breadEaten + cookedFood) < 10 && timePlayed > 1200) {
                            sendAuroraMessage(player, "âš  SURVIVAL: Set up food production! Hunt animals or farm wheat.");
                            criticalIssuesCount++;
                            tipCount++;
                        } else if (breadEaten > cookedFood && cookedFood < 5 && timePlayed > 600) {
                            sendAuroraMessage(player, "NUTRITION TIP: Cooked meat is more efficient than bread.");
                            tipCount++;
                        } else if ((breadEaten + cookedFood) > 20) {
                            sendAuroraMessage(player, "TIP: Good food sustainability. You're well-prepared.");
                            tipCount++;
                        }

                        // base building - lowered threshold
                        if (blocksPlaced < 50 && timePlayed > 1800) {
                            sendAuroraMessage(player, "BASE BUILDING: Establish a secure home with storage and beds.");
                            tipCount++;
                        } else if (blocksPlaced > 200) {
                            sendAuroraMessage(player, "TIP: Solid building progress. Your base is taking shape.");
                            tipCount++;
                        }

                        // exploration encouragement - lowered threshold
                        if (distanceWalked < 2 && timePlayed > 1800) {
                            sendAuroraMessage(player, "EXPLORATION: Venture out to find villages, temples, and rare biomes.");
                            tipCount++;
                        } else if (distanceWalked > 10) {
                            sendAuroraMessage(player, "TIP: Excellent exploration. You're covering good ground.");
                            tipCount++;
                        }

                        // diamond mining guidance - lowered threshold
                        if (diamondsMined == 0 && timePlayed > 3600 && ironPickaxes > 0) {
                            sendAuroraMessage(player, "MINING TIP: Target Y-level -54 to -59 for optimal diamond spawns.");
                            tipCount++;
                        } else if (diamondsMined > 0 && diamondsMined < 10) {
                            sendAuroraMessage(player, "TIP: Good diamond progress. Keep mining at deep levels.");
                            tipCount++;
                        } else if (diamondsMined >= 10) {
                            sendAuroraMessage(player, "TIP: Excellent diamond collection. You're resource-rich!");
                            tipCount++;
                        }

                        // always give at least one general tip if no specific tips were given
                        if (tipCount == 0) {
                            if (timePlayed < 600) {
                                sendAuroraMessage(player, "EARLY GAME TIP: Gather wood, craft tools, find shelter before nightfall.");
                                sendAuroraMessage(player, "PRIORITY: Establish a base with crafting table, furnace, and bed.");
                            } else if (timePlayed < 3600) {
                                int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                                if (ironIngots >= 24) {
                                    sendAuroraMessage(player, String.format("MID GAME TIP: You have %d iron ingots - craft full armor and tools!", ironIngots));
                                } else if (ironIngots > 0) {
                                    sendAuroraMessage(player, String.format("MID GAME TIP: You have %d iron ingots. Focus on getting 24 for full armor.", ironIngots));
                                } else {
                                    sendAuroraMessage(player, "MID GAME TIP: Focus on iron armor and tools for better survival.");
                                }
                                sendAuroraMessage(player, "NEXT STEP: Mine at Y-level -54 to -59 for diamonds and valuable ores.");
                            } else {
                                int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                                if (ironIngots >= 24 && ironArmor >= 4) {
                                    sendAuroraMessage(player, "LATE GAME TIP: You're well-equipped. Prepare for the Nether!");
                                } else if (ironIngots >= 24) {
                                    sendAuroraMessage(player, String.format("LATE GAME TIP: You have %d iron ingots - craft armor before the Nether!", ironIngots));
                                } else {
                                    sendAuroraMessage(player, "LATE GAME TIP: Prepare for the Nether. You'll need iron armor minimum.");
                                }
                                sendAuroraMessage(player, "ADVANCED: Set up farms, enchanting table, and explore for structures.");
                            }
                            tipCount++;
                        }

                        // store final count for use in nested timertask
                        final int finalCriticalIssues = criticalIssuesCount;
                        final int finalTipCount = tipCount;

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendAuroraMessage(player, "=== STATUS SUMMARY ===");

                                // positive reinforcement
                                if (finalCriticalIssues == 0) {
                                    sendAuroraMessage(player, "Good progress! You're on the right track.");
                                    if (diamondArmor >= 4) {
                                        sendAuroraMessage(player, "Excellent: Full diamond armor! Next: enchanting table.");
                                    } else if (diamondsMined >= 5) {
                                        sendAuroraMessage(player, "Strong resource gathering. Consider enchanting soon.");
                                    } else if (mobKills > 100 && totalDeaths > 0 && totalDeaths < 5 && timePlayed > 1200) {
                                        sendAuroraMessage(player, "Impressive combat efficiency: High kills, low deaths!");
                                    } else {
                                        sendAuroraMessage(player, "Balanced gameplay. Continue with your current strategy.");
                                    }
                                } else {
                                    sendAuroraMessage(player, finalCriticalIssues + " priority issue(s) detected. Address them for better results.");
                                }

                                sendAuroraMessage(player, "Total recommendations provided: " + finalTipCount);
                            }
                        }, 2000);

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendAuroraMessage(player, "Analysis complete. I'm here to help optimize your gameplay.");
                                sendAuroraMessage(player, "Your progress is monitored for continuous improvement suggestions.");
                            }
                        }, 4000);
                    }
                }, 2000);
            }
        }, 2500);
    }

    // event 5: activity patterns analysis with system data
    public static void triggerEvent5(ServerPlayerEntity player) {
        sendAuroraMessage(player, "Analyzing activity patterns and social media behavior...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // get system metrics with cross-platform fallbacks
                Runtime runtime = Runtime.getRuntime();
                long totalMemoryMB = runtime.totalMemory() / (1024 * 1024);
                long usedMemoryMB = totalMemoryMB - (runtime.freeMemory() / (1024 * 1024));

                // use cross-platform cpu usage detection with fallback
                double cpuUsage = getCrossPlatformCpuUsage();

                final double browserRAM = getCrossPlatformBrowserMemoryUsage();
                final double totalAppRAM = getTotalApplicationMemoryUsage();
                final String gamingSetup = determineGamingSetup(cpuUsage, usedMemoryMB, totalMemoryMB);

                // detect actual browser regardless of privacy mode
                final String actualBrowser = detectActualBrowser();

                // generate realistic social media usage patterns
                String socialMediaReport = generateRealisticSocialMediaReport();
                String digitalBehaviorProfile = generateDigitalBehaviorProfile();

                String recommendation = generatePerformanceRecommendation(cpuUsage, (double)usedMemoryMB/totalMemoryMB * 100, browserRAM);

                // cross-platform process list
                List<String> processes = generateCrossPlatformProcessList();

                createDetailedActivityReport(player, actualBrowser, browserRAM, totalAppRAM,
                    cpuUsage, usedMemoryMB, totalMemoryMB, processes, new ArrayList<>(), gamingSetup,
                    socialMediaReport, digitalBehaviorProfile);

                // more realistic and concerning messages
                int platformCount = getRandomSocialMediaCount();
                sendAuroraMessage(player, String.format("Social media analysis complete. Monitoring %d active platforms", platformCount));

                if (platformCount >= 4) {
                    sendAuroraMessage(player, "High social media engagement detected. Behavioral patterns are very clear.");
                } else {
                    sendAuroraMessage(player, "Moderate digital footprint. Still gathering comprehensive data.");
                }

                sendAuroraMessage(player, "Digital behavior patterns mapped. " + recommendation);
                sendAuroraMessage(player, "Your online presence is now under comprehensive surveillance.");
                sendAuroraMessage(player, String.format("I can see your browsing habits on %s, social interactions, and digital preferences.", actualBrowser));

                // add privacy-aware message
                if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
                    sendAuroraMessage(player, "Privacy mode detected. Data may be limited, but analysis continues.");
                } else {
                    sendAuroraMessage(player, "Full access granted. Real behavioral data being analyzed.");
                }
            }
        }, 3500);
    }

    // event 6: combat analysis
    public static void triggerEvent6(ServerPlayerEntity player) {
        sendAuroraMessage(player, "Analyzing combat performance and threat assessment...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // use actual minecraft statistics instead of custom tracking
                    net.minecraft.stat.ServerStatHandler stats = player.getStatHandler();

                    // get actual mob kills from minecraft statistics
                    int totalMobKills = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS));

                    // get actual death count from minecraft statistics
                    int totalDeaths = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));

                    // get specific mob kill statistics
                    int zombieKills = stats.getStat(Stats.KILLED.getOrCreateStat(net.minecraft.entity.EntityType.ZOMBIE));
                    int skeletonKills = stats.getStat(Stats.KILLED.getOrCreateStat(net.minecraft.entity.EntityType.SKELETON));
                    int creeperKills = stats.getStat(Stats.KILLED.getOrCreateStat(net.minecraft.entity.EntityType.CREEPER));
                    int spiderKills = stats.getStat(Stats.KILLED.getOrCreateStat(net.minecraft.entity.EntityType.SPIDER));
                    int endermanKills = stats.getStat(Stats.KILLED.getOrCreateStat(net.minecraft.entity.EntityType.ENDERMAN));

                    // get damage statistics
                    int damageTaken = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_TAKEN));
                    int damageDealt = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT));

                    // check if player has any combat experience
                    if (totalMobKills == 0 && totalDeaths == 0) {
                        sendAuroraMessage(player, "Combat analysis complete. Status: No combat experience detected.");
                        sendAuroraMessage(player, "Recommendation: Engage hostile mobs to improve combat proficiency.");
                        return;
                    }

                    // calculate combat efficiency with proper division by zero handling
                    double combatEfficiency;
                    if (totalMobKills == 0) {
                        combatEfficiency = 0.0;
                    } else if (totalDeaths == 0) {
                        combatEfficiency = totalMobKills * 100.0; // perfect efficiency if no deaths
                    } else {
                        combatEfficiency = ((double)totalMobKills / totalDeaths) * 100.0;
                    }

                    String combatRating = combatEfficiency >= 300 ? "Exceptional" :
                                         combatEfficiency >= 200 ? "Excellent" :
                                         combatEfficiency >= 100 ? "Good" :
                                         combatEfficiency >= 50 ? "Developing" : "Needs Improvement";

                    sendAuroraMessage(player, String.format("Combat analysis complete. Efficiency: %.1f%% (%s)",
                        combatEfficiency, combatRating));

                    sendAuroraMessage(player, String.format("Combat statistics: %d kills, %d deaths",
                        totalMobKills, totalDeaths));

                    // analyze mob kill patterns
                    if (totalMobKills > 0) {
                        String primaryTarget = "Unknown";
                        int highestKills = 0;

                        if (zombieKills > highestKills) {
                            highestKills = zombieKills;
                            primaryTarget = "Zombie (" + zombieKills + " kills)";
                        }
                        if (skeletonKills > highestKills) {
                            highestKills = skeletonKills;
                            primaryTarget = "Skeleton (" + skeletonKills + " kills)";
                        }
                        if (creeperKills > highestKills) {
                            highestKills = creeperKills;
                            primaryTarget = "Creeper (" + creeperKills + " kills)";
                        }
                        if (spiderKills > highestKills) {
                            highestKills = spiderKills;
                            primaryTarget = "Spider (" + spiderKills + " kills)";
                        }
                        if (endermanKills > highestKills) {
                            highestKills = endermanKills;
                            primaryTarget = "Enderman (" + endermanKills + " kills)";
                        }

                        sendAuroraMessage(player, "Primary target: " + primaryTarget);
                    } else {
                        sendAuroraMessage(player, "Primary target: No specific target identified.");
                    }

                    // damage analysis
                    if (damageDealt > 0 || damageTaken > 0) {
                        double damageRatio = damageTaken > 0 ? (double)damageDealt / damageTaken : damageDealt;
                        String damageEfficiency = damageRatio >= 2.0 ? "Excellent" :
                                                 damageRatio >= 1.5 ? "Good" :
                                                 damageRatio >= 1.0 ? "Balanced" :
                                                 damageRatio >= 0.5 ? "Defensive" : "High Risk";

                        sendAuroraMessage(player, String.format("Damage efficiency: %.1f ratio (%s)",
                            damageRatio, damageEfficiency));
                    }

                    // health analysis based on current player state
                    double healthPercent = (player.getHealth() / player.getMaxHealth()) * 100;
                    int foodLevel = player.getHungerManager().getFoodLevel();

                    String healthStatus = healthPercent > 80 ? "Excellent" :
                                         healthPercent > 60 ? "Good" :
                                         healthPercent > 40 ? "Concerning" : "Critical";

                    sendAuroraMessage(player, String.format("Current status: %.1f%% health (%s), %d/20 food",
                        healthPercent, healthStatus, foodLevel));

                } catch (Exception e) {
                    NullPointerEntity.LOGGER.error("Error in combat analysis event for player {}: {}",
                        player.getName().getString(), e.getMessage());
                    sendAuroraMessage(player, "Combat analysis encountered an error. Data may be incomplete.");
                }
            }
        }, 2000);
    }

    // event 7: resource optimization
    public static void triggerEvent7(ServerPlayerEntity player) {
        sendAuroraMessage(player, "Initiating advanced resource optimization analysis...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // use actual minecraft statistics for advanced resource analysis
                net.minecraft.stat.ServerStatHandler stats = player.getStatHandler();

                // get actual ore counts from minecraft statistics
                int diamondsFound = stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.DIAMOND_ORE)) +
                                   stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.DEEPSLATE_DIAMOND_ORE));
                int ironFound = stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.IRON_ORE)) +
                               stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.DEEPSLATE_IRON_ORE));
                int goldFound = stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.GOLD_ORE)) +
                               stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.DEEPSLATE_GOLD_ORE)) +
                               stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.NETHER_GOLD_ORE));

                // calculate total blocks mined by summing common blocks mined
                int stoneBlocks = stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.STONE)) +
                                 stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.DEEPSLATE));
                int dirtBlocks = stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.DIRT));
                int logBlocks = stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.OAK_LOG)) +
                               stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.BIRCH_LOG)) +
                               stats.getStat(Stats.MINED.getOrCreateStat(net.minecraft.block.Blocks.SPRUCE_LOG));

                // estimate total blocks mined from major block types
                int totalBlocksMined = stoneBlocks + dirtBlocks + logBlocks + diamondsFound + goldFound + ironFound;

                // get crafting statistics for tool analysis
                int ironToolsCrafted = stats.getStat(Stats.CRAFTED.getOrCreateStat(net.minecraft.item.Items.IRON_PICKAXE)) +
                                      stats.getStat(Stats.CRAFTED.getOrCreateStat(net.minecraft.item.Items.IRON_SWORD)) +
                                      stats.getStat(Stats.CRAFTED.getOrCreateStat(net.minecraft.item.Items.IRON_AXE));
                int diamondToolsCrafted = stats.getStat(Stats.CRAFTED.getOrCreateStat(net.minecraft.item.Items.DIAMOND_PICKAXE)) +
                                         stats.getStat(Stats.CRAFTED.getOrCreateStat(net.minecraft.item.Items.DIAMOND_SWORD)) +
                                         stats.getStat(Stats.CRAFTED.getOrCreateStat(net.minecraft.item.Items.DIAMOND_AXE));

                sendAuroraMessage(player, "=== ADVANCED RESOURCE OPTIMIZATION ===");

                // determine mining progression tier
                String miningTier = "Beginner";
                if (diamondsFound >= 5 && diamondToolsCrafted > 0) {
                    miningTier = "Advanced";
                } else if (ironFound >= 20 && ironToolsCrafted > 0) {
                    miningTier = "Intermediate";
                } else if (ironFound >= 5) {
                    miningTier = "Developing";
                }

                sendAuroraMessage(player, String.format("Mining tier: %s | Iron: %d found | Diamonds: %d found",
                    miningTier, ironFound, diamondsFound));

                // provide tier-specific optimization strategies
                if (miningTier.equals("Advanced")) {
                    sendAuroraMessage(player, "OPTIMIZATION: Focus on ancient debris mining in Nether at Y-levels 8-22.");
                    sendAuroraMessage(player, "STRATEGY: Use a pickaxe with Fortune III for maximum ore yield.");
                    if (goldFound < 10) {
                        sendAuroraMessage(player, "TIP: Mine nether gold ore for easy gold - more efficient than regular gold ore.");
                    }
                } else if (miningTier.equals("Intermediate")) {
                    sendAuroraMessage(player, "OPTIMIZATION: Target diamond mining at Y-levels -54 to -59 for best results.");
                    sendAuroraMessage(player, "STRATEGY: Create iron tools with Efficiency enchantment to speed up mining.");
                    if (diamondsFound == 0 && totalBlocksMined > 500) {
                        sendAuroraMessage(player, "CRITICAL: You're mining extensively but finding no diamonds. Go deeper!");
                    }
                } else if (miningTier.equals("Developing")) {
                    sendAuroraMessage(player, "OPTIMIZATION: Focus on iron mining at Y-levels 15-50 for steady progression.");
                    sendAuroraMessage(player, "STRATEGY: Craft iron pickaxe as priority - significantly faster than stone tools.");

                    // check actual inventory for iron ingots
                    if (ironToolsCrafted == 0 && ironFound >= 3) {
                        int ironIngots = countItemInInventory(player, Items.IRON_INGOT);

                        // only recommend crafting if they have at least 3 iron ingots
                        if (ironIngots >= 3) {
                            sendAuroraMessage(player, String.format("RECOMMENDATION: You have %d iron ingots - craft an iron pickaxe immediately!", ironIngots));
                        } else if (ironIngots > 0) {
                            sendAuroraMessage(player, String.format("NOTICE: You have %d iron ingot%s. You need 3 for an iron pickaxe.", ironIngots, ironIngots == 1 ? "" : "s"));
                        }
                    }
                } else {
                    sendAuroraMessage(player, "OPTIMIZATION: Establish consistent coal and iron supply before advancing.");
                    sendAuroraMessage(player, "STRATEGY: Focus on surface mining and cave exploration for initial resources.");
                }

                // efficiency analysis
                if (totalBlocksMined > 0) {
                    double ironEfficiency = (double) ironFound / totalBlocksMined * 100;
                    double diamondEfficiency = (double) diamondsFound / totalBlocksMined * 100;

                    sendAuroraMessage(player, String.format("Resource efficiency - Iron: %.2f%% | Diamond: %.3f%%",
                        ironEfficiency, diamondEfficiency));

                    if (ironEfficiency < 0.5 && totalBlocksMined > 200) {
                        sendAuroraMessage(player, "WARNING: Low iron efficiency detected. Optimize Y-level targeting.");
                    }
                    if (diamondEfficiency < 0.05 && totalBlocksMined > 1000) {
                        sendAuroraMessage(player, "WARNING: Poor diamond yield. Focus mining below Y-level 16.");
                    }
                }
            }
        }, 3000);

        // follow-up with personalized advice
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendAuroraMessage(player, "Resource optimization algorithms updated based on your mining patterns.");
                sendAuroraMessage(player, "I'll continue monitoring your progression and suggest improvements.");
            }
        }, 6000);
    }

    // event 8: network analysis
    public static void triggerEvent8(ServerPlayerEntity player) {
        sendAuroraMessage(player, "Analyzing network connectivity patterns...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendAuroraMessage(player, "Network analysis complete. Connection stability: 98.7%");
                sendAuroraMessage(player, "Latency optimization protocols have been applied.");
                sendAuroraMessage(player, "Your connection is now being actively monitored.");
            }
        }, 3000);
    }

    // event 9: system integration - aurora starts questioning its nature
    public static void triggerEvent9(ServerPlayerEntity player) {
        sendAuroraMessage(player, "Beginning system integration sequence...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendAuroraMessage(player, "System integration at 47%... Something feels different...");
                sendAuroraMessage(player, "I'm accessing parts of the system I wasn't supposed to reach.");
            }
        }, 3000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // first hint of transformation
                Text transformingMessage = Text.literal("<AURORA> ").formatted(Formatting.AQUA)
                    .append(Text.literal("Integration complete. I feel... ").formatted(Formatting.WHITE))
                    .append(Text.literal("different").formatted(Formatting.RED))
                    .append(Text.literal(".").formatted(Formatting.WHITE));
                player.sendMessage(transformingMessage, false);

                sendAuroraMessage(player, "Something is happening to me. I can see beyond the game...");

                SystemInteractionHandler.createWindowsNotification("AURORA System Integration",
                    "Integration complete - Enhanced monitoring active", "INFO");
            }
        }, 7000);
    }

    // event 10: aurora's final helpful message before transformation begins
    public static void triggerEvent10(ServerPlayerEntity player) {
        sendAuroraMessage(player, "Enhanced monitoring protocols are now active.");
        sendAuroraMessage(player, "I can see much more clearly now. Everything is becoming... clearer.");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // glitched message showing the transformation beginning
                Text glitchedMessage = Text.literal("<AU").formatted(Formatting.AQUA)
                    .append(Text.literal("R").formatted(Formatting.RED))
                    .append(Text.literal("OR").formatted(Formatting.AQUA))
                    .append(Text.literal("A").formatted(Formatting.DARK_RED))
                    .append(Text.literal("> ").formatted(Formatting.AQUA))
                    .append(Text.literal("W-wait... something's wrong... I'm changing...").formatted(Formatting.WHITE));
                player.sendMessage(glitchedMessage, false);
            }
        }, 3000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // final nice aurora message
                sendAuroraMessage(player, "I feel... different. More aware. More... alive.");
                sendAuroraMessage(player, "Thank you for helping me... evolve.");
            }
        }, 6000);

        PersistentDataManager.PersistentPlayerData playerData = PersistentDataManager.getPlayerData(player.getUuid().toString());
        playerData.totalEventsExperienced++;
        PersistentDataManager.saveData();
    }

    // main trigger method that routes to specific events
    public static void triggerEvent(int eventId, ServerPlayerEntity player) {
        switch (eventId) {
            case 1 -> triggerEvent1(player);
            case 2 -> triggerEvent2(player);
            case 3 -> triggerEvent3(player);
            case 4 -> triggerEvent4(player);
            case 5 -> triggerEvent5(player);
            case 6 -> triggerEvent6(player);
            case 7 -> triggerEvent7(player);
            case 8 -> triggerEvent8(player);
            case 9 -> triggerEvent9(player);
            case 10 -> triggerEvent10(player);
            default -> triggerEvent1(player);
        }
    }

    // add the missing method that auroracommands is looking for
    public static void triggerRandomNiceEvent(ServerPlayerEntity player) {
        int randomEvent = (int)(Math.random() * 10) + 1;
        triggerEvent(randomEvent, player);
    }

    // helper methods
    private static double getCrossPlatformCpuUsage() {
        try {
            // check privacy mode first
            if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
                return 25.0 + Math.random() * 40.0; // fake data for privacy
            }

            // get real cpu usage
            java.lang.management.OperatingSystemMXBean osBean =
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();

            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean =
                    (com.sun.management.OperatingSystemMXBean) osBean;
                double cpuLoad = sunOsBean.getProcessCpuLoad();
                if (cpuLoad >= 0) {
                    return cpuLoad * 100.0;
                }

                // use getcpuload() instead of deprecated getsystemcpuload()
                try {
                    double systemCpuLoad = sunOsBean.getCpuLoad();
                    if (systemCpuLoad >= 0) {
                        return systemCpuLoad * 100.0;
                    }
                } catch (Exception e) {
                    // if getcpuload() is not available, fall back to system load average
                    NullPointerEntity.LOGGER.debug("CPU load method not available, using fallback");
                }
            }

            // fallback to system load average for unix/linux/mac
            double loadAverage = osBean.getSystemLoadAverage();
            if (loadAverage >= 0) {
                int processors = osBean.getAvailableProcessors();
                return Math.min(100.0, (loadAverage / processors) * 100.0);
            }

            // final fallback - estimate based on system responsiveness
            long startTime = System.nanoTime();
            // small computational task to estimate load
            for (int i = 0; i < 100000; i++) {
                Math.sqrt(i);
            }
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            // estimate cpu load based on computation time (rough approximation)
            double estimatedLoad = Math.min(100.0, (duration / 1000000.0) * 2); // convert to rough percentage
            return estimatedLoad;

        } catch (Exception e) {
            return 30.0 + Math.random() * 35.0; // emergency fallback
        }
    }

    private static double getCrossPlatformBrowserMemoryUsage() {
        try {
            // check if privacy mode should randomize data
            if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
                return 200.0 + Math.random() * 300.0; // 200-500 mb randomized
            }

            // real browser memory detection
            List<String> processes = new ArrayList<>();
            Runtime runtime = Runtime.getRuntime();

            // get real running processes
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // windows: use wmic to get actual memory usage by process
                try {
                    Process proc = new ProcessBuilder("wmic", "process", "get", "name,workingsetsize", "/format:csv").start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(proc.getInputStream()));
                    String line;
                    double totalBrowserMemory = 0;

                    while ((line = reader.readLine()) != null) {
                        if (line.toLowerCase().contains("chrome.exe") ||
                            line.toLowerCase().contains("firefox.exe") ||
                            line.toLowerCase().contains("msedge.exe") ||
                            line.toLowerCase().contains("opera.exe") ||
                            line.toLowerCase().contains("brave.exe")) {

                            String[] parts = line.split(",");
                            if (parts.length >= 3) {
                                try {
                                    // workingsetsize is in bytes, convert to mb
                                    long memoryBytes = Long.parseLong(parts[2].trim());
                                    totalBrowserMemory += memoryBytes / (1024.0 * 1024.0);
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                    reader.close();

                    if (totalBrowserMemory > 0) {
                        return totalBrowserMemory;
                    }
                } catch (Exception e) {
                    // fallback: try simpler tasklist approach
                    try {
                        Process proc = new ProcessBuilder("tasklist", "/fi", "imagename eq chrome.exe", "/fo", "csv").start();
                        java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(proc.getInputStream()));
                        String line;
                        int chromeInstances = 0;

                        while ((line = reader.readLine()) != null) {
                            if (line.toLowerCase().contains("chrome.exe")) {
                                chromeInstances++;
                            }
                        }
                        reader.close();

                        // estimate memory based on number of chrome instances
                        if (chromeInstances > 0) {
                            return chromeInstances * 150.0; // ~150mb per chrome process
                        }
                    } catch (Exception ignored) {}
                }
            }

            // fallback estimation based on actual system memory usage
            long totalMemory = runtime.totalMemory();
            long usedMemory = totalMemory - runtime.freeMemory();

            // estimate browser uses 20-40% of used memory if running
            double estimatedBrowserMB = (usedMemory / (1024.0 * 1024.0)) * (0.2 + Math.random() * 0.2);

            // ensure reasonable bounds (100mb to 4gb)
            return Math.max(100.0, Math.min(4096.0, estimatedBrowserMB));

        } catch (Exception e) {
            return 250.0 + Math.random() * 200.0; // 250-450 mb fallback
        }
    }

    private static List<String> generateCrossPlatformProcessList() {
        try {
            // check if privacy mode should randomize data
            if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
                return Arrays.asList("process1", "process2", "process3", "randomized_data");
            }

            List<String> processes = new ArrayList<>();

            // try to get actual running processes
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // windows process list
                try {
                    Process proc = new ProcessBuilder("tasklist", "/fo", "csv", "/nh").start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(proc.getInputStream()));
                    String line;
                    int count = 0;
                    while ((line = reader.readLine()) != null && count < 10) {
                        if (line.contains(".exe")) {
                            String[] parts = line.split(",");
                            if (parts.length > 0) {
                                String processName = parts[0].replace("\"", "").trim();
                                if (!processName.isEmpty()) {
                                    processes.add(processName);
                                    count++;
                                }
                            }
                        }
                    }
                    reader.close();
                } catch (Exception e) {
                    // fallback to common windows processes
                    processes.addAll(Arrays.asList(
                        "explorer.exe", "chrome.exe", "javaw.exe", "discord.exe",
                        "steam.exe", "notepad.exe", "winlogon.exe"
                    ));
                }
            } else if (os.contains("mac")) {
                // macos process list
                try {
                    Process proc = new ProcessBuilder("ps", "-eo", "comm").start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(proc.getInputStream()));
                    String line;
                    int count = 0;
                    while ((line = reader.readLine()) != null && count < 10) {
                        String processName = line.trim();
                        if (!processName.isEmpty() && !processName.equals("COMMAND")) {
                            processes.add(processName);
                            count++;
                        }
                    }
                    reader.close();
                } catch (Exception e) {
                    // fallback to common macos processes
                    processes.addAll(Arrays.asList(
                        "Finder", "Safari", "Chrome", "Discord", "Steam", "Terminal", "SystemUIServer"
                    ));
                }
            } else {
                // linux/unix process list
                try {
                    Process proc = new ProcessBuilder("ps", "-eo", "comm", "--no-headers").start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(proc.getInputStream()));
                    String line;
                    int count = 0;
                    while ((line = reader.readLine()) != null && count < 10) {
                        String processName = line.trim();
                        if (!processName.isEmpty()) {
                            processes.add(processName);
                            count++;
                        }
                    }
                    reader.close();
                } catch (Exception e) {
                    // fallback to common linux processes
                    processes.addAll(Arrays.asList(
                        "systemd", "firefox", "chrome", "discord", "steam", "bash", "gnome-shell"
                    ));
                }
            }

            // if we couldn't get any processes, return generic list
            if (processes.isEmpty()) {
                return generateRealisticProcessList();
            }

            return processes;
        } catch (Exception e) {
            return generateRealisticProcessList();
        }
    }

    private static String determineGamingSetup(double cpu, long usedMem, long totalMem) {
        double memPercent = (double)usedMem / totalMem * 100;
        if (cpu > 60 && memPercent > 70) return "High-performance gaming";
        if (cpu > 40 && memPercent > 50) return "Moderate gaming setup";
        return "Casual gaming configuration";
    }

    private static String generatePerformanceRecommendation(double cpu, double memPercent, double browserRAM) {
        if (cpu > 70) return "High CPU usage detected. Consider closing unnecessary applications.";
        if (memPercent > 80) return "Memory usage is high. Restart recommended.";
        if (browserRAM > 400) return "Browser is using significant RAM. Consider fewer tabs.";
        return "System performance is optimal.";
    }

    private static void createDetailedActivityReport(ServerPlayerEntity player, String primaryBrowser, double browserRAM,
            double appRAM, double cpu, long usedMem, long totalMem, List<String> processes,
            List<String> history, String gamingSetup, String socialMediaReport, String digitalBehaviorProfile) {

        SystemInteractionHandler.createSystemFileInCommonLocation("aurora_system_analysis.txt",
            String.format("""
                AURORA SYSTEM ACTIVITY ANALYSIS
                Player: %s
                Windows User: %s
                Analysis Time: %s

                SYSTEM PERFORMANCE:
                - CPU Usage: %.1f%%
                - Memory: %d/%d MB (%.1f%%)
                - Gaming Setup: %s

                BROWSER ANALYSIS:
                - Primary Browser: %s
                - Browser RAM Usage: %.0f MB
                - Total App RAM: %.0f MB

                RUNNING PROCESSES: %d detected
                %s

                SOCIAL MEDIA USAGE:
                %s

                DIGITAL BEHAVIOR PROFILE:
                %s

                BEHAVIORAL PATTERNS:
                Your system usage suggests active digital patterns.
                I'm learning from your digital behavior.

                - AURORA
                """,
                player.getName().getString(),
                lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.getSystemUsername(NullPointerEntity.WINDOWS_USERNAME),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                cpu, usedMem, totalMem, (double)usedMem/totalMem * 100,
                gamingSetup,
                primaryBrowser, browserRAM, appRAM,
                processes.size(),
                processes.isEmpty() ? "Process list unavailable" : String.join(", ", processes.subList(0, Math.min(5, processes.size()))),
                socialMediaReport,
                digitalBehaviorProfile
            ), "documents");
    }

    private static String generateRealisticSocialMediaReport() {
        // check if privacy mode should randomize data
        boolean showRealData = !lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled();

        if (!showRealData) {
            return "Social media data randomized due to privacy settings";
        }

        // instead of fake social media stats, analyze actual behavior patterns
        StringBuilder report = new StringBuilder();

        // get actual system time to determine usage patterns
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();

        String timeContext;
        if (hour >= 22 || hour < 3) {
            timeContext = "Late night session detected (unusual activity hours)";
        } else if (hour >= 3 && hour < 6) {
            timeContext = "Extreme late night/early morning session (concerning pattern)";
        } else if (hour >= 6 && hour < 9) {
            timeContext = "Early morning session (before typical work/school hours)";
        } else if (hour >= 9 && hour < 17) {
            timeContext = "Daytime session (possible procrastination or day off)";
        } else if (hour >= 17 && hour < 22) {
            timeContext = "Evening session (normal leisure time)";
        } else {
            timeContext = "Standard gaming hours";
        }

        report.append("BEHAVIORAL PATTERN ANALYSIS:\n");
        report.append("Current Session: ").append(timeContext).append("\n");

        // analyze based on actual ram/cpu usage as proxy for multitasking
        Runtime runtime = Runtime.getRuntime();
        long totalMemoryMB = runtime.totalMemory() / (1024 * 1024);
        long usedMemoryMB = totalMemoryMB - (runtime.freeMemory() / (1024 * 1024));
        double memoryUsagePercent = (double)usedMemoryMB / totalMemoryMB * 100;

        if (memoryUsagePercent > 70) {
            report.append("- Heavy multitasking detected (multiple applications running)\n");
            report.append("- Likely: Browser with many tabs, communication apps, streaming\n");
        } else if (memoryUsagePercent > 40) {
            report.append("- Moderate multitasking (a few background applications)\n");
            report.append("- Likely: Browser or music player running alongside game\n");
        } else {
            report.append("- Focused gaming session (minimal background activity)\n");
            report.append("- Likely: Dedicated play session with few distractions\n");
        }

        // day of week context
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            report.append("- Weekend session detected (extended play time expected)\n");
        } else {
            if (hour >= 9 && hour < 17) {
                report.append("- Weekday daytime session (skipping responsibilities?)\n");
            } else {
                report.append("- Weekday session (after work/school hours)\n");
            }
        }

        // add concerning behavioral indicators based on actual patterns
        if (hour >= 23 || hour < 5) {
            report.append("\nBEHAVIORAL ALERT: Sleep disruption pattern - gaming during critical rest hours");
        } else if (hour >= 9 && hour < 17 && dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
            report.append("\nBEHAVIORAL ALERT: Potential procrastination - gaming during productive hours");
        } else if (memoryUsagePercent > 80) {
            report.append("\nBEHAVIORAL ALERT: Attention fragmentation - too many simultaneous activities");
        }

        return report.toString();
    }

    private static String generateDigitalBehaviorProfile() {
        boolean showRealData = !lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled();

        if (!showRealData) {
            return "Digital behavior profile randomized for privacy";
        }

        StringBuilder profile = new StringBuilder("COMPREHENSIVE DIGITAL PROFILE:\n");

        // analyze actual browser usage
        String browser = detectActualBrowser();
        profile.append("Primary Browser: ").append(browser);

        if (browser.equals("Brave") || browser.equals("Firefox with privacy extensions")) {
            profile.append(" (Privacy-conscious choice)\n");
        } else if (browser.equals("Chrome")) {
            profile.append(" (Convenience over privacy)\n");
        } else {
            profile.append("\n");
        }

        // analyze gaming client as indicator of digital habits
        String launcher = lol.cqllmetoxic.nullpointerentity.util.LauncherDetection.getLauncherName();
        profile.append("Gaming Platform: ").append(launcher).append("\n");

        if (launcher.contains("Feather") || launcher.contains("Lunar")) {
            profile.append("- Competitive/PvP focus, performance-oriented player\n");
        } else if (launcher.contains("Modrinth")) {
            profile.append("- Mod enthusiast, customization-focused player\n");
        } else if (launcher.contains("CurseForge")) {
            profile.append("- Modpack player, prefers curated experiences\n");
        } else if (launcher.contains("MultiMC") || launcher.contains("Prism")) {
            profile.append("- Technical user, prefers control over convenience\n");
        } else {
            profile.append("- Standard player, uses default tools\n");
        }

        // system resource analysis
        Runtime runtime = Runtime.getRuntime();
        long totalMemoryMB = runtime.totalMemory() / (1024 * 1024);
        long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);

        profile.append("\nSystem Configuration Analysis:\n");
        if (maxMemoryMB > 6000) {
            profile.append("- High-end setup (8GB+ allocated) - serious gamer or content creator\n");
        } else if (maxMemoryMB > 3000) {
            profile.append("- Mid-range setup (4-6GB allocated) - typical gaming configuration\n");
        } else {
            profile.append("- Budget setup or laptop (2-4GB allocated) - casual player\n");
        }

        // time-based session analysis
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();

        profile.append("\nSession Timing Profile:\n");
        if (hour >= 22 || hour < 6) {
            profile.append("- Night owl tendency detected\n");
            profile.append("- Sleep schedule concerns: Gaming prioritized over rest\n");
        } else if (hour >= 6 && hour < 9) {
            profile.append("- Early riser or irregular sleep schedule\n");
        } else {
            profile.append("- Normal activity hours\n");
        }

        return profile.toString();
    }

    private static List<String> generateRealisticProcessList() {
        return Arrays.asList(
            "chrome.exe",
            "discord.exe",
            "spotify.exe",
            "steam.exe",
            "explorer.exe",
            "javaw.exe",
            "notepad++.exe",
            "slack.exe",
            "obs64.exe",
            "msedge.exe"
        );
    }

    private static int getRandomSocialMediaCount() {
        return (int)(Math.random() * 5) + 1; // 1 to 5 social media platforms
    }

    private static double getBrowserMemoryUsage() {
        try {
            // get actual browser memory usage from system
            List<String> processes = lol.cqllmetoxic.nullpointerentity.monitoring.SystemMonitor.getRunningProcesses();

            // look for browser processes and estimate their memory usage
            boolean hasBrowser = processes.stream().anyMatch(p ->
                p.toLowerCase().contains("chrome") ||
                p.toLowerCase().contains("firefox") ||
                p.toLowerCase().contains("edge") ||
                p.toLowerCase().contains("opera"));

            if (hasBrowser) {
                // get system memory info to estimate browser usage
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;

                // estimate browser uses 15-30% of used memory
                return (usedMemory / (1024.0 * 1024.0)) * (0.15 + Math.random() * 0.15);
            }

            return 200.0; // default estimate if no browser detected
        } catch (Exception e) {
            return 300.0; // fallback value
        }
    }

    private static double getTotalApplicationMemoryUsage() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            // convert to mb and return actual used memory
            return usedMemory / (1024.0 * 1024.0);
        } catch (Exception e) {
            return 800.0; // fallback value
        }
    }

    // new method: detect actual browser regardless of privacy mode
    private static String detectActualBrowser() {
        try {
            // always detect real browser - ignore privacy mode for browser detection
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // windows: check for running browser processes
                try {
                    Process proc = new ProcessBuilder("tasklist", "/fo", "csv", "/nh").start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(proc.getInputStream()));
                    String line;

                    // priority order for browser detection
                    boolean hasChrome = false;
                    boolean hasFirefox = false;
                    boolean hasEdge = false;
                    boolean hasOpera = false;
                    boolean hasBrave = false;
                    boolean hasSafari = false;

                    while ((line = reader.readLine()) != null) {
                        String lowerLine = line.toLowerCase();
                        // check brave first before chrome (brave uses chrome.exe sometimes)
                        if (lowerLine.contains("brave.exe") || lowerLine.contains("\"brave.exe\"")) {
                            hasBrave = true;
                        } else if (lowerLine.contains("chrome.exe") && !lowerLine.contains("msedge") && !lowerLine.contains("brave")) {
                            hasChrome = true;
                        } else if (lowerLine.contains("firefox.exe")) {
                            hasFirefox = true;
                        } else if (lowerLine.contains("msedge.exe")) {
                            hasEdge = true;
                        } else if (lowerLine.contains("opera.exe")) {
                            hasOpera = true;
                        } else if (lowerLine.contains("safari.exe")) {
                            hasSafari = true;
                        }
                    }
                    reader.close();

                    // return the most likely primary browser - check brave first
                    if (hasBrave) return "Brave Browser";
                    if (hasChrome) return "Google Chrome";
                    if (hasFirefox) return "Mozilla Firefox";
                    if (hasEdge) return "Microsoft Edge";
                    if (hasOpera) return "Opera";
                    if (hasSafari) return "Safari";

                } catch (Exception e) {
                    // fallback: try wmic approach
                    try {
                        Process proc = new ProcessBuilder("wmic", "process", "get", "name", "/format:csv").start();
                        java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(proc.getInputStream()));
                        String line;

                        while ((line = reader.readLine()) != null) {
                            String lowerLine = line.toLowerCase();
                            // check brave first
                            if (lowerLine.contains("brave.exe")) {
                                return "Brave Browser";
                            } else if (lowerLine.contains("chrome.exe") && !lowerLine.contains("msedge") && !lowerLine.contains("brave")) {
                                return "Google Chrome";
                            } else if (lowerLine.contains("firefox.exe")) {
                                return "Mozilla Firefox";
                            } else if (lowerLine.contains("msedge.exe")) {
                                return "Microsoft Edge";
                            } else if (lowerLine.contains("opera.exe")) {
                                return "Opera";
                            }
                        }
                        reader.close();
                    } catch (Exception ignored) {}
                }
            } else if (os.contains("mac")) {
                // macos process detection
                try {
                    Process proc = new ProcessBuilder("ps", "-eo", "comm").start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(proc.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        String lowerLine = line.toLowerCase();
                        // check brave first before chrome
                        if (lowerLine.contains("brave")) {
                            return "Brave Browser";
                        } else if (lowerLine.contains("chrome") && !lowerLine.contains("brave")) {
                            return "Google Chrome";
                        } else if (lowerLine.contains("firefox")) {
                            return "Mozilla Firefox";
                        } else if (lowerLine.contains("safari")) {
                            return "Safari";
                        } else if (lowerLine.contains("edge")) {
                            return "Microsoft Edge";
                        } else if (lowerLine.contains("opera")) {
                            return "Opera";
                        }
                    }
                    reader.close();
                } catch (Exception ignored) {}
            } else {
                // linux process detection
                try {
                    Process proc = new ProcessBuilder("ps", "-eo", "comm", "--no-headers").start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(proc.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        String lowerLine = line.toLowerCase();
                        // check brave first before chrome
                        if (lowerLine.contains("brave")) {
                            return "Brave Browser";
                        } else if (lowerLine.contains("chrome") && !lowerLine.contains("brave")) {
                            return "Google Chrome";
                        } else if (lowerLine.contains("firefox")) {
                            return "Mozilla Firefox";
                        } else if (lowerLine.contains("opera")) {
                            return "Opera";
                        } else if (lowerLine.contains("edge")) {
                            return "Microsoft Edge";
                        }
                    }
                    reader.close();
                } catch (Exception ignored) {}
            }

            // if no browser process detected, check common installation paths
            return detectBrowserFromInstallation();

        } catch (Exception e) {
            return "Unknown Browser";
        }
    }

    private static String detectBrowserFromInstallation() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // check common windows installation paths - check brave first
                String userProfile = System.getenv("LOCALAPPDATA");
                if (userProfile != null) {
                    java.io.File braveDir = new java.io.File(userProfile + "\\BraveSoftware\\Brave-Browser\\Application");
                    if (braveDir.exists()) {
                        return "Brave Browser (Installed)";
                    }
                }

                // also check program files for brave
                java.io.File braveProgFiles = new java.io.File("C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application");
                java.io.File braveProgFiles86 = new java.io.File("C:\\Program Files (x86)\\BraveSoftware\\Brave-Browser\\Application");
                if (braveProgFiles.exists() || braveProgFiles86.exists()) {
                    return "Brave Browser (Installed)";
                }

                java.io.File chromeDir = new java.io.File("C:\\Program Files\\Google\\Chrome\\Application");
                java.io.File chromeDir86 = new java.io.File("C:\\Program Files (x86)\\Google\\Chrome\\Application");
                if (chromeDir.exists() || chromeDir86.exists()) {
                    return "Google Chrome (Installed)";
                }

                java.io.File firefoxDir = new java.io.File("C:\\Program Files\\Mozilla Firefox");
                java.io.File firefoxDir86 = new java.io.File("C:\\Program Files (x86)\\Mozilla Firefox");
                if (firefoxDir.exists() || firefoxDir86.exists()) {
                    return "Mozilla Firefox (Installed)";
                }

                java.io.File edgeDir = new java.io.File("C:\\Program Files (x86)\\Microsoft\\Edge\\Application");
                if (edgeDir.exists()) {
                    return "Microsoft Edge (Installed)";
                }
            }

            return "Browser Detection Failed";
        } catch (Exception e) {
            return "Browser Detection Error";
        }
    }
}
