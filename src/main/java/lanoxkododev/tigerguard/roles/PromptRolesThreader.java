package lanoxkododev.tigerguard.roles;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

public class PromptRolesThreader extends Thread {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	EmbedMessageFactory embedder = new EmbedMessageFactory();
	EmbedBuilder embed = new EmbedBuilder();
	TigerLogs logger = new TigerLogs();
	StringSelectInteractionEvent event;
	Guild guild;
	boolean redo;

	public PromptRolesThreader(StringSelectInteractionEvent inputEvent, boolean needsRedo)
	{
		event = inputEvent;
		guild = inputEvent.getGuild();
		redo = needsRedo;
	}

	@Override
	public void run()
	{
		readyPrompt();
	}

	private void readyPrompt()
	{
		if (redo)
		{
			MessageHistory history = MessageHistory.getHistoryFromBeginning(event.getChannel()).complete();
			List<Message> mess = history.getRetrievedHistory();

			//Check if message is by this bot, if yes then delete that message and reiterate through generating the message again with the new code.
			for (Message msg : mess)
			{
				if (msg.getAuthor().equals(User.fromId(994551850072277082L)))
				{
					if (TigerGuard.isDebugMode()) { System.out.println("Clearing message: " + msg.getIdLong() + " by user: " + msg.getAuthor()); }
					msg.delete().complete();
				}
			}
		}

		createRoles(event);
	}

	private String produceStandardSequence(StringSelectInteractionEvent event, String baseText, LinkedHashMap <String, String> roles)
	{
		String output = baseText;
		if (TigerGuard.isDebugMode()) { System.out.println("Output equals: " + output);}

		for (String check : roles.keySet())
		{
			if (TigerGuard.isDebugMode())
			{
				System.out.println(roles.keySet());
				System.out.println(roles.get(check));
			}
			Guild guild = event.getGuild();
			String toConvert = guild.getRolesByName(roles.get(check), true).toString();

			if (TigerGuard.isDebugMode()) { System.out.println("'check': " + check + "\nPre conversion: " + toConvert);}

			int startIndex = toConvert.indexOf("[");
			int endIndex = toConvert.indexOf("=");
			String replacement = "";
			String toBeReplaced = toConvert.substring(startIndex, endIndex + 1);
			String modifiedString = toConvert.replace(toBeReplaced, replacement);

			int nextIndex = modifiedString.indexOf(")");
			int lastIndex = modifiedString.indexOf("]");
			String replacement2 = "";
			String nextReplacement = modifiedString.substring(nextIndex, lastIndex + 1);
			String finalString = modifiedString.replace(nextReplacement, replacement2);


			output = output + "\n" + check + " ➤ <@&" + finalString + ">";

			if (TigerGuard.isDebugMode() ) { System.out.println("final string: " + finalString);}
		}
		roles.clear();
		return output;
	}

	private void createRoles(StringSelectInteractionEvent event)
	{
		embed.setTitle("╔═══════╗\n"
				+ "      𝙍𝙊𝙇𝙀𝙎\n"
				+ "╚═══════╝");
		embed.setDescription("Below is a list of Self-Assignable roles for you to select.\nPlease take some time to sign up for whatever roles you feel comfortable with!\n\n"
				+ "**Note!** This will be refreshed occasionally to work with the API as updates roll out! You do not need to react again to keep your roles when this occurs.\n"
				+ "As such, if you need to remove a role simply react to add a reaction then click again to remove the role!\n\nIf you encounter issues adding or removing roles, ping <@200305907572146177>");
		embed.setColor(0x12602f);
		embed.setFooter(null, event.getMember().getUser().getAvatarUrl());

		event.getChannel().sendMessageEmbeds(embed.build()).complete();
		embed.clear();
		createRolesAnnouncement(event);
	}

