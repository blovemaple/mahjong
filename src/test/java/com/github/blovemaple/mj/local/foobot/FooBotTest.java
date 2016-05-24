package com.github.blovemaple.mj.local.foobot;

import static com.github.blovemaple.mj.action.standard.StandardActionType.*;
import static com.github.blovemaple.mj.object.PlayerLocation.*;
import static com.github.blovemaple.mj.object.TileRank.NumberRank.*;
import static com.github.blovemaple.mj.object.TileRank.ZiRank.*;
import static com.github.blovemaple.mj.object.TileSuit.*;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.cli.CliRunner;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.local.barbot.BarBot;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;
import com.github.blovemaple.mj.rule.simple.SimpleGameStrategy;

public class FooBotTest {

	// private static FooBot bot = new FooBot("Foo");
	private static BarBot bot = new BarBot("Bar");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// 让日志输出到文件
		LogManager.getLogManager().readConfiguration(CliRunner.class.getResource("/logging.properties").openStream());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private GameContext context;

	@Before
	public void setUp() throws Exception {
		MahjongTable table = new MahjongTable();
		table.init();
		table.readyForGame(Tile.all());
		table.draw(53);

		PlayerInfo selfInfo = table.getPlayerInfos().get(EAST);
		selfInfo.setPlayer(bot);
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, SAN), 0));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, SAN), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, SI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, LIU), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, QI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, SI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, LIU), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, QI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, SI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, LIU), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, QI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, SI), 2));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, LIU), 2));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, QI), 2));
		// selfInfo.setLastDrawedTile(Tile.of(TileType.of(WAN, SAN), 0));

		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, YI), 0));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, SI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, QI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, YI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, SI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, QI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, YI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, SI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, QI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, DONG_FENG), 2));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, NAN), 2));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, BEI), 2));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, XI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, ZHONG), 1));
		// selfInfo.setLastDrawedTile(Tile.of(TileType.of(WAN, YI), 0));

		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, YI), 0));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, SI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, BA), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, ER), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, JIU), 1));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, SI), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, SI), 1));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, WU), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, JIU), 0));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, NAN), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, NAN), 2));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, XI), 1));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, BEI), 2));
		// selfInfo.getAliveTiles().add(Tile.of(TileType.of(ZI, BAI), 3));
		selfInfo.setLastDrawedTile(Tile.of(TileType.of(BING, SI), 1));

		context = new GameContext(table, new SimpleGameStrategy(), TimeLimitStrategy.NO_LIMIT);
		context.actionDone(new Action(DISCARD, Tile.of(TileType.of(BING, SI), 2)), SOUTH);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testChooseAction() throws InterruptedException {
		Set<ActionType> actionTypes = new HashSet<>();
		actionTypes.add(PENG);
		Action action = bot.chooseAction(context.getPlayerView(EAST), actionTypes);
		System.out.println(action);
	}

}
