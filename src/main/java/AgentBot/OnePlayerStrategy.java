package AgentBot;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.object.TileType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * For CS5100 Final Project.
 * One-player strategy.
 */
public class OnePlayerStrategy {
  private List<Tile> tilesInHand; //tiles after picking, need to discard 1.
  private List<Tile> unknownTiles;
  private List<Action> actions;
  private HandFactory factory = HandFactory.getHandFactory();
  private static OnePlayerStrategy s = new OnePlayerStrategy();


  //Parameter grading each set
  private int tripletOrSequence = 0;
  private int tuple = 0;
  //not complete set
  private double lack1Base = 0;
  private double need = 0; //add need * probability of tile needed to the points
  private int single =0; //which should be negative as punishment

  private OnePlayerStrategy(){
    this.tilesInHand = new ArrayList<>();
    this.actions = new ArrayList<>();
    this.unknownTiles = new ArrayList<>();
  }

  public static OnePlayerStrategy getInstance() {
    return s;
  }

  public void setTiles(List<Tile> tilesInHand, List<Action> actions,
                       List<Tile> unknownTiles) {
    this.tilesInHand = tilesInHand;
    this.actions = actions;
    this.unknownTiles = unknownTiles;
  }

  public void setParameter(int tripletOrSequence, int tuple, int lack1Base,
                           int need, int single){
    this.tripletOrSequence = tripletOrSequence;
    this.tuple = tuple;
    this.lack1Base = lack1Base;
    this.need = need;
    this.single = single;
  }


  /**
   * Discard action decided.
   * @return
   */
  public Action discardOnePlayerStategy(){
    Map<TileSuit, int[]> divided = (divideBySuit(tilesInHand));
    Set<TileType> discardChoices = discardChoice(divided);
    if(discardChoices.size() == 1){
      Action result = tileToAction((TileType) discardChoices.toArray()[0]);
      System.out.println("Action: " + result.toString() );
      return result;
    }
    double maxGrade = - Double.MAX_VALUE;
    TileType best = (TileType) discardChoices.toArray()[0];
    for(TileType discard: discardChoices){
      Map<TileSuit, int[]> afterDiscard = inHandDiscard1(divided, discard);
      double grade = 0;
      for (Map.Entry<TileSuit, int[]> entry:afterDiscard.entrySet()){
        grade += maxGrade(entry.getValue(),entry.getKey());
      }
      if(grade > maxGrade) best = discard;
    }
    System.out.println("Action: " + best.toString());
    return tileToAction(best);
  }

  /**
   *
   */
  public double maxGrade(int[] oneSuit, TileSuit suit){
    Set<List<String>> combinations;
    if(suit.equals(TileSuit.ZI)){
      combinations = factory.combinationForWindDragon(oneSuit);
    } else{
      combinations = factory.getCombination(oneSuit);
    }
//    System.out.println(suit+": " + combinations.toString());

    double maxGrade = - Double.MAX_VALUE;
    for(List<String> comb:combinations) {
      Map<String, List<Integer>> gradeHelper = factory.gradeHelper(
              comb,
              oneSuit);
      int grade = 0;
      //complete
      grade += tripletOrSequence * (gradeHelper.get(HandFactory.COMPLETE).get
              (0) + gradeHelper.get(HandFactory.COMPLETE).get(1));
      //deal with 4
      //tuple
      grade += tuple * gradeHelper.get(HandFactory.TUPLE).get(0);
      List<Integer> inCompleteInfo = gradeHelper.get(HandFactory.INCOMPLETE);
      int numSet = inCompleteInfo.get(0);
      grade += lack1Base * numSet;
      inCompleteInfo.remove(0);
      double inCompleteGrade = 0;
      for(Integer incomplete : inCompleteInfo){
        inCompleteGrade += need * ((numUnknowTileType(TileType.of(suit,
                incomplete+1))/ 4.00) + 1);
      }
      grade += inCompleteGrade/numSet;
      for(Integer s: gradeHelper.get(HandFactory.SINGLE)){
        grade += s * ( -1 + single * 2);
      }
      if(grade > maxGrade) maxGrade = grade;
    }
      return maxGrade;
  }


