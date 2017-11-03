package com.github.blovemaple.mj.rule.fan;

import static com.github.blovemaple.mj.rule.fan.FanTestUtils.*;

import org.junit.runner.RunWith;

import com.github.blovemaple.mj.rule.simple.NormalWinType;
import com.github.blovemaple.mj.rule.win.WinInfo;
import com.github.blovemaple.mj.rule.win.load.FanTypeLoader;

/**
 * 用FanTestRunner跑番种测试。用@FanTestBaseName指定番种文件。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
@RunWith(FanTestRunner.class)
@FanTestBaseName("guobiao")
public class FanTest {

	/**
	 * 临时进行单个测试用。
	 */
	public static void main(String[] args) {
		String tiles = "ZZZZZZ ZFZFZF ZBZBZB B9B9B9 T1T1";
		WinInfo winInfo = parseWinInfo(tiles);

		NormalWinType.get().parseWinTileUnits(winInfo);
		int c = FanTypeLoader.parseMatcher("4[KG]0").matchCount(winInfo);
		System.out.println(c);
	}

}
