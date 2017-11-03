package com.github.blovemaple.mj.rule.win.load;

import static com.github.blovemaple.mj.object.StandardTileUnitType.*;
import static com.github.blovemaple.mj.object.TileRank.ZiRank.*;
import static com.github.blovemaple.mj.object.TileSuit.*;
import static com.github.blovemaple.mj.rule.win.load.FanTypeMatchingTile.DynamicTileRank.*;
import static com.github.blovemaple.mj.utils.MyUtils.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.object.TileUnitType;
import com.github.blovemaple.mj.rule.simple.NormalWinType;
import com.github.blovemaple.mj.rule.win.FanType;
import com.github.blovemaple.mj.rule.win.FanTypeMatcher;
import com.github.blovemaple.mj.rule.win.WinType;
import com.github.blovemaple.mj.rule.win.load.LogicalFanTypeMatcher.LogicalOp;

/**
 * 从文件读取番种定义的工具。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class FanTypeLoader {
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_SCORE = "score";
	private static final String COLUMN_IS_WIN_TYPE = "is_win_type";
	private static final String COLUMN_DEFINITION = "definition";
	private static final String COLUMN_COVER = "cover";
	private static final String COLUMN_UNITS_NO_YJK = "units_no_yao_jiu_ke";

	private static final String PKG_PROP_PREFIX = "fantype.pkg.";

	/**
	 * 从文件读取番种列表。
	 * 
	 * @param baseName
	 *            读取的文件名（不包含后缀，后缀固定为.fantypes.csv）
	 * @return 番种列表
	 * @throws IOException
	 */
	public static WinAndFanTypes loadFanTypes(String baseName) throws IOException {
		Properties props = new Properties();
		props.load(FanTypeLoader.class.getResourceAsStream("/fantype.properties"));
		String basePackage = props.getProperty(PKG_PROP_PREFIX + baseName);

		Map<String, LoadedFanType> fanTypes = new LinkedHashMap<>();
		Map<String, List<String>> coveredByName = new HashMap<>();

		try (Reader reader = new InputStreamReader(
				FanTypeLoader.class.getResource("/" + baseName + ".fantypes.csv").openStream())) {
			Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(reader);
			for (CSVRecord record : records) {
				String name = record.get(COLUMN_NAME);
				String scoreStr = record.get(COLUMN_SCORE);
				int score = !scoreStr.isEmpty() ? Integer.parseInt(scoreStr) : 0;
				boolean isWinType = "1".equals(record.get(COLUMN_IS_WIN_TYPE));
				String definition = record.get(COLUMN_DEFINITION);
				String coverStr = record.get(COLUMN_COVER);
				String[] cover = coverStr.isEmpty() ? new String[0] : coverStr.split(";");
				boolean unitsNoYaoJiuKe = "1".equals(record.get(COLUMN_UNITS_NO_YJK));

				FanTypeMatcher matcher;
				try {
					matcher = parseMatcher(definition, fanTypes, basePackage, name);
				} catch (Exception e) {
					throw new RuntimeException("Error parse fan type " + name, e);
				}
				LoadedFanType type;
				if (isWinType) {
					type = new LoadedWinFanType(name, matcher, score);
				} else {
					type = new LoadedFanType(name, matcher, score);
				}
				type.setUnitsNoYaoJiuKe(unitsNoYaoJiuKe);
				fanTypes.put(name, type);
				coveredByName.put(name, Arrays.asList(cover));
			}
		}

		List<FanType> validFanTypes = fanTypes.values().stream()
				// 去掉番数为0的
				.filter(fanType -> fanType.score() > 0)
				// 填充covered
				.peek(fanType -> {
					List<String> coveredNames = coveredByName.get(fanType.name());
					if (coveredNames != null) {
						Set<FanType> covered = coveredNames.stream().map(fanTypes::get).peek(Objects::requireNonNull)
								.collect(Collectors.toSet());
						fanType.setCovered(covered);
					}
				})
				// 收集成list
				.collect(Collectors.toList());

		List<WinType> validWinTypes = fanTypes.values().stream()
				// 过滤出WinType
				.filter(fanType -> (fanType instanceof WinType)).map(fanType -> (WinType) fanType)
				.collect(Collectors.toList());
		// 加上NormalWinType
		validWinTypes.add(NormalWinType.get());

		return new WinAndFanTypes(baseName, validFanTypes, validWinTypes);
	}

	public static FanTypeMatcher parseMatcher(String definition) {
		return parseMatcher(definition, null, null, null);
	}

	/**
	 * 根据定义字符串解析一个matcher。
	 */
	private static FanTypeMatcher parseMatcher(String definition, Map<String, LoadedFanType> fanTypes, String pkg,
			String name) {
		definition = definition.replaceAll("\\s", "");
		if (definition.isEmpty()) {
			try {
				// 从类读取
				Class<?> fanTypeClass = Class.forName(pkg + "." + nameToClassName(name));
				return (FanTypeMatcher) fanTypeClass.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Cannot read definition for fan type " + name, e);
			}
		}

		switch (definition.charAt(0)) {
		case '~':
		case '^':
			return parseAllMatching(definition);
		case '<':
			return parseInvoking(definition, fanTypes);
		case '(':
		case '-':
			return parseLogical(definition, fanTypes, pkg, name);
		default:
			return parseIncluding(definition);
		}
	}

	private static String nameToClassName(String name) {
		char[] chars = name.toCharArray();
		StringBuilder className = new StringBuilder();
		boolean afterUnderline = false;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c != '_') {
				if (i > 0 && !afterUnderline)
					c = Character.toLowerCase(c);
				className.append(c);
			}
			afterUnderline = (c == '_');
		}
		return className.toString();
	}

	private static IncludingFanTypeMatcher parseIncluding(String definition) {
		return new IncludingFanTypeMatcher(parseObjects(definition));
	}

	private static AllMatchingFanTypeMatcher parseAllMatching(String definition) {
		String objectsStr = definition.substring(1);
		List<FanTypeMatching> objects = parseObjects(objectsStr);
		boolean negate = definition.charAt(0) == '^';
		return new AllMatchingFanTypeMatcher(objects, negate);
	}

	private static InvokingFanTypeMatcher parseInvoking(String definition, Map<String, LoadedFanType> fanTypes) {
		String invoking = definition.substring(1, definition.length() - 1);
		return new InvokingFanTypeMatcher(invoking, fanTypes);
	}

	private static LogicalFanTypeMatcher parseLogical(String definition, Map<String, LoadedFanType> fanTypes,
			String pkg, String name) {
		switch (definition.charAt(0)) {
		case '(':
			String logicalStr = definition.substring(1, definition.length() - 1);
			int opPosition = Math.max(logicalStr.lastIndexOf("&"), logicalStr.lastIndexOf("|"));
			if (opPosition < 0)
				throw new IllegalArgumentException("Cannot find logical operator in: " + definition);
			LogicalOp op = logicalStr.charAt(opPosition) == '&' ? LogicalOp.AND : LogicalOp.OR;
			FanTypeMatcher matcher1 = parseMatcher(logicalStr.substring(0, opPosition), fanTypes, pkg, name);
			FanTypeMatcher matcher2 = parseMatcher(logicalStr.substring(opPosition + 1), fanTypes, pkg, name);
			return new LogicalFanTypeMatcher(op, matcher1, matcher2);
		case '-':
			String oriMatchingStr = definition.substring(1);
			FanTypeMatcher matcher = parseMatcher(oriMatchingStr, fanTypes, pkg, name);
			return new LogicalFanTypeMatcher(LogicalOp.NOT, matcher, null);
		default:
			throw new IllegalArgumentException("Illegal logical definition: " + definition);
		}
	}

	private static List<FanTypeMatching> parseObjects(String definition) {
		String[] objectsInDef = definition.split(",");
		List<FanTypeMatching> result = Arrays.stream(objectsInDef).map(FanTypeLoader::parseObjectInDef)
				.flatMap(List::stream).collect(Collectors.toList());
		return result;
	}

	private static final Pattern PATTERN_AND = Pattern.compile("\\{.+?\\}");
	private static final Pattern PATTERN_OR = Pattern.compile("\\[.+?\\]");

	private static List<FanTypeMatching> parseObjectInDef(String definition) {
		// 如果有数量，处理数量，拆分成若干个对象
		if (Character.isDigit(definition.charAt(0)))
			return parseDefsWithQuantity(definition).stream().map(FanTypeLoader::parseObjectInDef).flatMap(List::stream)
					.collect(Collectors.toList());

		// 如果整体是OR（object/object/...），拆分并组合成一个multi对象
		String[] orObjectStrs = definition.split("/");
		if (orObjectStrs.length > 1) {
			List<FanTypeMatching> orObjects = Arrays.stream(orObjectStrs).map(FanTypeLoader::parseObjectInDef)
					.flatMap(List::stream).collect(Collectors.toList());
			return Collections.singletonList(new FanTypeMatchingMulti(orObjects));
		}

		// 如果有大括号，处理大括号，拆分成若干个对象
		if (definition.contains("{"))
			return parseDefsByBrackets(definition, PATTERN_AND).stream().map(FanTypeLoader::parseObjectInDef)
					.flatMap(List::stream).collect(Collectors.toList());

		// 否则，如果有中括号，处理中括号，拆分并组合成一个multi对象
		if (definition.contains("[")) {
			List<FanTypeMatching> orObjects = parseDefsByBrackets(definition, PATTERN_OR).stream()
					.map(FanTypeLoader::parseObjectInDef).flatMap(List::stream).collect(Collectors.toList());
			return Collections.singletonList(new FanTypeMatchingMulti(orObjects));
		}

		// 否则，parse成一个tile或unit对象
		switch (definition.charAt(0)) {
		case '@':
		case '!':
			return Collections.singletonList(parseTileObject(definition));
		default:
			return Collections.singletonList(parseUnitObject(definition));
		}
	}

	private static List<String> parseDefsWithQuantity(String definition) {
		StringBuilder quantityStr = new StringBuilder();
		for (char c : definition.toCharArray()) {
			if (Character.isDigit(c))
				quantityStr.append(c);
			else
				break;
		}
		int quantity = Integer.parseInt(quantityStr.toString());
		String objectStr = definition.substring(quantityStr.length());
		return Collections.nCopies(quantity, objectStr);
	}

	private static List<String> parseDefsByBrackets(String definition, Pattern patternInBracket) {
		List<String> plainSubStrings = new ArrayList<>();
		List<List<Character>> selectCharsGroups = new ArrayList<>();

		Matcher matcher = patternInBracket.matcher(definition);
		int crtIndex = 0;
		while (matcher.find()) {
			String plainPart = definition.substring(crtIndex, matcher.start());
			plainSubStrings.add(plainPart);

			List<Character> chars = matcher.group().substring(1, matcher.group().length() - 1).chars()
					.mapToObj(i -> Character.valueOf((char) i)).collect(Collectors.toList());
			selectCharsGroups.add(chars);

			crtIndex = matcher.end();
		}

		String tailPlainString = crtIndex < definition.length() ? definition.substring(crtIndex) : null;

		return selectStream(selectCharsGroups).map(chars -> {
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < chars.size(); i++)
				result.append(plainSubStrings.get(i)).append(chars.get(i));
			if (tailPlainString != null)
				result.append(tailPlainString);
			return result.toString();
		}).collect(Collectors.toList());
	}

	private static final Map<Character, TileSuit> SUITS = new HashMap<>();
	private static final Map<Character, TileRank<?>> RANKS = new HashMap<>();
	static {
		SUITS.put('W', WAN);
		SUITS.put('T', TIAO);
		SUITS.put('B', BING);
		SUITS.put('Z', ZI);

		RANKS.put('E', DONG_FENG);
		RANKS.put('S', NAN);
		RANKS.put('W', XI);
		RANKS.put('N', BEI);
		RANKS.put('Z', ZHONG);
		RANKS.put('F', FA);
		RANKS.put('B', BAI);
		RANKS.put('Q', QUANFENG);
		RANKS.put('M', MENFENG);
		for (char c = '1'; c <= '9'; c++)
			RANKS.put(c, TileRank.NumberRank.ofNumber(Integer.parseInt(Character.toString(c))));
	}

	private static FanTypeMatchingTile parseTileObject(String definition) {
		TileSuit suit = null;
		char suitVar = 0;
		TileRank<?> rank = null;
		int rankVarOffset = 0;
		char rankVar = 0;

		char suitSign = definition.charAt(0);
		char suitChar = definition.charAt(1);
		switch (suitSign) {
		case '!':
			suit = SUITS.get(suitChar);
			if (suit == null)
				throw new IllegalArgumentException("Illegal suit: " + suitChar);
			break;
		case '@':
			suitVar = suitChar;
			break;
		default:
			throw new IllegalArgumentException("Illegal suit sign: " + suitSign);
		}

		char rankSign = definition.charAt(2);
		char rankFirstChar = definition.charAt(3);
		switch (rankSign) {
		case '!':
			rank = RANKS.get(rankFirstChar);
			if (rank == null)
				throw new IllegalArgumentException("Illegal rank: " + rankFirstChar);
			break;
		case '#':
			if (definition.length() > 4) {
				rankVarOffset = Integer.parseInt(Character.toString(rankFirstChar));
				rankVar = definition.charAt(4);
			} else
				rankVar = rankFirstChar;
			break;
		default:
			throw new IllegalArgumentException("Illegal rank sign: " + rankSign);
		}

		return new FanTypeMatchingTile(suit, suitVar, rank, rankVarOffset, rankVar);
	}

	private static final Map<Character, TileUnitType> UNIT_TYPES = new HashMap<>();
	static {
		UNIT_TYPES.put('K', KEZI);
		UNIT_TYPES.put('S', SHUNZI);
		UNIT_TYPES.put('G', GANGZI);
		UNIT_TYPES.put('J', JIANG);
	}

	private static FanTypeMatchingUnit parseUnitObject(String definition) {
		TileUnitType unitType = null;
		Boolean isHidden = null; // null不限，true暗，false明
		FanTypeMatching tile = null;

		char typeChar = definition.charAt(0);
		unitType = UNIT_TYPES.get(typeChar);
		if (unitType == null)
			throw new IllegalArgumentException("Illegal unit type: " + typeChar);

		int crtIndex = 1;

		if (definition.length() > crtIndex) {
			char mingAnChar = definition.charAt(1);
			switch (mingAnChar) {
			case '0':
				isHidden = true;
				crtIndex++;
				break;
			case '1':
				isHidden = false;
				crtIndex++;
				break;
			}
		}

		if (definition.length() > crtIndex) {
			int tileStartIndex = isHidden == null ? 1 : 2;
			String tileString = definition.substring(tileStartIndex, definition.length());
			List<FanTypeMatching> tileObjects = parseObjectInDef(tileString);
			if (tileObjects.size() != 1)
				throw new IllegalArgumentException("Illegal tile def in unit: " + tileString);
			tile = tileObjects.get(0);
		}

		return new FanTypeMatchingUnit(unitType, isHidden, tile);
	}

	private FanTypeLoader() {
	}
}
