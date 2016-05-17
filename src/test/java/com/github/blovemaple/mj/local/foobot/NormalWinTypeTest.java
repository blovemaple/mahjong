package com.github.blovemaple.mj.local.foobot;

import static com.github.blovemaple.mj.object.TileRank.NumberRank.*;
import static com.github.blovemaple.mj.object.TileSuit.*;

import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.simple.NormalWinType;

public class NormalWinTypeTest {
	private NormalWinType winType = new NormalWinType();
	private PlayerInfo selfInfo;
	private Collection<Tile> candidates;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		candidates = Tile.all();

		selfInfo = new PlayerInfo();
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, SAN), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, SAN), 1));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, SI), 1));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(WAN, LIU), 1));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, SI), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, LIU), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(TIAO, QI), 0));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, SI), 1));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, LIU), 1));
		selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, QI), 1));
		 selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, SI), 2));
		 selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, LIU), 2));
		 selfInfo.getAliveTiles().add(Tile.of(TileType.of(BING, QI), 2));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		winType.changingsForWin(selfInfo, 0, candidates).close();
	}

}
