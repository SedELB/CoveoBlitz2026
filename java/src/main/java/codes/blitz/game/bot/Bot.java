package codes.blitz.game.bot;

import codes.blitz.game.generated.*;

import java.util.*;

public class Bot {
    Map<Spore, Position> sporesLockedIn;
    Set<Position> positionsLockedIn;

    public Bot() {
        sporesLockedIn = new HashMap<Spore, Position>();
        positionsLockedIn = new HashSet<Position>();
        System.out.println("Initializing our super mega duper bot");
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
        int forceDesTroupes = world.spawners().isEmpty() ? 1 : Math.max(3, biomasse / world.spawners().size());

        // Spawn de nouvelles spores
        spawn(myTeam, actions, forceDesTroupes);

        // Créer des spawners stratégiquement (pas trop tôt)
        if (myTeam.nutrients() >= myTeam.nextSpawnerCost() && myTeam.spawners().size() < 5) {
            Spore bestSporeForSpawner = findBestSporeForSpawner(myTeam, world, myTeamId);
            if (bestSporeForSpawner != null) {
                actions.add(new SporeCreateSpawnerAction(bestSporeForSpawner.id()));
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
     * Priorise : tuiles à 0 biomasse, haute valeur nutritive, proximité
     * Évite : spores neutres avec biomasse > 20
     */
    private Position findBestTileToCapture(Spore spore, GameWorld world, String myTeamId) {
        Position bestPosition = null;
        double bestScore = -1;

        int width = world.map().width();
        int height = world.map().height();

        // Chercher dans un rayon raisonnable autour de la spore
        int searchRadius = 15;

        for (int x = Math.max(0, spore.position().x() - searchRadius);
             x < Math.min(width, spore.position().x() + searchRadius); x++) {
            for (int y = Math.max(0, spore.position().y() - searchRadius);
                 y < Math.min(height, spore.position().y() + searchRadius); y++) {

                Position pos = new Position(x, y);

                // Vérifier si la tile n'est pas déjà contrôlée par nous
                String owner = world.ownershipGrid()[x][y];
                if (owner != null && owner.equals(myTeamId)) {
                    continue; // Déjà contrôlée
                }

                // Éviter les positions déjà ciblées par d'autres spores
                if (positionsLockedIn.contains(pos)) {
                    continue;
                }

                // NOUVEAU: Vérifier s'il y a une spore neutre forte (>20) à cette position
                if (hasStrongNeutralSpore(pos, world)) {
                    continue; // Éviter complètement les spores neutres > 20
                }

                // Calculer le score de cette position
                double distance = calculateDistance(spore.position(), pos);
                if (distance == 0) continue;

                int nutrientValue = world.map().nutrientGrid()[x][y];
                int currentBiomass = world.biomassGrid()[x][y];

                // Score de base = valeur nutritive / distance
                double score = (nutrientValue + 1) / (distance + 1);

                // BONUS MAJEUR: Tuiles à 0 biomasse (vides ou traces ennemies faibles)
                if (currentBiomass == 0) {
                    score *= 3.0; // Triple le score pour les tuiles vides
                }

                // Bonus si la tile n'est contrôlée par personne
                if (owner == null || owner.isEmpty()) {
                    score *= 1.5;
                }

                // Bonus si c'est une tile avec spore neutre FAIBLE (< biomasse de notre spore)
                boolean hasWeakNeutral = false;
                for (Spore neutralSpore : world.spores()) {
                    if (neutralSpore.teamId() == null &&
                            neutralSpore.position().equals(pos) &&
                            neutralSpore.biomass() < spore.biomass() &&
                            neutralSpore.biomass() <= 20) { // Seulement les faibles
                        hasWeakNeutral = true;
                        break;
                    }
                }
                if (hasWeakNeutral) {
                    score *= 1.5; // Bonus modéré pour les neutres faibles
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestPosition = pos;
                }
            }
        }

        return bestPosition;
    }

    /**
     * Vérifie si une position contient une spore neutre avec biomasse > 20
     */
    private boolean hasStrongNeutralSpore(Position pos, GameWorld world) {
        for (Spore neutralSpore : world.spores()) {
            if (neutralSpore.teamId() == null && // Spore neutre
                    neutralSpore.position().equals(pos) &&
                    neutralSpore.biomass() > 20) { // Biomasse > 20
                return true;
            }
        }
        return false;
    }

    /**
     * Trouve la meilleure spore pour créer un spawner
     * Choisit une position avec beaucoup de nutriments environnants
     */
    private Spore findBestSporeForSpawner(TeamInfo myTeam, GameWorld world, String myTeamId) {
        Spore bestSpore = null;
        double bestScore = -1;

        for (Spore spore : myTeam.spores()) {
            if (spore.biomass() < myTeam.nextSpawnerCost()) {
                continue; // Pas assez de biomasse
            }

            // Calculer le score basé sur la richesse du territoire environnant
            int localNutrients = 0;
            int radius = 5;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int x = spore.position().x() + dx;
                    int y = spore.position().y() + dy;

                    if (x >= 0 && x < world.map().width() &&
                            y >= 0 && y < world.map().height()) {
                        localNutrients += world.map().nutrientGrid()[x][y];
                    }
                }
            }

            if (localNutrients > bestScore) {
                bestScore = localNutrients;
                bestSpore = spore;
            }
        }

        return bestSpore;
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

    private Spore trouverEnnemiProche(Spore monSpore, GameWorld world, String teamId) {
        Spore proche = null;
        double minDist = Double.MAX_VALUE;

        for (Spore ennemi : world.spores()) {
            if (!ennemi.teamId().equals(teamId)) {
                double dist = calculateDistance(ennemi.position(), monSpore.position());
                if (dist < minDist) {
                    minDist = dist;
                    proche = ennemi;
                }
            }
        }

        return proche;
    }
}