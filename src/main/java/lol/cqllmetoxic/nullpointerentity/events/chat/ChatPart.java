package lol.cqllmetoxic.nullpointerentity.events.chat;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Random;

/**
 * A localized chat fragment: a translation key plus its args (player name, game term, the player's
 * own quoted text, …). built into a {@link Text#translatable} so the receiving client renders it in
 * its own< language - this is what makes chat replies localize per player instead of being
 * baked to English on the server. user-supplied content is passed as an arg, never keyed.
 */
public record ChatPart(String key, Object... args) {
    private static final Random RNG = new Random();

    public MutableText toText() {
        return Text.translatable(key, args);
    }

    /** server-side rendered length (en_us), used only as a typing-time estimate - never displayed. */
    public int estimatedLength() {
        return toText().getString().length();
    }

    /** picks one of {@code base.1 .. base.n} at random, with the given args. */
    public static ChatPart pick(String base, int n, Object... args) {
        return new ChatPart(base + "." + (1 + RNG.nextInt(n)), args);
    }
}
