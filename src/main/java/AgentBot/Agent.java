package AgentBot;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.local.AbstractBot;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroupPlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * For CS5100 Final Project.
 * Agent bot.
 */
public class Agent extends AbstractBot {
  private PlayerLocation playerLocation;
  private OnePlayerStrategy strategy = OnePlayerStrategy.getInstance();

  public Agent(String name) {
    super(name);
  }

  public Agent() {
    this("Agent");
    strategy.setParameter(10,5,1,1,-1);
  }

  @Override
  protected Action chooseCpgdAction(GameContextPlayerView contextView, Set<ActionType> actionTypes, List<Action> actions) throws InterruptedException {
    System.out.println("Agent in hand: " + getCurrentHand(contextView));
    if(actions.size() ==1) return actions.get(0);
    List<Action> notDiscard = actions.stream().filter(action -> {
              if(action == null || action.getType()== null || action.getType()
                      .name() == null)
                return false;
              return !action.getType().name().equalsIgnoreCase("discard");
    }
            ).collect(Collectors.toList());
    if(!notDiscard.isEmpty()) return notDiscard.get(0);
    strategy.setTiles(new ArrayList<>(getCurrentHand(contextView)),actions,getUnknownTiles
            (contextView.getMyInfo(),contextView));
//    System.out.println("Actions: " + actions.toString());
    return strategy.discardOnePlayerStategy();
  }


  /**
   * Tiles in hand.
   */
  private Set<Tile> getCurrentHand(GameContextPlayerView contextView) {
    return contextView.getMyInfo().getAliveTiles();
  }

  private List<Tile> getUnknownTiles(PlayerInfo myInfo, GameContextPlayerView context) {
    List<Tile> unknownTiles = new ArrayList<>
            (context.getGameStrategy().getAllTiles());
//    unknownTiles.removeAll(myInfo.getDiscardedTiles());
    unknownTiles.removeAll(myInfo.getAliveTiles());
//    for(TileGroup group:myInfo.getTileGroups()){
//      unknownTiles.removeAll(group.getTiles());
//    }
    context.getTableView()
            .getPlayerInfoView().forEach(
            (location, playerInfoPlayerView) ->
            {
              unknownTiles.removeAll(playerInfoPlayerView.getDiscardedTiles());
              for (TileGroupPlayerView group : playerInfoPlayerView.getTileGroups()) {
                if(group == null || group.getTiles() == null) continue;
                unknownTiles.removeAll(group.getTiles());
              }
            });
    return unknownTiles;
  }
}
