package codes.blitz.game.generated;


/**
 * Create a spawner using part of the spore's biomass at its current position. Cost follows
 * exponential sequence: 0, 1, 3, 7, 15, 31...
 *
 * @param sporeId ID of the spore to create a spawner.
 */
public record SporeCreateSpawnerAction(ActionType type, String sporeId) implements Action {
  public SporeCreateSpawnerAction(String sporeId) {
    this(ActionType.SPORE_CREATE_SPAWNER, sporeId);
  }
}
