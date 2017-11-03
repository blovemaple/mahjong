package com.github.blovemaple.mj.rule.win.load;

import java.util.List;

import com.github.blovemaple.mj.rule.win.FanType;
import com.github.blovemaple.mj.rule.win.WinType;

/**
 * 番种和和牌类型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class WinAndFanTypes {
	private String baseName;
	private List<FanType> fanTypes;
	private List<WinType> winTypes;

	public WinAndFanTypes(String baseName, List<FanType> fanTypes, List<WinType> winTypes) {
		this.baseName = baseName;
		this.fanTypes = fanTypes;
		this.winTypes = winTypes;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public List<FanType> getFanTypes() {
		return fanTypes;
	}

	public void setFanTypes(List<FanType> fanTypes) {
		this.fanTypes = fanTypes;
	}

	public List<WinType> getWinTypes() {
		return winTypes;
	}

	public void setWinTypes(List<WinType> winTypes) {
		this.winTypes = winTypes;
	}

}