	private void createRolesAnnouncement(StringSelectInteractionEvent event)
	{
		String output = "";
		LinkedHashMap<String, String> roleInfo = new LinkedHashMap<>();
		roleInfo.put("✅", "Announcements ✅");
		//roleInfo.put("❌", "TigerGuard Announcements 🐯"); //Add later or remove reference, refers to design rework for announcements

		output = produceStandardSequence(event, "**Do you want to opt in for server announcements?**\n*Does not refer to any other role pings, "
				+ "This does not refer to @_everyone/here/silent pings.*\n", roleInfo);

		embed.setTitle("╔════════════════╗\n"
				+ "      𝘼𝙉𝙉𝙊𝙐𝙉𝘾𝙀𝙈𝙀𝙉𝙏𝙎\n"
				+ "╚════════════════╝");
		embed.setDescription(output);
		embed.setColor(0xcc0066); //0x12602f
		embed.setFooter(null, event.getMember().getUser().getAvatarUrl());

		Message msg = event.getChannel().sendMessageEmbeds(embed.build()).complete();
		msg.addReaction(Emoji.fromFormatted("✅")).queue();
		//msg.addReaction(Emoji.fromFormatted("❌")).queue();
		embed.clear();
		createMessageGender(event);
	}

	private void createMessageGender(StringSelectInteractionEvent event)
	{
		String output = "";
		LinkedHashMap<String, String> roleInfo = new LinkedHashMap<>();
		roleInfo.put("♂️", "Male (He/Him)");
		roleInfo.put("♀️", "Female (She/Her)");
		roleInfo.put("👨", "FTM/Trans-Male");
		roleInfo.put("👩", "MTF/Trans-Female");
		roleInfo.put("<:nonbinary:995509267543101501>", "Nonbinary");
		roleInfo.put("<:genderfluid:995509249209802883>", "Genderfluid");

		output = produceStandardSequence(event, "**What gender do you identify with?**\n*These roles are not pingable*\n", roleInfo);

		embed.setTitle("╔════════╗\n"
				+ "      𝙂𝙀𝙉𝘿𝙀𝙍\n"
				+ "╚════════╝");
		embed.setDescription(output);
		embed.setColor(0x4bb2bd); //0x12602f
		embed.setFooter(null, event.getMember().getUser().getAvatarUrl());

		Message msg = event.getChannel().sendMessageEmbeds(embed.build()).complete();
		msg.addReaction(Emoji.fromFormatted("♂️")).queue();
		msg.addReaction(Emoji.fromFormatted("♀️")).queue();
		msg.addReaction(Emoji.fromFormatted("👨")).queue();
		msg.addReaction(Emoji.fromFormatted("👩")).queue();
		msg.addReaction(Emoji.fromCustom("nonbinary", 995509267543101501L, false)).queue();
		msg.addReaction(Emoji.fromCustom("genderfluid", 995509249209802883L, false)).queue();
		embed.clear();
		createMessageSexuality(event);
	}

