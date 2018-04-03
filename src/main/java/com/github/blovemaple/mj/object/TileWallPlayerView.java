package com.github.blovemaple.mj.object;

import java.util.List;

/**
 * TileWall给玩家的视图。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface TileWallPlayerView {

	int getRemainTileCount();

	public List<? extends TileWallPilePlayerView> getPiles(PlayerLocation location);
}
