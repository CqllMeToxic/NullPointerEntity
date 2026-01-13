package lol.cqllmetoxic.nullpointerentity.events.chat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Timer;
import java.util.TimerTask;

/**
 * creates typing simulation effects to make entities feel more human-like
 */
public class TypingSimulator {

    /**
     * send a message with realistic typing simulation
     */
    public static void sendMessageWithTyping(ServerPlayerEntity player, String entity, String message, String phase) {
        String playerName = player.getName().getString();

        // calculate realistic typing time based on message length and entity
        int typingTime = calculateTypingTime(message, entity, phase);

        // show typing indicator
        showTypingIndicator(player, entity, phase);

        // send actual message after typing delay
        Timer messageTimer = new Timer();
        messageTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                hideTypingIndicator(player, entity);

                // send the actual message directly (fix: remove recursive call)
                if (entity.equals("NULLPOINTER")) {
                    Text nullMessage = Text.literal("<NullPointerEntity> ").formatted(Formatting.DARK_RED)
                        .append(Text.literal(message).formatted(Formatting.RED));
                    player.sendMessage(nullMessage, false);
                } else {
                    // phase-based aurora color - use yellow during transition phase
                    Formatting auroraColor = phase.equals("TRANSITION") ? Formatting.YELLOW : Formatting.AQUA;
                    Text auroraMessage = Text.literal("<AURORA> ").formatted(auroraColor)
                        .append(Text.literal(message).formatted(Formatting.WHITE));
                    player.sendMessage(auroraMessage, false);
                }

                messageTimer.cancel();
            }
        }, typingTime);
    }

    /**
     * calculate realistic typing time based on message complexity and entity personality
     */
    private static int calculateTypingTime(String message, String entity, String phase) {
        int baseTime;

        if (entity.equals("NULLPOINTER")) {
            // nullpointer types slower, more menacingly
            baseTime = message.length() * 150; // 150ms per character
        } else {
            // aurora types faster, more efficiently
            baseTime = message.length() * 80; // 80ms per character
        }

        // phase-based modifiers
        switch (phase) {
            case "NICE" -> baseTime = (int)(baseTime * 0.8); // faster, eager to help
            case "TRANSITION" -> baseTime = (int)(baseTime * 1.2); // slower, more thoughtful
            case "HOSTILE" -> baseTime = (int)(baseTime * 1.1); // deliberate typing
            case "JUMPSCARE" -> baseTime = (int)(baseTime * 1.5); // slow, menacing
        }

        // add some randomization for realism
        int variance = (int)(baseTime * 0.3);
        baseTime += (Math.random() - 0.5) * variance;

        // ensure minimum and maximum times
        return Math.max(500, Math.min(baseTime, 8000));
    }

    /**
     * show typing indicator with dots animation
     */
    private static void showTypingIndicator(ServerPlayerEntity player, String entity, String phase) {
        Formatting nameColor;

        if (entity.equals("AURORA")) {
            // make aurora yellow during transition phase
            if (phase.equals("TRANSITION")) {
                nameColor = Formatting.YELLOW;
            } else {
                nameColor = Formatting.AQUA;
            }
        } else {
            nameColor = Formatting.DARK_RED;
        }

        Text typingText = Text.literal("<" + entity + "> ").formatted(nameColor)
            .append(Text.literal("typing...").formatted(Formatting.GRAY, Formatting.ITALIC));

        player.sendMessage(typingText, true); // send as action bar

        // animate dots
        animateTypingDots(player, entity, nameColor, 3);
    }

    /**
     * animate typing dots for more realism
     */
    private static void animateTypingDots(ServerPlayerEntity player, String entity, Formatting nameColor, int cycles) {
        Timer dotTimer = new Timer();

        for (int i = 0; i <= cycles; i++) {
            final int dots = (i % 4) + 1; // cycle through 1-4 dots

            dotTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String dotString = ".".repeat(dots) + " ".repeat(4 - dots);

                    Text animatedText = Text.literal("<" + entity + "> ").formatted(nameColor)
                        .append(Text.literal("typing" + dotString).formatted(Formatting.GRAY, Formatting.ITALIC));

                    player.sendMessage(animatedText, true);
                }
            }, i * 600); // 600ms between dot animations
        }

        // schedule cleanup
        dotTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                dotTimer.cancel();
            }
        }, (cycles + 1) * 600);
    }

    /**
     * clear typing indicator
     */
    private static void hideTypingIndicator(ServerPlayerEntity player, String entity) {
        // clear action bar
        player.sendMessage(Text.literal(""), true);
    }

    /**
     * send message with smart typing simulation based on context
     */
    public static void sendContextualMessageWithTyping(ServerPlayerEntity player, String entity, String message, String phase, boolean isUrgent) {
        if (isUrgent || message.length() < 20) {
            // send immediately for short or urgent messages
            if (entity.equals("NULLPOINTER")) {
                ChatMessageFormatter.sendNullPointerMessage(player, message);
            } else {
                ChatMessageFormatter.sendAuroraMessage(player, message);
            }
        } else {
            // use typing simulation for longer messages
            sendMessageWithTyping(player, entity, message, phase);
        }
    }

    /**
     * simulate entity "thinking" before typing
     */
    public static void sendMessageWithThinking(ServerPlayerEntity player, String entity, String message, String phase) {
        String playerName = player.getName().getString();
        Formatting nameColor = entity.equals("AURORA") ? Formatting.AQUA : Formatting.DARK_RED;

        // show thinking indicator
        Text thinkingText = Text.literal("<" + entity + "> ").formatted(nameColor)
            .append(Text.literal("...").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));

        player.sendMessage(thinkingText, true);

        // think for a moment, then start typing
        Timer thinkTimer = new Timer();
        thinkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendMessageWithTyping(player, entity, message, phase);
                thinkTimer.cancel();
            }
        }, 1500 + (int)(Math.random() * 1000)); // 1.5-2.5 seconds thinking time
    }
}
