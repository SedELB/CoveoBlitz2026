package codes.blitz.game.generated;


/**
 * Move the spore one tile in the specified direction. Leaves biomass trail on empty tiles.
 *
 * @param sporeId ID of the spore to move.
 * @param direction Direction vector to move. Should be one of: {x:0,y:-1} (up), {x:0,y:1} (down),
 *     {x:-1,y:0} (left), {x:1,y:0} (right).
 */
public record SporeMoveAction(ActionType type, String sporeId, Position direction)
    implements Action {
  public SporeMoveAction(String sporeId, Position direction) {
    this(ActionType.SPORE_MOVE, sporeId, direction);
  }
}
