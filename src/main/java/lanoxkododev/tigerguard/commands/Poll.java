package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Poll implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerLogs logger = new TigerLogs();

	@Override
	public String getName()
	{
		return "poll";
	}

	@Override
	public String getDescription()
	{
		return "Open the poll management menu to create or delete a poll.";
	}

	@Override
	public boolean isNSFW()
	{
		return false;
	}

	@Override
	public List<OptionData> getOptions()
	{
		return null;
	}
	
	@Override
	public DefaultMemberPermissions getDefaultPermission()
	{
		return DefaultMemberPermissions.ENABLED;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		TigerGuardDB db = TigerGuardDB.getTigerGuardDB();
		
		if (!db.checkRow("tempPollData", "id", event.getGuild().getIdLong())) db.addGuildToTempPollTable(event.getGuild().getIdLong());

		List<Button> buttons = new ArrayList<>();
		//Consider option logic for closing a poll before its time runs out.
		//buttons.add(Button.danger("poll-Init-Close", "Close a Poll Early").withEmoji(Emoji.fromFormatted("📊")));
		buttons.add(Button.success("poll-new", "Create a New Poll").withEmoji(Emoji.fromFormatted("🖋")));

		if (!db.checkForTable(event.getGuild().getIdLong() + "polls")) db.createGuildPollTable(event.getGuild().getIdLong());

		event.replyEmbeds(embedder.simpleEmbed("Which option would you like to perform?", null, null, ColorCodes.POLL,
			"Please indiciate which action you'd like to perform using the buttons below."))
			.addActionRow(buttons).setEphemeral(true).queue();
	}
}
