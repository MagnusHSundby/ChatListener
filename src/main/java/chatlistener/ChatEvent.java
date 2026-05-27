package chatlistener;

import com.mojang.authlib.GameProfile;
import java.util.regex.Matcher;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ChatEvent {

  private final Component message;
  private final @Nullable GameProfile sender;
  private final @Nullable Matcher matcher;
  private final boolean own;

  public ChatEvent(Component message, @Nullable GameProfile sender, @Nullable Matcher matcher, boolean own) {
    this.message = message;
    this.sender = sender;
    this.matcher = matcher;
    this.own = own;
  }

  /** Returns the plain text content of the message, without formatting codes. */
  public String text() {
    return message.getString();
  }

  /** Returns the full message component, including formatting. */
  public Component message() {
    return message;
  }

  /**
   * Returns the sender's GameProfile, or null for system messages. On servers like Hypixel
   * all messages arrive as system messages, so this will typically be null.
   */
  public @Nullable GameProfile sender() {
    return sender;
  }

  /**
   * Returns true if this message was sent by the local player. Only reliable on vanilla
   * servers and singleplayer — always false on servers like Hypixel.
   */
  public boolean isOwn() {
    return own;
  }

  /**
   * Returns the regex {@link Matcher} after the match, or null if the event was not
   * triggered via {@code onMatch} or {@code onPhrase}.
   *
   * <pre>{@code
   * ChatListener.onMatch("You earned (\\d+) coins", event -> {
   *     String amount = event.group(1); // "500"
   * });
   * }</pre>
   */
  public @Nullable Matcher matcher() {
    return matcher;
  }

  /**
   * Returns the nth regex capture group, or null if unavailable or index is out of range.
   *
   * <pre>{@code
   * ChatListener.onMatch("(\\w+) joined the game", event -> {
   *     String name = event.group(1); // e.g. "Steve"
   * });
   * }</pre>
   */
  public @Nullable String group(int index) {
    if (matcher == null) return null;
    try {
      return matcher.group(index);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }
}
