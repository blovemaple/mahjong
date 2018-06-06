package com.github.blovemaple.mj.cli.framework.component;

import java.io.IOException;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntSupplier;

import com.github.blovemaple.mj.cli.ansi.AnsiColor;
import com.github.blovemaple.mj.cli.framework.layout.CliFlowLayout;
import com.github.blovemaple.mj.cli.framework.layout.CliFlowLayout.CliFlowLayoutDirection;

import jline.console.ConsoleReader;

/**
 * 命令行界面根面板。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public final class CliRootPanel extends CliPanel {
	public static void main(String[] args) throws IOException {
		CliRootPanel rootPanel = CliRootPanel.get();
		rootPanel.setBackground(AnsiColor.CYAN);

		CliPanel panel = new CliPanel();
		CliFlowLayout layout = new CliFlowLayout(CliFlowLayoutDirection.BOTTOM_RIGHT_VERTICAL);
		layout.setColumnGap(3);
		layout.setRowGap(5);
		// layout.setLineSize(2);
		panel.setLayout(layout);
		panel.setBackground(AnsiColor.BLUE);
//		panel.setTop(() -> 1);
//		panel.setLeft(() -> 1);
//		rootPanel.addChild(panel);
//
//		CliLabel label1 = new CliLabel("World一二三Haha1");
//		label1.setBackground(AnsiColor.GREEN);
//		label1.setWidth(() -> 1);
//		panel.addChild(label1);
//
//		CliLabel label2 = new CliLabel("World一二三Haha2");
//		label2.setForeground(AnsiColor.RED);
//		label2.setBackground(AnsiColor.GREEN);
//		label2.setWidth(() -> 5);
//		label2.setTop(() -> label1.getBottom(panel).orElse(0) + 3);
//		panel.addChild(label2);
//
//		CliLabel label3 = new CliLabel("World一二三Haha3");
//		label3.setBackground(AnsiColor.GREEN);
//		label3.setWidth(() -> 3);
//		label3.setTop(() -> label2.getBottom(panel).orElse(0) + 3);
//		panel.addChild(label3);
//
//		rootPanel.repaint();
//		System.out.println(rootPanel.getWidth(null));
//		System.out.println(rootPanel.getHeight(null));
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

	private final ConsoleReader console;

	private CliRootPanel() throws IOException {
		console = new ConsoleReader(null, System.in, System.out, null);
		setBackground(AnsiColor.DEFAULT);
	}

	@Override
	public int getDefaultWidth(int height) {
		return getWidth();
	}

	@Override
	public int getDefaultHeight(int width) {
		return getHeight();
	}

	public int getWidth() {
		return console.getTerminal().getWidth();
		// return 100;
	}

	public int getHeight() {
		return console.getTerminal().getHeight();
		// return 50;
	}

	@Override
	public void setMinWidth(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPrefWidth(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxWidth(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMinHeight(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPrefHeight(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxHeight(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFreeLeft(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFreeRight(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFreeTop(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFreeBottom(IntSupplier value) {
		throw new UnsupportedOperationException();
	}

}
