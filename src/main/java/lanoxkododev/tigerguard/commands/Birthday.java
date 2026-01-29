package lanoxkododev.tigerguard.commands;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Birthday implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	
	@Override
	public String getName()
	{
		return "birthday";
	}

	@Override
	public String getDescription()
	{
		return "birthday command suite";
	}
	
	@Override
	public List<SubcommandData> getSubcommands()
	{
		List<SubcommandData> subcommands = new ArrayList<>();
		subcommands.add(new SubcommandData("set", "Set your birthday")
			.addOption(OptionType.STRING, "date", "Your birthday in DD/MM/YYYY (day/month/year) format", true));
		subcommands.add(new SubcommandData("unset", "Remove your birthday"));
		subcommands.add(new SubcommandData("view", "View time until next birthday")
			.addOption(OptionType.USER, "member", "Check the time till next birthday for another member.", false));
		
		return subcommands;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		String table = event.getGuild().getIdLong() + "bdays";
		
		if (!tigerGuardDB.getValueBoolean("guildFeatures", "birthdayFeature", "guild", event.getGuild().getIdLong()))
		{
			event.replyEmbeds(embedder.simpleEmbed("Birthday feature set not enabled", null, null, ColorCodes.UNABLE,
				"This server has not enabled the usage of the birthday feature set. This can be enabled by this servers' administrator(s)."))
				.setEphemeral(true).queue();
			return;
		}
		
		String subcommand = event.getSubcommandName();
		
		if (subcommand == null)
		{
			event.replyEmbeds(embedder.simpleEmbed("Invalid command usage", null, null, ColorCodes.ERROR,
				"Your input for the command appears to be wrong. Please try again")).setEphemeral(true).queue();
			return;
		}
		
		switch (subcommand)
		{
			case "set" -> set(event, table);
			case "unset" -> unset(event, table);
			case "view" -> view(event, table);
			default -> event.replyEmbeds(embedder.simpleEmbed("Unknown subcommand", null, null, ColorCodes.ERROR,
				"subcommand entered does not match expected options.")).setEphemeral(true).queue();
		}
	}
	
	private void set(SlashCommandInteractionEvent event, String table)
	{
		event.deferReply().setEphemeral(true).queue();
		
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
		
		try
		{
			LocalDate dateIntermediate = LocalDate.parse(event.getOption("date").getAsString(), inputFormatter);
			String date = dateIntermediate.toString();
			
			//String date = LocalDate.parse(input, inputFormatter).toString();
			
			if (tigerGuardDB.checkRow(table, "member", event.getMember().getIdLong()))
			{
				tigerGuardDB.basicUpdate(table, "date", date, "member", event.getMember().getIdLong());
			}
			else tigerGuardDB.firstInsertion(table + " (member, date) VALUES (" + event.getMember().getIdLong() + ", '" + date + "')");
			
			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Birthday set", null, null, ColorCodes.FINISHED,
				"Your birthday has been set and may now be used with the other birthday subcommands!")).queue();
		}
		catch (DateTimeParseException e)
		{
			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Invalid format or data", null, null, ColorCodes.ERROR,
				"Hmm, I might be a guard, but I do believe something is wrong here.\n"
				+ "Please verify you entered data in `DD/MM/YYYY` and that the values are correct, ie no dates like 30/02/1987 (Feb 30th, 1987) "
				+ "nor 51/07/2002 (July 51st, 2002).")).queue();
			e.printStackTrace();
		}
	}
	
	private void unset(SlashCommandInteractionEvent event, String table)
	{
		event.deferReply().setEphemeral(true).queue();
		Long memberID = event.getMember().getIdLong();
		
		if (tigerGuardDB.checkRow(table, "member", memberID))
		{
			tigerGuardDB.deleteRow(table, "member", memberID);
			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Birthday removed", null, null, ColorCodes.CONFIRMATION,
				"As requested, I have removed your birthday from my resources.")).queue();
		}
		else
		{
			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("No action needed", null, null, ColorCodes.CONFIRMATION,
				"Your birthday is not detected in my resources; therefore, there is nothing to remove.")).queue();
		}
	}
	
	private int calculateRemainingDays(String memberDate)
	{
		LocalDate today = LocalDate.now();
		LocalDate dob = LocalDate.parse(memberDate);
		LocalDate nextBirthday = dob.withYear(today.getYear());

		if (!nextBirthday.isAfter(today)) nextBirthday = nextBirthday.plusYears(1);
		
		return (int)ChronoUnit.DAYS.between(today, nextBirthday);
	}
	
	private void view(SlashCommandInteractionEvent event, String table)
	{
		event.deferReply().setEphemeral(false).queue();
		
		String tigerguardBirthdate = "2022-07-07"; //TigerGuard's 1.0 first live release date, August 7th, 2022
		Member referencedMember = (event.getOption("member") != null) ? event.getGuild().getMember(event.getOption("member").getAsMember()) : event.getMember();
		
		if (referencedMember != null)
		{
			if (referencedMember != event.getGuild().getMember(TigerGuard.getTigerGuard().getSelf()))
			{
				if (tigerGuardDB.checkRow(table, "member", referencedMember.getIdLong()))
				{
					String memberDate = tigerGuardDB.getValueString(table, "date", "member", referencedMember.getIdLong());
					event.getHook().sendMessageEmbeds(embedder.simpleEmbed(null, null, null, ColorCodes.BIRTHDAY, referencedMember.getAsMention() + "'s"
						+ " next birthday is in `" + calculateRemainingDays(memberDate) + "` days :birthday:")).queue();
				}
				else
				{
					event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Unknown details", null, null, ColorCodes.UNABLE,
						"Pardon, but it seems I do not have those details - the user has not come to me to set that value!\n\n"
						+ "-# Message will be deleted in 1 minute")).queue(msg -> {
						msg.delete().queueAfter(60, TimeUnit.SECONDS);
					});
				}
			}
			else
			{
				event.getHook().sendMessageEmbeds(embedder.simpleEmbed(null, null, null, ColorCodes.BIRTHDAY, "My next birthday is in `"
					+ calculateRemainingDays(tigerguardBirthdate) + "` days :birthday:\nThanks for checking!")).queue();
			}
		}
		else
		{
			event.getHook().sendMessageEmbeds(embedder.simpleEmbed(null, null, null, ColorCodes.MEH_NOTICE,
				"Hmm, you may be referencing someone I have not logged coming through the gate. Are you aware of someone I'm not?")).queue();
		}
	}
}
