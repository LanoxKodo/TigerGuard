package lanoxkododev.tigerguard.messages;

import lanoxkododev.tigerguard.TigerGuardDB;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class EmbedDeleterThreader extends Thread {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	StringSelectInteractionEvent event;
	String input;

	public EmbedDeleterThreader(StringSelectInteractionEvent eventIn, String inputIn)
	{
		event = eventIn;
		input = inputIn;
	}

	@Override
	public void run()
	{
		deleteProcess();
	}

	private void deleteProcess()
	{
		TigerGuardDB.getTigerGuardDB().deleteRow(event.getGuild().getIdLong() + "embeds", "name", input);
	}
}