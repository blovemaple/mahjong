package com.github.blovemaple.mj.cli.framework.layout;

import static com.github.blovemaple.mj.cli.framework.layout.CliBoundField.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.Map;

import com.github.blovemaple.mj.cli.framework.component.CliComponent;
import com.github.blovemaple.mj.cli.framework.component.CliPanel;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliFreeLayout implements CliLayout {

	@Override
	public Map<CliComponent, CliBound> layout(CliPanel parent, int parentWidth, int parentHeight) {
		return parent.getChildren().stream().collect(toMap(identity(), child -> {
			CliBound bound = new CliBound();
			bound.set(TOP, child.getFreePosition(TOP));
			bound.set(LEFT, child.getFreePosition(LEFT));
			bound.set(WIDTH, child.getPrefSize(WIDTH, CliComponent.PREF_VALUE));
			bound.set(HEIGHT, child.getPrefSize(HEIGHT, CliComponent.PREF_VALUE));
			return bound;
		}));
	}

}
