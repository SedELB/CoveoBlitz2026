package codes.blitz.game.generated;


/**
 * A spore entity that can move and engage in combat.
 *
 * @param id Unique identifier of the spore.
 * @param teamId Team that controls this spore.
 * @param position Current position of the spore.
 * @param biomass Current biomass level determining combat strength.
 */
public record Spore(String id, String teamId, Position position, int biomass) {}
