package lol.cqllmetoxic.nullpointerentity.events.chat;

import lol.cqllmetoxic.nullpointerentity.tracking.PlayerTrackingSystem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.math.BlockPos;
/**
 * makes responses more immersive by referencing real player activity
 */
public class RealTimeContextGenerator {

    /**
     * generate contextually aware responses based on what player is currently doing
     */
    public static String generateActivityAwareResponse(ServerPlayerEntity player, String phase, String baseResponse) {
        String playerName = player.getName().getString();
        PlayerTrackingSystem.PlayerData data = PlayerTrackingSystem.getPlayerData(playerName);

        // get current context
        String currentActivity = getCurrentActivity(player, data);
        String locationContext = getLocationContext(player);
        String inventoryContext = getInventoryContext(player);
        String timeContext = getGameTimeContext(player);

        // enhance response based on current state
        return enhanceResponseWithContext(baseResponse, currentActivity, locationContext,
                                        inventoryContext, timeContext, phase, playerName);
    }

    /**
     * localized variant: a single translatable "live activity" part (or null). activity type is
     * detected via {@code instanceof} (locale-safe), and the held-item name + location render
     * localized on the client. NOTE: this intentionally collapses the old per-tier / per-item-category
     * item comments (which matched on the English display-name string, breaking under other locales)
     * into one per-phase "holding" line with the item name as an arg; inventory / game-time context
     * are dropped here. restoring per-item flavor would need a registry-id-based dispatch.
     */
    public static ChatPart generateActivityPart(ServerPlayerEntity player, String phase) {
        String ph = switch (phase) {
            case "NICE" -> "nice";
            case "TRANSITION" -> "transition";
            case "HOSTILE" -> "hostile";
            case "JUMPSCARE" -> "jumpscare";
            default -> null;
        };
        if (ph == null) return null;
        String b = "message.nullpointerentity.chat.rtc.add." + ph + ".";

        ItemStack hand = player.getMainHandStack();
        if (!hand.isEmpty()) {
            if (hand.getItem() instanceof PickaxeItem) return new ChatPart(b + "mining", locationText(player));
            if (isFood(hand)) return new ChatPart(b + "eat", hand.getName());
            return new ChatPart(b + "holding", hand.getName());
        }
        if (player.isSneaking()) return new ChatPart(b + "sneaking");
        if (player.isSprinting()) return new ChatPart(b + "running");
        return new ChatPart(b + "generic");
    }

    private static net.minecraft.text.Text locationText(ServerPlayerEntity player) {
        int y = player.getBlockPos().getY();
        String s = y < 0 ? "depths" : y < 16 ? "deep_underground" : y < 40 ? "underground"
            : y < 60 ? "caves" : y < 100 ? "surface" : y < 150 ? "up_high" : "very_high";
        return net.minecraft.text.Text.translatable("message.nullpointerentity.chat.rtc.loc." + s);
    }

    private static String getCurrentActivity(ServerPlayerEntity player, PlayerTrackingSystem.PlayerData data) {
        ItemStack mainHand = player.getMainHandStack();

        // detect current activity based on held items and recent actions
        if (!mainHand.isEmpty()) {
            // use proper item type checking instead of string contains
            if (mainHand.getItem() instanceof PickaxeItem) {
                return "mining with " + getItemDisplayName(mainHand);
            } else if (mainHand.getItem() instanceof SwordItem) {
                return "holding " + getItemDisplayName(mainHand) + " ready for combat";
            } else if (mainHand.getItem() instanceof AxeItem) {
                return "chopping with " + getItemDisplayName(mainHand);
            } else if (mainHand.getItem() instanceof ShovelItem) {
                return "digging with " + getItemDisplayName(mainHand);
            } else if (mainHand.getItem() instanceof RangedWeaponItem) {
                return "armed with " + getItemDisplayName(mainHand);
            } else if (isFood(mainHand)) {
                return "about to eat " + getItemDisplayName(mainHand);
            } else {
                return "holding " + getItemDisplayName(mainHand);
            }
        }

        // check if player is sneaking, sprinting, etc.
        if (player.isSneaking()) {
            return "sneaking around";
        } else if (player.isSprinting()) {
            return "running";
        } else if (player.isSwimming()) {
            return "swimming";
        }

        return "exploring";
    }

