package lanoxkododev.tigerguard.polls;

import java.util.ArrayList;

import org.javatuples.Pair;
import org.javatuples.Quintet;
import org.javatuples.Sextet;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.TigerLogs;
import net.dv8tion.jda.api.entities.channel.ChannelType;

public class PollReviewThread extends Thread {

	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	ArrayList<Pair<Long, String>> pollTables;
	boolean bootRun = false;

	public PollReviewThread(ArrayList<Pair<Long, String>> inputList, boolean bootRunIn)
	{
		pollTables = inputList;
		bootRun = bootRunIn;
	}

	@Override
	public void run()
	{
		review();
	}

	private void review()
	{
		//Create empty Sextet: Guild ID, Poll ID (message id), ChannelType, Channel it's in, The string identifier "Duo/Trio/etc", and the poll's end time.
		ArrayList<Sextet<Long, Long, ChannelType,Long, String, Long>> pollData = new ArrayList<>();
		for (int a = 0; a < pollTables.size(); a++)
		{
			//Get all polls for the guild using the specified table which is in the format of a string located at value1 of our initial data.
			ArrayList<Quintet<Long, ChannelType, Long, String, Long>> temp = tigerGuardDB.getPollsData(pollTables.get(a).getValue1(), bootRun);

			//Add each resulting Quartet to the pollData array. Append Guild id to the starting value and push the rest back by 1 to fit into Quintet.
			for (int b = 0; b < temp.size(); b++)
			{
				pollData.add(Sextet.with(pollTables.get(a).getValue0(), temp.get(b).getValue0(), temp.get(b).getValue1(), temp.get(b).getValue2(),
					temp.get(b).getValue3(), temp.get(b).getValue4()));
			}
		}

		//Iterate through all the polls we just retrieved
		for (Sextet<Long, Long, ChannelType, Long, String, Long> element : pollData) {
			long currentTime = System.currentTimeMillis();
			long expectedEndTime = element.getValue5();

			//Check if the poll is going to end within 10 minutes OR has already passed its end time.
			if (((expectedEndTime - currentTime) <= 600000) || currentTime > expectedEndTime)
			{
				//Update DB to show the poll sent to the new thread is actively been maintained, thus preventing it from being checked again in the next loop should it still be present for some reason.
				tigerGuardDB.pollUpdateInitiatedCheck(element.getValue0(), element.getValue1());
				new Thread(new PollClosureThread(element)).start();
			}
		}
	}
}
