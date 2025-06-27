package lanoxkododev.tigerguard.events;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.audio.AudioComplex;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import lanoxkododev.tigerguard.pagination.Pages;
import lanoxkododev.tigerguard.roles.RoleManagement;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.FileUpload;

public class ButtonClickEvents extends ListenerAdapter {

	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerLogs logger = new TigerLogs();
	Pages pages = Pages.getInstance();

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event)
	{
		event.deferReply().queue(); //Apparently needed by 'roleembed' events way below. Not sure where to place for now so here be the earliest for no particular reason. Both love and hate REST event handling.
		Guild guild = event.getGuild();
		Member member = event.getMember();
		List<Button> buttons = new ArrayList<>();

		MessageEmbed emb1 = null;
		String buttonName = event.getButton().getId();

		try
		{
			emb1 = event.getMessage().getEmbeds().get(0);
		}
		catch (Exception e) {}

		Integer val = null;
		if (emb1 != null)
		{
			if (emb1.getFooter() != null && (buttonName != "embedCreatorConfirm" || buttonName != "embedCreatorCancel"))
			{
				if (!buttonName.equals("embedCreatorConfirm") && !buttonName.equals("embedCreatorCancel") && !buttonName.equals("roleembed-confirm")
					&& !buttonName.equals("roleembed-cancel")) val = Integer.parseInt(emb1.getFooter().getText().substring(5));
			}
		}

		String[] buttonDivider = buttonName.split("-", 2);
		String buttonType = buttonDivider[0];
		String buttonVariant = buttonDivider[1];

		if (TigerGuard.isDebugMode())
		{
			logger.log(LogType.INFO, "Button pushed: " + buttonVariant + " && val=" + val);
		}

		switch (buttonType)
		{
			case "poll":
				switch (buttonVariant)
				{
					case "vote-yay":
						event.replyEmbeds(embedder.simpleEmbed("Thank you for casting your selection of '**Yay**'", null, null, ColorCodes.CONFIRMATION,
							"I will forward this to the poll box for when the results are ready to be read.\nIf you change your mind of your selection, you may switch it at any time while the poll is open!"))
							.setEphemeral(true).queue();
							tigerGuardDB.pollVoteUpdate(event.getMessageIdLong(), event.getMember().getIdLong(), 'y');
						break;
					case "vote-abstain":
						event.replyEmbeds(embedder.simpleEmbed("Thank you for casting your selection of '**Abstain**'", null, null, ColorCodes.CONFIRMATION,
							"I will forward this to the poll box for when the results are ready to be read.\nIf you change your mind of your selection, you may switch it at any time while the poll is open!"))
							.setEphemeral(true).queue();
							tigerGuardDB.pollVoteUpdate(event.getMessageIdLong(), event.getMember().getIdLong(), 'a');
						break;
					case "vote-nay":
						event.replyEmbeds(embedder.simpleEmbed("Thank you for casting your selection of '**Nay**'", null, null, ColorCodes.CONFIRMATION,
							"I will forward this to the poll box for when the results are ready to be read.\nIf you change your mind of your selection, you may switch it at any time while the poll is open!"))
							.setEphemeral(true).queue();
							tigerGuardDB.pollVoteUpdate(event.getMessageIdLong(), event.getMember().getIdLong(), 'n');
						break;
					case "new":
						event.editMessageEmbeds(embedder.simpleEmbed("How long should this poll last?", null, null, ColorCodes.POLL, "Select the time length type from the dropdown menu below.\nThe options range from 5 minutes up to 7 days."))
						.setActionRow(StringSelectMenu.create("poll-new-time")
							.addOption("5 Minutes", "5-minute").addOption("10 Minutes", "10-minute").addOption("15 Minutes", "15-minute").addOption("30 Minutes", "30-minute").addOption("45 Minutes", "45-minute")
							.addOption("1 Hour", "1-hour").addOption("3 Hours", "3-hour").addOption("6 Hours", "6-hour").addOption("12 Hours", "12-hour").addOption("1 Day", "1-day").addOption("2 Days", "2-day")
							.addOption("3 Days", "3-day").addOption("4 Days", "4-day").addOption("5 Days", "5-day").addOption("6 Days", "6-day").addOption("7 Days", "7-day").build()).queue();
						break;
					//case "poll-close": - re-add when a more preferred handler for closing polls early has been designed.
					//	event.editMessageEmbeds(embedder.simpleEmbed("Which poll would you like to close?", null, null, ColorCodes.POLL, "Select a poll from the dropdown menu below.")).queue();
					//	break;
					case "duo":
						event.replyModal(new ModalEvents().DuoPoll()).queue(_ -> event.getHook().deleteOriginal().queue());
						break;
					case "trio":
						buttons.add(Button.secondary("poll-trioA", "Option A"));
						buttons.add(Button.secondary("poll-trioB", "Option B"));
						event.editMessageEmbeds(embedder.simpleEmbed("Which kind of Yay/Nay/Abstain polls is needed?", "Please review this carefully", null, ColorCodes.POLL,
							"All Trio polls will have a __winning threshold condition of 51% of all votes cast__ , ie a basic majority to pass. Abstains do not mean nullifed votes, though how you count them will affect results:\n\n"
							+ "A) Abstains do NOT affect the final result. Passing = Yay > Nay OR Nay > Yay\n"
							+ "B) Abstains DO affect the final result. Passing = Yay > Nay + Abstain OR Nay + Abstain > Yay\n\n"
							+ "For a poll to be counted as 'Pass', it must get 51% in-favor votes (majority yay). Otherwise 50% or less of nay plus abstain votes leads to a fail."))
							.setActionRow(buttons).queue();
						break;
					case "trioA":
						event.replyModal(new ModalEvents().TrioPoll('A')).queue(_ -> event.getHook().deleteOriginal().queue());
						break;
					case "trioB":
						event.replyModal(new ModalEvents().TrioPoll('B')).queue(_ -> event.getHook().deleteOriginal().queue());
						break;
				}
				break;
			case "rankcustom":
				switch (buttonVariant)
				{
					case "start":
						{
							InputStream image = pages.getImageAsStream(0);
							event.editMessageEmbeds(embedder.paginater(pages.rankCustomizerPages(0))).setFiles(FileUpload.fromData(image, "image.png")).queue();
						}
						break;
					case "prev":
						if ((val == 0) || (val - 1 == 0))
						{
							InputStream image = pages.getImageAsStream(0);
							event.editMessageEmbeds(embedder.paginater(pages.rankCustomizerPages(0))).setFiles(FileUpload.fromData(image, "image.png")).queue();
						}
						else
						{
							InputStream image = pages.getImageAsStream(val - 1);
							event.editMessageEmbeds(embedder.paginater(pages.rankCustomizerPages(val - 1))).setFiles(FileUpload.fromData(image, "image.png")).queue();
						}
						break;
					case "next":
						if ((val == 6) || (val + 1 == 6) || (val + 1 > 6))
						{
							InputStream image = pages.getImageAsStream(pages.imageListSize());
							event.editMessageEmbeds(embedder.paginater(pages.rankCustomizerPages(5))).setFiles(FileUpload.fromData(image, "image.png")).queue(a -> {
								a.sendMessageEmbeds(embedder.simpleEmbed("You've hit the end of the pages mate", null, null, ColorCodes.MEH_NOTICE,
									"Looks like you've reached the end of the available pagination system here. If you haven't made a choice and still want to simply click the other way back through! "
									+ "Otherwise simply dismiss these embeds to remove them~")).setEphemeral(true).queue();
							});
						}
						else
						{
							InputStream image = pages.getImageAsStream(val + 1);
							event.editMessageEmbeds(embedder.paginater(pages.rankCustomizerPages(val + 1))).setFiles(FileUpload.fromData(image, "image.png")).queue();
						}
						break;
					case "end":
						{
							InputStream image = pages.getImageAsStream(pages.imageListSize());
							event.editMessageEmbeds(embedder.paginater(pages.rankCustomizerPages(5))).setFiles(FileUpload.fromData(image, "image.png")).queue();
						}
						break;
					case "select":
						if (val != 0)
						{
							tigerGuardDB.updateUserRankImage(member.getIdLong(), val);
							event.replyEmbeds(embedder.simpleEmbed("Image selected", null, null, ColorCodes.FINISHED, "I have recorded your selection and marked it accordingly. Next time you or I run a rank event, this background will appear!")).setEphemeral(true).queue();
						}
						else
						{
							event.replyEmbeds(embedder.simpleEmbed("Pardon, but this image here is view-only", null, null, ColorCodes.UNABLE, "Sorry to trick you but the image seen here is just a placeholder for all the other nice photos you could set :)")).setEphemeral(true).queue();
						}
						break;
				}
				break;
			case "nsfw":
				Long nsfwRoleID = Long.valueOf(tigerGuardDB.getGuildNSFWStatusRole(guild.getIdLong()));

				if (nsfwRoleID != null)
				{
					List<Role> memberRoles = member.getRoles();

					switch (buttonVariant)
					{
						case "provision":
							if (!memberRoles.contains(guild.getRoleById(nsfwRoleID)))
							{
								guild.addRoleToMember(member, guild.getRoleById(nsfwRoleID)).queue();
								event.replyEmbeds(embedder.simpleEmbed("NSFW Access granted", null, null, ColorCodes.NSFW, "You now have <@&" + nsfwRoleID + "> role, enjoy~ ;)")).setEphemeral(true).queue();
							}
							else
							{
								event.replyEmbeds(embedder.simpleEmbed("NSFW Access already granted!", null, null, ColorCodes.NSFW, "There's nothing more needed here, carry on with your day mate :)")).setEphemeral(true).queue();
							}
							break;
						case "deprovision":
							if (memberRoles.contains(guild.getRoleById(nsfwRoleID)))
							{
								guild.removeRoleFromMember(member, guild.getRoleById(nsfwRoleID)).queue();
								event.replyEmbeds(embedder.simpleEmbed("NSFW Access removed", null, null, ColorCodes.NSFW, "You will no longer see the NSFW channels. If you wish to re-enable this at any time, please utilize the other button from the NSFW embed!")).setEphemeral(true).queue();
							}
							else
							{
								event.replyEmbeds(embedder.simpleEmbed("NSFW Access not in your role list!", null, null, ColorCodes.NSFW, "There's nothing more needed here for you do not have the NSFW Access role. Carry on with your day mate :)")).setEphemeral(true).queue();
							}
							break;
					}
				}
				else
				{
					event.replyEmbeds(embedder.simpleEmbed("Error: Guild's NSFW role missing or not set", null, null, ColorCodes.ERROR,
						"Currently I do not seem to have notes on what this server's expected NSFW role is. Either this server has not set it prevously or it has been taken away by some dasterdly yarnball whom rolled by and pulled it away, bugs always come with strings attached. *sigh*\n\nIf your server's administrators are not aware, advise them to use my config command to specify a NSFW role so this feature can be utilized.")).setEphemeral(true).queue();
				}
				break;
			case "roleembed":
				switch (buttonVariant)
				{
					case "confirm":
						{
							tigerGuardDB.setReactionRoleEmbed(guild.getIdLong(), "custom");
							
							logger.log(LogType.DEBUG, "Message ID: " + event.getMessage().getIdLong());
							
							//event.getMessage().delete().queue();
							//event.getChannel().sendMessageEmbeds((embedder.simpleEmbed("Confirmation accepted!", null, null, ColorCodes.FINISHED, "The embed has been saved and can be used now using the command flow:\n"
							//	+ "**/tg-update-config** -> **Embed manager** -> **Print embed** -> *Name of embed*"))).queue();
							
							deleteMessage(event);
							//event.getMessage().delete().queue();
							event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Confirmation accepted!", null, null, ColorCodes.FINISHED, "The embed has been saved and can be used now using the command flow:\n"
								+ "**/tg-update-config** -> **Embed manager** -> **Print embed** -> *Name of embed*")).setEphemeral(true).queue();
							
							//event.getMessage().delete().queue(a -> {
							//	event.replyEmbeds(embedder.simpleEmbed("Confirmation accepted!", null, null, ColorCodes.FINISHED, "The embed has been saved and can be used now using the command flow:\n"
							//		+ "**/tg-update-config** -> **Embed manager** -> **Print embed** -> *Name of embed*")).setEphemeral(true).queue();
							//});
							tigerGuardDB.deleteRow("tempEmbedData", "guild", guild.getIdLong());
						}
						break;
					case "cancel":
						//event.getMessage().delete().queue(a -> {
						//	event.replyEmbeds(embedder.simpleEmbed("Cancelling request!", null, null, ColorCodes.FINISHED, "The embed has not been saved as requested.")).setEphemeral(true).queue();
						//});
						//event.getMessage().delete().queue();
						deleteMessage(event);
						event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Cancelling request!", null, null, ColorCodes.FINISHED, "The embed has not been saved as requested.")).setEphemeral(true).queue();
						tigerGuardDB.deleteRow("tempEmbedData", "guild", guild.getIdLong());
						break;
				}
				break;
			case "music":
				switch (buttonVariant)
				{
					case "pause-resume":
						if (!member.getVoiceState().inAudioChannel()) exceptionReason(event);
						else
						{
							if (event.getButton().getLabel().contains("Pause"))
							{
								AudioComplex.getInstance().acquireMusicManager(guild).setPauseStatus(true);
								event.editButton(Button.primary("music-pause-resume", "Resume").withEmoji(Emoji.fromCustom(":play:", 1040148261702471741L, false))).queue();
							}
							else
							{
								AudioComplex.getInstance().acquireMusicManager(guild).setPauseStatus(false);
								event.editButton(Button.primary("music-pause-resume", "Pause").withEmoji(Emoji.fromCustom(":pause:", 1040148262449061898L, false))).queue();
							}
						}
						break;
					case "skip":
						if (!member.getVoiceState().inAudioChannel()) exceptionReason(event);
						
						AudioComplex.getInstance().acquireMusicManager(guild).skip();
						event.getMessage().delete().queue();
						event.replyEmbeds(embedder.simpleEmbed(null, "Skipping to next song", TigerGuard.getTigerGuard().getSelf().getEffectiveAvatarUrl(),
							ColorCodes.CONFIRMATION, "This message will auto-delete in 20 seconds.")).queue(a -> { a.deleteOriginal().queueAfter(20,  TimeUnit.SECONDS); });
						//TODO else PlayerLogic.getInstance().skip(guild); - reference for how the future skip logic might be handled
						break;
					case "stop":
						if (!member.getVoiceState().inAudioChannel()) exceptionReason(event);
						else
						{
							AudioComplex.getInstance().getClient().getOrCreateLink(event.getGuild().getIdLong()).destroy();
							AudioComplex.getInstance().acquireMusicManager(guild).stop();

							event.replyEmbeds(embedder.simpleEmbed(null, "║ Leaving channel due to stop button usage", TigerGuard.getTigerGuard().getSelf().getEffectiveAvatarUrl(),
								ColorCodes.CONFIRMATION, "Shutting down audio instance and queue.\nThis message will auto-delete in 20 seconds."))
								.queue(a -> { a.deleteOriginal().queueAfter(20,  TimeUnit.SECONDS); });
						}
						break;
				}
				break;
			case "tigerguardfeature":
				deleteMessage(event);
				
				switch (buttonVariant)
				{
					case "delete-colors-confirm":
						//event.getMessage().delete().queue();
						RoleManagement.getINSTANCE().deleteRoles(event, "colors");
						break;
					case "delete-colors-cancel":
						//deleteMessage(event);
						event.getHook().sendMessageEmbeds(embedder.simpleEmbed("You selected to not delete the color roles", null, null, ColorCodes.CONFIRMATION,
							"I will cancel the deletion event.\nShould you need to delete the roles, simply run this command again and select the 'Confirm' option.\n\n" +
							"This message will auto-delete in 30 seconds.")).setEphemeral(true).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
						break;
					case "delete-levels-confirm":
						//event.getMessage().delete().queue();
						RoleManagement.getINSTANCE().deleteRoles(event, "levels");
						break;
					case "delete-levels-cancel":
						//event.getMessage().delete().queue();
						event.getHook().sendMessageEmbeds(embedder.simpleEmbed("You selected to not delete the level roles", null, null, ColorCodes.CONFIRMATION,
								"I will cancel the deletion event.\nShould you need to delete the roles, simply run this command again and select the 'Confirm' option.\n\n" +
								"This message will auto-delete in 30 seconds.")).setEphemeral(true).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
						break;
				}
				break;
		}
	}
	
	private void deleteMessage(ButtonInteractionEvent event)
	{
		event.getMessage().delete().queue();
	}

	private void exceptionReason(ButtonInteractionEvent event)
	{
		event.replyEmbeds(embedder.simpleEmbed("You are not present in a voice channel for this action.", null, null, ColorCodes.ERROR,
			"You must be in a voice channel to utilize the music-button features.")).setEphemeral(true).queue();
	}
}