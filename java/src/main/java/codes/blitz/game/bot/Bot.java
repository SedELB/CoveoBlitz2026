package codes.blitz.game.bot;

import codes.blitz.game.generated.*;

import java.util.*;

public class Bot {
    Map<Spore, Position> sporesLockedIn;
    Set<Position> positionsLockedIn;

    public Bot() {
        sporesLockedIn = new HashMap<Spore, Position>();
        positionsLockedIn = new HashSet<Position>();
    }

    /*
     * Stratégie améliorée pour maximiser la capture de tiles
     */
    public List<Action> getActions(TeamGameState gameMessage) {
        List<Action> actions = new ArrayList<>();
        TeamInfo myTeam = gameMessage.world().teamInfos().get(gameMessage.yourTeamId());
        GameWorld world = gameMessage.world();
        String myTeamId = gameMessage.yourTeamId();
        int biomasse = calculateBiomass(myTeam);
        int forceDesTroupes = 1; //world.spawners().isEmpty() ? 1 : Math.max(3, biomasse / world.spawners().size());

        // Spawn de nouvelles spores
        spawn(myTeam, actions, forceDesTroupes);

        // Créer des spawners stratégiquement (pas trop tôt)
        if (myTeam.nutrients() >= myTeam.nextSpawnerCost()) {
            if (!world.spores().isEmpty()) {
                actions.add(new SporeCreateSpawnerAction(world.spores().getFirst().id()));
            }
        }

        // Réinitialiser les positions verrouillées chaque tour
        positionsLockedIn.clear();

        // Mouvement vers tiles non contrôlées
        for (Spore spore : myTeam.spores()) {
            if (spore.biomass() >= 2) { // Seulement les spores actives
                Position target = findBestTileToCapture(spore, world, myTeamId);
                if (target != null) {
                    actions.add(new SporeMoveToAction(spore.id(), target));
                    positionsLockedIn.add(target);
                }
            }
        }

        return actions;
    }

    /**
     * Trouve la meilleure tile à capturer pour une spore donnée
     * Priorise : haute valeur nutritive, proximité, tiles non contrôlées
     */
    private Position findBestTileToCapture(Spore spore, GameWorld world, String myTeamId) {
        Position bestPosition = null;
        double bestScore = -1;

        int width = world.map().width();
        int height = world.map().height();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                Position pos = new Position(x, y);
                int score = -1;
                String owner = world.ownershipGrid()[x][y];
                int strengh = world.biomassGrid()[x][y];

                if (!world.teamInfos().containsKey(owner) || strengh >= 10) {
                    continue;
                }

                // Bonus si la tile n'est contrôlée par personne
                if (owner.isEmpty()) {
                    return pos;
                }
            }
        }

        return bestPosition;
    }

    private int calculateBiomass(TeamInfo myTeam) {
        int count = 0;
        for (Spore spore : myTeam.spores()) {
            count += spore.biomass();
        }
        return count;
    }

    private void spawn(TeamInfo myTeam, List<Action> actions, int forceDesTroupes) {
        for (Spawner spawner : myTeam.spawners()) {
            if (myTeam.nutrients() >= forceDesTroupes) {
                actions.add(new SpawnerProduceSporeAction(spawner.id(), forceDesTroupes));
            }
        }
    }

    private double calculateDistance(Position p1, Position p2) {
        return Math.abs(p1.x() - p2.x()) + Math.abs(p1.y() - p2.y());
    }
}