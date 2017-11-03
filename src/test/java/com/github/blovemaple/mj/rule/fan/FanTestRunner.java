package com.github.blovemaple.mj.rule.fan;

import static com.github.blovemaple.mj.utils.LambdaUtils.*;
import static java.util.function.Function.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import com.github.blovemaple.mj.rule.win.FanType;
import com.github.blovemaple.mj.rule.win.WinInfo;
import com.github.blovemaple.mj.rule.win.load.FanTypeLoader;
import com.github.blovemaple.mj.rule.win.load.WinAndFanTypes;

/**
 * 番种测试。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FanTestRunner extends Runner {
	private Class<?> testClass;
	private Description rootDesc;
	private Map<Description, TestCase> tests;

	private static class TestCase {
		private WinInfo winInfo;
		private Map<FanType, Integer> expectedFans;
		private WinAndFanTypes allWinAndFanTypes;
	}

	public FanTestRunner(Class<?> clazz) throws IOException {
		this.testClass = clazz;
		initDescriptions();
	}

	private void initDescriptions() throws IOException {
		rootDesc = Description.createSuiteDescription("fantests");
		tests = new HashMap<>();

		FanTestBaseName[] baseNames = testClass.getAnnotationsByType(FanTestBaseName.class);
		Arrays.stream(baseNames).map(FanTestBaseName::value).forEach(rethrowConsumer(this::initByBaseName));
	}

	private static final String COLUMN_WIN_INFO = "win_info";
	private static final String COLUMN_FAN_TYPES = "fan_types";

	private void initByBaseName(String baseName) throws IOException {
		Description baseNameDesc = Description.createSuiteDescription(baseName);
		rootDesc.addChild(baseNameDesc);

		WinAndFanTypes winAndFanTypes = FanTypeLoader.loadFanTypes(baseName);
		Map<String, FanType> fanTypesByName = winAndFanTypes.getFanTypes().stream()
				.collect(Collectors.toMap(FanType::name, identity()));

		try (Reader reader = new InputStreamReader(
				FanTypeLoader.class.getResource("/" + baseName + ".fantest.csv").openStream())) {
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
			int index = 0;
			for (CSVRecord record : records) {
				index++;
				String winInfoStr = record.get(COLUMN_WIN_INFO);
				String fanTypesStr = record.get(COLUMN_FAN_TYPES);

				TestCase testCase = new TestCase();
				testCase.allWinAndFanTypes = winAndFanTypes;
				testCase.winInfo = FanTestUtils.parseWinInfo(winInfoStr);
				if (fanTypesStr.isEmpty())
					testCase.expectedFans = Collections.emptyMap();
				else {
					testCase.expectedFans = new HashMap<>();
					String[] fanTypeStrs = fanTypesStr.split(",");
					for (String fanTypeStr : fanTypeStrs) {
						String fanTypeName;
						int fanCount;
						if (fanTypeStr.contains(":")) {
							int speratorIndex = fanTypeStr.indexOf(":");
							fanTypeName = fanTypeStr.substring(0, speratorIndex);
							fanCount = Integer.parseInt(fanTypeStr.substring(speratorIndex));
						} else {
							fanTypeName = fanTypeStr;
							fanCount = 1;
						}
						FanType fanType = fanTypesByName.get(fanTypeName);
						if (fanType == null)
							throw new RuntimeException(
									"Unrecognized fan type " + fanTypeName + " for win info " + winInfoStr);
						testCase.expectedFans.put(fanType, fanType.score() * fanCount);

					}
				}

				Description caseDesc = Description.createTestDescription(testClass, index + ": " + winInfoStr);
				baseNameDesc.addChild(caseDesc);
				tests.put(caseDesc, testCase);
			}
		}
	}

	@Override
	public Description getDescription() {
		return rootDesc;
	}

	@Override
	public void run(RunNotifier notifier) {
		tests.forEach((desc, testCase) -> {
			notifier.fireTestStarted(desc);
			try {
				test(testCase);
			} catch (Exception e) {
				notifier.fireTestFailure(new Failure(desc, e));
			}
			notifier.fireTestFinished(desc);
		});
	}

	private void test(TestCase testCase) throws FanTestFailureException {
		Map<FanType, Integer> fans = FanType.getFans(testCase.winInfo, testCase.allWinAndFanTypes.getFanTypes(),
				testCase.allWinAndFanTypes.getWinTypes());
		if (!fans.equals(testCase.expectedFans))
			throw new FanTestFailureException(testCase.expectedFans, fans);
	}

}
