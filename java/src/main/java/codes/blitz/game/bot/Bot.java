package codes.blitz.game.bot;

import codes.blitz.game.generated.*;
import java.util.*;

public class Bot {
    private Map<String, Position> sporeTargets;
    private Set<Position> claimedTargets;
    private Random random;

    public Bot() {
        sporeTargets = new HashMap<>();
        claimedTargets = new HashSet<>();
        random = new Random();
        System.out.println("Initializing aggressive colonization bot - AVOID ROOTS!");
    }

    public List<Action> getActions(TeamGameState gameMessage) {
        List<Action> actions = new ArrayList<>();
        TeamInfo myTeam = gameMessage.world().teamInfos().get(gameMessage.yourTeamId());
        GameWorld world = gameMessage.world();
        String neutralId = gameMessage.constants().neutralTeamId();

        // Clear claimed targets each tick
        claimedTargets.clear();

        // Strategy priorities:
        // 1. Create spawners at strategic locations
        handleSpawnerCreation(myTeam, world, actions);

        // 2. Spawn new spores from spawners
        handleSporeProduction(myTeam, world, actions);

        // 3. Move spores strategically - COLONIZE EVERYTHING!
        handleSporeMovement(myTeam, world, gameMessage.yourTeamId(), neutralId, actions);

        return actions;
    }

    private void handleSpawnerCreation(TeamInfo myTeam, GameWorld world, List<Action> actions) {
        // Create spawner if we can afford it and have spores
        if (myTeam.nutrients() >= myTeam.nextSpawnerCost() && !myTeam.spores().isEmpty()) {
            // Find best spore to convert - prefer ones far from existing spawners
            Spore bestSpore = findBestSpawnerCandidate(myTeam, world);
            if (bestSpore != null) {
                actions.add(new SporeCreateSpawnerAction(bestSpore.id()));
            }
        }
    }

    private Spore findBestSpawnerCandidate(TeamInfo myTeam, GameWorld world) {
        if (myTeam.spawners().isEmpty()) {
            // First spawner - use any spore
            return myTeam.spores().isEmpty() ? null : myTeam.spores().get(0);
        }

        Spore bestSpore = null;
        double maxMinDist = 0;

        for (Spore spore : myTeam.spores()) {
            // Find minimum distance to any existing spawner
            double minDist = Double.MAX_VALUE;
            for (Spawner spawner : myTeam.spawners()) {
                double dist = manhattanDistance(spore.position(), spawner.position());
                minDist = Math.min(minDist, dist);
            }

            // Prefer spores that are far from existing spawners
            if (minDist > maxMinDist) {
                maxMinDist = minDist;
                bestSpore = spore;
            }
        }

        return bestSpore;
    }

    private void handleSporeProduction(TeamInfo myTeam, GameWorld world, List<Action> actions) {
        if (myTeam.spawners().isEmpty()) return;

        int totalBiomass = calculateTotalBiomass(myTeam);
        int targetSporeSize = Math.max(10, totalBiomass / (myTeam.spawners().size() * 3));

        for (Spawner spawner : myTeam.spawners()) {
            // Only spawn if we have enough nutrients
            if (myTeam.nutrients() >= targetSporeSize) {
                actions.add(new SpawnerProduceSporeAction(spawner.id(), targetSporeSize));
            }
        }
    }

    private void handleSporeMovement(TeamInfo myTeam, GameWorld world, String teamId, String neutralId, List<Action> actions) {
        for (Spore spore : myTeam.spores()) {
            Position target = determineSporeTarget(spore, world, teamId, neutralId, myTeam);

            if (target != null) {
                sporeTargets.put(spore.id(), target);
                actions.add(new SporeMoveToAction(spore.id(), target));
            }
        }
    }

    private Position determineSporeTarget(Spore spore, GameWorld world, String teamId, String neutralId, TeamInfo myTeam) {
        // Priority 1: Attack nearby weak enemies (NOT NEUTRAL!)
        Spore weakEnemy = findWeakNearbyEnemy(spore, world, teamId, neutralId);
        if (weakEnemy != null) {
            claimedTargets.add(weakEnemy.position());
            return weakEnemy.position();
        }

        // Priority 2: ALWAYS expand to unclaimed territory - NEVER STOP COLONIZING!
        Position expansion = findBestExpansionTarget(spore, world, teamId, neutralId);
        if (expansion != null) {
            claimedTargets.add(expansion);
            return expansion;
        }

        // Priority 3: If somehow no expansion targets, find ANY non-owned, non-neutral tile
        Position anyTarget = findAnyUnclaimedTile(spore, world, teamId, neutralId);
        if (anyTarget != null) {
            return anyTarget;
        }

        // Priority 4: Support nearby allies or regroup
        Position support = findSupportPosition(spore, world, teamId, myTeam);
        if (support != null) {
            return support;
        }

        // Fallback: Move to map center for better positioning
        return new Position(world.map().width() / 2, world.map().height() / 2);
    }

