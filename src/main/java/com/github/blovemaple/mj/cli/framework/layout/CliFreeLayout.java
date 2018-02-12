package com.github.blovemaple.mj.cli.framework.layout;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Map;

import com.github.blovemaple.mj.cli.framework.component.CliComponent;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliFreeLayout implements CliLayout {

	@Override
	public Map<CliComponent, CliLayoutSetting> layout(CliComponent parent) {
		return parent.getChildren().stream().collect(toMap(identity(), child -> {
			CliLayoutSetting setting = new CliLayoutSetting();
			Arrays.stream(CliLayoutSettingType.values())
					.forEach(type -> setting.set(type, child.get(type, parent).orElse(0)));
			setting.validate();
			return setting;
		}));
	}

}
