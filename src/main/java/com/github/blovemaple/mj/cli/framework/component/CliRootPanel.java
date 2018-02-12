package com.github.blovemaple.mj.cli.framework.component;

import java.io.IOException;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntSupplier;

import com.github.blovemaple.mj.cli.ansi.AnsiColor;
import com.github.blovemaple.mj.cli.framework.layout.CliFlowLayout;
import com.github.blovemaple.mj.cli.framework.layout.CliFlowLayout.CliPanelFlowLayoutDirection;

/**
 * 命令行界面根面板。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class CliRootPanel extends CliPanel {
	public static void main(String[] args) throws IOException {
		CliRootPanel rootPanel = CliRootPanel.get();
		rootPanel.setBackground(AnsiColor.CYAN);

		CliPanel panel = new CliPanel();
		CliFlowLayout layout = new CliFlowLayout(CliPanelFlowLayoutDirection.BOTTOM_LEFT_VERTICAL);
		layout.setColumnGap(3);
		layout.setRowGap(5);
		layout.setLineSize(2);
		panel.setLayout(layout);
		panel.setBackground(AnsiColor.BLUE);
		panel.setTopBySelf(() -> 1);
		panel.setLeftBySelf(() -> 1);
		rootPanel.addChild(panel);

		CliLabel label1 = new CliLabel("World一二三Haha1");
		label1.setBackground(AnsiColor.GREEN);
		label1.setWidthBySelf(() -> 1);
		panel.addChild(label1);

		CliLabel label2 = new CliLabel("World一二三Haha2");
		label2.setForeground(AnsiColor.RED);
		label2.setBackground(AnsiColor.GREEN);
		label2.setWidthBySelf(() -> 5);
		label2.setTopBySelf(() -> label1.getBottom(panel).orElse(0) + 3);
		panel.addChild(label2);

		CliLabel label3 = new CliLabel("World一二三Haha3");
		label3.setBackground(AnsiColor.GREEN);
		label3.setWidthBySelf(() -> 3);
		label3.setTopBySelf(() -> label2.getBottom(panel).orElse(0) + 3);
		panel.addChild(label3);

		rootPanel.repaint();
		// System.out.println(label2.getTop(panel));
		// System.out.println(label2.getLeft(panel));
		// System.out.println(label2.getHeight(panel));
		// System.out.println(label2.getWidth(panel));
	}

	private static CliRootPanel i;

	/**
	 * 返回单例。
	 * 
	 * @throws IOException
	 */
	public static synchronized CliRootPanel get() throws IOException {
		if (i == null)
			i = new CliRootPanel();
		return i;
	}

	// private final ConsoleReader console;

	private CliRootPanel() throws IOException {
		// console = new ConsoleReader(null, System.in, System.out, null);
		setBackground(AnsiColor.DEFAULT);
	}

	@Override
	protected void setDefaultWidthAndHeight() {
	}

	@Override
	public OptionalInt getWidth(CliComponent parent) {
		// return OptionalInt.of(console.getTerminal().getWidth());
		return OptionalInt.of(100);
	}

	@Override
	public OptionalInt getHeight(CliComponent parent) {
		// return OptionalInt.of(console.getTerminal().getHeight());
		return OptionalInt.of(50);
	}

	@Override
	public OptionalInt getLeft(CliComponent parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OptionalInt getRight(CliComponent parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OptionalInt getTop(CliComponent parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OptionalInt getBottom(CliComponent parent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWidthBySelf(IntSupplier widthBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHeightBySelf(IntSupplier heightBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLeftBySelf(IntSupplier leftBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRightBySelf(IntSupplier rightBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTopBySelf(IntSupplier topBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBottomBySelf(IntSupplier bottomBySelf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setWidthWithParent(Function<CliComponent, Integer> widthWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHeightWithParent(Function<CliComponent, Integer> heightWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLeftWithParent(Function<CliComponent, Integer> leftWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRightWithParent(Function<CliComponent, Integer> rightWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTopWithParent(Function<CliComponent, Integer> topWithParent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setBottomWithParent(Function<CliComponent, Integer> bottomWithParent) {
		throw new UnsupportedOperationException();
	}

}
