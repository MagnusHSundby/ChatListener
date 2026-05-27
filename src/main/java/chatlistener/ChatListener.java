package chatlistener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

public class ChatListener {

  private record Registration(
      int priority, Predicate<ChatEvent> filter, Consumer<ChatEvent> handler) {}

  private static final List<Registration> handlers = new ArrayList<>();
  private static final List<Runnable> pendingActions = new ArrayList<>();

  public static void register() {
    ClientReceiveMessageEvents.ALLOW_CHAT.register(
        (message, signedMessage, sender, params, receptionTime) -> {
          Minecraft mc = Minecraft.getInstance();
          boolean isOwn =
              sender != null
                  && mc.getUser() != null
                  && sender.id().equals(mc.getUser().getProfileId());
          dispatch(new ChatEvent(message, sender, null, isOwn));
          return true;
        });

    ClientReceiveMessageEvents.ALLOW_GAME.register(
        (message, overlay) -> {
          if (overlay) return true;
          dispatch(new ChatEvent(message, null, null, false));
          return true;
        });

    ClientTickEvents.END_CLIENT_TICK.register(
        client -> {
          if (pendingActions.isEmpty()) return;
          List<Runnable> toRun = new ArrayList<>(pendingActions);
          pendingActions.clear();
          toRun.forEach(Runnable::run);
        });
  }

  /**
   * Registers a handler that fires when the message matches a regex pattern. The handler receives a
   * {@link ChatEvent} with a populated {@link java.util.regex.Matcher} for accessing capture
   * groups.
   *
   * <pre>{@code
   * // Matches any message containing "joined the game"
   * ChatListener.onMatch("(\\w+) joined the game", event -> {
   *     String name = event.group(1);
   *     Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Welcome " + name + "!"));
   * });
   * }</pre>
   *
   * @param regex the regex pattern to match against the full message text
   * @param handler the action to run on the next tick when a match is found
   * @param priority higher values run first; default is 0
   */
  public static void onMatch(String regex, Consumer<ChatEvent> handler, int priority) {
    Pattern pattern = Pattern.compile(regex);
    handlers.add(
        new Registration(
            priority,
            event -> pattern.matcher(event.text()).find(),
            event -> {
              Matcher m = pattern.matcher(event.text());
              if (m.find()) {
                handler.accept(new ChatEvent(event.message(), event.sender(), m, event.isOwn()));
              }
            }));
    handlers.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
  }

  /**
   * Registers a handler that fires when the message matches a regex pattern.
   *
   * <pre>{@code
   * ChatListener.onMatch("You earned (\\d+) coins", event -> {
   *     String amount = event.group(1); // e.g. "500"
   * });
   * }</pre>
   *
   * @param regex the regex pattern to match against the full message text
   * @param handler the action to run on the next tick when a match is found
   */
  public static void onMatch(String regex, Consumer<ChatEvent> handler) {
    onMatch(regex, handler, 0);
  }

  /**
   * Registers a handler that fires when the message contains the phrase as a distinct match — not
   * as part of a larger word. Uses {@link Pattern#quote} internally, so no regex knowledge is
   * needed.
   *
   * <pre>{@code
   * // Matches "gg" but not "eggs" or "ggs"
   * ChatListener.onPhrase("gg", event -> {
   *     Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Good game!"));
   * });
   *
   * // Also works for multi-word phrases
   * ChatListener.onPhrase("Sending to server", event -> {
   *     Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Warping..."));
   * });
   * }</pre>
   *
   * @param phrase the literal phrase to match as a whole word or phrase
   * @param handler the action to run on the next tick when a match is found
   * @param priority higher values run first; default is 0
   */
  public static void onPhrase(String phrase, Consumer<ChatEvent> handler, int priority) {
    onMatch("\\b" + Pattern.quote(phrase) + "\\b", handler, priority);
  }

  /**
   * Registers a handler that fires when the message contains the phrase as a distinct match.
   *
   * <pre>{@code
   * ChatListener.onPhrase("ping", event -> {
   *     Minecraft.getInstance().gui.getChat().addMessage(Component.literal("pong"));
   * });
   * }</pre>
   *
   * @param phrase the literal phrase to match as a whole word or phrase
   * @param handler the action to run on the next tick when a match is found
   */
  public static void onPhrase(String phrase, Consumer<ChatEvent> handler) {
    onPhrase(phrase, handler, 0);
  }

  /**
   * Registers a handler that fires only when the entire message is exactly equal to the given text.
   *
   * <pre>{@code
   * // Fires only if the message is exactly "hello" — not "hello world" or "say hello"
   * ChatListener.onExact("hello", event -> {
   *     Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Hey!"));
   * });
   * }</pre>
   *
   * @param text the exact message text to match against
   * @param handler the action to run on the next tick when a match is found
   * @param priority higher values run first; default is 0
   */
  public static void onExact(String text, Consumer<ChatEvent> handler, int priority) {
    handlers.add(new Registration(priority, event -> event.text().equals(text), handler));
    handlers.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
  }

  /**
   * Registers a handler that fires only when the entire message is exactly equal to the given text.
   *
   * <pre>{@code
   * ChatListener.onExact("!reload", event -> {
   *     // runs only when the message is literally "!reload"
   * });
   * }</pre>
   *
   * @param text the exact message text to match against
   * @param handler the action to run on the next tick when a match is found
   */
  public static void onExact(String text, Consumer<ChatEvent> handler) {
    onExact(text, handler, 0);
  }

  private static void dispatch(ChatEvent event) {
    for (Registration reg : handlers) {
      if (reg.filter().test(event)) {
        pendingActions.add(() -> reg.handler().accept(event));
      }
    }
  }
}
