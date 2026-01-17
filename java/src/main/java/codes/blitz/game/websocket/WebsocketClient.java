package codes.blitz.game.websocket;

import codes.blitz.game.bot.Bot;
import codes.blitz.game.generated.Action;
import codes.blitz.game.generated.TeamGameState;
import codes.blitz.game.message.bot.BotCommandMessage;
import codes.blitz.game.message.bot.BotRegistrationWithToken;
import codes.blitz.game.message.bot.BotRegistrationWithoutToken;
import codes.blitz.game.message.serialization.MessageDecoder;
import codes.blitz.game.message.serialization.MessageEncoder;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EncodeException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint(decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class WebsocketClient {
  private static final String TOKEN_KEY = "TOKEN";

  private final Bot bot;
  private final CountDownLatch latch;

  public WebsocketClient(CountDownLatch latch) {
    this.latch = Objects.requireNonNull(latch);
    this.bot = new Bot();
  }

  @OnOpen
  public void onOpen(Session session) throws IOException, EncodeException {
    Record message;
    if (System.getenv(TOKEN_KEY) != null) {
      String token = System.getenv(TOKEN_KEY);
      message = new BotRegistrationWithToken(token);
    } else {
      message = new BotRegistrationWithoutToken("myJavaBot");
    }

    session.getBasicRemote().sendObject(message);
  }

  @OnMessage
  public void processMessageFromServer(TeamGameState receivedMessage, Session session)
      throws IOException, EncodeException {
    if (!receivedMessage.lastTickErrors().isEmpty()) {
      System.err.printf(
          "Previous tick error: %s%n", String.join(", ", receivedMessage.lastTickErrors()));
    }

    System.out.printf("Playing tick '%d'.%n", receivedMessage.tick());

    List<Action> actions = Collections.emptyList();

    // Just so your bot doesn't completely crash. ;)
    try {
      actions = bot.getActions(receivedMessage);
    } catch (Exception exception) {
      System.out.println("Exception while getting next moves: ");
      exception.printStackTrace(System.out);
    }

    BotCommandMessage botMessage = new BotCommandMessage(receivedMessage.tick(), actions);

    session.getBasicRemote().sendObject(botMessage);
  }

  @OnClose
  public void onClose(CloseReason closeReason) {
    System.out.println("Closing the websocket for this reason: " + closeReason);
    latch.countDown();
  }
}
