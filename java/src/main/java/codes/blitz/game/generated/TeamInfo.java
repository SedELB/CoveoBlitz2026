package codes.blitz.game.generated;

import java.util.List;

/**
 * Information for a team.
 *
 * @param teamId Team identifier.
 * @param isAlive True if the team is still alive in the game.
 * @param nutrients Current nutrients available to the team.
 * @param spores List of all spores controlled by the team.
 * @param spawners List of all spawners controlled by the team.
 * @param nextSpawnerCost Cost in biomass to create the next spawner.
 */
public record TeamInfo(
    String teamId,
    boolean isAlive,
    int nutrients,
    List<Spore> spores,
    List<Spawner> spawners,
    int nextSpawnerCost) {}
