package com.github.blovemaple.mj.rule.fan;

import static com.github.blovemaple.mj.rule.fan.FanTestUtils.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.blovemaple.mj.cli.CliRunner;
import com.github.blovemaple.mj.rule.simple.NormalWinType;
import com.github.blovemaple.mj.rule.win.FanType;
import com.github.blovemaple.mj.rule.win.WinInfo;
import com.github.blovemaple.mj.rule.win.WinType;
import com.github.blovemaple.mj.rule.win.load.FanTypeLoader;

public class FanTypeLoaderTest {

	public static void main(String[] args) {
		System.out.println(Arrays.asList("".split(",")).size());
	}

	private final WinType normalWinType = NormalWinType.get();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		LogManager.getLogManager().readConfiguration(CliRunner.class.getResource("/logging.properties").openStream());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		String tiles = "T1T2T3T1T2T3T7T8T9T7T8T9T5T5";
		WinInfo winInfo = parseWinInfo(tiles);

		List<FanType> fanTypes = FanTypeLoader.loadFanTypes("guobiao").getFanTypes();
		FanType.getFans(winInfo, fanTypes, Collections.singleton(normalWinType))
				.forEach((fanType, value) -> System.out.println(fanType.name() + " " + value));

		assertEquals(0, 0);
	}

	@Test
	public void testYSSLH() throws SecurityException, IOException {
		String tiles = "T1T2T3T1T2T3T7T8T9T7T8T9T5T5";
		WinInfo winInfo = parseWinInfo(tiles);

		int c = FanTypeLoader.parseMatcher("~@a#?/!Z#?").matchCount(winInfo);
		System.out.println(c);
	}

}
