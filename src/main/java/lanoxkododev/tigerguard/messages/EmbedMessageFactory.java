package lanoxkododev.tigerguard.messages;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;

import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class EmbedMessageFactory {

	/**
	 * MessageEmbed provider with simple variables.
	 *
	 * @param title		 - The title text, appears in bold.
	 * @param author	 - The author for this embed.
	 * @param authorIcon - The avatarURL of the user this embed should show.
	 * @param color		 - The color the embed edge should show.
	 * @param text		 - The description text contents.
	 * @return
	 */
	public MessageEmbed simpleEmbed(String title, String author, String authorIcon, ColorCodes color, String text)
	{
		return new EmbedBuilder().setTitle(title).setAuthor(author, null, authorIcon).setColor(color.value).setDescription(text).build();
	}

	public MessageEmbed pollDuoEmbed(String title, ColorCodes color, String body, Integer yay, Integer nay)
	{
		return new EmbedBuilder().setTitle(title).setColor(color.value).setDescription(body)
			.addField("Yay", yay.toString(), true).addField("Nay", nay.toString(), true).build();
	}

	public MessageEmbed pollTrioEmbed(String title, ColorCodes color, String body, Integer yay, Integer nay, Integer abs)
	{
		return new EmbedBuilder().setTitle(title).setColor(color.value).setDescription(body)
			.addField("Yay", yay.toString(), true).addField("Nay", nay.toString(), true).addField("Abstain", abs.toString(), true).build();
	}

	/**
	 * MessageEmbed provider similar to {@link #simpleEmbed(String, String, ColorCodes, String)} but with image parameters.
	 *
	 * @param title		 - The title text, appears in bold.
	 * @param author	 - The author for this embed.
	 * @param authorIcon - The avatarURL of the user this embed should show.
	 * @param color		 - The color the embed edge should show.
	 * @param text		 - The description text contents.
	 * @param thumbnail	 - The photo to appear in the thumbnail slot.
	 * @param banner	 - The photo to appear in the message image slot.
	 * @return
	 */
	public MessageEmbed imageEmbed(String title, String author, String authorIcon, ColorCodes color, String text, String thumbnail, String banner)
	{
		return new EmbedBuilder().setTitle(title).setAuthor(author, null, authorIcon).setColor(color.value).setDescription(text).setThumbnail(thumbnail).setImage(banner).build();
	}

	/**
	 * Paginator for initial index values.
	 *
	 * @param input - Values in order: Title, ColorCode, Description, Page counter
	 * @return
	 */
	public MessageEmbed paginaterIndex(Quartet<String, ColorCodes, String, String> input)
	{
		return new EmbedBuilder().setTitle(input.getValue0()).setColor(input.getValue1().value).setDescription(input.getValue2()).setFooter(input.getValue3()).build();
	}

	/**
	 * Paginator for all other index values that do not fit from {@link #paginaterIndex(Quartet)}
	 *
	 * @param input - Values in order: Title, ColorCode, Image, Description, Page counter
	 * @return
	 */
	public MessageEmbed paginater(Quintet<String, ColorCodes, String, String, String> input)
	{
		return new EmbedBuilder().setTitle(input.getValue0()).setColor(input.getValue1().value).setImage("attachment://image.png").setDescription(input.getValue3()).setFooter(input.getValue4()).build();
	}

	public MessageEmbed regularEmbed(String titleIn, String colorIn, String bodyIn)
	{
		String title = null;
		String color = null;
		String description = null;

		if (titleIn != null && titleIn != "") title = titleIn;
		if (colorIn != null) color = colorIn;
		else color = ColorCodes.TIGER_FUR.toHexString();
		if (bodyIn != null) description = bodyIn;

		if (title == null || title.isEmpty()) return new EmbedBuilder().setColor(Color.decode(color)).setDescription(description.replace("\\n", "\n")).build();
		else return new EmbedBuilder().setTitle(title.replace("\\n", "\n")).setColor(Color.decode(color)).setDescription(description.replace("\\n", "\n")).build();
	}

	/**
	 * A basic embed
	 *
	 * @param title	- The title for the ember
	 * @param color	- The ColorCode type for coloring
	 * @param body	- The regular body text of the embed
	 * @return
	 */
	public MessageEmbed tigerEmbed(String title, ColorCodes color, String body)
	{
		return new EmbedBuilder().setTitle(title).setColor(Color.decode(color.toHexString())).setDescription(body.replace("\\n", "\n")).build();
	}

	public MessageEmbed roleEmbed(Triplet<String, String, String> input, ArrayList<String> roles, ArrayList<String> emojis)
	{
		String str = "";
		if (input.getValue2() != null) str += input.getValue2() + "\n";

		for (int a = 0; a < roles.size(); a++)
		{
			str += "\n" + emojis.get(a) + " ➤ " + roles.get(a);
		}

		return new EmbedBuilder().setTitle(input.getValue0().replace("\\n", "\n")).setColor(Color.decode(input.getValue1())).setDescription(str.replace("\\n", "\n")).build();
	}

	public MessageEmbed colorEmbed(Triplet<String, String, String> input, ArrayList<Long> roles, ArrayList<String> emojis)
	{
		String str = "";
		if (input.getValue2() != null) str += input.getValue2() + "\n";

		for (int a = 0; a <= roles.size(); a++)
		{
			if (emojis.get(a) == "🧽") str += "\n\n" + emojis.get(a) + " ➤ " + "Remove any color role without selecting another.";
			else str += "\n" + emojis.get(a) + " ➤ <@&" + roles.get(a) + ">";
		}

		return new EmbedBuilder().setTitle(input.getValue0().replace("\\n", "\n")).setColor(Color.decode(input.getValue1())).setDescription(str.replace("\\n", "\n")).setThumbnail("attachment://rainbow.png").build();
	}

	public MessageEmbed createHelpEmbed()
	{
		String desc = "Ohh, asking zis big cat for some help, eh? Hah, well here is what I can help with:\n\n"
				+ "Need to configure a channel/category/role(s)/etc for me to assist with?\n"
				+ "-Use: **/tg-update-config**, then select the relevant sub-menu selections.\n\n"

				+ "Need to review what I have on file for zis server?\n"
				+ "-Use: **/tg-view-config**\n\n"

				+ "Need to create a custom embed for reaction roles your server?\n"
				+ "-Use: **/tg-create-reaction-embed**\n\n"

				+ "**Something else?**\n"
				+ "Reach out on my support server (https://discord.gg/Gd8NDkyu4V) to get help from my dev or provide suggestions!";

		return new EmbedBuilder().setAuthor("TigerGuard Help").setColor(ColorCodes.INFO.value).setDescription(desc).build();
	}

	/**
	 * EmbedMessage provider for music embeds.
	 *
	 * @param trackInfo	- The AudioTrackInfo object for the track being played
	 * @param queueSize	- The size of the queue remaining
	 * @return
	 */
	public MessageEmbed musicNowPlaying(TrackInfo info, int queueSize)
	{
		long durationInit = info.getLength();
		long durationMod = durationInit / 1000;
        long hours = durationMod / 3600;
        if (hours > 0) durationMod = durationMod % 3600;

        long minutes = durationMod / 60;
        long seconds = durationMod % 60;

        String formattedTime = "[";
		if (hours > 0) formattedTime += hours + ":";
		
		if (seconds < 10) formattedTime += minutes + ":0" + seconds + "]";
		else formattedTime += minutes + ":" + seconds + "]";

		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(info.getTitle()).setDescription(info.getAuthor()).setColor(ColorCodes.MUSIC.value).setThumbnail(info.getArtworkUrl())
			.addField("Duration", formattedTime, true).addField("Source", "[link](<" + info.getUri() + ">)", true)
			.addField("In Queue", String.valueOf(queueSize), true);

		return eb.build();
	}

	public List<Button> musicActionRowButtonProvider()
	{
		List<Button> buttons = new ArrayList<>();
		buttons.add(Button.primary("music-pause-resume", "Pause").withEmoji(Emoji.fromCustom(":pause:", 1040148262449061898L, false)));
		buttons.add(Button.primary("music-skip", "Skip").withEmoji(Emoji.fromCustom(":skip:", 1040148263199846400L, false)));
		buttons.add(Button.primary("music-stop", "Stop").withEmoji(Emoji.fromCustom(":stop:", 1040148264349085726L, false)));

		return buttons;
	}

	public MessageEmbed voiceErrorEmbed()
	{
		return new EmbedBuilder().setAuthor("Unable to process this command!").setColor(ColorCodes.N_A.value).setDescription("Sorry, but this command only works while you are in a Voice Channel!").build();
	}

	public MessageEmbed accessErrorEmbed()
	{
		String desc = "I was unable to verify that you can use this command due to lacking any of the following:\n"
				+ "A) You are the server owner.\nB) You have one of the listed roles for this server as set by the owner that may use this command.\n\n"
				+ "If you believe this is in error, refer to your server's administrative users (admin, staff, mod, etc) for further assistance.";

		return new EmbedBuilder().setAuthor("Unable to use the specified command; user configuration setup failure").setColor(ColorCodes.ERROR.value).setDescription(desc).build();
	}
}