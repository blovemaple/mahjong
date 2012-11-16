package blove.mj.record;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import blove.mj.GameResult;
import blove.mj.PlayerLocation;
import blove.mj.PointsResult;

/**
 * 位于本地的当前系统用户的记录管理器。
 * 
 * @author blovemaple
 */
public class UserRecorder implements Recorder {
	private static UserRecorder instance;

	/**
	 * 返回唯一实例。
	 * 
	 * @return 实例
	 * @throws IOException
	 */
	public static UserRecorder getRecorder() throws IOException {
		if (instance == null) {
			synchronized (UserRecorder.class) {
				if (instance == null)
					instance = new UserRecorder();
			}
		}
		return instance;
	}

	private static final Path DATA_DIR = Paths.get(
			System.getProperty("user.home"), ".mahjong");
	private static final String RECORDS_DB_NAME = "records",
			RECORDS_MAP_NAME = "records", RECORDS_STATS_MAP_NAME = "points";

	private final DB recordsDB;
	private final ConcurrentNavigableMap<Date, GameResult> recordsMap;
	private final ConcurrentMap<String, Integer> recordsStatsMap;

	private UserRecorder() throws IOException {
		// 确保recordsDB在程序退出前被关闭
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (recordsDB != null)
					recordsDB.close();
			}
		});

		Files.createDirectories(DATA_DIR);
		DBMaker dbMaker = DBMaker.newFileDB(
				DATA_DIR.resolve(RECORDS_DB_NAME).toFile())
				.transactionDisable();// XXX - 事务模式被关闭，因为MapDB本身的Bug。
		recordsDB = dbMaker.make();
		recordsMap = recordsDB.getTreeMap(RECORDS_MAP_NAME);
		recordsStatsMap = recordsDB.getHashMap(RECORDS_STATS_MAP_NAME);

	}

	@Override
	public int getPoints(String playerName) {
		Integer points = recordsStatsMap.get(playerName);
		if (points == null)
			points = 0;
		return points;
	}

	@Override
	public void addResult(GameResult result) {
		long crtTime = System.currentTimeMillis();
		Date crtTimeDate;
		while (recordsMap.containsKey(crtTimeDate = new Date(crtTime)))
			crtTime++;
		recordsMap.put(crtTimeDate, result);

		PointsResult pointsResult = result.getPoints();
		for (Map.Entry<PlayerLocation, String> player : result.getPlayers()
				.entrySet()) {
			PlayerLocation location = player.getKey();
			String playerName = player.getValue();

			Integer point = getPoints(playerName);
			point += pointsResult.getPoints(location);
			recordsStatsMap.put(playerName, point);
		}

		// XXX - 事务模式被关闭，因为MapDB本身的Bug。
		// recordsDB.commit();
	}
}
