package lanoxkododev.tigerguard.time;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.javatuples.Triplet;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.polls.TigerPolls;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class TimingThread extends Thread {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	TimeDates dates = new TimeDates();
	TigerPolls polls = new TigerPolls();
	boolean stopStatus = false;
	boolean firstBoot = true;
	boolean nearMidnight = false;

	SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	ZonedDateTime date = ZonedDateTime.now();

	/**
	 * TigerGuard's time-loop class. This will handle iterating things that need to be checked every so often,
	 * those items will be called from this loop as to not block the main thread frequently.
	 */
	public TimingThread()
	{
		this.setName("timeThread");
	}

	@Override
	public void run()
	{
		process();
	}

	private void process()
	{
		dates.initDates();
		verifyTimesVC();

		int day, month, year;
		day = date.getDayOfMonth();
		month = date.getMonthValue();
		year = date.getYear();

		dates.checkDateStatus(day, month, year);
		timeEvenSync(date.getSecond());

		if (date.getHour() == 0)
		{
			nearMidnight = true;
		}

		while (!stopStatus)
		{
			polls.beginPollReview(firstBoot);

			date = ZonedDateTime.now();
			if (!nearMidnight && date.getHour() == 0)
			{
				nearMidnight = true;
				year = date.getYear();
				month = date.getMonthValue();
				day = date.getDayOfMonth();

				dates.checkDateStatus(day, month, year);
			}

			//Reset midnight boolean
			if (nearMidnight && date.getHour() != 0)
			{
				nearMidnight = false;
			}

			sleepEnactor(600000);
		}
	}

	public void updateStopStatus()
	{
		stopStatus = true;
		this.interrupt();
		logger.log(LogType.INFO, "Time thread stopped.");
	}

	/**
	 * Sync time to have minute and second portions be even min:sec time (00:00:00 - midnight, 14:20:00 - afternoon-ish, etc)
	 *
	 * @param seconds - the seconds returned from the previous time.now() reference.
	 */
	private void timeEvenSync(int seconds)
	{
		polls.beginPollReview(firstBoot);
		firstBoot = false;

		if (seconds != 0)
		{
			sleepEnactor(1000*(59-seconds));
		}

		int minutes = ZonedDateTime.now().getMinute();
		if (minutes % 10 != 0)
		{
			sleepEnactor(60000 * (10 - (minutes - (10 * (minutes / 10)))));
		}
	}

	/**
	 * General method for putting time thread to sleep.
	 *
	 * @param value - the amount of time requested to sleep.
	 */
	private void sleepEnactor(int value)
	{
		try
		{
			Thread.sleep(value);
		}
		catch (InterruptedException e)
		{
			if (!stopStatus)
			{
				logger.logErr(LogType.WARNING, "Failure setting time thread to sleep with value of " + value, null, e);
			}
		}
	}

	private void verifyTimesVC()
	{
		if (!tigerguardDB.checkForTable("voiceTracker"))
		{
			tigerguardDB.createTable("voiceTracker (id VARCHAR(45), init VARCHAR(45), guild VARCHAR(45));");
		}

		ArrayList<Triplet<Long, Long, Long>> voiceItems = tigerguardDB.bootVoiceVerify();

		Long currentTime = System.currentTimeMillis();

		JDA jda = TigerGuard.TigerGuardInstance.getJDA();
		//JDA jda = TigerGuard.getTigerGuard().getJDA();

		for (Triplet<Long, Long, Long> pull : voiceItems)
		{
			Long member = pull.getValue0();
			Long guild = pull.getValue2();
			Guild guildInstance = jda.getGuildById(guild);
			guildInstance.retrieveMemberById(member).queue(a -> {
				if (a.getVoiceState().getChannel() == null)
				{
					long timeBegan = pull.getValue1();

					if (timeBegan != 0)
					{
						double total = currentTime - timeBegan;
						double calc = total / 900000;

						if (calc >= 1)
						{
							if (!tigerguardDB.checkRow(guild + "xp", "member", member))
							{
								tigerguardDB.insertUserIntoGuildXPTable(guild + "xp", member);
							}

							if (calc <= 56)
							{
								tigerguardDB.updateGuildRankXp(guildInstance, a, (int)Math.round(15 * calc), null, null);
							}
							else
							{
								tigerguardDB.updateGuildRankXp(guildInstance, a, Math.round(15 * 56), null, null);
							}
						}

						tigerguardDB.basicDelete("voiceTracker", "member", member);
					}
				}
			});
		}
	}
}