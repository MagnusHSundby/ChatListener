package chatlistener;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Testing implements ClientModInitializer {
  public static final String MOD_ID = "testing";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitializeClient() {
    ChatListener.register();

    ChatListener.onPhrase(
        "ping",
        event -> {
          Minecraft.getInstance().gui.getChat().addMessage(Component.literal("pong"));
        });
    LOGGER.info("ChatListener Loaded");
  }
}
