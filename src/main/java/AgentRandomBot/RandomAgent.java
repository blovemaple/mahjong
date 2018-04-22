package AgentRandomBot;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContextPlayerView;
import com.github.blovemaple.mj.local.AbstractBot;

import java.util.List;
import java.util.Set;

public class RandomAgent extends AbstractBot {
  public RandomAgent(String name) {
    super(name);
  }

  public RandomAgent() {
    this("Random");
  }

  @Override
  protected Action chooseCpgdAction(GameContextPlayerView contextView, Set<ActionType> actionTypes, List<Action> actions) throws InterruptedException {
    int numActions = actions.size();
    return actions.get((int)Math.random()*numActions);
  }
}
