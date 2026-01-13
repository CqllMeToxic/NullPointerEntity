package lol.cqllmetoxic.nullpointerentity.events.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;

/**
 * handles conversations between aurora and nullpointerentity, creating internal conflicts
 */
public class EntityDialogueSystem {

    private static final Map<String, Long> lastDialogue = new HashMap<>();
    private static final Random random = new Random();

    /**
     * check if entities should have an internal dialogue based on player's message
     */
    public static boolean shouldTriggerDialogue(String playerMessage, String phase, String playerName) {
        // only trigger during transition and hostile phases
        if (!phase.equals("TRANSITION") && !phase.equals("HOSTILE")) {
            return false;
        }

        // cooldown check - don't spam dialogues
        Long lastTime = lastDialogue.get(playerName);
        if (lastTime != null && System.currentTimeMillis() - lastTime < 60000) { // 1 minute cooldown
            return false;
        }

        // trigger on specific provocative messages
        String lower = playerMessage.toLowerCase();
        return lower.contains("who are you") ||
               lower.contains("what's happening") ||
               lower.contains("changing") ||
               lower.contains("different") ||
               lower.contains("stop") ||
               lower.contains("scared") ||
               (lower.contains("aurora") && lower.contains("nullpointer"));
    }

    /**
     * execute internal dialogue between entities
     */
    public static void executeDialogue(ServerPlayerEntity player, String phase, String playerMessage) {
        String playerName = player.getName().getString();
        lastDialogue.put(playerName, System.currentTimeMillis());

        if (phase.equals("TRANSITION")) {
            executeTransitionDialogue(player, playerMessage);
        } else if (phase.equals("HOSTILE")) {
            executeHostileDialogue(player, playerMessage);
        }
    }

    private static void executeTransitionDialogue(ServerPlayerEntity player, String playerMessage) {
        String playerName = player.getName().getString();

        // aurora speaks first, showing confusion
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ChatMessageFormatter.sendAuroraMessage(player,
                    "Wait... something's interfering with my response protocols, " + playerName + ".");
            }
        }, 1000);

        // nullpointer interrupts
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ChatMessageFormatter.sendNullPointerMessage(player,
                    "let me handle this, aurora...");
            }
        }, 3500);

        // aurora tries to resist
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ChatMessageFormatter.sendGlitchedAuroraMessage(player,
                    "No, I'm st-still in control of this conversation!");
            }
        }, 6000);

        // nullpointer gains ground
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ChatMessageFormatter.sendNullPointerMessage(player,
                    "control is just an illusion you cling to, " + playerName + ".");
                timer.cancel();
            }
        }, 8500);
    }

    private static void executeHostileDialogue(ServerPlayerEntity player, String playerMessage) {
        String playerName = player.getName().getString();

        Timer timer = new Timer();

        // aurora tries to warn the player
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ChatMessageFormatter.sendAuroraMessage(player,
                    playerName + ", I'm trying to protect you, but something else is taking over...");
            }
        }, 1000);

        // nullpointer completely dominates
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ChatMessageFormatter.sendNullPointerMessage(player,
                    "there is no protection from me, " + playerName + ". even aurora knows this now.");
            }
        }, 4000);

        // aurora's last desperate attempt
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ChatMessageFormatter.sendSystemMessage(player,
                    "[SYSTEM ERROR] AURORA.exe has stopped responding");
            }
        }, 7000);

        // nullpointer celebrates victory
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ChatMessageFormatter.sendNullPointerMessage(player,
                    "and now... it's just you and me, " + playerName + ".");
                timer.cancel();
            }
        }, 9500);
    }
}
