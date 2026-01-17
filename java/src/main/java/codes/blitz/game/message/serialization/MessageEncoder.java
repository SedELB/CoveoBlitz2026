package codes.blitz.game.message.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class MessageEncoder implements Encoder.Text<Object> {
  private static final JsonMapper jsonMapper = JsonMapperInstanceHolder.getInstance();

  @Override
  public String encode(Object message) throws EncodeException {
    try {
      return jsonMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new EncodeException(message, "Failed to encode POJO!", e);
    }
  }
}
