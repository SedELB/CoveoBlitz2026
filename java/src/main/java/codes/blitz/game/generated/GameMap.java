package codes.blitz.game.generated;


/**
 * The game map.
 *
 * @param width Width of the map.
 * @param height Height of the map.
 * @param nutrientGrid The nutrients level per tile.
 */
public record GameMap(int width, int height, int[][] nutrientGrid) {}