    private Spore findWeakNearbyEnemy(Spore spore, GameWorld world, String teamId, String neutralId) {
        Spore target = null;
        double bestScore = Double.MIN_VALUE;

        for (Spore enemy : world.spores()) {
            // SKIP OUR TEAM AND NEUTRAL
            if (enemy.teamId().equals(teamId) || enemy.teamId().equals(neutralId)) continue;

            double dist = manhattanDistance(spore.position(), enemy.position());

            // Only consider nearby enemies
            if (dist > 15) continue;

            // Score: prefer weak enemies that are close
            double biomassAdvantage = spore.biomass() - enemy.biomass();
            double score = biomassAdvantage * 10 - dist;

            // Only attack if we're stronger
            if (biomassAdvantage > 0 && score > bestScore && !claimedTargets.contains(enemy.position())) {
                bestScore = score;
                target = enemy;
            }
        }

        return target;
    }

    private Position findBestExpansionTarget(Spore spore, GameWorld world, String teamId, String neutralId) {
        Position best = null;
        double bestScore = Double.MIN_VALUE;
        int searchRadius = 25; // Increased search radius to find more targets

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                int x = spore.position().x() + dx;
                int y = spore.position().y() + dy;

                // Check bounds
                if (x < 0 || x >= world.map().width() || y < 0 || y >= world.map().height()) {
                    continue;
                }

                Position pos = new Position(x, y);

                // Skip if already claimed this tick
                if (claimedTargets.contains(pos)) continue;

                String owner = world.ownershipGrid()[x][y];

                // *** CRITICAL: NEVER TARGET NEUTRAL TILES ***
                if (owner != null && owner.equals(neutralId)) {
                    continue;
                }

                // Skip tiles we already own - but keep expanding!
                if (owner != null && owner.equals(teamId)) {
                    continue;
                }

                int nutrientValue = world.map().nutrientGrid()[x][y];
                double dist = manhattanDistance(spore.position(), pos);

                // Score: high nutrients, close distance, prefer empty tiles
                double score = nutrientValue * 100 / (dist + 1);

                // Bonus for empty tiles (null owner)
                if (owner == null) {
                    score += 50;
                }

                if (score > bestScore) {
                    bestScore = score;
                    best = pos;
                }
            }
        }

        return best;
    }

    private Position findAnyUnclaimedTile(Spore spore, GameWorld world, String teamId, String neutralId) {
        // Spiral search from spore position to find ANY unclaimed tile
        int maxRadius = Math.max(world.map().width(), world.map().height());

        for (int radius = 1; radius < maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    // Only check perimeter of current radius
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) continue;

                    int x = spore.position().x() + dx;
                    int y = spore.position().y() + dy;

                    if (x < 0 || x >= world.map().width() || y < 0 || y >= world.map().height()) {
                        continue;
                    }

                    String owner = world.ownershipGrid()[x][y];

                    // NOT NEUTRAL, NOT OURS = TARGET!
                    if ((owner == null || (!owner.equals(teamId) && !owner.equals(neutralId)))) {
                        return new Position(x, y);
                    }
                }
            }
        }

        return null;
    }

    private Position findSupportPosition(Spore spore, GameWorld world, String teamId, TeamInfo myTeam) {
        // Find the centroid of our forces
        if (myTeam.spores().isEmpty()) return null;

        int sumX = 0, sumY = 0;
        for (Spore ally : myTeam.spores()) {
            sumX += ally.position().x();
            sumY += ally.position().y();
        }

        return new Position(
                sumX / myTeam.spores().size(),
                sumY / myTeam.spores().size()
        );
    }

    private int calculateTotalBiomass(TeamInfo team) {
        int total = 0;
        for (Spore spore : team.spores()) {
            total += spore.biomass();
        }
        return total;
    }

    private double manhattanDistance(Position p1, Position p2) {
        return Math.abs(p1.x() - p2.x()) + Math.abs(p1.y() - p2.y());
    }
}