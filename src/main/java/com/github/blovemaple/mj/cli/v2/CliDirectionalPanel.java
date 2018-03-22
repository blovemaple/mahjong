package com.github.blovemaple.mj.cli.v2;

import static com.github.blovemaple.mj.cli.framework.layout.CliFlowLayout.CliFlowLayoutDirection.*;

import com.github.blovemaple.mj.cli.framework.component.CliPanel;
import com.github.blovemaple.mj.cli.framework.layout.CliFlowLayout;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliDirectionalPanel extends CliPanel {
	private final CliViewDirection direction;

	public CliDirectionalPanel(CliViewDirection direction) {
		this.direction = direction;

		CliFlowLayout layout = new CliFlowLayout();
		setLayout(layout);

		switch (direction) {
		case UNDER:
			layout.setDirection(TOP_LEFT_HORIZONTAL);
			break;
		case RIGHT:
			layout.setDirection(BOTTOM_LEFT_VERTICAL);
			break;
		case UPPER:
			layout.setDirection(BOTTOM_RIGHT_HORIZONTAL);
			break;
		case LEFT:
			layout.setDirection(TOP_RIGHT_VERTICAL);
			break;
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public CliFlowLayout getLayout() {
		return (CliFlowLayout) super.getLayout();
	}

	public CliViewDirection getDirection() {
		return direction;
	}

}
