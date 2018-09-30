package com.github.blovemaple.mj.action;

import com.github.blovemaple.mj.action.standard.StageSwitchActionType;

/**
 * 切换阶段的动作。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class StageSwitchAction extends Action {

	private final String nextStageName;

	public StageSwitchAction(String nextStageName) {
		super(StageSwitchActionType.INSTANCE);
		this.nextStageName = nextStageName;
	}

	public String getNextStageName() {
		return nextStageName;
	}

}
