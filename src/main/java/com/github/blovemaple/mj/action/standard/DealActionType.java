package com.github.blovemaple.mj.action.standard;

import java.util.stream.Stream;

import com.github.blovemaple.mj.action.Action;
import com.github.blovemaple.mj.action.AutoActionType;
import com.github.blovemaple.mj.action.IllegalActionException;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.PlayerLocation.Relation;

/**
 * 动作类型“发牌”。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DealActionType implements AutoActionType {

	protected DealActionType() {
	}

	@Override
	public boolean isLegalAction(GameContext context, Action action) {
		return context.getDoneActions().stream().map(Action::getType).noneMatch(this::matchBy);
	}

	@Override
	public void doAction(GameContext context, Action action) throws IllegalActionException {
		MahjongTable table = context.getTable();
		PlayerLocation zhuang = context.getZhuangLocation();
		for (int i = 0; i < 4; i++) {
			int drawCount = i < 3 ? 4 : 1;
			Stream.of(Relation.values()).map(zhuang::getLocationOf)
					.map(context::getPlayerInfoByLocation)
					.map(PlayerInfo::getAliveTiles)
					.forEach(aliveTiles -> aliveTiles
							.addAll(table.draw(drawCount)));
		}
		context.getPlayerInfoByLocation(zhuang).getAliveTiles()
				.addAll(table.draw(1));
	}

}
