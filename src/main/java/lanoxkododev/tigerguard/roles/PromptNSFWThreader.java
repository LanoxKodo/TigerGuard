package lanoxkododev.tigerguard.roles;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

public class PromptNSFWThreader extends Thread {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerLogs logger = new TigerLogs();
	StringSelectInteractionEvent event;
	Guild guild;

	public PromptNSFWThreader(StringSelectInteractionEvent inputEvent)
	{
		event = inputEvent;
		guild = inputEvent.getGuild();
	}

	@Override
	public void run()
	{
		readyPrompt();
	}

	private void readyPrompt()
	{
		MessageChannel channel = event.getChannel();
		EmbedBuilder embed = new EmbedBuilder();
		InputStream nsfwIcon = getClass().getResourceAsStream("/assets/misc/18.png");
		InputStream nsfwBanner = getClass().getResourceAsStream("/assets/misc/nsfwChannelBanner.png");

		List<Button> button = new ArrayList<>();
		button.add(Button.primary("nsfw-provision", "Yea, provide me NSFW Access").withEmoji(Emoji.fromFormatted("<:18:1095600870441357383>")));
		button.add(Button.secondary("nsfw-deprovision", "Remove NSFW Access").withEmoji(Emoji.fromFormatted("🧽")));

		Long nsfwRole = tigerguardDB.getGuildNSFWStatusRole(event.getGuild().getIdLong());
		Long staffRole = tigerguardDB.getGuildStaffRole(event.getGuild().getIdLong());
		Long suppStaffRole = tigerguardDB.getGuildSupportingStaffRole(event.getGuild().getIdLong());

		if (nsfwRole == 0)
		{
			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("Server has not set a NSFW role", null, null, ColorCodes.MEH_NOTICE,
				"This server has not set up a role for allowing NSFW access to NSFW areas in my config. Please first set up a role for this purpose and " +
				"then point to it using my command \"**/tg-update-config**\" > \"**Identify server-made permission roles**\" > \"**Update value for NSFW Access role**\"." +
				"\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
		}
		else
		{
			String description = "\n\nAccess to NSFW areas requires you to have the <@&" + nsfwRole + "> role for this server.\n\nThis role can be obtained by clicking the below button, but before you do so please ensure that you:"
				+ "\n**✓ Required:** You agree that you are 18+. If it is found out that you are underage and have this role, you will be at risk of a ban by the directive of this server's nsfw/age policies."
				+ "\n\nThe following are prohibited:\n**✗  No:** Content depicting characters of underage status. This kind of content is not allowed under any circumstances.\""
				+ "\n**✗  No:** Revenge porn or harassment of other content posted."
				+ "\n\nIf you find people violating these or causing other distrubances then contact <@&" + staffRole + ">";

			if (suppStaffRole != 0)
			{
				description += " or <@&" + suppStaffRole + ">";
			}

			embed.setTitle("__**NSFW Channel guidelines**__")
				.setColor(ColorCodes.NSFW.value)
				.setDescription(description + " immediately!")
				.setThumbnail("attachment://18.png")
				.setImage("attachment://nsfwChannelBanner.png");
			channel.sendMessageEmbeds(embed.build()).addFiles(FileUpload.fromData(nsfwIcon, "18.png"),FileUpload.fromData(nsfwBanner, "nsfwChannelBanner.png")).setActionRow(button).queue();
		}
	}
}
