package com.github.blovemaple.mj.rule.win;

/**
 * 番种。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface FanType {
	/**
	 * 返回番种的名称，作为标识。<br>
	 * 默认为类的SimpleName，全大写。
	 */
	public default String name() {
		return this.getClass().getSimpleName().toUpperCase();
	}

	/**
	 * 检查和牌是否符合此番种。
	 */
	public boolean match(WinInfo winInfo);
	
}