	private void createMessageSexuality(StringSelectInteractionEvent event)
	{
		String output = "";
		LinkedHashMap<String, String> roleInfo = new LinkedHashMap<>();
		roleInfo.put(":rainbow_flag:", "Gay");
		roleInfo.put("<:straightflag:995483929836519474>", "Straight");
		roleInfo.put("<:bisexualflag:995483530698166372>", "Bisexual");
		roleInfo.put("<:lesbianflag:995486072618369064>", "Lesbian");
		roleInfo.put("<:pansexualflag:995485156871766029>", "Pansexual");
		roleInfo.put("<:asexualflag:995483896961577040>", "Asexual");
		roleInfo.put(":purple_heart:", "Other Orientation(s)");

		output = produceStandardSequence(event, "**What sexual orientation do you identify with?**\n*These roles are not pingable*\n", roleInfo);

		embed.setTitle("╔══════════╗\n"
				+ "      𝙎𝙀𝙓𝙐𝘼𝙇𝙄𝙏𝙔\n"
				+ "╚══════════╝");
		embed.setDescription(output);
		embed.setColor(0xad57e2); //0x12602f
		embed.setFooter(null, event.getMember().getUser().getAvatarUrl());

		Message msg = event.getChannel().sendMessageEmbeds(embed.build()).complete();
		msg.addReaction(Emoji.fromFormatted("🏳️‍🌈")).queue();
		msg.addReaction(Emoji.fromCustom("straightflag", 995483929836519474L, false)).queue();
		msg.addReaction(Emoji.fromCustom("bisexualflag", 995483530698166372L, false)).queue();
		msg.addReaction(Emoji.fromCustom("lesbianflag", 995486072618369064L, false)).queue();
		msg.addReaction(Emoji.fromCustom("pansexualflag", 995485156871766029L, false)).queue();
		msg.addReaction(Emoji.fromCustom("asexualflag", 995483896961577040L, false)).queue();
		msg.addReaction(Emoji.fromFormatted("U+1F49C")).queue();
		embed.clear();
		createMessageGamingPlatforms(event, embed);
	}

	private void createMessageGamingPlatforms(StringSelectInteractionEvent event, EmbedBuilder embed)
	{
		String output = "";
		LinkedHashMap<String, String> roleInfo = new LinkedHashMap<>();
		roleInfo.put("🖥️", "PC/Steam Deck Gamer");
		roleInfo.put("<:nintendo:995529374038229092>", "Nintendo Gamer");
		roleInfo.put("<:playstation:995529373232939140>", "Playstation Gamer");
		roleInfo.put("<:xbox:995529372599599234>", "Xbox Gamer");
		roleInfo.put("🥽", "VR Gamer");
		roleInfo.put("📱", "Mobile Gamer");

		output = produceStandardSequence(event, "**What platform do you game on?**\n*These roles cannot be pinged but are used for games that are platform specific to it.\n"
				+ "Each role will activate a text channel dedicated to the selected system.*\n", roleInfo);

		embed.setTitle("╔══════════════════╗\n"
				+ "      𝙂𝘼𝙈𝙄𝙉𝙂 𝙋𝙇𝘼𝙏𝙁𝙊𝙍𝙈𝙎\n"
				+ "╚══════════════════╝");
		embed.setDescription(output);
		embed.setColor(0x0d14cc);
		embed.setFooter(null, event.getMember().getUser().getAvatarUrl());

		Message msg = event.getChannel().sendMessageEmbeds(embed.build()).complete();
		msg.addReaction(Emoji.fromFormatted("🖥️")).queue();
		msg.addReaction(Emoji.fromCustom("nintendo", 995529374038229092L, false)).queue();
		msg.addReaction(Emoji.fromCustom("playstation", 995529373232939140L, false)).queue();
		msg.addReaction(Emoji.fromCustom("xbox", 995529372599599234L, false)).queue();
		msg.addReaction(Emoji.fromFormatted("🥽")).queue();
		msg.addReaction(Emoji.fromFormatted("📱")).queue();
		embed.clear();
		createMessageGames(event);
	}

