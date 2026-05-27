# ChatListener

A Fabric client-side library for listening to incoming chat messages in Minecraft. Designed for use on servers like Hypixel where all messages arrive as system messages.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.19.2+
- Fabric API

## Usage

### Setup

Call `ChatListener.register()` once in your `onInitializeClient`:

```java
public void onInitializeClient() {
    ChatListener.register();
}
```

### Methods

#### `onMatch(String regex, Consumer<ChatEvent> handler)`

Fires when the message matches a regex pattern. Matches substrings — `"ping"` will also match `"warping"`.

```java
ChatListener.onMatch("You earned (\\d+) coins", event -> {
    String amount = event.group(1); // e.g. "500"
});
```

#### `onPhrase(String phrase, Consumer<ChatEvent> handler)`

Fires when the message contains the phrase as a distinct word or phrase. `"ping"` will not match `"warping"`. No regex knowledge needed.

```java
ChatListener.onPhrase("ping", event -> {
    // fires on "ping" but not "warping"
});
```
