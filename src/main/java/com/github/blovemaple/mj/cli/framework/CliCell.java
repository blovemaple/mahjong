package com.github.blovemaple.mj.cli.framework;

import static com.github.blovemaple.mj.cli.ansi.AnsiColor.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.github.blovemaple.mj.cli.ansi.AnsiColor;
import com.github.blovemaple.mj.cli.ansi.SgrParam;
import com.github.blovemaple.mj.cli.framework.component.CliComponent;
import com.github.blovemaple.mj.utils.MyUtils;

/**
 * 命令行界面单元格，即一个字符的位置。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliCell implements Cloneable {
	private final List<CliComponent> ownerChain = new ArrayList<>();
	private AnsiColor foreground = DEFAULT, background = null;
	private char text = ' ';

	public CliCell(CliComponent finalOwder) {
		ownerChain.add(finalOwder);
	}

	public void addOwner(CliComponent owner) {
		ownerChain.add(owner);
	}

	public AnsiColor getForeground() {
		return foreground;
	}

	public void setForeground(AnsiColor foreground) {
		this.foreground = foreground;
	}

	public AnsiColor getBackground() {
		return background;
	}

	public void setBackground(AnsiColor background) {
		this.background = background;
	}

	public char getText() {
		return text;
	}

	public void setText(char text) {
		this.text = text;
	}

	public int getTextWidth() {
		return MyUtils.strWidth(String.valueOf(text));
	}

	public SgrParam[] getSgrParams() {
		return new SgrParam[] { foreground.getFgParam(), background.getBgParam() };
	}

	public boolean equalByContent(CliCell other) {
		if (other == null)
			return false;
		if (background != other.background)
			return false;
		if (foreground != other.foreground)
			return false;
		if (text != other.text)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((background == null) ? 0 : background.hashCode());
		result = prime * result + ((foreground == null) ? 0 : foreground.hashCode());
		result = prime * result + text;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CliCell other = (CliCell) obj;
		if (background != other.background)
			return false;
		if (foreground != other.foreground)
			return false;
		if (text != other.text)
			return false;
		return true;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
