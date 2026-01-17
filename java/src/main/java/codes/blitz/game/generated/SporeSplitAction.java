package codes.blitz.game.generated;


/**
 * Split a spore into two spores, distributing biomass between them. Original spore moves with
 * specified biomass, new spore created at original position with remaining biomass.
 *
 * @param sporeId ID of the spore to split.
 * @param biomassForMovingSpore Amount of biomass for the moving spore (must be at least 1 and less
 *     than current biomass).
 * @param direction Direction for the original spore to move. Should be one of: {x:0,y:-1} (up),
 *     {x:0,y:1} (down), {x:-1,y:0} (left), {x:1,y:0} (right).
 */
public record SporeSplitAction(
    ActionType type, String sporeId, int biomassForMovingSpore, Position direction)
    implements Action {
  public SporeSplitAction(String sporeId, int biomassForMovingSpore, Position direction) {
    this(ActionType.SPORE_SPLIT, sporeId, biomassForMovingSpore, direction);
  }
}
