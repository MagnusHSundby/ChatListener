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

Both methods accept an optional `int priority` as a third argument. Higher values run first. Default is `0`.

### ChatEvent

| Method | Description |
|---|---|
| `event.text()` | Plain text of the message |
| `event.message()` | Full `Component` with formatting |
| `event.sender()` | `GameProfile` of sender, or `null` for system messages |
| `event.isOwn()` | `true` if sent by the local player (vanilla/singleplayer only) |
| `event.group(int)` | Regex capture group by index, or `null` |
| `event.matcher()` | The raw `Matcher` object, or `null` |

## Adding as a dependency

Publish to local Maven:

```bash
./gradlew publishToMavenLocal
```

In your mod's `build.gradle`:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    modImplementation include("chatlistener:chatlistener:1.0.0")
}
```
