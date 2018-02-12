package com.github.blovemaple.mj.cli.framework.layout;

import java.util.Map;

import com.github.blovemaple.mj.cli.framework.component.CliComponent;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface CliLayout {
	/**
	 * 根据指定的父组件，返回其所有子组件的尺寸和位置。
	 */
	Map<CliComponent, CliLayoutSetting> layout(CliComponent parent);
}
