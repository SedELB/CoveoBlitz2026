package codes.blitz.game.generated;


/**
 * Move the spore towards the specified position using pathfinding. Moves one tile closer each turn.
 *
 * @param sporeId ID of the spore to move.
 * @param position Target position to move towards.
 */
public record SporeMoveToAction(ActionType type, String sporeId, Position position)
    implements Action {
  public SporeMoveToAction(String sporeId, Position position) {
    this(ActionType.SPORE_MOVE_TO, sporeId, position);
  }
}
