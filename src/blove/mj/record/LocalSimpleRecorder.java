package blove.mj.record;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import blove.mj.GameResult;
import blove.mj.PlayerLocation;
import blove.mj.PointsResult;

/**
 * 位于本地的当前系统用户的记录管理器，仅记录累计分数。
 * 
 * @author 陈通
 */
public class LocalSimpleRecorder implements Recorder {
	private static LocalSimpleRecorder instance;

	/**
	 * 返回唯一实例。
	 * 
	 * @return 实例
	 * @throws IOException
	 */
	public static LocalSimpleRecorder getRecorder() throws IOException {
		if (instance == null) {
			synchronized (LocalSimpleRecorder.class) {
				if (instance == null)
					instance = new LocalSimpleRecorder();
			}
		}
		return instance;
	}

	private static final Path DATA_DIR = Paths.get(
			System.getProperty("user.home"), ".mahjong");
	private static final String POINTS_FILE_NAME = "points.properties";
	private static final Path POINTS_FILE = DATA_DIR.resolve(POINTS_FILE_NAME);
	private static final String COMMENTS = "Points record for mahjong.";
	private static final String LAST_DEALER_KEY = "last-dealer";
	private final Properties properties;

	private LocalSimpleRecorder() throws IOException {
		if (!Files.exists(POINTS_FILE)) {
			Files.createDirectories(DATA_DIR);
			Files.createFile(POINTS_FILE);
		}

		properties = new Properties();
		properties.load(Files.newInputStream(POINTS_FILE));
	}

	private void storeProperties() throws IOException {
		properties.store(Files.newOutputStream(POINTS_FILE), COMMENTS);
	}

	@Override
	public PlayerLocation getLastDealerLocation() {
		String dealer = properties.getProperty(LAST_DEALER_KEY);
		return dealer == null ? null : PlayerLocation.valueOf(dealer);
	}

	@Override
	public int getPoints(String playerName) {
		String points = properties.getProperty(playerName, "0");
		return Integer.parseInt(points);
	}

	@Override
	public void addResult(GameResult result) throws IOException {
		PointsResult pointsResult = result.getPoints();
		for (Map.Entry<PlayerLocation, String> player : result.getPlayers()
				.entrySet()) {
			PlayerLocation location = player.getKey();
			String playerName = player.getValue();

			int point = getPoints(playerName);
			point += pointsResult.getPoints(location);
			properties.setProperty(playerName, Integer.toString(point));
		}

		properties.setProperty(LAST_DEALER_KEY, result.getDealerLocation()
				.name());

		storeProperties();
	}
}
