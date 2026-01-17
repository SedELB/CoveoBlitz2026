package codes.blitz.game.generated;

import java.util.List;

/**
 * State of the game for a specific team.
 *
 * @param tick Current tick number.
 * @param yourTeamId Your team id.
 * @param lastTickErrors Errors that happened during the last tick.
 * @param constants Game constants.
 * @param teamIds List of all the teams currently playing.
 * @param world The game map, and objects
 */
public record TeamGameState(
    int tick,
    String yourTeamId,
    List<String> lastTickErrors,
    Constants constants,
    List<String> teamIds,
    GameWorld world) {}
