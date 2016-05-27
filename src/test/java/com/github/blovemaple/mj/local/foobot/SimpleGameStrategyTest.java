package com.github.blovemaple.mj.local.foobot;

import static com.github.blovemaple.mj.object.TileRank.NumberRank.*;
import static com.github.blovemaple.mj.object.TileSuit.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.simple.SimpleGameStrategy;

public class SimpleGameStrategyTest {
	private SimpleGameStrategy strategy = new SimpleGameStrategy();
	private PlayerInfo selfInfo;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		selfInfo = new PlayerInfo();

		selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, YI), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, YI), 1));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, ER), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, SAN), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, SI), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, YI), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, YI), 1));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, YI), 2));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		// strategy.getFans(selfInfo, null).forEach((a, b) -> System.out.println(a + " " + b));
	}

}
