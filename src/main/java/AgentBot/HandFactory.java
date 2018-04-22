package AgentBot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deal with one suit combinations.
 */
public class HandFactory {
  private Map<int[], Set<List<String>>> combinationCache;
  private static HandFactory handFactory = new HandFactory();
  public final static String COMPLETE = "complete";
  public final static String TUPLE = "tuple";
  public final static String INCOMPLETE = "incomplete";
  public final static String SINGLE = "single";


  public static HandFactory getHandFactory() {
    return handFactory;
  }

  private HandFactory() {
    this.combinationCache = new HashMap<>(); //todo: may load from some text file.
  }

  public Set<List<String>> getCombination(int[] inHand) {
    if (combinationCache.containsKey(inHand)) return combinationCache.get
            (inHand);
    Set<List<String>> result = new HashSet<>();
    for (int i = 0; i < inHand.length; i++) {
      if (inHand[i] < 1) continue;
      //possibility of A AA AAA
      for (int j = 1; j <= inHand[i]; j++) {
        if (j == 1) {
          String cSet = "";
          int[] copyHand = copyArray(inHand);

          for (int seq = i; seq <= i + 2; seq++) {
            if (seq >= inHand.length || copyHand[seq] < 1) continue;
            cSet += seq;
            copyHand[seq] -= 1;
          }

//            System.out.println("i: " + i + " j: " + j+"inhand: " + arrayToString
//                    (inHand));

          Set<List<String>> a = getCombination(copyHand);
          Set<List<String>> newA = new HashSet<>();
          for (List<String> s : a) {
            s.add(cSet);
            s.sort(Comparator.naturalOrder());
            newA.add(s);
          }
          result.addAll(newA);
        }

        String currentSet = "";
        int[] copyOfInHand = copyArray(inHand);
        for (int t = 0; t < j; t++) {
          currentSet += i;
        }

        copyOfInHand[i] -= j;
        Set<List<String>> after = getCombination(copyOfInHand);
        Set<List<String>> newAfter = new HashSet<>();
        for (List<String> s : after) {
          s.add(currentSet);
          s.sort(Comparator.naturalOrder());
          newAfter.add(s);
        }
        result.addAll(newAfter);
      }
    }
    if (result.isEmpty()) result.add(new ArrayList<>());
    int min = result.stream().map(set -> set.size()).min(Comparator
            .naturalOrder()).get();
    Set<List<String>> minSets = result.stream().filter(set -> set.size() == min)
            .collect(Collectors
                    .toSet());
    combinationCache.put(inHand, minSets);
    return minSets;
  }


  /**
   * Get all combinations for winds and dragons.
   */
  public Set<List<String>> combinationForWindDragon(int[] suit) {
    if(suit.length != 7) throw new IllegalArgumentException("Not wind and "
            + "dragon suit length");
    //Dragon and suits.
    Set<List<String>> combinations = new HashSet<>();
      List<String> element = new ArrayList<>();
      for (int i = 0; i < suit.length; i++) {
        String s = "";
        for (int j = 0; j < suit[i]; j++) {
          s += i;
        }
        if(s!=""){
          element.add(s);
        }
      }
      combinations.add(element);
      return combinations;
  }


  public int singleDistance(int single, int[] hand) {
    int i = 0;
    boolean end = false;
    if(single<0 || single>=hand.length) throw new IllegalArgumentException
            ("Single tile is not valid" + single);
    while (!end) {
      boolean toRight = (single + i) >= hand.length;
      boolean toLeft = (single - i) < 0;
      if(toRight && toLeft) break;
      if (!toRight && hand[single + i] != 0) {
        return i;
      }
      if (!toLeft && hand[single - i] != 0) {
        return i;
      }
      i++;
    }
    return Math.min(single, hand.length - single);
  }

  /**
   * Map stores for one combination：
   * Complete: hashSet with 2 elements: number of complete sets(size 4)
   *                                    number of complete sets(size 3)
   * Incomplete: 0:number of incomplete sets
   *             1+: all the indexes of tiles waiting for
   * tuple: hashSet with 1 element: number of tuples
   * single: minimum distances for all the single tile
   */
  public Map<String, List<Integer>> gradeHelper(List<String> comb, int[]
          inHand) {
    Map<String, List<Integer>> map = new HashMap<>();
    map.put(COMPLETE, new ArrayList<>());
    map.put(INCOMPLETE, new ArrayList<>());
    map.put(TUPLE, new ArrayList<>());
    map.put(SINGLE, new ArrayList<>());
    int fourNum = 0;
    int completeNum = 0;
    int incompleteNum = 0;
    int tupleNum = 0;
    for (String s : comb) {
      if(s.trim().length() == 4){
        fourNum ++;
      }
      if (s.trim().length() == 3) {
        completeNum ++;
      } else if (s.trim().length() == 1) {
        map.get(SINGLE).add(singleDistance(Integer.parseInt(s.trim()), inHand));
      } else if (s.trim().length() == 2) {
        IncompleteGroup incomplete = new IncompleteGroup(s.trim().toCharArray());
        if (incomplete.isTuple()) {
          tupleNum++;
        } else {
          incompleteNum ++ ;
          map.get(INCOMPLETE).addAll(incomplete.want());
        }
      }
    }
    map.get(INCOMPLETE).add(0, incompleteNum);
    map.get(COMPLETE).add(fourNum);
    map.get(COMPLETE).add(completeNum);
    map.get(TUPLE).add(tupleNum);
    return map;
  }

  private String arrayToString(int[] array) {
    String s = "";
    for (int i = 0; i < array.length; i++) {
      s += array[i] + " ";
    }
    return s;
  }

  private int[] copyArray(int[] array) {
    int[] s = new int[array.length];
    for (int i = 0; i < array.length; i++) {
      s[i] = array[i];
    }
    return s;
  }

  /**
   * 2 tiles that lack 1 can be a complete set.
   * AB, AC, AA
   */
  class IncompleteGroup {
    int[] incomplete;

    public IncompleteGroup(char[] incompleteChar) {
      int[] incomplete = new int[incompleteChar.length];
      for (int i = 0; i < incompleteChar.length; i++) {
        incomplete[i] = incompleteChar[i] - '0';
      }
      if (incomplete.length != 2 || incomplete[0] < 0 || incomplete[0] > 8 ||
              incomplete[1] < 0 || incomplete[1] > 8)
        throw new IllegalArgumentException("cannot be incomplete set");
      this.incomplete = incomplete;
    }


    /**
     * The wanted tiles that can make this set be complete.
     */
    public Set<Integer> want() {
      Set<Integer> wanted = new HashSet<>();
      if (incomplete[0] == incomplete[1]) {
        wanted.add(incomplete[0]);
      } else if (Math.abs(incomplete[0] - incomplete[1]) == 1) {
        int left = Math.min(incomplete[0], incomplete[1]);
        if (left - 1 >= 0) wanted.add(left - 1);
        if (left + 2 <= 8) wanted.add(left + 2);
      } else if (Math.abs(incomplete[0] - incomplete[1]) == 2) {
        wanted.add(Math.min(incomplete[0], incomplete[1]) + 1);
      }
      return wanted;
    }

    public boolean isTuple() {
      return incomplete[0] == incomplete[1];
    }
  }
}
