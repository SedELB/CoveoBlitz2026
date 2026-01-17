package codes.blitz.game.bot;

import codes.blitz.game.generated.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bot {
    Random random = new Random();

    public Bot() {
        System.out.println("Initializing Xavier's super mega duper bot");
    }

    /*
     * Here is where the magic happens, for now the moves are not very good. I bet you can do better ;)
     */
    public List<Action> getActions(TeamGameState gameMessage) {
        TeamInfo myTeamInfos = gameMessage.world().teamInfos().get(gameMessage.yourTeamId());
        List<Action> actions = new ArrayList<>();

        TeamInfo myTeam = gameMessage.world().teamInfos().get(gameMessage.yourTeamId());
        if (myTeamInfos.nextSpawnerCost() <= myTeamInfos.nutrients()) {
            actions.add(new SporeCreateSpawnerAction(myTeam.spores().getFirst().id()));
        }

        for (Spore spore : myTeam.spores()) {
            actions.add(
                    new SporeMoveToAction(
                            spore.id(),
                            new Position(
                                    random.nextInt(gameMessage.world().map().width()),
                                    random.nextInt(gameMessage.world().map().height()))));
        }

        for (Spawner spawner : myTeam.spawners()) {
            actions.add(new SpawnerProduceSporeAction(spawner.id(), 1));
        }
        // You can clearly do better than the random actions above. Have fun!!
        return actions;
    }
}
