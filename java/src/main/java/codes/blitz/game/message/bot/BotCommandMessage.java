package codes.blitz.game.message.bot;

import codes.blitz.game.generated.Action;
import codes.blitz.game.message.MessageType;
import java.util.List;

public record BotCommandMessage(MessageType type, Number tick, List<Action> actions) {
  public BotCommandMessage(Number tick, List<Action> actions) {
    this(MessageType.COMMAND, tick, actions);
  }
}
