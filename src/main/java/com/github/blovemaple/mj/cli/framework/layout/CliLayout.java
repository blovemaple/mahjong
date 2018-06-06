package com.github.blovemaple.mj.cli.framework.layout;

import java.util.Map;

import com.github.blovemaple.mj.cli.framework.component.CliComponent;
import com.github.blovemaple.mj.cli.framework.component.CliPanel;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface CliLayout {
	/**
	 * 根据指定的父组件，返回其所有子组件的尺寸和位置。
	 * 
	 * @param parent
	 *            父组件
	 * @param parentWidth
	 *            父组件宽度，-1表示按首选宽度
	 * @param parentHeight
	 *            父组件高度，-1表示按首选高度
	 * @return 子组件 - 尺寸和位置
	 */
	Map<CliComponent, CliBound> layout(CliPanel parent, int parentWidth, int parentHeight);
}
