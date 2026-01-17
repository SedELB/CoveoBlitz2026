package codes.blitz.game.generated;


/**
 * A spawner that can produce spores.
 *
 * @param id Unique identifier of the spawner.
 * @param teamId Team that controls this spawner.
 * @param position Position of the spawner.
 */
public record Spawner(String id, String teamId, Position position) {}
