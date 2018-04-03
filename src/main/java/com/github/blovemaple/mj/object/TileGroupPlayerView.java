package com.github.blovemaple.mj.object;

import java.util.Set;

import com.github.blovemaple.mj.object.PlayerLocation.Relation;

/**
 * {@link TileGroup}接口。作为给其他玩家的视图时，隐藏暗杠的牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface TileGroupPlayerView {

	/**
	 * 返回类型。
	 * 
	 * @return 类型
	 */
	public TileGroupType getType();

	/**
	 * 返回牌组中所有牌。类型为暗杠时返回null。
	 * 
	 * @return tiles 集合
	 */
	public Set<Tile> getTilesView();

	/**
	 * 返回得牌来自于哪个关系的玩家。
	 * 
	 * @return 玩家位置
	 */
	public Relation getFromRelation();

	/**
	 * 返回得牌。类型为暗杠时返回null。
	 * 
	 * @return 得牌
	 */
	public Tile getGotTileView();

}