  /**
   * Given a tile type to discard, return a copy of hand after removing
   * exactly 1 that type.
   */
  public Map<TileSuit, int[]> inHandDiscard1(Map<TileSuit,
          int[]> divided, TileType discard) {
    Map<TileSuit, int[]> dividedCopy = new HashMap<>(divided);
    if(discard.isNumberRank()){
      dividedCopy.get(discard.suit())[discard.number()-1] -= 1;
    } else{
      dividedCopy.get(discard.suit())[discard.notNumberIndex()] -= 1;
    }
    return dividedCopy;
  }

  /**
   * Divide tiles into different suit.
   * Map -> key: suit,
   * value: tiles in this suit, use int[9] to represent 1-9 for
   * number suits. For no-number suits, index 0-6 represents DONG_FENG
   * (East), NAN(South), XI(West), BEI(North), ZHONG(Red), FA(Green),
   * BAI(White)
   */
  public Map<TileSuit, int[]> divideBySuit(List<Tile> tiles) {
    int[] windAndDragon = new int[7];
    int[] number = new int[9];
    int[] stick = new int[9];
    int[] ball = new int[9];
    for (Tile tile : tiles) {
      if(!tile.type().isNumberRank()){
        //ignore hua suit
        if(tile.type().suit().equals(TileSuit.ZI)) windAndDragon[tile.type
                ().notNumberIndex()]++;
        continue;
      }
      switch (tile.type().suit()) {
        case WAN:
          number[tile.type().number() - 1]++;
          continue;
        case BING:
          ball[tile.type().number() - 1]++;
          continue;
        case TIAO:
          stick[tile.type().number() - 1]++;
      }
    }
    Map<TileSuit, int[]> map = new HashMap<>();
    map.put(TileSuit.BING, ball);
    map.put(TileSuit.TIAO, stick);
    map.put(TileSuit.WAN, number);
    map.put(TileSuit.ZI, windAndDragon);
    return map;
  }

  /**
   * The number of a tile type unknown.
   * @param tileType
   * @return
   */
  private int numUnknowTileType(TileType tileType){
    Map<TileSuit, int[]> dividedUnknown = divideBySuit(unknownTiles);
    if(tileType.isNumberRank()){
      return dividedUnknown.get(tileType.suit())[tileType.number()-1];
    } else {
      return dividedUnknown.get(tileType.suit())[tileType.notNumberIndex()];
    }
  }

  /**
   * Get tiles that could be discarded.
   * If a tile type is independent:
   * **A** or *1** or 0** or **8 or **7*, discard immediately.
   * Do not discard AAA, AAAA.
   */
  public Set<TileType> discardChoice(Map<TileSuit, int[]> groupedTiles) {
    Set<TileType> discardChoices = new HashSet<>();
    for (TileSuit suit : groupedTiles.keySet()) {
      int[] tilesInSuit = groupedTiles.get(suit);
      for (int i = 0; i < tilesInSuit.length; i++) {
        if (tilesInSuit[i] == 0) continue;
        if (tilesInSuit[i] == 1) {
          //for winds and dragons
          if(suit.equals(TileSuit.ZI)){
            discardChoices.add(TileType.of(suit, i));
            return discardChoices;
          }
          //for independent
          boolean independent = empty(i, 1, tilesInSuit)
                  && empty(i, 2, tilesInSuit)
                  && empty(i,-1,tilesInSuit)
                  && empty(i, -2, tilesInSuit);
          discardChoices.add(TileType.of(suit, i + 1));
          if(independent){
            return discardChoices;
          }
        }
        if (tilesInSuit[i] < 3) {
          if(suit.equals(TileSuit.ZI)){
            discardChoices.add(TileType.of(suit,i));
          } else {
            discardChoices.add(TileType.of(suit, i + 1));
          }
        }
      }
    }
    return discardChoices;
  }

  private boolean empty(int index, int distance, int[] array){
    int newIndex = index + distance;
    return newIndex < 0 || newIndex>=array.length || array[newIndex] == 0;
  }

  /**
   * Decide discarding action from the discarding tile type.
   */
  private Action tileToAction(TileType toDiscard) {
    for (Action action : actions) {
      if (action.getTile().type().compareTo(toDiscard) == 0) {
        return action;
      }
    }
    throw new UnsupportedOperationException("No such action found: discard" +
            toDiscard.toString());
  }

}
