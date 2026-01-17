/*
 * Copyright (c) Coveo Solutions Inc.
 */
package codes.blitz.game.message.bot;

import codes.blitz.game.message.MessageType;

public record BotRegistrationWithoutToken(MessageType type, String teamName) {
  public BotRegistrationWithoutToken(String teamName) {
    this(MessageType.REGISTER, teamName);
  }
}
