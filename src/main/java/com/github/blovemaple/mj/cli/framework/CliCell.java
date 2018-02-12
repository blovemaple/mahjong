package com.github.blovemaple.mj.cli.framework;

import static com.github.blovemaple.mj.cli.ansi.AnsiColor.*;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.github.blovemaple.mj.cli.ansi.AnsiColor;
import com.github.blovemaple.mj.cli.ansi.SgrParam;

/**
 * 命令行界面单元格，即一个字符的位置。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliCell implements Cloneable {
	private AnsiColor foreground = DEFAULT, background = null;
	private char text = ' ';
	/**
	 * 由于汉字占两格，每个汉字后的单元格使用占位符，以使行内单元格数与行宽匹配。
	 */
	private boolean isPlaceholder;

	public CliCell() {
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

	public boolean isPlaceholder() {
		return isPlaceholder;
	}

	public void setPlaceholder(boolean isPlaceholder) {
		this.isPlaceholder = isPlaceholder;
	}

	public SgrParam[] getSgrParams() {
		return new SgrParam[] { foreground.getFgParam(), background.getBgParam() };
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
