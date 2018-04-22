package AgentTest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import AgentBot.HandFactory;

public class HandFactoryTest {
  private HandFactory factory = HandFactory.getHandFactory();

  @Test
  public void testGetCombination(){
    int[] hand = {0,3,3,3,0,0,0,0,0};
    System.out.println(factory.getCombination
            (hand).toString());
  }

  @Test
  public void testCombWindsAndDragon(){
    int[] windsAndDragons = {0,1,2,0,3,0,0};
    System.out.println(factory.combinationForWindDragon(windsAndDragons));
  }

  @Test
  public void testGradeHelper(){
    List<String> comb = new ArrayList<>();
    int[] array = {1,1,3,0,4,4,1,1,2};
    comb.add("01");
    comb.add("22");
    comb.add("24");
    comb.add("444");
    comb.add("5555");
    comb.add("678");
    comb.add("8");
    Map<String, List<Integer>> helper = factory.gradeHelper(comb,array);
    helper.forEach((key,value) -> System.out.println(key +": "+ value.toString()));
  }
}
