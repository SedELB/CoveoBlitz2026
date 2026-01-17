package codes.blitz.game.generated;


/**
 * Create a new spore at the spawner location with specified biomass. Costs nutrients equal to
 * biomass.
 *
 * @param spawnerId ID of the spawner that will produce the spore.
 * @param biomass Amount of biomass for the new spore. Must be positive and team must have enough
 *     nutrients.
 */
public record SpawnerProduceSporeAction(ActionType type, String spawnerId, int biomass)
    implements Action {
  public SpawnerProduceSporeAction(String spawnerId, int biomass) {
    this(ActionType.SPAWNER_PRODUCE_SPORE, spawnerId, biomass);
  }
}
