package codes.blitz.game.generated;

import java.util.List;
import java.util.Map;

/**
 * The game world.
 *
 * @param map The loaded game map. This map will never change during the game.
 * @param biomassGrid The biomass level per tile. Use this in combination with ownershipGrid.
 * @param ownershipGrid The owner id per tile. Will be the team id if owned by a player. Use this in
 *     combination with biomassGrid.
 * @param spores The spores in the map.
 * @param spawners The spawners in the map.
 * @param teamInfos Current info of each team by team id.
 */
public record GameWorld(
    GameMap map,
    int[][] biomassGrid,
    String[][] ownershipGrid,
    List<Spore> spores,
    List<Spawner> spawners,
    Map<String, TeamInfo> teamInfos) {}
