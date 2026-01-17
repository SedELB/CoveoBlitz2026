/*
 * Copyright (c) Coveo Solutions Inc.
 */
package codes.blitz.game.message.bot;

import codes.blitz.game.message.MessageType;

public record BotRegistrationWithToken(MessageType type, String token) {
  public BotRegistrationWithToken(String token) {
    this(MessageType.REGISTER, token);
  }
}
