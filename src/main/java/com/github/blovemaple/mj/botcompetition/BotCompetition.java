package com.github.blovemaple.mj.botcompetition;

import static com.github.blovemaple.mj.object.PlayerLocation.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.LogManager;

import com.github.blovemaple.mj.cli.CliRunner;
import com.github.blovemaple.mj.game.GameResult;
import com.github.blovemaple.mj.game.MahjongGame;
import com.github.blovemaple.mj.local.AbstractBot;
import com.github.blovemaple.mj.local.bazbot.BazBot;
import com.github.blovemaple.mj.object.MahjongTable;
import com.github.blovemaple.mj.object.Player;
import com.github.blovemaple.mj.rule.GameStrategy;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;
import com.github.blovemaple.mj.rule.simple.SimpleGameStrategy;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class BotCompetition {
	public static void main(String[] args) {
		new BotCompetition(BazBot::new, BazBot::new).compete(1000);
	}

	private Supplier<? extends AbstractBot> botSupplier1, botSupplier2;

	private GameStrategy gameStrategy = new SimpleGameStrategy();
	private TimeLimitStrategy timeStrategy = TimeLimitStrategy.NO_LIMIT;

	public BotCompetition(Supplier<? extends AbstractBot> botSupplier1, Supplier<? extends AbstractBot> botSupplier2) {
		this.botSupplier1 = botSupplier1;
		this.botSupplier2 = botSupplier2;
	}

	// 序号，获胜的bot，坐庄的bot，耗时1，调用次数1，耗时2，调用次数2，本局总耗时
	public void compete(int gameCount) {
		try {
			LogManager.getLogManager()
					.readConfiguration(CliRunner.class.getResource("/logging_botcompetition.properties").openStream());

			AbstractBot bot11 = botSupplier1.get();
			AbstractBot bot12 = botSupplier1.get();
			AbstractBot bot21 = botSupplier2.get();
			AbstractBot bot22 = botSupplier2.get();

			for (int gameIndex = 1; gameCount >= 0 && gameIndex <= gameCount; gameIndex++) {
				bot11.resetCostStat();
				bot12.resetCostStat();
				bot21.resetCostStat();
				bot22.resetCostStat();

				long startTime = System.nanoTime();
				GameResult result;
				if (gameIndex % 2 == 1)
					result = compete(bot11, bot21, bot12, bot22);
				else
					result = compete(bot21, bot11, bot22, bot12);
				long gameCost = System.nanoTime() - startTime;

				AbstractBot winner = null;
				if (result.getWinnerLocation() != null)
					winner = (AbstractBot) result.getPlayerInfos().get(result.getWinnerLocation()).getPlayer();
				AbstractBot zhuang = (AbstractBot) result.getPlayerInfos().get(result.getZhuangLocation()).getPlayer();
				long cost1 = bot11.getCostSum() + bot12.getCostSum();
				int invoke1 = bot11.getInvokeCount() + bot12.getInvokeCount();
				long cost2 = bot21.getCostSum() + bot22.getCostSum();
				int invoke2 = bot21.getInvokeCount() + bot22.getInvokeCount();

				List<String> outputs = List.of( //
						Integer.toString(gameIndex), //
						winner == null ? "0" : winner == bot11 || winner == bot12 ? "1" : "2", //
						zhuang == bot11 || zhuang == bot12 ? "1" : "2", //
						Long.toString(Math.round(cost1 / 1_000_000D)), //
						Integer.toString(invoke1), //
						Long.toString(Math.round(cost2 / 1_000_000D)), //
						Integer.toString(invoke2), //
						Long.toString(Math.round(gameCost / 1_000_000D)) //
				);

				System.out.println(String.join("\t", outputs));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private GameResult compete(Player player1, Player player2, Player player3, Player player4) {
		try {
			MahjongTable table = new MahjongTable();
			table.init();
			table.setPlayer(EAST, player1);
			table.setPlayer(SOUTH, player2);
			table.setPlayer(WEST, player3);
			table.setPlayer(NORTH, player4);

			MahjongGame game = new MahjongGame(gameStrategy, timeStrategy);
			return game.play(table);
		} catch (InterruptedException e) {
			// not possible
			throw new RuntimeException(e);
		}
	}
}
