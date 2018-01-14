package com.github.blovemaple.mj.cli.ansi;

import java.util.Arrays;

/**
 * 生成CSI序列的工具。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class Csi {

	private static final char ESC_CHAR = (char) 0x1B;

	public static String of(CsiFinalByte finalByte, int... params) {
		return "" + ESC_CHAR + '[' //
				+ String.join(";", Arrays.stream(params).mapToObj(String::valueOf).toArray(String[]::new)) //
				+ finalByte.get();
	}

	public static String ed() {
		return of(CsiFinalByte.ED);
	}

	public static String cup(int x, int y) {
		return of(CsiFinalByte.CUP, x, y);
	}

	public static String sgr(SgrParam... params) {
		return of(CsiFinalByte.SGR, Arrays.stream(params).mapToInt(SgrParam::get).toArray());
	}

	public static void main(String[] args) {
		System.out.println(
				Csi.of(CsiFinalByte.SGR, SgrParam.CROSSED_OUT.get()) + "ABC" + Csi.of(CsiFinalByte.SGR) + "DEF");
	}

	private Csi() {
	}
}
