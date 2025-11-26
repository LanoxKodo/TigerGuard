package lanoxkododev.tigerguard.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedDeleterThreader;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import lanoxkododev.tigerguard.messages.EmbedPrinterThreader;
import lanoxkododev.tigerguard.roles.RoleManagement;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;

public class SelectEvents extends ListenerAdapter {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();

	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event)
	{
		String selection = event.getValues().get(0);
		InteractionHook hook = event.getHook();
		List<Button> buttons = new ArrayList<>();

		switch (event.getComponentId())
		{
			case "s1-tg-update-config":
				switch (selection)
				{
					case "s2-permission-roles":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Select which role permission type you wish to edit", null, null, ColorCodes.TIGER_FUR, "Each will ask you for an existing role to be selected from."))
							.setActionRow(StringSelectMenu.create("s3-permission-roles")
								.addOption("Update value for Admin Role", "s4-role-admin", "Select the Admin role. (if role has 'adminstrator' permission)")
								.addOption("Update value for Primary Staff Role", "s4-role-primary-staff", "Select the Primary Staff role.")
								.addOption("Update value for Secondary Staff Role", "s4-role-secondary-staff", "Select the Secondary Staff role.")
								.addOption("Update value for Member Role", "s4-role-member", "Select the Member role.")
								.addOption("Update value for NSFW Access Role", "s4-role-nsfw", "Select the NSFW Access role.")
								.build()).queue();
						break;
					case "s2-tigerguard-items":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Select which role system type you wish to edit", null, null, ColorCodes.TIGER_FUR, "Each will prompt you for further options pertaining to the following."))
							.setActionRow(StringSelectMenu.create("s3-tigerguard-items")
							.addOption("Color role configurator", "s4-role-colors", "Add/Repair/Delete color roles for this server.")//submenuColorRoles
							.addOption("Level role configurator", "s4-role-levels", "Add/Repaur/Delete level roles for this server.")//submenuLevelRoles
							.build()).queue();
						break;
					case "s2-categorization-items":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Select which role permission type you wish to edit", null, null, ColorCodes.TIGER_FUR, "Each will ask you for an existing channel or category to be selected from."))
							.setActionRow(StringSelectMenu.create("s3-categorization-items")//tigerguardServerAssitanceItemsSubmenu
								.addOption("Define Announcement Channel", "s4-announcement", "Select the Announcement channel.")

								.addOption("Define Music Channel", "s4-music", "Select the Music text channel.")
								.addOption("Define Level Channel", "s4-level", "Select the Level text channel.")
								.addOption("Define Bot Channel", "s4-bot", "Select the BotSpam text channel.")

								.addOption("Define CustomVC Creation Voice Channel", "s4-customvc-channel", "Select the Voice Channel for CustomVC creation handling.")
								.addOption("Define GuildSize Voice Channel", "s4-size-channel", "Select the Member Counter Voice Channel.")
								.build()).queue();
						break;
					case "s2-embed-manager":
						event.deferEdit().queue();
						serverMessageItemsSection(event, hook);
						break;
				}
				break;
			case "s3-permission-roles":
				switch (selection)
				{
					case "s4-role-admin":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose a role this server uses as an 'administrative' role similar to server owner.", null, null, ColorCodes.TIGER_FUR,
							"**NOTE:** This is a dangerous setting and completely optional. Do not set if there is no admin role, it's not required in 99% of use-cases - use primary-staff-role option instead!"))
							.setActionRow(EntitySelectMenu.create("e1-role-admin", SelectTarget.ROLE).build()).queue();
						break;
					case "s4-role-primary-staff":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the main/primary Staff role for this server.", null, null, ColorCodes.TIGER_FUR, null))
							.setActionRow(EntitySelectMenu.create("e1-role-primary-staff", SelectTarget.ROLE).build()).queue();
						break;
					case "s4-role-secondary-staff":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the secondary Staff role for this server.", null, null, ColorCodes.TIGER_FUR, null))
							.setActionRow(EntitySelectMenu.create("e1-role-secondary-staff", SelectTarget.ROLE).build()).queue();
						break;
					case "s4-role-member":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the regular Member role for this server.", null, null, ColorCodes.TIGER_FUR, null))
							.setActionRow(EntitySelectMenu.create("e1-role-member", SelectTarget.ROLE).build()).queue();
						break;
					case "s4-role-nsfw":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the NSFW permission role for this server.", null, null, ColorCodes.TIGER_FUR, "TigerGuard uses this to block certain actions for non-NSFW users."))
							.setActionRow(EntitySelectMenu.create("e1-role-nsfw", SelectTarget.ROLE).build()).queue();
						break;
				}
				break;
			case "s3-tigerguard-items":
				switch (selection)
				{
					case "s4-role-colors":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Select which operation you'd like to use", null, null, ColorCodes.TIGER_FUR, null))
							.setActionRow(StringSelectMenu.create("s5-color-operations")
								.addOption("Create color roles", "s5-color-operations-create", "Create new/additional color roles.")
								.addOption("Repair & Reset color roles", "s5-color-operations-repair", "Repair missing color roles and set all to their default values.")
								.addOption("Delete color roles", "s5-color-operations-delete", "Delete all color roles. (Will ask for confirmation)")
								.build()).queue();
						break;
					case "s4-role-levels":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Select which operation you'd like to use", null, null, ColorCodes.TIGER_FUR, null))
							.setActionRow(StringSelectMenu.create("s5-level-operations")
								.addOption("Create levelUp roles", "s5-level-operations-create", "Create new/additional level roles.")
								.addOption("Repair levelUp roles", "s5-level-operations-repair", "Repair missing level roles.")
								.addOption("Delete levelUp roles", "s5-level-operations-delete", "Delete all level roles. (Will ask for confirmation)")
								.build()).queue();
						break;
				}
				break;
			case "s3-categorization-items":
				switch (selection)
				{
					case "s4-announcement":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the text channel where announcements can be sent to, if I ever have any", null, null, ColorCodes.TIGER_FUR, "This may be removed later on."))
							.setActionRow(EntitySelectMenu.create("e1-announcement-channel", SelectTarget.CHANNEL).setChannelTypes(ChannelType.TEXT).build()).queue();
						break;
					case "s4-bot-testing":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the text channel where testing or other debug stuff occurs in", null, null, ColorCodes.TIGER_FUR,
							"I will use this as a backup channel if I cannot use a channel for any various issue I run into. It's recommended that this channel be one that is seen only by administrative users if possible."))
							.setActionRow(EntitySelectMenu.create("e1-bot-testing-channel", SelectTarget.CHANNEL).setChannelTypes(ChannelType.TEXT).build()).queue();
						break;
					case "s4-music":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the text channel where music embeds can be sent to", null, null, ColorCodes.TIGER_FUR,
							"This is required for my music feature to work. I create 2 kinds of embeds in this channel: A \"request confirmation\" embed and a \"currently playing\" embed which has buttons for various taks with the current song/queue."))
							.setActionRow(EntitySelectMenu.create("e1-music-channel", SelectTarget.CHANNEL).setChannelTypes(ChannelType.TEXT).build()).queue();
						break;
					case "s4-level":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the text channel where leveling embeds can be sent to", null, null, ColorCodes.TIGER_FUR,
							"This is used for displaying user's level up events primarily."))
							.setActionRow(EntitySelectMenu.create("e1-level-channel", SelectTarget.CHANNEL).setChannelTypes(ChannelType.TEXT).build()).queue();
						break;
					case "s4-bot":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the text channel where general bot 'spam' messages can be sent to", null, null, ColorCodes.TIGER_FUR,
							"This is used mainly for misc items that pertain to debugging and such."))
							.setActionRow(EntitySelectMenu.create("e1-bot-channel", SelectTarget.CHANNEL).setChannelTypes(ChannelType.TEXT).build()).queue();
						break;
					case "s4-customvc-channel":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the voice channel where people can join to have custom voice channels created", null, null, ColorCodes.TIGER_FUR,
							"This channel will be considered the 'Join-To-Create' channel, anyone who joins it will be moved into a vc I create; and once that new vc is left empty I will delete it by myself.\n\n"
							+ "NOTE: Ensure this is in a separate category dedicated for Custom VC's. If you fail to do this and this category the selected channel is in has 'normal' VC's I ***will*** delete these when they too are empty."))
							.setActionRow(EntitySelectMenu.create("e1-customvc-channel", SelectTarget.CHANNEL).setChannelTypes(ChannelType.VOICE).build()).queue();
						break;
					case "s4-size-channel":
						event.deferEdit().queue();
						hook.editOriginalEmbeds(embedder.simpleEmbed("Choose the voice channel where I can show the so-original member count for the server", null, null, ColorCodes.TIGER_FUR,
							"The voice channel will be formatted as: 'Members #'"))
							.setActionRow(EntitySelectMenu.create("e1-size-channel", SelectTarget.CHANNEL).setChannelTypes(ChannelType.VOICE).build()).queue();
						break;
				}
				break;
			case "s3-em-items-with-color":
				switch (selection)
				{
					case "s4-em-print":
						System.out.println("Guild " + event.getGuild().getIdLong() + " is requesting list of embeds.");
						event.deferEdit().queue();
						ArrayList<String> embeds = tigerGuardDB.getEmbedNames(event.getGuild().getIdLong());
						System.out.println("Embeds (DB) size: " + embeds.size());
						Builder selectBuilder = StringSelectMenu.create("s5-em-req-print");

						for (String str : embeds)
						{
							selectBuilder.addOption(str, str);
							System.out.println(str);
						}

						if (selectBuilder.getOptions().size() == 0 || selectBuilder.getOptions() == null)
						{
							hook.editOriginalEmbeds(embedder.simpleEmbed("No embeds currently configured!", null, null, ColorCodes.UNABLE, "Sorry but this option won't work until you create an embed for me to list!\n"
								+ "You can do so by running \"/tg-create-reaction-embed\" and filling in the needed info and then confirming the preview embed to ensure it matches your expectations!\n\n"
								+ "This message will auto-delete in 30 seconds.")).queue(a -> a.delete().queueAfter(30, TimeUnit.SECONDS));
						}
						else
						{
							hook.editOriginalEmbeds(embedder.simpleEmbed("Which embed would you like to print?", null, null, ColorCodes.TIGER_FUR, null)).setActionRow(selectBuilder.build()).queue();
						}
						break;
					case "s4-em-delete":
						event.deferEdit().queue();
						ArrayList<String> embedsDel = tigerGuardDB.getEmbedNames(event.getGuild().getIdLong());
						Builder selectDeleter = StringSelectMenu.create("s5-em-req-delete");

						for (String str : embedsDel)
						{
							selectDeleter.addOption(str, str);
						}

						if (selectDeleter.getOptions().size() == 0 || selectDeleter.getOptions() == null)
						{
							hook.editOriginalEmbeds(embedder.simpleEmbed("No embeds currently configured!", null, null, ColorCodes.UNABLE, "Sorry but this sub-option won't work until you create an embed for me to list!\n"
								+ "You can do so by running \"/tg-create-reaction-embed\" and filling in the needed info and then confirming the preview embed to ensure it matches your expectations!\n\n"
								+ "This message will auto-delete in 30 seconds.")).queue(a -> a.delete().queueAfter(30, TimeUnit.SECONDS));
						}
						else
						{
							hook.editOriginalEmbeds(embedder.simpleEmbed("Which embed would you like to delete?", null, null, ColorCodes.TIGER_FUR, null)).setActionRow(selectDeleter.build()).queue();
						}
						break;
					case "s4-em-prompt-color":
						event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
						RoleManagement.getINSTANCE().stringSelectAction(event, "color");
						break;
					case "s4-em-prompt-nsfw":
						event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
						RoleManagement.getINSTANCE().stringSelectAction(event, "nsfw");
						break;
				}
				break;
			case "s3-em-items-plain":
				switch (selection)
				{
					case "s4-em-print":
						System.out.println("Guild " + event.getGuild().getIdLong() + " is requesting list of embeds.");
						event.deferEdit().queue();
						ArrayList<String> embeds = tigerGuardDB.getEmbedNames(event.getGuild().getIdLong());
						System.out.println("Embeds (DB) size: " + embeds.size());
						Builder selectBuilder = StringSelectMenu.create("s5-em-req-print");

						for (String str : embeds)
						{
							selectBuilder.addOption(str, str);
							System.out.println(str);
						}

						if (selectBuilder.getOptions().size() == 0 || selectBuilder.getOptions() == null)
						{
							hook.editOriginalEmbeds(embedder.simpleEmbed("No embeds currently configured!", null, null, ColorCodes.UNABLE, "Sorry but this option won't work until you create an embed for me to list!\n"
								+ "You can do so by running \"/tg-create-reaction-embed\" and filling in the needed info and then confirming the preview embed to ensure it matches your expectations!\n\n"
								+ "This message will auto-delete in 30 seconds.")).queue(a -> a.delete().queueAfter(30, TimeUnit.SECONDS));
						}
						else
						{
							hook.editOriginalEmbeds(embedder.simpleEmbed("Which embed would you like to print?", null, null, ColorCodes.TIGER_FUR, null)).setActionRow(selectBuilder.build()).queue();
						}
						break;
					case "s4-em-delete":
						event.deferEdit().queue();
						ArrayList<String> embedsDel = tigerGuardDB.getEmbedNames(event.getGuild().getIdLong());
						Builder selectDeleter = StringSelectMenu.create("s5-em-req-delete");

						for (String str : embedsDel)
						{
							selectDeleter.addOption(str, str);
						}

						if (selectDeleter.getOptions().size() == 0 || selectDeleter.getOptions() == null)
						{
							hook.editOriginalEmbeds(embedder.simpleEmbed("No embeds currently configured!", null, null, ColorCodes.UNABLE, "Sorry but this sub-option won't work until you create an embed for me to list!\n"
								+ "You can do so by running \"/tg-create-reaction-embed\" and filling in the needed info and then confirming the preview embed to ensure it matches your expectations!\n\n"
								+ "This message will auto-delete in 30 seconds.")).queue(a -> a.delete().queueAfter(30, TimeUnit.SECONDS));
						}
						else
						{
							hook.editOriginalEmbeds(embedder.simpleEmbed("Which embed would you like to delete?", null, null, ColorCodes.TIGER_FUR, null)).setActionRow(selectDeleter.build()).queue();
						}
						break;
					case "s4-em-prompt-color":
						event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
						RoleManagement.getINSTANCE().stringSelectAction(event, "color");
						break;
					case "s4-em-prompt-nsfw":
						event.getChannel().deleteMessageById(event.getMessageIdLong()).queue();
						RoleManagement.getINSTANCE().stringSelectAction(event, "nsfw");
						break;
				}
				break;
			case "s5-em-req-print":
				event.getMessage().delete().queue();
				event.deferReply().setEphemeral(true).queue();
				hook.sendMessageEmbeds(embedder.simpleEmbed("Requested embed will be printed!", null, null, ColorCodes.CONFIRMATION,
					"If the embed fails to send or some other issue occurs, please reach out on my support server!")).queue();
				new Thread(new EmbedPrinterThreader(event, selection)).start();
				break;
			case "s5-em-req-delete":
				event.getMessage().delete().queue();
				event.deferReply().setEphemeral(true).queue();
				hook.sendMessageEmbeds(embedder.simpleEmbed("Requested embed data will be deleted from my files!", null, null, ColorCodes.CONFIRMATION,
					"If you have any lingering embed messages that pointed to this instance, simply delete them and you're all set for I only logged basic data that did not pertain to the channel(s) it may have been in!")).queue();
				new Thread(new EmbedDeleterThreader(event, selection)).start();
				break;
			case "s5-color-operations":
				switch (selection)
				{
					case "s5-color-operations-create":
						Long messageToDel = event.getMessageIdLong();
						int rolesCount = event.getGuild().getRoles().size()-1;


						if (tigerGuardDB.checkRow("colorRoles", "guild", event.getGuild().getIdLong()))
						{
							if (250-rolesCount-(18-tigerGuardDB.selectColorRolesCount(event.getGuild().getIdLong())) > 0)
							{
								RoleManagement.getINSTANCE().stringSelectAction(event, "repairColor");
							}
							else
							{
								event.replyEmbeds(embedder.simpleEmbed("Role space requirement issue", null, null, ColorCodes.UNABLE, "I'm sorry, but this server has used up enough roles to make my color roles feature unusable.\n"
									+ "I can support a total of 18 color roles, as a result this server must have at least 18 free role slots available. Currently this server has used " + rolesCount + " out of 250 role slots.")).queue();
							}
						}
						else
						{
							if (250-rolesCount > 18)
							{
								event.replyEmbeds(embedder.simpleEmbed("I will now begin creating the 18 color roles", null, null, ColorCodes.CONFIRMATION,
								"Please allow me a moment to configure these roles as requested, I will send a confirmation message once this is finished!\n\n" +
								"This message will auto-delete in 30 seconds.")).queue(msg -> msg.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
								RoleManagement.getINSTANCE().stringSelectAction(event, "createColor");
							}
							else
							{
								event.replyEmbeds(embedder.simpleEmbed("Role space requirement issue", null, null, ColorCodes.UNABLE, "I'm sorry, but this server has used up enough roles to make my color roles feature unusable.\n"
									+ "I can support a total of 18 color roles, as a result this server must have at least 18 free role slots available. Currently this server has used " + rolesCount + " out of 250 role slots.")).queue();
							}
						}

						event.getChannel().deleteMessageById(messageToDel).queue();
						break;
					case "s5-color-operations-repair":
						event.getMessage().delete().queue();
						RoleManagement.getINSTANCE().stringSelectAction(event, "repairColor");
						break;
					case "s5-color-operations-delete":
						event.deferEdit().queue();
						List<Button> colorRoledeletionButtons = new ArrayList<>();
						colorRoledeletionButtons.add(Button.primary("tigerguardfeature-delete-colors-confirm", "Approve").withEmoji(Emoji.fromFormatted("✔")));//deleteColorRolesConfirm
						colorRoledeletionButtons.add(Button.primary("tigerguardfeature-delete-colors-cancel", "Cancel").withEmoji(Emoji.fromFormatted("❌")));//deleteColorRolesCancel

						hook.editOriginalEmbeds(embedder.simpleEmbed("Please confirm that you would like to continue with deleting all color roles.", null, null, ColorCodes.TIGER_FUR,
							"Click the \"Approve\" button to continue, otherwise press \"Cancel\" to cancel the request.\nOnce either buttons are clicked, this message will auto-delete.\n\n"
							+ "Continuing with deleting the roles will create a completion message once the process is completed, it too will auto-delete after it is spawned."))
							.setActionRow(colorRoledeletionButtons).queue();
						break;
				}
				break;
			case "s5-level-operations":
				switch (selection)
				{
					case "s5-level-operations-create":
						Long messageToDel = event.getMessageIdLong();

						int max = tigerGuardDB.getMaxLevel();
						int current = 0;

						if (tigerGuardDB.checkForTable(event.getGuild().getIdLong() + "lvlroles"))
						{
							try
							{
								current = tigerGuardDB.getGuildKnownLevelUpRoleCount(event.getGuild().getIdLong());
							}
							catch (Exception e)
							{
								logger.log(LogType.DATABASE_ERROR, "Unable to locate the knownLevelRole value from the table tigerguard_db." + event.getGuild().getIdLong() + "lvlroles");
							}
						}

						int rolesCount = event.getGuild().getRoles().size()-1;

						if (250-rolesCount > 0)
						{
							if (max-current > 0)
							{
								event.replyModal(new ModalEvents().levelRoleCreationModal(event.getMember(), event.getGuild(), event, max, current)).queue();
							}
							else
							{
								event.replyEmbeds(embedder.simpleEmbed("Max number of level roles reached", null, null, ColorCodes.FLARE, "Currently I only support a max number of " + max + " level-up roles.\n" +
									"I may support more in the future but for the time being I simply can only offer that many for now. I apologize on my bookkeeping skills, I am only a guard.\n\n" +
									"This message will auto-delete in 30 seconds.")).queue(msg -> msg.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
							}
						}
						else
						{
							event.replyEmbeds(embedder.simpleEmbed("Role space requirement issue", null, null, ColorCodes.UNABLE, "I'm sorry, but this server has used up enough roles to make my level roles feature unusable.\n"
								+ "I can support a total of 40 level roles. Currently this server has used " + rolesCount + " out of 250 role slots.\n\n"
								+ "**Note:** You do not need 40 free role slots for I can work with less; just know that if you wish to have all 40 then you must have enough free role slots!"
								+ "\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.deleteOriginal().queueAfter(1, TimeUnit.MINUTES));
						}
						event.getChannel().deleteMessageById(messageToDel).queue();
						break;
					case "s5-level-operations-repair":
						event.getMessage().delete().queue();
						RoleManagement.getINSTANCE().stringSelectAction(event, "repairLevel");
						break;
					case "s5-level-operations-delete":
						event.deferEdit().queue();
						List<Button> lvlRoleDeletionButtons = new ArrayList<>();
						lvlRoleDeletionButtons.add(Button.primary("tigerguardfeature-delete-levels-confirm", "Approve").withEmoji(Emoji.fromFormatted("✔")));//deleteLevelRolesConfirm
						lvlRoleDeletionButtons.add(Button.primary("tigerguardfeature-delete-levels-cancel", "Cancel").withEmoji(Emoji.fromFormatted("❌")));//deleteLevelRolesCancel

						hook.editOriginalEmbeds(embedder.simpleEmbed("Please confirm that you would like to continue with deleting the level roles.", null, null, ColorCodes.TIGER_FUR,
							"\"Click the \"Approve\" button to continue, otherwise press \"Cancel\" to cancel the request.\nOnce either buttons are clicked, this message will auto-delete.\n\n"
							+ "Continuing with deleting the roles will create a completion message once the process is completed, it too will auto-delete after it is spawned."))
							.setActionRow(lvlRoleDeletionButtons).queue();
						break;
				}
				break;
			case "poll-new-time":
				event.deferEdit().queue();
				String[] selectionSplit = selection.split("-");
				tigerGuardDB.setPollTempTimeData(event.getGuild().getIdLong(), Integer.parseInt(selectionSplit[0]), selectionSplit[1]);
				buttons.add(Button.primary("poll-duo", "Create a Yay/Nay Poll").withEmoji(Emoji.fromFormatted("2️⃣")));
				buttons.add(Button.primary("poll-trio", "Create a Yay/Nay/Abstain Poll").withEmoji(Emoji.fromFormatted("3️⃣")));
				hook.editOriginalEmbeds(embedder.simpleEmbed("What kind of poll do you wish to create?", null, null, ColorCodes.POLL, "The following poll types are available. You will be prompted with a Modal once one of these options is pressed."))
					.setActionRow(buttons).queue();
				break;
		}
	}

	@Override
	public void onEntitySelectInteraction(EntitySelectInteractionEvent event)
	{
		String entitySelection = event.getComponentId();
		long guild = event.getGuild().getIdLong();
		long selection = event.getValues().get(0).getIdLong();
		String delM = "\n\nThis message will auto-delete in 30 seconds";
		String delM6 = "\n\nThis message will auto-delete in 1 minute";
		event.getMessage().delete().queue();

		switch (entitySelection)
		{
			case "e1-role-admin":
				tigerGuardDB.basicUpdate("guildInfo", "adminRole", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Admin role specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the admin role.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-role-primary-staff":
				tigerGuardDB.basicUpdate("guildInfo", "primaryStaffRole", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Primary staff role specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the primary staff role.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-role-secondary-staff":
				tigerGuardDB.basicUpdate("guildInfo", "secondaryStaffRole", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Primary staff role specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the primary staff role.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-role-member":
				tigerGuardDB.basicUpdate("guildInfo", "memberRole", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Member role specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the regular member role.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-role-nsfw":
				tigerGuardDB.basicUpdate("guildInfo", "nsfwRole", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("NSFW role specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the nsfw role.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-announcement-channel": //TODO DELETE
				tigerGuardDB.basicUpdate("guildInfo", "announcementChannel", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Announcement channel specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the channel for announcement purposes.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-bot-testing-channel": //TODO DELETE
				tigerGuardDB.basicUpdate("guildInfo", "botSpamChannel", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Bot/Testing channel specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the channel for bot spam and/or testing purposes.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-music-channel":
				tigerGuardDB.basicUpdate("guildInfo", "musicChannel", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Music channel specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the music channel.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-level-channel":
				tigerGuardDB.basicUpdate("guildInfo", "levelChannel", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Level channel specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the level channel.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-bot-channel":
				tigerGuardDB.basicUpdate("guildInfo", "botSpamChannel", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Bot channel specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the channel for misc bot messages.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
			case "e1-customvc-channel":
				tigerGuardDB.basicUpdate("guildInfo", "dynamicVcChannel", selection, "guild", guild);
				tigerGuardDB.basicUpdate("guildInfo", "dynamicVcCategory", event.getGuild().getVoiceChannelById(selection).getParentCategoryIdLong(), "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("CustomVC join-to-create vc specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the 'Join-To-Create' channel for creating custom VC's.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!\n\n"
					+ "***NOTE*** Ensure the custom VC channel is in a category with no other voice channels. I will delete any voice channels besides the voice channel you set whenever they go empty!" + delM6)).queue(a -> a.deleteOriginal().queueAfter(60, TimeUnit.SECONDS));
				break;
			case "e1-size-channel":
				tigerGuardDB.basicUpdate("guildInfo", "guildSizeChannel", selection, "guild", guild);
				event.replyEmbeds(embedder.simpleEmbed("Member counter specification confirmed!", null, null, ColorCodes.FINISHED, "You selected" + event.getValues().get(0) + " for what this server recognizes as the channel for tracking the member count of the server.\n"
					+ "I will now recognize this as well; you can change this again at anytime using this command once more!" + delM)).queue(a -> a.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
				break;
		}
	}

	private void serverMessageItemsSection(StringSelectInteractionEvent event, InteractionHook hook)
	{
		if (tigerGuardDB.checkRow("colorRoles", "guild", event.getGuild().getIdLong()))
		{
			hook.editOriginalEmbeds(embedder.simpleEmbed("Select which operation you'd like to utilize", null, null, ColorCodes.TIGER_FUR, null))
				.setActionRow(StringSelectMenu.create("s3-em-items-with-color")
				.addOption("Print a previously made custom embed", "s4-em-print", "Print an embed.")//embedRequestPrint
				.addOption("Edit the data for an existing embed", "s4-em-edit", "Edit an embed.")
				.addOption("Delete a previously made custom embed", "s4-em-delete", "Select an embed to delete.")//embedRequestDelete
				.addOption("Print Color roles embed", "s4-em-prompt-color", "Print out the embed for Color roles.")//promptColor
				.addOption("Print NSFW prompt", "s4-em-prompt-nsfw", "Print out the NSFW access embed.")//promptNSFW
				.build()).queue();
		}
		else
		{
			hook.editOriginalEmbeds(embedder.simpleEmbed("Select which operation you'd like to utilize", null, null, ColorCodes.TIGER_FUR, null))
				.setActionRow(StringSelectMenu.create("s3-em-items-plain")
				.addOption("Print a previously made custom embed", "s4-em-print", "Print an embed.")
				.addOption("Edit the data for an existing embed", "s4-em-edit", "Edit an embed.")
				.addOption("Delete a previously made custom embed", "s4-em-delete", "Select an embed to delete.")
				.addOption("Print NSFW prompt", "s4-em-prompt-nsfw", "Print out the NSFW access embed.")
				.build()).queue();
		}
	}
}