	private void createMessageGames(StringSelectInteractionEvent event)
	{
		String output = "";
		LinkedHashMap<String, String> roleInfo = new LinkedHashMap<>();

		roleInfo.put("<:amongus:995529375531401266>", "Among Us");
		roleInfo.put("<:ark:996293825695854672>", "Ark: Survival Evolved");
		roleInfo.put("🔪", "Dead By Daylight");
		roleInfo.put("<:fallguys:995529379000094811>", "Fall Guys");
		roleInfo.put("<:fortnite:995529367767748659>", "Fortnite");
		roleInfo.put("<:grass_block:995878092226691182>", "Minecraft");
		roleInfo.put("🌌","Multiverse");
		roleInfo.put("<:overwatch:1020510934596325407>","Overwatch 2");
		roleInfo.put("<:paladins:1020510984407875615>", "Paladins");
		roleInfo.put("🐼", "Super Animal Royale");
		roleInfo.put("💬", "VRChat");

		output = produceStandardSequence(event, "**What cross-play games do you play?**\n*Each role will activate a text channel dedicated to that game.\n"
				+ "These roles can be pinged.\nFurther info on more but not all cross-play titles can be found at: <https://crossplaygames.com/games>*\n", roleInfo);

		embed.setTitle("╔═══════╗\n"
				+ "      𝙂𝘼𝙈𝙀𝙎\n"
				+ "╚═══════╝");
		embed.setDescription(output);
		embed.setColor(0x45a026);
		embed.setFooter(null, event.getMember().getUser().getAvatarUrl());

		Message msg = event.getChannel().sendMessageEmbeds(embed.build()).complete();

		msg.addReaction(Emoji.fromCustom("amongus", 995529375531401266L, false)).queue();
		msg.addReaction(Emoji.fromCustom("ark", 996293825695854672L, false)).queue();
		msg.addReaction(Emoji.fromFormatted("🔪")).queue();
		msg.addReaction(Emoji.fromCustom("fallguys", 995529379000094811L, false)).queue();
		msg.addReaction(Emoji.fromCustom("fortnite", 995529367767748659L, false)).queue();
		msg.addReaction(Emoji.fromCustom("grass_block", 995878092226691182L, false)).queue();
		msg.addReaction(Emoji.fromFormatted("🌌")).queue();
		msg.addReaction(Emoji.fromCustom("overwatch", 1020510934596325407L, false)).queue();
		msg.addReaction(Emoji.fromCustom("paladins", 1020510984407875615L, false)).queue();
		msg.addReaction(Emoji.fromFormatted("🐼")).queue();
		msg.addReaction(Emoji.fromFormatted("💬")).queue();
		embed.clear();
		createMessageActivities(event);
	}

	private void createMessageActivities(StringSelectInteractionEvent event)
	{
		String output = "";
		LinkedHashMap<String, String> roleInfo = new LinkedHashMap<>();
		roleInfo.put("🍿", "Watch Party");
		roleInfo.put("🎵", "Music Hangout");
		roleInfo.put("📹", "Chat Party");
		roleInfo.put("🎉", "Other Events 🎉");

		output = produceStandardSequence(event, "**What activities do you want to join?**\n*These roles can be pinged.*\n", roleInfo);

		embed.setTitle("╔═══════════╗\n"
				+ "      𝘼𝘾𝙏𝙄𝙑𝙄𝙏𝙄𝙀𝙎\n"
				+ "╚═══════════╝");
		embed.setDescription(output);
		embed.setColor(0xffff00);
		embed.setFooter(null, event.getMember().getUser().getAvatarUrl());

		Message msg = event.getChannel().sendMessageEmbeds(embed.build()).complete();

		msg.addReaction(Emoji.fromFormatted("🍿")).queue();
		msg.addReaction(Emoji.fromFormatted("🎵")).queue();
		msg.addReaction(Emoji.fromFormatted("📹")).queue();
		msg.addReaction(Emoji.fromFormatted("🎉")).queue();
		embed.clear();

		if (tigerguardDB.checkRow("color_roles", "guild_id", guild.getIdLong()))
		{
			createColorSelector(event);
		}
		else
		{
			createMessageCanWeGetMore(event);
		}
	}

