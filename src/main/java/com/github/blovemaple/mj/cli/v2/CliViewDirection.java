package com.github.blovemaple.mj.cli.v2;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum CliViewDirection {
	DOWN, RIGHT, UP, LEFT;

	public CliViewDirection next() {
		return values()[(this.ordinal() + 1) % 4];
	}
}
