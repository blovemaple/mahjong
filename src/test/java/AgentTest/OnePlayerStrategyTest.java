package AgentTest;

import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.object.TileType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import AgentBot.OnePlayerStrategy;

public class OnePlayerStrategyTest {
  TileType[] numbers = new TileType[9];
  TileType[] sticks = new TileType[9];
  TileType[] balls = new TileType[9];
  TileType[] windsAndDragons = new TileType[7];
  OnePlayerStrategy strategy = OnePlayerStrategy.getInstance();

  @Before
  public void setup(){
    for(int i =0; i<9; i++){
      numbers[i] = TileType.of(TileSuit.WAN,i+1);
      sticks[i] = TileType.of(TileSuit.TIAO, i+1);
      balls[i] = TileType.of(TileSuit.BING,i+1);
      if(i<7){
        windsAndDragons[i] = TileType.of(TileSuit.ZI,i);
      }

    }
  }

  private String arrayToString(int[] array) {
    String s = "";
    for (int i = 0; i < array.length; i++) {
      s += array[i] + " ";
    }
    return s;
  }

  @Test
  public void testConstructTile(){
    TileType threeStick = TileType.of(TileSuit.TIAO,3);
    Assert.assertEquals(threeStick.toString(),"SAN TIAO");
  }

  @Test
  public void testDivideBySuit(){
    List<Tile> tiles = new ArrayList<>();
    tiles.add(Tile.of(numbers[0],1));
    tiles.add(Tile.of(numbers[0],2));
    tiles.add(Tile.of(sticks[2],2));
    tiles.add(Tile.of(balls[3],1));
    tiles.add(Tile.of(windsAndDragons[2],2));
    tiles.add(Tile.of(TileType.of(TileSuit.HUA, TileRank.HuaRank.CHUN),0));

    System.out.println(tiles.toString());
    Map<TileSuit, int[]> divided = strategy.divideBySuit(tiles);
    divided.forEach((suit,set) -> System.out.print(suit
            + ": "+ arrayToString(set)));

    //test discard 1
    Map<TileSuit, int[]> afterDiscard = strategy.inHandDiscard1(divided,
            windsAndDragons[2]);
    System.out.println();
    afterDiscard.forEach((suit,set) -> System.out.print(suit
            + ": "+ arrayToString(set)));
  }


  @Test
  public void testDiscardChoices(){
    Map<TileSuit, int[]> hand = new HashMap<>();
    Map<TileSuit, int[]> hand1 = new HashMap<>();
    Map<TileSuit, int[]> hand2 = new HashMap<>();
//round 1
    int[] windsAndDragons = {0,1,1,1,0,0,0};
    int[] balls = {1,0,0,0,1,0,0,1,0};
    int[] sticks = {0,1,3,0,1,1,1,0,1};
    hand.put(TileSuit.ZI, windsAndDragons);
    hand1.put(TileSuit.BING, balls);
    hand2.put(TileSuit.TIAO,sticks);
    System.out.println(strategy.discardChoice(hand));
    System.out.println(strategy.discardChoice(hand1));
    System.out.println(strategy.discardChoice(hand2));
//round 2
    balls[0] = 0;
    System.out.println(strategy.discardChoice(hand1));
    sticks[1] = 0;
    System.out.println(strategy.discardChoice(hand2));
    windsAndDragons[1] = 2;
    System.out.println(strategy.discardChoice(hand));
  }


}
