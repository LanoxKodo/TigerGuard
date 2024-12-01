package lanoxkododev.tigerguard.time;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;

import org.javatuples.Pair;
import org.javatuples.Quintet;

import lanoxkododev.tigerguard.ThreadUtilities;
import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.JDA;

public class TimeDates {

	TigerLogs logger = new TigerLogs();
	EmbedMessageFactory embedder = new EmbedMessageFactory();
	ZonedDateTime timeLocal = ZonedDateTime.now();
	ZonedDateTime timeUTC = ZonedDateTime.ofInstant(timeLocal.toInstant(), ZoneOffset.UTC);
	int year;
	int month;
	int day;

	/**
	 * ArrayList of 'holidays' that occur today. Input of Holiday Title, Holiday Description, and Double value for XP bonus.
	 */
	static Pair<String, String> currentHoliday = null;

	//Holiday Name, Month number, Day number, boolean isDynamic, weekNumber number, weekDay dayName
	//static ArrayList<Octet<String, Integer, Integer, Boolean, Integer, DayOfWeek, String, Double>> datesOlder =
	/**
	 * Dates array:
	 * value0 - String - date title
	 * value1 - Pair<Integer, Integer> - date range start month/day
	 * value2 - Pair<Integer, Integer> - date range end month/day
	 * value3 - String - the date's output description message
	 * value4 - Double - xp_boost value while date is active
	 */
	static ArrayList<Quintet<String, Pair<Integer, Integer>, Pair<Integer, Integer>, String, Double>> dates = new ArrayList<>();

	static boolean boostDate = false;
	static double boostValue = 0;
	static Long boostDateTimeEnd = null;

	/**
	 * Age of TigerGuard, since creation year of 2022, not the iteration period from 2020-2022 which was pre-TigerGuard.
	 * @return age
	 */
	private int tigerGuardAge()
	{
		return year - 2022;
	}

	public boolean getBoostDateStatus()
	{
		return boostDate;
	}

	public double getBoostValue()
	{
		return boostValue;
	}

	public void initDates()
	{
		year = ZonedDateTime.now().getYear();
		boostDate = false;

		//Modern Data format
		//0 - DATE NAME  |  1 - Pair<Month Start, Day Start>  |  2  Pair<Month End, Day End>  | 3  DATE DESCRIPTION  |  4  XP VALUE AS DOUBLE

		//Dates listed in order:
		//Jan
		//Feb
		//Mar
		//Apr
		//May
		//Jun
		//Jul
		dates.add(Quintet.with("TigerGuard's Reflection", Pair.with(7, 7), Pair.with(7, 9),
			"Greetings all, I come bearing news of a new XP boost!\n\nToday marks the day of my stripes have aged another year, now being " + tigerGuardAge()
			+ " years.\n\nWith that said, let's celebrate the occasion with a bonus to your xp endeavors, let's scale the boost for this event. "
			+ "All xp gains are increased by ***" + tigerGuardAge() + "X*** until July 9th!", Double.valueOf(tigerGuardAge())));
		//Aug
		//Sep
		//Oct
		dates.add(Quintet.with("The Yearly Spooky Nights", Pair.with(10, 28), Pair.with(10, 31), "We are finally here, a time for a fright!\nBut what could be near, I can't see hardly anything in sight.\n"
			+ "I really should be in bed, I should check in with the local innkeeper.\nYet I appear to be lost with something ahead, is that our resident grimweeper?"
			+ "\n\nJokes aside, for these most spectacular kinds of nights, enjoy a ***2.5X*** bonus rate for all your xp gains!", 2.5));
		//Nov
		//Dec
		dates.add(Quintet.with("New Year Approaching!", Pair.with(12, 29), Pair.with(1, 2), "Soon the world will be finishing another orbit around the sun, so much has elasped since the last one! "
			+ "With this incoming year, I hope you all embark on grand adventures wherever those might take place.\n\nTo celebrate this yearly occasion, "
			+ "I'm feeling that now would be a good time for all xp endeavors be boosted at a **3X** rate from now until January 2nd.", 3.0));
	}