	private void createColorSelector(StringSelectInteractionEvent event)
	{
		String output = "";
		InputStream rainbow = getClass().getResourceAsStream("/res/misc/rainbow.png");

		LinkedHashMap<String, String> roleInfo = new LinkedHashMap<>();
		roleInfo.put("🍎", "Red");
		roleInfo.put("🥭", "Red Orange");
		roleInfo.put("🍊", "Orange");
		roleInfo.put("🍋", "Orange Yellow");
		roleInfo.put("🍌", "Yellow");
		roleInfo.put("🍈", "Lime");
		roleInfo.put("🥦", "Green");
		roleInfo.put("🧊", "Aqua");
		roleInfo.put("💧", "Turquoise");
		roleInfo.put("🍶", "Blue");
		roleInfo.put("🍇", "Purple");
		roleInfo.put("🎂", "Pink");
		roleInfo.put("🍑", "Fuchsia");
		roleInfo.put("🍓", "Dark Red");
		roleInfo.put("🥥", "Brown");
		roleInfo.put("🥑", "Dark Green");
		roleInfo.put("🫐", "Dark Blue");
		roleInfo.put("🍆", "Dark Purple");

		output = produceStandardSequence(event, "**Below are colors you can choose to have your name appear as!**\n*Only 1 can be chosen at any given time.*\n", roleInfo);

		embed.setThumbnail("attachment://rainbow.png");
		embed.setTitle("╔════════╗\n"
				+ "      𝘾𝙊𝙇𝙊𝙍𝙎\n"
				+ "╚════════╝");
		embed.setDescription(output + "\n\n🧽 ➤ Remove color without choosing another");
		embed.setColor(0x96A0FF);
		embed.setFooter(null, event.getMember().getUser().getAvatarUrl());

		event.getChannel().sendMessageEmbeds(embed.build()).addFiles(FileUpload.fromData(rainbow, "rainbow.png")).queue(msg -> {
			msg.addReaction(Emoji.fromFormatted("🍎")).queue();
			msg.addReaction(Emoji.fromFormatted("🥭")).queue();
			msg.addReaction(Emoji.fromFormatted("🍊")).queue();
			msg.addReaction(Emoji.fromFormatted("🍋")).queue();
			msg.addReaction(Emoji.fromFormatted("🍌")).queue();
			msg.addReaction(Emoji.fromFormatted("🍈")).queue();
			msg.addReaction(Emoji.fromFormatted("🥦")).queue();
			msg.addReaction(Emoji.fromFormatted("🧊")).queue();
			msg.addReaction(Emoji.fromFormatted("💧")).queue();
			msg.addReaction(Emoji.fromFormatted("🍶")).queue();
			msg.addReaction(Emoji.fromFormatted("🍇")).queue();
			msg.addReaction(Emoji.fromFormatted("🎂")).queue();
			msg.addReaction(Emoji.fromFormatted("🍑")).queue();
			msg.addReaction(Emoji.fromFormatted("🍓")).queue();
			msg.addReaction(Emoji.fromFormatted("🥥")).queue();
			msg.addReaction(Emoji.fromFormatted("🥑")).queue();
			msg.addReaction(Emoji.fromFormatted("🫐")).queue();
			msg.addReaction(Emoji.fromFormatted("🍆")).queue();
			msg.addReaction(Emoji.fromFormatted("🧽")).queue();
		});
		embed.clear();
		createMessageCanWeGetMore(event);
	}

	private void createMessageCanWeGetMore(StringSelectInteractionEvent event)
	{
		String output = "";

		output = "Make sure to begin at the top and work your way down! If there is something we missed or didn't cover that you want to see as a role, then:\n\n"
				+ "**contact **➞** ModMail / Staff!**\n\n If what you're seeking is golden then we will update our systems and enhance the server with your request!";

		embed.setTitle("╔════════════════════╗\n"
				+ "      𝙎𝙊𝙈𝙀𝙏𝙃𝙄𝙉𝙂 𝙈𝙄𝙎𝙎𝙄𝙉𝙂?\n"
				+ "╚════════════════════╝");
		embed.setDescription(output);
		embed.setColor(0xff9900);
		embed.setFooter(null, event.getMember().getUser().getAvatarUrl());

		event.getChannel().sendMessageEmbeds(embed.build()).complete();
		embed.clear();
	}
}
