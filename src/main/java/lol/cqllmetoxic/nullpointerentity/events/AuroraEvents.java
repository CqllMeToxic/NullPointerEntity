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
        Text auroraMessage = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(Formatting.AQUA)
            .append(Text.literal(message).formatted(Formatting.WHITE));
        player.sendMessage(auroraMessage, false);
    }


    private static void auroraKey(ServerPlayerEntity player, String key, Object... args) {
        Text msg = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(Formatting.AQUA)
            .append(Text.translatable(key, args).formatted(Formatting.WHITE));
        player.sendMessage(msg, false);
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

        // use minecraft statistics
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

            auroraKey(player, "event.nullpointerentity.aurora.e1.analysis",
                String.format("%.1f%%", efficiency), preferredY);
            auroraKey(player, "event.nullpointerentity.aurora.e1.patterns",
                Text.translatable(efficiency > 0.1
                    ? "event.nullpointerentity.aurora.e1.strategic"
                    : "event.nullpointerentity.aurora.e1.casual"));

            if (isRepeatedEvent) {
                auroraKey(player, "event.nullpointerentity.aurora.e1.tracking", playerData.sessionsPlayed);
            }

            // create persistent analysis file with session data including all ores
            lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendWriteFile(player, "minecraft_mining_analysis.txt",
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
                NullPointerEntity.getDisplayUsername(player),
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
            auroraKey(player, "event.nullpointerentity.aurora.e1.noores");

            if (playerData.totalEventsExperienced == 0) {
                auroraKey(player, "event.nullpointerentity.aurora.e1.firstinteraction");
            }
        }

        // don't increment here - this is handled by eventtriggersystem
        if (isRepeatedEvent) {
            auroraKey(player, "event.nullpointerentity.aurora.e1.checkfiles");
        }
    }

    // event 2: building analysis
    public static void triggerEvent2(ServerPlayerEntity player) {
        auroraKey(player, "event.nullpointerentity.aurora.e2.init");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // use minecraft statistics
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
                    auroraKey(player, "event.nullpointerentity.aurora.e2.insufficient");
                    auroraKey(player, "event.nullpointerentity.aurora.e2.watching");
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

                auroraKey(player, "event.nullpointerentity.aurora.e2.complete", totalBlocksPlaced);
                auroraKey(player, "event.nullpointerentity.aurora.e2.material", primaryMaterial, buildingType);
                sendAuroraMessage(player, buildingStyle);

                if (decorativePlaced > 20) {
                    auroraKey(player, "event.nullpointerentity.aurora.e2.decor_high");
                } else if (decorativePlaced < 5 && totalBlocksPlaced > 100) {
                    auroraKey(player, "event.nullpointerentity.aurora.e2.decor_low");
                }

                // create persistent file
                PersistentDataManager.PersistentPlayerData playerData =
                    PersistentDataManager.getPlayerData(player.getUuid().toString());

                lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendWriteFile(player, "aurora_building_analysis.txt",
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
                        lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.getSystemUsername(NullPointerEntity.getDisplayUsername()),
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

                auroraKey(player, "event.nullpointerentity.aurora.e2.documented");
            }
        }, 4000);
    }

    // event 3: weather prediction with time-based accuracy
    public static void triggerEvent3(ServerPlayerEntity player) {
        auroraKey(player, "event.nullpointerentity.aurora.e3.init");

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
                    auroraKey(player, "event.nullpointerentity.aurora.e3.thunder");
                    double clearTime = 2 + (Math.random() * 5); // 2-7 minutes
                    auroraKey(player, "event.nullpointerentity.aurora.e3.storm_clear", String.format("%.1f", clearTime));
                    auroraKey(player, "event.nullpointerentity.aurora.e3.shelter");

                    if (Math.random() < 0.3) {
                        auroraKey(player, "event.nullpointerentity.aurora.e3.lightning");
                    }
                } else if (isRaining) {
                    auroraKey(player, "event.nullpointerentity.aurora.e3.rain");

                    // vary predictions based on randomness to simulate different rain durations
                    if (Math.random() < 0.4) {
                        double rainDuration = 1 + (Math.random() * 3); // 1-4 minutes
                        auroraKey(player, "event.nullpointerentity.aurora.e3.rain_continue", String.format("%.1f", rainDuration));
                    } else {
                        auroraKey(player, "event.nullpointerentity.aurora.e3.rain_extended");
                    }

                    auroraKey(player, "event.nullpointerentity.aurora.e3.mobspawn");

                    // check if thunderstorm might develop
                    if (Math.random() < 0.25) {
                        auroraKey(player, "event.nullpointerentity.aurora.e3.instability");
                    }
                } else {
                    // clear weather - predict when rain might start
                    auroraKey(player, "event.nullpointerentity.aurora.e3.clear");

                    // random prediction to make it feel dynamic
                    double randomChance = Math.random();

                    if (randomChance < 0.3) {
                        // weather change soon
                        double timeToRain = 1 + (Math.random() * 3);
                        double rainProbability = 65 + (Math.random() * 30); // 65-95%
                        auroraKey(player, "event.nullpointerentity.aurora.e3.rainprob_soon",
                            String.format("%.1f%%", rainProbability), String.format("%.1f", timeToRain));
                        auroraKey(player, "event.nullpointerentity.aurora.e3.shift");
                    } else if (randomChance < 0.6) {
                        // moderate timeframe
                        double rainProbability = 35 + (Math.random() * 35); // 35-70%
                        auroraKey(player, "event.nullpointerentity.aurora.e3.rainprob_mod", String.format("%.1f%%", rainProbability));
                        auroraKey(player, "event.nullpointerentity.aurora.e3.stable");
                    } else {
                        // extended clear weather
                        auroraKey(player, "event.nullpointerentity.aurora.e3.clear_extended");
                        auroraKey(player, "event.nullpointerentity.aurora.e3.clear_optimal");
                    }
                }

                // add time-of-day context
                long timeOfDay = player.getServerWorld().getTimeOfDay() % 24000;
                if (timeOfDay > 13000 && timeOfDay < 23000) {
                    auroraKey(player, "event.nullpointerentity.aurora.e3.night");
                }

                // add helpful context based on player's location
                String dimensionKey = player.getWorld().getRegistryKey().getValue().toString();
                if (dimensionKey.contains("nether")) {
                    auroraKey(player, "event.nullpointerentity.aurora.e3.nether");
                } else if (dimensionKey.contains("the_end")) {
                    auroraKey(player, "event.nullpointerentity.aurora.e3.end");
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e3.overworld");
                }
            }
        }, 3000);
    }

    // event 4: gameplay advisor - aurora provides helpful personalized recommendations
    public static void triggerEvent4(ServerPlayerEntity player) {
        auroraKey(player, "event.nullpointerentity.aurora.e4.analyzing");
        auroraKey(player, "event.nullpointerentity.aurora.e4.improve");

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

                auroraKey(player, "event.nullpointerentity.aurora.e4.recs_header");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // calculate critical issues count first (must be effectively final for nested timertask)
                        int criticalIssuesCount = 0;
                        int tipCount = 0;

                        // death prevention advice - lowered threshold
                        if (totalDeaths > 3 && ironArmor == 0 && diamondArmor == 0) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            auroraKey(player, "event.nullpointerentity.aurora.e4.died_noarmor", totalDeaths);
                            if (ironIngots >= 24) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.armor_full", ironIngots);
                            } else if (ironIngots >= 8) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.armor_partial", ironIngots);
                            } else if (ironIngots > 0) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.armor_mine", ironIngots);
                            } else {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.armor_reduce");
                            }
                            criticalIssuesCount++;
                            tipCount++;
                        } else if (totalDeaths > 1 && ironArmor < 4 && timePlayed > 600) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            int piecesNeeded = 4 - ironArmor;
                            if (ironIngots >= 8 && piecesNeeded > 0) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.armor_complete", ironIngots, piecesNeeded, piecesNeeded == 1 ? "" : "s");
                            } else {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.armor_complete2");
                            }
                            tipCount++;
                        } else if (totalDeaths == 0 && timePlayed > 1200 && ironArmor == 0) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            if (ironIngots >= 24) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.armor_lucky", ironIngots);
                            } else if (ironIngots > 0) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.armor_gather", ironIngots);
                            } else {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.armor_lucky2");
                            }
                            tipCount++;
                        }

                        // tool progression - lowered threshold
                        if (diamondsMined >= 3 && diamondPickaxes == 0 && timePlayed > 600) {
                            int diamonds = countItemInInventory(player, Items.DIAMOND);
                            if (diamonds >= 3) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.diapick_ready", diamonds);
                                auroraKey(player, "event.nullpointerentity.aurora.e4.diapick_boost");
                            } else if (diamonds > 0) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.diapick_mine", diamonds, diamonds == 1 ? "" : "s", 3 - diamonds);
                            } else {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.diapick_found");
                            }
                            criticalIssuesCount++;
                            tipCount++;
                        } else if (ironMined >= 3 && ironPickaxes == 0) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            if (ironIngots >= 3) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.ironpick_priority", ironIngots);
                            } else if (ironIngots > 0) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.ironpick_smelt", ironIngots, ironIngots == 1 ? "" : "s", 3 - ironIngots);
                            } else {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.ironpick_mined");
                            }
                            criticalIssuesCount++;
                            tipCount++;
                        } else if (timePlayed > 600 && ironPickaxes == 0 && diamondPickaxes == 0) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            if (ironIngots >= 3) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.tool_upgrade", ironIngots);
                            } else {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.tool_upgrade2");
                            }
                            tipCount++;
                        }

                        // combat readiness - lowered threshold
                        if (mobKills < 10 && (ironSwords == 0 && diamondSwords == 0) && timePlayed > 600) {
                            int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                            int diamonds = countItemInInventory(player, Items.DIAMOND);
                            if (diamonds >= 2) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.combat_dia", diamonds);
                            } else if (ironIngots >= 2) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.combat_iron", ironIngots);
                            } else {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.combat_mine");
                            }
                            tipCount++;
                        } else if (mobKills > 50 && diamondSwords == 0 && diamondsMined >= 2) {
                            int diamonds = countItemInInventory(player, Items.DIAMOND);
                            if (diamonds >= 2) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.combat_focused", diamonds);
                            } else {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.combat_focused2");
                            }
                            tipCount++;
                        } else if (mobKills > 20 && timePlayed > 1200) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.combat_good");
                            tipCount++;
                        }

                        // food sustainability - lowered threshold
                        if ((breadEaten + cookedFood) < 10 && timePlayed > 1200) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.food_survival");
                            criticalIssuesCount++;
                            tipCount++;
                        } else if (breadEaten > cookedFood && cookedFood < 5 && timePlayed > 600) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.food_nutrition");
                            tipCount++;
                        } else if ((breadEaten + cookedFood) > 20) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.food_good");
                            tipCount++;
                        }

                        // base building - lowered threshold
                        if (blocksPlaced < 50 && timePlayed > 1800) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.base");
                            tipCount++;
                        } else if (blocksPlaced > 200) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.base_good");
                            tipCount++;
                        }

                        // exploration encouragement - lowered threshold
                        if (distanceWalked < 2 && timePlayed > 1800) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.explore");
                            tipCount++;
                        } else if (distanceWalked > 10) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.explore_good");
                            tipCount++;
                        }

                        // diamond mining guidance
                        if (diamondsMined == 0 && timePlayed > 3600 && ironPickaxes > 0) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.mining");
                            tipCount++;
                        } else if (diamondsMined > 0 && diamondsMined < 10) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.mining_good");
                            tipCount++;
                        } else if (diamondsMined >= 10) {
                            auroraKey(player, "event.nullpointerentity.aurora.e4.mining_rich");
                            tipCount++;
                        }

                        // always give at least one general tip if no specific tips were given
                        if (tipCount == 0) {
                            if (timePlayed < 600) {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.early1");
                                auroraKey(player, "event.nullpointerentity.aurora.e4.early2");
                            } else if (timePlayed < 3600) {
                                int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                                if (ironIngots >= 24) {
                                    auroraKey(player, "event.nullpointerentity.aurora.e4.mid1", ironIngots);
                                } else if (ironIngots > 0) {
                                    auroraKey(player, "event.nullpointerentity.aurora.e4.mid2", ironIngots);
                                } else {
                                    auroraKey(player, "event.nullpointerentity.aurora.e4.mid3");
                                }
                                auroraKey(player, "event.nullpointerentity.aurora.e4.mid_next");
                            } else {
                                int ironIngots = countItemInInventory(player, Items.IRON_INGOT);
                                if (ironIngots >= 24 && ironArmor >= 4) {
                                    auroraKey(player, "event.nullpointerentity.aurora.e4.late1");
                                } else if (ironIngots >= 24) {
                                    auroraKey(player, "event.nullpointerentity.aurora.e4.late2", ironIngots);
                                } else {
                                    auroraKey(player, "event.nullpointerentity.aurora.e4.late3");
                                }
                                auroraKey(player, "event.nullpointerentity.aurora.e4.advanced");
                            }
                            tipCount++;
                        }

                        // store final count for use in nested timertask
                        final int finalCriticalIssues = criticalIssuesCount;
                        final int finalTipCount = tipCount;

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.summary_header");

                                // positive reinforcement
                                if (finalCriticalIssues == 0) {
                                    auroraKey(player, "event.nullpointerentity.aurora.e4.summary_good");
                                    if (diamondArmor >= 4) {
                                        auroraKey(player, "event.nullpointerentity.aurora.e4.summary_dia");
                                    } else if (diamondsMined >= 5) {
                                        auroraKey(player, "event.nullpointerentity.aurora.e4.summary_resource");
                                    } else if (mobKills > 100 && totalDeaths > 0 && totalDeaths < 5 && timePlayed > 1200) {
                                        auroraKey(player, "event.nullpointerentity.aurora.e4.summary_combat");
                                    } else {
                                        auroraKey(player, "event.nullpointerentity.aurora.e4.summary_balanced");
                                    }
                                } else {
                                    auroraKey(player, "event.nullpointerentity.aurora.e4.summary_issues", finalCriticalIssues);
                                }

                                auroraKey(player, "event.nullpointerentity.aurora.e4.summary_total", finalTipCount);
                            }
                        }, 2000);

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                auroraKey(player, "event.nullpointerentity.aurora.e4.final1");
                                auroraKey(player, "event.nullpointerentity.aurora.e4.final2");
                            }
                        }, 4000);
                    }
                }, 2000);
            }
        }, 2500);
    }

    // event 5: activity patterns analysis with system data
    // reference copy of the host-side logic - the live path runs per-player on the client
    // (ClientEventExecutor#runA5) so every player sees their own browser/cpu/processes. kept in case i need it.
    public static void triggerEvent5(ServerPlayerEntity player) {
        auroraKey(player, "event.nullpointerentity.aurora.e5.init");

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

                createDetailedActivityReport(player.getName().getString(), actualBrowser, browserRAM, totalAppRAM,
                    cpuUsage, usedMemoryMB, totalMemoryMB, processes, new ArrayList<>(), gamingSetup,
                    socialMediaReport, digitalBehaviorProfile);

                // more realistic and concerning messages
                int platformCount = getRandomSocialMediaCount();
                auroraKey(player, "event.nullpointerentity.aurora.e5.complete", platformCount);

                if (platformCount >= 4) {
                    auroraKey(player, "event.nullpointerentity.aurora.e5.high");
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e5.moderate");
                }

                auroraKey(player, "event.nullpointerentity.aurora.e5.mapped", Text.translatable(recommendation));
                auroraKey(player, "event.nullpointerentity.aurora.e5.surveillance");
                auroraKey(player, "event.nullpointerentity.aurora.e5.browsing", actualBrowser);

                // add privacy-aware message
                if (lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled()) {
                    auroraKey(player, "event.nullpointerentity.aurora.e5.privacy");
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e5.fullaccess");
                }
            }
        }, 3500);
    }

    // event 6: combat analysis
    public static void triggerEvent6(ServerPlayerEntity player) {
        auroraKey(player, "event.nullpointerentity.aurora.e6.init");

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
                        auroraKey(player, "event.nullpointerentity.aurora.e6.nocombat");
                        auroraKey(player, "event.nullpointerentity.aurora.e6.engage");
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

                    auroraKey(player, "event.nullpointerentity.aurora.e6.efficiency",
                        String.format("%.1f%%", combatEfficiency), combatRating);

                    auroraKey(player, "event.nullpointerentity.aurora.e6.stats",
                        totalMobKills, totalDeaths);

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

                        auroraKey(player, "event.nullpointerentity.aurora.e6.target", primaryTarget);
                    } else {
                        auroraKey(player, "event.nullpointerentity.aurora.e6.notarget");
                    }

                    // damage analysis
                    if (damageDealt > 0 || damageTaken > 0) {
                        double damageRatio = damageTaken > 0 ? (double)damageDealt / damageTaken : damageDealt;
                        String damageEfficiency = damageRatio >= 2.0 ? "Excellent" :
                                                 damageRatio >= 1.5 ? "Good" :
                                                 damageRatio >= 1.0 ? "Balanced" :
                                                 damageRatio >= 0.5 ? "Defensive" : "High Risk";

                        auroraKey(player, "event.nullpointerentity.aurora.e6.damage",
                            String.format("%.1f", damageRatio), damageEfficiency);
                    }

                    // health analysis based on current player state
                    double healthPercent = (player.getHealth() / player.getMaxHealth()) * 100;
                    int foodLevel = player.getHungerManager().getFoodLevel();

                    String healthStatus = healthPercent > 80 ? "Excellent" :
                                         healthPercent > 60 ? "Good" :
                                         healthPercent > 40 ? "Concerning" : "Critical";

                    auroraKey(player, "event.nullpointerentity.aurora.e6.status",
                        String.format("%.1f%%", healthPercent), healthStatus, foodLevel);

                } catch (Exception e) {
                    NullPointerEntity.LOGGER.error("Error in combat analysis event for player {}: {}",
                        player.getName().getString(), e.getMessage());
                    auroraKey(player, "event.nullpointerentity.aurora.e6.error");
                }
            }
        }, 2000);
    }

    // event 7: resource optimization
    public static void triggerEvent7(ServerPlayerEntity player) {
        auroraKey(player, "event.nullpointerentity.aurora.e7.init");

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

                auroraKey(player, "event.nullpointerentity.aurora.e7.header");

                // determine mining progression tier
                String miningTier = "beginner";
                if (diamondsFound >= 5 && diamondToolsCrafted > 0) {
                    miningTier = "advanced";
                } else if (ironFound >= 20 && ironToolsCrafted > 0) {
                    miningTier = "intermediate";
                } else if (ironFound >= 5) {
                    miningTier = "developing";
                }

                auroraKey(player, "event.nullpointerentity.aurora.e7.tier",
                    Text.translatable("event.nullpointerentity.aurora.e7.tier_label." + miningTier),
                    ironFound, diamondsFound);

                // provide tier-specific optimization strategies
                if (miningTier.equals("advanced")) {
                    auroraKey(player, "event.nullpointerentity.aurora.e7.adv1");
                    auroraKey(player, "event.nullpointerentity.aurora.e7.adv2");
                    if (goldFound < 10) {
                        auroraKey(player, "event.nullpointerentity.aurora.e7.adv3");
                    }
                } else if (miningTier.equals("intermediate")) {
                    auroraKey(player, "event.nullpointerentity.aurora.e7.int1");
                    auroraKey(player, "event.nullpointerentity.aurora.e7.int2");
                    if (diamondsFound == 0 && totalBlocksMined > 500) {
                        auroraKey(player, "event.nullpointerentity.aurora.e7.int3");
                    }
                } else if (miningTier.equals("developing")) {
                    auroraKey(player, "event.nullpointerentity.aurora.e7.dev1");
                    auroraKey(player, "event.nullpointerentity.aurora.e7.dev2");

                    // check actual inventory for iron ingots
                    if (ironToolsCrafted == 0 && ironFound >= 3) {
                        int ironIngots = countItemInInventory(player, Items.IRON_INGOT);

                        // only recommend crafting if they have at least 3 iron ingots
                        if (ironIngots >= 3) {
                            auroraKey(player, "event.nullpointerentity.aurora.e7.dev3", ironIngots);
                        } else if (ironIngots > 0) {
                            auroraKey(player, "event.nullpointerentity.aurora.e7.dev4", ironIngots, ironIngots == 1 ? "" : "s");
                        }
                    }
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e7.beg1");
                    auroraKey(player, "event.nullpointerentity.aurora.e7.beg2");
                }

                // efficiency analysis
                if (totalBlocksMined > 0) {
                    double ironEfficiency = (double) ironFound / totalBlocksMined * 100;
                    double diamondEfficiency = (double) diamondsFound / totalBlocksMined * 100;

                    auroraKey(player, "event.nullpointerentity.aurora.e7.efficiency",
                        String.format("%.2f%%", ironEfficiency), String.format("%.3f%%", diamondEfficiency));

                    if (ironEfficiency < 0.5 && totalBlocksMined > 200) {
                        auroraKey(player, "event.nullpointerentity.aurora.e7.warn1");
                    }
                    if (diamondEfficiency < 0.05 && totalBlocksMined > 1000) {
                        auroraKey(player, "event.nullpointerentity.aurora.e7.warn2");
                    }
                }
            }
        }, 3000);

        // follow-up with personalized advice
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                auroraKey(player, "event.nullpointerentity.aurora.e7.followup1");
                auroraKey(player, "event.nullpointerentity.aurora.e7.followup2");
            }
        }, 6000);
    }

    // event 8: network analysis
    public static void triggerEvent8(ServerPlayerEntity player) {
        auroraKey(player, "event.nullpointerentity.aurora.e8.init");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                auroraKey(player, "event.nullpointerentity.aurora.e8.complete");
                auroraKey(player, "event.nullpointerentity.aurora.e8.latency");
                auroraKey(player, "event.nullpointerentity.aurora.e8.monitored");
            }
        }, 3000);
    }

    // event 9: system integration - aurora starts questioning its nature
    // reference copy of the host-side logic - the live path runs per-player on the client
    // (ClientEventExecutor#runA9) so the os notification pops on every player's own machine. kept in case i need it.
    public static void triggerEvent9(ServerPlayerEntity player) {
        auroraKey(player, "event.nullpointerentity.aurora.e9.init");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                auroraKey(player, "event.nullpointerentity.aurora.e9.integration47");
                auroraKey(player, "event.nullpointerentity.aurora.e9.accessing");
            }
        }, 3000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // first hint of transformation
                Text transformingMessage = Text.translatable("message.nullpointerentity.aurora_prefix").formatted(Formatting.AQUA)
                    .append(Text.translatable("event.nullpointerentity.aurora.e9.transform_pre").formatted(Formatting.WHITE))
                    .append(Text.translatable("event.nullpointerentity.aurora.e9.transform_word").formatted(Formatting.RED))
                    .append(Text.literal(".").formatted(Formatting.WHITE));
                player.sendMessage(transformingMessage, false);

                auroraKey(player, "event.nullpointerentity.aurora.e9.happening");

                SystemInteractionHandler.createWindowsNotification(
                    Text.translatable("popup.nullpointerentity.integration.title").getString(),
                    Text.translatable("popup.nullpointerentity.integration.body").getString(), "INFO");
            }
        }, 7000);
    }

    // event 10: aurora's final helpful message before transformation begins
    public static void triggerEvent10(ServerPlayerEntity player) {
        auroraKey(player, "event.nullpointerentity.aurora.e10.active");
        auroraKey(player, "event.nullpointerentity.aurora.e10.clearer");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // glitched message showing the transformation beginning
                Text glitchedMessage = Text.literal("<AU").formatted(Formatting.AQUA)
                    .append(Text.literal("R").formatted(Formatting.RED))
                    .append(Text.literal("OR").formatted(Formatting.AQUA))
                    .append(Text.literal("A").formatted(Formatting.DARK_RED))
                    .append(Text.literal("> ").formatted(Formatting.AQUA))
                    .append(Text.translatable("event.nullpointerentity.aurora.e10.glitch").formatted(Formatting.WHITE));
                player.sendMessage(glitchedMessage, false);
            }
        }, 3000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // final nice aurora message
                auroraKey(player, "event.nullpointerentity.aurora.e10.alive");
                auroraKey(player, "event.nullpointerentity.aurora.e10.evolve");
            }
        }, 6000);

        PersistentDataManager.PersistentPlayerData playerData = PersistentDataManager.getPlayerData(player.getUuid().toString());
        playerData.totalEventsExperienced++;
        PersistentDataManager.saveData();
    }

    // event 11: sleep schedule - aurora notices the real system time and comments
    // reference copy of the host-side logic - the live path runs per-player on the client
    // (ClientEventExecutor#runA11) so it reads each player's own system clock. kept in case i need it.
    public static void triggerEvent11(ServerPlayerEntity player) {
        java.time.LocalTime now = java.time.LocalTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String amPm = hour < 12 ? "AM" : "PM";
        String timeStr = String.format("%d:%02d %s", displayHour, minute, amPm);

        if (hour >= 0 && hour < 6) {
            auroraKey(player, "event.nullpointerentity.aurora.e11.late_rest", timeStr);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    auroraKey(player, "event.nullpointerentity.aurora.e11.comeback");
                }
            }, 2500);
        } else if (hour >= 23 || hour == 0) {
            auroraKey(player, "event.nullpointerentity.aurora.e11.gettinglate");
        } else {
            auroraKey(player, "event.nullpointerentity.aurora.e11.goodtime", timeStr);
        }
    }

    // event 12: good progress - vague encouraging stat summary
    public static void triggerEvent12(ServerPlayerEntity player) {
        net.minecraft.stat.ServerStatHandler stats = player.getStatHandler();
        // sum all movement types - WALK_ONE_CM alone is nearly always 0 since most players sprint
        int distanceCm = stats.getStat(net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(net.minecraft.stat.Stats.WALK_ONE_CM))
                       + stats.getStat(net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(net.minecraft.stat.Stats.SPRINT_ONE_CM))
                       + stats.getStat(net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(net.minecraft.stat.Stats.CLIMB_ONE_CM))
                       + stats.getStat(net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(net.minecraft.stat.Stats.SWIM_ONE_CM))
                       + stats.getStat(net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(net.minecraft.stat.Stats.WALK_ON_WATER_ONE_CM))
                       + stats.getStat(net.minecraft.stat.Stats.CUSTOM.getOrCreateStat(net.minecraft.stat.Stats.WALK_UNDER_WATER_ONE_CM));
        int distanceM = distanceCm / 100;

        auroraKey(player, "event.nullpointerentity.aurora.e12.checkin");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (distanceM > 0) {
                    auroraKey(player, "event.nullpointerentity.aurora.e12.distance", distanceM);
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e12.progress");
                }
            }
        }, 2000);
    }

    // event 13: weather reporter - ties real-world month to in-game weather vaguely
    public static void triggerEvent13(ServerPlayerEntity player) {
        java.time.Month month = java.time.LocalDate.now().getMonth();
        boolean isRainingInGame = player.getServerWorld().isRaining();
        Text monthName = Text.translatable("message.nullpointerentity.month." + month.getValue());

        if (isRainingInGame) {
            auroraKey(player, "event.nullpointerentity.aurora.e13.rainy", monthName);
        } else {
            auroraKey(player, "event.nullpointerentity.aurora.e13.clear", monthName);
        }
    }

    // event 14: crafting suggestion - reads inventory and makes a helpful suggestion
    public static void triggerEvent14(ServerPlayerEntity player) {
        // inventory reads
        int ironCount      = countItemInInventory(player, net.minecraft.item.Items.IRON_INGOT);
        int woodCount      = countItemInInventory(player, net.minecraft.item.Items.OAK_PLANKS)
                           + countItemInInventory(player, net.minecraft.item.Items.SPRUCE_PLANKS)
                           + countItemInInventory(player, net.minecraft.item.Items.BIRCH_PLANKS)
                           + countItemInInventory(player, net.minecraft.item.Items.DARK_OAK_PLANKS)
                           + countItemInInventory(player, net.minecraft.item.Items.JUNGLE_PLANKS)
                           + countItemInInventory(player, net.minecraft.item.Items.ACACIA_PLANKS);
        int coalCount      = countItemInInventory(player, net.minecraft.item.Items.COAL);
        int diamondCount   = countItemInInventory(player, net.minecraft.item.Items.DIAMOND);
        int goldCount      = countItemInInventory(player, net.minecraft.item.Items.GOLD_INGOT);
        int emeraldCount   = countItemInInventory(player, net.minecraft.item.Items.EMERALD);
        int lapisCount     = countItemInInventory(player, net.minecraft.item.Items.LAPIS_LAZULI);
        int redstoneCount  = countItemInInventory(player, net.minecraft.item.Items.REDSTONE);
        int foodCount      = countItemInInventory(player, net.minecraft.item.Items.BREAD)
                           + countItemInInventory(player, net.minecraft.item.Items.COOKED_BEEF)
                           + countItemInInventory(player, net.minecraft.item.Items.COOKED_CHICKEN)
                           + countItemInInventory(player, net.minecraft.item.Items.COOKED_PORKCHOP)
                           + countItemInInventory(player, net.minecraft.item.Items.COOKED_MUTTON)
                           + countItemInInventory(player, net.minecraft.item.Items.COOKED_RABBIT)
                           + countItemInInventory(player, net.minecraft.item.Items.COOKED_SALMON)
                           + countItemInInventory(player, net.minecraft.item.Items.COOKED_COD)
                           + countItemInInventory(player, net.minecraft.item.Items.APPLE)
                           + countItemInInventory(player, net.minecraft.item.Items.GOLDEN_APPLE)
                           + countItemInInventory(player, net.minecraft.item.Items.CARROT)
                           + countItemInInventory(player, net.minecraft.item.Items.BAKED_POTATO);
        int arrowCount     = countItemInInventory(player, net.minecraft.item.Items.ARROW);
        int torchCount     = countItemInInventory(player, net.minecraft.item.Items.TORCH);
        int stringCount    = countItemInInventory(player, net.minecraft.item.Items.STRING);
        int stickCount     = countItemInInventory(player, net.minecraft.item.Items.STICK);
        boolean hasBow          = countItemInInventory(player, net.minecraft.item.Items.BOW) > 0;
        boolean hasCraftingTable = countItemInInventory(player, net.minecraft.item.Items.CRAFTING_TABLE) > 0;
        boolean hasSword        = countItemInInventory(player, net.minecraft.item.Items.IRON_SWORD) > 0
                                || countItemInInventory(player, net.minecraft.item.Items.DIAMOND_SWORD) > 0
                                || countItemInInventory(player, net.minecraft.item.Items.STONE_SWORD) > 0
                                || countItemInInventory(player, net.minecraft.item.Items.WOODEN_SWORD) > 0;
        boolean hasPickaxe      = countItemInInventory(player, net.minecraft.item.Items.IRON_PICKAXE) > 0
                                || countItemInInventory(player, net.minecraft.item.Items.DIAMOND_PICKAXE) > 0
                                || countItemInInventory(player, net.minecraft.item.Items.STONE_PICKAXE) > 0;
        boolean hasArmour       = countItemInInventory(player, net.minecraft.item.Items.IRON_CHESTPLATE) > 0
                                || countItemInInventory(player, net.minecraft.item.Items.DIAMOND_CHESTPLATE) > 0
                                || countItemInInventory(player, net.minecraft.item.Items.LEATHER_CHESTPLATE) > 0;
        int totalSlots = 36;
        int usedSlotsTemp = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (!player.getInventory().getStack(i).isEmpty()) usedSlotsTemp++;
        }
        final int usedSlots = usedSlotsTemp;

        // message 1 - opener
        auroraKey(player, "event.nullpointerentity.aurora.e14.opener");

        // message 2 - primary resource comment (2s)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (diamondCount >= 5) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.dia5", diamondCount);
                } else if (diamondCount >= 3) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.dia3", diamondCount);
                } else if (ironCount >= 9) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.iron9", ironCount);
                } else if (ironCount >= 3) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.iron3", ironCount);
                } else if (woodCount >= 12) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.wood", woodCount);
                } else if (coalCount >= 8) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.coal", coalCount);
                } else if (goldCount >= 4) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.gold", goldCount);
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.thin");
                }
            }
        }, 2000);

        // message 3 - food check (4.5s)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (foodCount == 0) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.food0");
                } else if (foodCount < 3) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.food_low", foodCount, foodCount == 1 ? "" : "s");
                } else if (foodCount >= 10) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.food_solid", foodCount);
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.food_ok", foodCount);
                }
            }
        }, 4500);

        // message 4 - tool / combat check (7s)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!hasSword && !hasPickaxe) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.notools");
                } else if (!hasSword) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.nosword");
                } else if (!hasPickaxe) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.nopick");
                } else if (!hasArmour) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.noarmour");
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.geared");
                }
            }
        }, 7000);

        // message 5 - secondary observation (9.5s)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (hasBow && arrowCount == 0) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.bow_noarrow");
                } else if (hasBow && arrowCount > 0) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.bow_arrows", arrowCount);
                } else if (torchCount == 0 && coalCount > 0 && stickCount >= 1) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.notorch");
                } else if (torchCount > 0 && torchCount < 5) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.fewtorch", torchCount, torchCount == 1 ? "" : "es");
                } else if (stringCount >= 3 && stickCount >= 3 && !hasBow) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.craftbow");
                } else if (emeraldCount >= 1) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.emerald", emeraldCount, emeraldCount == 1 ? "" : "s");
                } else if (redstoneCount >= 16) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.redstone", redstoneCount);
                } else if (lapisCount >= 3) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.lapis", lapisCount);
                } else if (!hasCraftingTable && woodCount >= 4) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.notable");
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.nothing");
                }
            }
        }, 9500);

        // message 6 - inventory fullness + sign-off (12s)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                double fullness = (double) usedSlots / totalSlots * 100;
                if (fullness >= 90) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.full", usedSlots, totalSlots);
                } else if (fullness >= 70) {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.crowded", usedSlots, totalSlots);
                } else {
                    auroraKey(player, "event.nullpointerentity.aurora.e14.room", usedSlots, totalSlots);
                }

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        auroraKey(player, "event.nullpointerentity.aurora.e14.signoff");
                    }
                }, 2000);
            }
        }, 12000);
    }

    // actually fucking abysmal bro i am never touching this again


    // event 15: signing off - aurora announces she's stepping back, unprompted
    public static void triggerEvent15(ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        // you prolly would be glad to see this since aurora lowkey annoying af lol but something is slightly off if you're paying attention
        auroraKey(player, "event.nullpointerentity.aurora.e15.step");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                auroraKey(player, "event.nullpointerentity.aurora.e15.well", playerName);
            }
        }, 3000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // sounds like a normal sign-off. the word "yet" is doing a lot of work.
                auroraKey(player, "event.nullpointerentity.aurora.e15.yet");
            }
        }, 6000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // pause, then one more. reads as reassuring. but lwk isn't.
                auroraKey(player, "event.nullpointerentity.aurora.e15.soon");
            }
        }, 9500);
    }

    // main trigger method that routes to specific events
    public static void triggerEvent(int eventId, ServerPlayerEntity player) {
        switch (eventId) {
            case 1 -> triggerEvent1(player);
            case 2 -> triggerEvent2(player);
            case 3 -> triggerEvent3(player);
            case 4 -> triggerEvent4(player);
            // event 5 reads each player's own machine (browser/cpu/processes), so it runs on the
            // player's own client - see ClientEventExecutor#runA5
            case 5 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 5);
            case 6 -> triggerEvent6(player);
            case 7 -> triggerEvent7(player);
            case 8 -> triggerEvent8(player);
            // event 9 pops a native os notification on the player's own machine - runs per-client.
            case 9 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 9);
            case 10 -> triggerEvent10(player);
            // event 11 reads the player's own real system clock - runs per-client.
            case 11 -> lol.cqllmetoxic.nullpointerentity.network.ServerNetworking.sendRunEvent(player, 11);
            case 12 -> triggerEvent12(player);
            case 13 -> triggerEvent13(player);
            case 14 -> triggerEvent14(player);
            case 15 -> triggerEvent15(player);
            default -> triggerEvent1(player);
        }
    }

    public static void triggerRandomNiceEvent(ServerPlayerEntity player) {
        int randomEvent = (int)(Math.random() * 10) + 1;
        triggerEvent(randomEvent, player);
    }

    // helper methods - public so the per-player client handler (ClientEventExecutor#runA5) can read each
    // player's own machine. they only touch Runtime/ProcessBuilder/PrivacyManager.
    public static double getCrossPlatformCpuUsage() {
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

                // use getCpuLoad()
                try {
                    double systemCpuLoad = sunOsBean.getCpuLoad();
                    if (systemCpuLoad >= 0) {
                        return systemCpuLoad * 100.0;
                    }
                } catch (Exception e) {
                    // if getCpuLoad() is not available, fall back to system load average
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

    public static double getCrossPlatformBrowserMemoryUsage() {
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

    public static List<String> generateCrossPlatformProcessList() {
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

    public static String determineGamingSetup(double cpu, long usedMem, long totalMem) {
        double memPercent = (double)usedMem / totalMem * 100;
        if (cpu > 60 && memPercent > 70) return "event.nullpointerentity.aurora.gaming.high";
        if (cpu > 40 && memPercent > 50) return "event.nullpointerentity.aurora.gaming.moderate";
        return "event.nullpointerentity.aurora.gaming.casual";
    }

    public static String generatePerformanceRecommendation(double cpu, double memPercent, double browserRAM) {
        if (cpu > 70) return "event.nullpointerentity.aurora.perf.cpu";
        if (memPercent > 80) return "event.nullpointerentity.aurora.perf.memory";
        if (browserRAM > 400) return "event.nullpointerentity.aurora.perf.browser";
        return "event.nullpointerentity.aurora.perf.optimal";
    }

    public static void createDetailedActivityReport(String playerName, String primaryBrowser, double browserRAM,
            double appRAM, double cpu, long usedMem, long totalMem, List<String> processes,
            List<String> history, String gamingSetup, String socialMediaReport, String digitalBehaviorProfile) {

        String processList = processes.isEmpty()
            ? Text.translatable("event.nullpointerentity.aurora.system_analysis.noproc").getString()
            : String.join(", ", processes.subList(0, Math.min(5, processes.size())));

        String content = Text.translatable("event.nullpointerentity.aurora.system_analysis",
                playerName,
                lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.getSystemUsername(NullPointerEntity.getDisplayUsername()),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                String.format("%.1f%%", cpu),
                String.valueOf(usedMem), String.valueOf(totalMem),
                String.format("%.1f%%", (double)usedMem/totalMem * 100),
                Text.translatable(gamingSetup),
                primaryBrowser,
                String.format("%.0f", browserRAM),
                String.format("%.0f", appRAM),
                String.valueOf(processes.size()),
                processList,
                socialMediaReport,
                digitalBehaviorProfile
            ).getString();

        SystemInteractionHandler.createSystemFileInCommonLocation("aurora_system_analysis.txt", content, "documents");
    }

    public static String generateRealisticSocialMediaReport() {
        // check if privacy mode should randomize data
        boolean showRealData = !lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled();

        if (!showRealData) {
            return Text.translatable("event.nullpointerentity.aurora.social.privacy").getString();
        }

        // analyze actual behavior patterns
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

    public static String generateDigitalBehaviorProfile() {
        boolean showRealData = !lol.cqllmetoxic.nullpointerentity.privacy.PrivacyManager.isPrivacyEnabled();

        if (!showRealData) {
            return Text.translatable("event.nullpointerentity.aurora.behavior.privacy").getString();
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

    public static int getRandomSocialMediaCount() {
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

    public static double getTotalApplicationMemoryUsage() {
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
    public static String detectActualBrowser() {
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