	protected void checkDateStatus(int day, int month, int year)
	{
		if (boostDateTimeEnd == null)
		{
			assignDateCheck(day, month, year);
		}
		else
		{
			if (System.currentTimeMillis() >= boostDateTimeEnd)
			{
				boostDate = false;
				boostValue = 0;
				boostDateTimeEnd = null;
				currentHoliday = null;
				assignDateCheck(day, month, year);
			}
		}
	}

	private void assignDateCheck(int day, int month, int year)
	{
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

		dates.forEach(a -> {
			if (a.getValue1().getValue0() == month || a.getValue2().getValue0() == month)
			{
				//Check if month values are the same for date ranges that span the same month
				if (a.getValue1().getValue0() == a.getValue2().getValue0())
				{
					if (day >= a.getValue1().getValue1() && day <= a.getValue2().getValue1())
					{
						Date end = null;

						try
						{
							end = format.parse(String.format("%d/%d/%d %d:%d:%d", a.getValue2().getValue0(), a.getValue2().getValue1(), year, 11, 59, 00));
						}
						catch (Exception e)
						{
							logger.logErr(LogType.ERROR, "Failure parsing dates", null, e);
						}

						boostChanged(a.getValue0(), a.getValue3(), a.getValue4(), end);
						return;
					}
				}
				else //month values are different
				{
					Date start = null;
					Date end = null;
					String dateFormat = "%d/%d/%d %d:%d:%d";

					try
					{
						//Check if date occurs around a new year period
						if (a.getValue1().getValue0() == 12 && a.getValue2().getValue0() == 1)
						{
							start = format.parse(String.format(dateFormat, a.getValue1().getValue0(), a.getValue1().getValue1(), year, 11, 59, 00));
							end = format.parse(String.format(dateFormat, a.getValue2().getValue0(), a.getValue2().getValue1(), year+1, 11, 59, 00));
						}
						else
						{
							start = format.parse(String.format(dateFormat, a.getValue1().getValue0(), a.getValue1().getValue1(), year, 11, 59, 00));
							end = format.parse(String.format(dateFormat, a.getValue2().getValue0(), a.getValue2().getValue1(), year, 11, 59, 00));
						}
						end = format.parse(String.format(dateFormat, a.getValue2().getValue0(), a.getValue2().getValue1(), year, 11, 59, 00));
					}
					catch (Exception e)
					{
						logger.logErr(LogType.ERROR, "Failure parsing dates.", null, e);
					}

					Long time = System.currentTimeMillis();
					if (time >= start.getTime() && time <= end.getTime())
					{
						boostChanged(a.getValue0(), a.getValue3(), a.getValue4(), end);
						return;
					}
				}
			}
		});
	}

	private void boostChanged(String title, String description, Double value, Date date)
	{
		boostDate = true;
		boostValue = value;
		boostDateTimeEnd = date.getTime();
		currentHoliday = Pair.with(title, description);
		logger.log(LogType.INFO, "Date boost event engaged, date title: " + currentHoliday.getValue0());

		JDA jda = TigerGuard.getTigerGuard().getJDA();
		ThreadUtilities.createGenericThread(a -> {
			jda.getGuilds().forEach(b -> {
				if (TigerGuardDB.getTigerGuardDB().checkRow("levelRoles", "id", b.getIdLong()))
				{
					Long channel = TigerGuardDB.getTigerGuardDB().getGuildLevelChannel(b.getIdLong());

					if (channel != 0L)
					{
						b.getTextChannelById(channel).sendMessageEmbeds(embedder.simpleEmbed(currentHoliday.getValue0(), null, null, ColorCodes.TIGER_FUR, currentHoliday.getValue1())).queue();
					}
					else
					{
						b.getDefaultChannel().asTextChannel().sendMessageEmbeds(embedder.simpleEmbed(currentHoliday.getValue0(), null, null, ColorCodes.TIGER_FUR, currentHoliday.getValue1())).queue();
					}
				}
			});
		}, null, null, false, true);
	}
}