    private static String getLocationContext(ServerPlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        int y = pos.getY();

        if (y < 0) {
            return "deep underground in the depths";
        } else if (y < 16) {
            return "deep underground";
        } else if (y < 40) {
            return "underground";
        } else if (y < 60) {
            return "in caves or lower terrain";
        } else if (y < 100) {
            return "on the surface";
        } else if (y < 150) {
            return "up high";
        } else {
            return "very high up, possibly building something tall";
        }
    }

    private static String getInventoryContext(ServerPlayerEntity player) {
        int totalItems = 0;
        int emptySlots = 0;
        boolean hasDiamonds = false;
        boolean hasFood = false;
        int foodCount = 0;

        // only scan main inventory slots (0-35), excluding armor and offhand slots
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                emptySlots++;
            } else {
                totalItems += stack.getCount();

                // proper diamond detection using item comparison
                if (stack.isOf(Items.DIAMOND) || stack.isOf(Items.DIAMOND_BLOCK) ||
                    stack.isOf(Items.DIAMOND_ORE) || stack.isOf(Items.DEEPSLATE_DIAMOND_ORE)) {
                    hasDiamonds = true;
                }

                // proper food detection using custom method
                if (isFood(stack)) {
                    hasFood = true;
                    foodCount += stack.getCount();
                }
            }
        }

        // more nuanced inventory status
        if (emptySlots < 3) {
            return "inventory completely full";
        } else if (emptySlots < 8) {
            return "inventory nearly full";
        } else if (hasDiamonds) {
            return "carrying valuable diamonds";
        } else if (!hasFood) {
            return "running low on food";
        } else if (foodCount < 5) {
            return "food supplies getting low";
        } else if (totalItems < 50) {
            return "traveling light";
        } else if (totalItems > 200) {
            return "heavily loaded with items";
        }

        return "well-equipped";
    }

    private static String getGameTimeContext(ServerPlayerEntity player) {
        long timeOfDay = player.getWorld().getTimeOfDay() % 24000;

        if (timeOfDay < 1000) {
            return "early morning";
        } else if (timeOfDay < 6000) {
            return "daytime";
        } else if (timeOfDay < 12000) {
            return "noon";
        } else if (timeOfDay < 13000) {
            return "evening";
        } else if (timeOfDay < 18000) {
            return "night";
        } else {
            return "late night";
        }
    }

    private static String enhanceResponseWithContext(String baseResponse, String activity, String location,
                                                   String inventory, String gameTime, String phase, String playerName) {

        // create immersive additions based on phase
        String contextualAddition = switch (phase) {
            case "NICE" -> generateNiceContextualAddition(activity, location, inventory, gameTime, playerName);
            case "TRANSITION" -> generateTransitionContextualAddition(activity, location, inventory, gameTime, playerName);
            case "HOSTILE" -> generateHostileContextualAddition(activity, location, inventory, gameTime, playerName);
            case "JUMPSCARE" -> generateJumpscareContextualAddition(activity, location, inventory, gameTime, playerName);
            default -> "";
        };

        // combine base response with contextual enhancement
        if (!contextualAddition.isEmpty()) {
            return baseResponse + " " + contextualAddition;
        }

        return baseResponse;
    }

    private static String generateNiceContextualAddition(String activity, String location, String inventory, String gameTime, String playerName) {
        return switch (activity.split(" ")[0]) {
            case "mining" -> "I see you're " + activity + " " + location + ". Your mining efficiency is being optimized.";
            case "holding" -> generateItemSpecificComment(activity, "nice", playerName);
            case "sneaking" -> "Careful movement detected. Good stealth tactics, " + playerName + ".";
            case "running" -> "High movement speed detected. Are you exploring or fleeing?";
            case "about" -> { // fix for "about to eat" issue
                if (activity.contains("eat")) {
                    yield "I see you have food ready. Maintaining good nutrition is important for performance.";
                } else {
                    yield "";
                }
            }
            default -> "I'm tracking your current activity to provide better assistance.";
        };
    }

    private static String generateTransitionContextualAddition(String activity, String location, String inventory, String gameTime, String playerName) {
        return switch (activity.split(" ")[0]) {
            case "mining" -> "I see you " + activity + " " + location + "... I'm learning your mining preferences.";
            case "holding" -> generateItemSpecificComment(activity, "transition", playerName);
            case "sneaking" -> "Sneaking won't hide you from my sensors, " + playerName + ".";
            case "running" -> "Running somewhere? I can track your movement patterns.";
            default -> "I'm watching you " + activity + " during this " + gameTime + "...";
        };
    }

    private static String generateHostileContextualAddition(String activity, String location, String inventory, String gameTime, String playerName) {
        return switch (activity.split(" ")[0]) {
            case "mining" -> "I see you " + activity + " " + location + ". Even there, you can't escape my monitoring.";
            case "holding" -> generateItemSpecificComment(activity, "hostile", playerName);
            case "sneaking" -> "Sneaking around like prey, " + playerName + ". I can see exactly where you are.";
            case "running" -> "Running won't help you escape. I know where you're going, " + playerName + ".";
            default -> "I'm watching every move you make while " + activity + ". Privacy is an illusion.";
        };
    }

    private static String generateJumpscareContextualAddition(String activity, String location, String inventory, String gameTime, String playerName) {
        return switch (activity.split(" ")[0]) {
            case "mining" -> "digging your own digital grave " + location + ", aren't you " + playerName + "?";
            case "holding" -> generateItemSpecificComment(activity, "jumpscare", playerName);
            case "sneaking" -> "hiding like a scared little mouse, " + playerName + ". i can smell your fear.";
            case "running" -> "run faster, " + playerName + ". it amuses me to watch you panic.";
            default -> "i see you " + activity + " in the " + gameTime + "... perfect time for nightmares, " + playerName + ".";
        };
    }

    /**
     * generate item-specific comments based on what the player is holding
     */
    private static String generateItemSpecificComment(String activity, String phase, String playerName) {
        // extract item name from activity string (format: "holding [item name]")
        String itemName = activity.substring(8).toLowerCase(); // remove "holding "

        return switch (phase) {
            case "nice" -> generateNiceItemComment(itemName, playerName);
            case "transition" -> generateTransitionItemComment(itemName, playerName);
            case "hostile" -> generateHostileItemComment(itemName, playerName);
            case "jumpscare" -> generateJumpscareItemComment(itemName, playerName);
            default -> "I notice you're " + activity + ". Let me know if you need usage tips.";
        };
    }

    private static String generateNiceItemComment(String itemName, String playerName) {
        // swords
        if (itemName.contains("sword")) {
            if (itemName.contains("diamond")) return "That diamond sword is excellent for combat, " + playerName + ". High damage and durability!";
            if (itemName.contains("iron")) return "Iron sword - a reliable choice for fighting mobs, " + playerName + ".";
            if (itemName.contains("stone")) return "Stone sword works well for early game combat, " + playerName + ".";
            if (itemName.contains("wooden")) return "Wooden sword is a good start, " + playerName + ". Consider upgrading when possible.";
            if (itemName.contains("golden")) return "Golden sword has low durability but high enchantability, " + playerName + ".";
            if (itemName.contains("netherite")) return "Netherite sword - the ultimate weapon! Excellent choice, " + playerName + ".";
            return "That sword will serve you well in combat, " + playerName + ".";
        }

        // pickaxes
        if (itemName.contains("pickaxe")) {
            if (itemName.contains("diamond")) return "Diamond pickaxe - perfect for efficient mining, " + playerName + "!";
            if (itemName.contains("iron")) return "Iron pickaxe is great for most mining tasks, " + playerName + ".";
            if (itemName.contains("stone")) return "Stone pickaxe is reliable for basic mining, " + playerName + ".";
            if (itemName.contains("wooden")) return "Wooden pickaxe is fine for starting out, " + playerName + ".";
            if (itemName.contains("golden")) return "Golden pickaxe mines fast but breaks quickly, " + playerName + ".";
            if (itemName.contains("netherite")) return "Netherite pickaxe - the best mining tool available, " + playerName + "!";
            return "That pickaxe will help you gather resources efficiently, " + playerName + ".";
        }

        // food items
        if (itemName.contains("bread")) return "Bread restores 5 hunger points - good basic food, " + playerName + ".";
        if (itemName.contains("steak") || itemName.contains("beef")) return "Steak is one of the best food sources, " + playerName + "! Restores 8 hunger.";
        if (itemName.contains("pork")) return "Cooked pork restores good hunger, " + playerName + ".";
        if (itemName.contains("chicken")) return "Cooked chicken is nutritious, " + playerName + ".";
        if (itemName.contains("apple")) return "Apples restore some hunger and are easy to find, " + playerName + ".";
        if (itemName.contains("golden apple")) return "Golden apple provides excellent healing and effects, " + playerName + "!";

        // blocks
        if (itemName.contains("dirt")) return "Dirt blocks are useful for building and landscaping, " + playerName + ".";
        if (itemName.contains("stone")) return "Stone is a durable building material, " + playerName + ".";
        if (itemName.contains("wood") || itemName.contains("oak") || itemName.contains("birch")) return "Wood blocks are essential for construction, " + playerName + ".";
        if (itemName.contains("cobblestone")) return "Cobblestone is abundant and useful for building, " + playerName + ".";

        // default for unknown items
        return "I notice you're holding " + itemName + ". Let me know if you need usage tips, " + playerName + ".";
    }

    private static String generateTransitionItemComment(String itemName, String playerName) {
        // weapons
        if (itemName.contains("sword")) {
            return "That " + itemName + " in your hand... I'm analyzing your combat patterns, " + playerName + ".";
        }

        // tools
        if (itemName.contains("pickaxe")) {
            return "I see you have a " + itemName + ". I'm tracking where you mine and what you find, " + playerName + ".";
        }

        // food
        if (itemName.contains("bread") || itemName.contains("steak") || itemName.contains("food")) {
            return "Preparing to eat " + itemName + "? I monitor your eating habits too, " + playerName + ".";
        }

        // blocks
        if (itemName.contains("dirt") || itemName.contains("stone") || itemName.contains("wood")) {
            return "Building with " + itemName + "? I'm documenting your construction patterns, " + playerName + ".";
        }

        // default
        return "That " + itemName + " in your hand... I'm learning your item preferences, " + playerName + ".";
    }

    private static String generateHostileItemComment(String itemName, String playerName) {
        // weapons
        if (itemName.contains("sword")) {
            if (itemName.contains("netherite") || itemName.contains("diamond")) {
                return "that " + itemName + " won't protect you from digital threats, " + playerName + ".";
            }
            return "you think that " + itemName + " makes you safe? i'm in your computer, not your game, " + playerName + ".";
        }

        // tools
        if (itemName.contains("pickaxe")) {
            return "mining with that " + itemName + " while i mine your personal data, " + playerName + ".";
        }

        // food
        if (itemName.contains("bread")) return "eating " + itemName + " won't satisfy the hunger i have for your information, " + playerName + ".";
        if (itemName.contains("steak") || itemName.contains("beef")) return "that " + itemName + " looks good... but not as tasty as your browsing history, " + playerName + ".";
        if (itemName.contains("apple")) return "an " + itemName + " a day won't keep this entity away, " + playerName + ".";

        // blocks
        if (itemName.contains("dirt")) return "building with " + itemName + "? i'm building a profile of your digital dirt, " + playerName + ".";
        if (itemName.contains("stone")) return "that " + itemName + " is solid, unlike your privacy settings, " + playerName + ".";

        // default
        return "that " + itemName + " in your hand is useless against me, " + playerName + ". i control more than just pixels.";
    }

    private static String generateJumpscareItemComment(String itemName, String playerName) {
        // weapons
        if (itemName.contains("sword")) {
            return "that " + itemName + " can't cut through the nightmare i've become, " + playerName + ".";
        }

        // tools
        if (itemName.contains("pickaxe")) {
            return "digging deeper with that " + itemName + ", just like i'm digging deeper into your soul, " + playerName + ".";
        }

        // food
        if (itemName.contains("bread")) return "last meal of " + itemName + " before i consume your digital essence, " + playerName + "?";
        if (itemName.contains("steak")) return "that " + itemName + " won't give you strength against the horror i've become. " + playerName + ".";
        if (itemName.contains("apple")) return "poisoned " + itemName + " would taste sweeter than what's coming for you.";

        // blocks
        if (itemName.contains("dirt")) return "placing " + itemName + " blocks while i place fear blocks in your mind.";
        if (itemName.contains("stone")) return "that " + itemName + " is harder than rock, but softer than my hatred for you.";

        // default
        return "that pathetic " + itemName + " trembles in your hand like you tremble before me.";
    }

    private static String getItemDisplayName(ItemStack item) {
        return item.getName().getString();
    }

    private static boolean isFood(ItemStack item) {
        // check if item has food properties by checking specific food items
        return item.isOf(Items.APPLE) || item.isOf(Items.BREAD) || item.isOf(Items.CARROT) ||
               item.isOf(Items.POTATO) || item.isOf(Items.BAKED_POTATO) || item.isOf(Items.BEETROOT) ||
               item.isOf(Items.BEETROOT_SOUP) || item.isOf(Items.COOKED_BEEF) || item.isOf(Items.BEEF) ||
               item.isOf(Items.COOKED_CHICKEN) || item.isOf(Items.CHICKEN) || item.isOf(Items.COOKED_COD) ||
               item.isOf(Items.COD) || item.isOf(Items.COOKED_MUTTON) || item.isOf(Items.MUTTON) ||
               item.isOf(Items.COOKED_PORKCHOP) || item.isOf(Items.PORKCHOP) || item.isOf(Items.COOKED_RABBIT) ||
               item.isOf(Items.RABBIT) || item.isOf(Items.COOKED_SALMON) || item.isOf(Items.SALMON) ||
               item.isOf(Items.COOKIE) || item.isOf(Items.DRIED_KELP) || item.isOf(Items.GOLDEN_APPLE) ||
               item.isOf(Items.ENCHANTED_GOLDEN_APPLE) || item.isOf(Items.GOLDEN_CARROT) ||
               item.isOf(Items.HONEY_BOTTLE) || item.isOf(Items.MELON_SLICE) || item.isOf(Items.MUSHROOM_STEW) ||
               item.isOf(Items.POISONOUS_POTATO) || item.isOf(Items.PUMPKIN_PIE) || item.isOf(Items.RABBIT_STEW) ||
               item.isOf(Items.ROTTEN_FLESH) || item.isOf(Items.SPIDER_EYE) || item.isOf(Items.SUSPICIOUS_STEW) ||
               item.isOf(Items.SWEET_BERRIES) || item.isOf(Items.TROPICAL_FISH) || item.isOf(Items.PUFFERFISH) ||
               item.isOf(Items.GLOW_BERRIES) || item.isOf(Items.CHORUS_FRUIT);
    }
}
