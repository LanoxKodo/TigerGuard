package lanoxkododev.tigerguard.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import lanoxkododev.tigerguard.polls.TigerPolls;
import lanoxkododev.tigerguard.roles.RoleManagement;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class ModalEvents extends ListenerAdapter {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	RoleManagement manager = new RoleManagement();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	TigerPolls tigerPoll = new TigerPolls();

	@Override
	public void onModalInteraction(ModalInteractionEvent event)
	{
		event.deferReply().setEphemeral(true).queue();
		List<Button> pollButtons = new ArrayList<>();
		String text = event.getModalId();

		switch (text)
		{
			case "levelUpRoleCreator":
				String amountToMakeStr = event.getValue("levelUpCreationAmount").getAsString();
				int amountToMake;

				try
				{
					amountToMake = Integer.parseInt(amountToMakeStr);
				}
				catch (NumberFormatException e)
				{
					amountToMake = 0;
				}

				if (amountToMake > 0)
				{
					//Servers can have 250 roles max and also the @everyone role, thus 251 in technical terms; check for space first.
					List<Role> roleAmount = event.getGuild().getRoles();
					int remainingRoleSlots = 251 - roleAmount.size();

					int currentRoleCount = tigerGuardDB.getGuildKnownLevelUpRoleCount(event.getGuild().getIdLong());

					if (amountToMake < remainingRoleSlots && currentRoleCount < tigerGuardDB.getMaxLevel())
					{
						manager.initiateLevelRoleSetup(event, amountToMake, currentRoleCount);

						event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Request accepted!", null, null, ColorCodes.CONFIRMATION, "I was able to validate your input of (" + amountToMake + ") to be assignable. Allow me a moment to configure these roles and after I finish you may customize them all you'd like for I will only need to keep the ID of each one on my end to keep track of the roles for management purposes." +
								"\n\nThis message will auto-delete in 1 minute.")).queue(mes -> mes.delete().queueAfter(60, TimeUnit.SECONDS));
					}
					else
					{
						event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Not enough space for the inquired role count", null, null, ColorCodes.ERROR,
							"I was unable to begin creating Level-Up roles with your specified amount (" + amountToMake + ") because the server has only (" + (remainingRoleSlots-1) + ") left.")).setEphemeral(true).queue();
					}
				}
				else
				{
					event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Error with entered input.", null, null, ColorCodes.ERROR, "There appears to have been an issue with the input I was provided which caused this error. Please try using the command again and see if it was a fluke. If it continues again, please reach out for support!")).setEphemeral(true).queue();
				}
				break;
			case "modalDuoPollCreation":
				pollButtons.add(Button.success("poll-vote-yay", "Yay").withEmoji(Emoji.fromFormatted("✅")));
				pollButtons.add(Button.danger("poll-vote-nay", "Nay").withEmoji(Emoji.fromFormatted("❎")));

				event.getChannel().sendMessageEmbeds(embedder.simpleEmbed(event.getValue("modalPollTitle").getAsString(), null, null, ColorCodes.POLL, event.getValue("modalPollDesc").getAsString()))
					.setComponents((ActionRow.of(pollButtons))).queue(msg -> {
						tigerGuardDB.pollCreation(event.getGuild().getIdLong(), msg.getIdLong(), msg.getChannelType(), msg.getChannel().getIdLong(), "Duo");
				});

				event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Poll request finalized", null, null, ColorCodes.FINISHED,
					"If the poll was not created, please seek help on my support server")).queue();
				break;
			case "modalTrioPollCreationA":
				trioPollHandOff(event, 'A');
				break;
			case "modalTrioPollCreationB":
				trioPollHandOff(event, 'B');
				break;
		}
	}

	private void trioPollHandOff(ModalInteractionEvent event, char type)
	{
		List<Button> pollButtons = new ArrayList<>();

		pollButtons.add(Button.success("poll-vote-yay", "Yay").withEmoji(Emoji.fromFormatted("✅")));
		pollButtons.add(Button.secondary("poll-vote-abstain", "Abstain").withEmoji(Emoji.fromFormatted("🏖")));
		pollButtons.add(Button.danger("poll-vote-nay", "Nay").withEmoji(Emoji.fromFormatted("❎")));

		String append = "";
		switch (type)
		{
			case 'A':
				append += "\n\nThis polls passing logic will exclude abstain votes.";
				break;
			case 'B':
				append += "\n\nThis polls passing logic will utilize abstain votes.";
				break;
		}

		event.getChannel().sendMessageEmbeds(embedder.simpleEmbed(event.getValue("modalPollTitle").getAsString(), null, null, ColorCodes.POLL, event.getValue("modalPollDesc").getAsString() + append))
		.setComponents((ActionRow.of(pollButtons))).queue(msg -> {
			tigerGuardDB.pollCreation(event.getGuild().getIdLong(), msg.getIdLong(), msg.getChannelType(), msg.getChannel().getIdLong(), "Trio" + type);
		});

		event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Poll request finalized", null, null, ColorCodes.FINISHED,
			"If the poll was not created, please seek help on my support server")).queue();
	}

	public Modal DuoPoll()
	{
		TextInput voteTitle = TextInput.create("modalPollTitle", "What's the title for the poll?", TextInputStyle.PARAGRAPH)
			.setPlaceholder("Max of 120 characters, minimum of 1.")
			.setMinLength(1).setMaxLength(120).build();

		TextInput voteDesc = TextInput.create("modalPollDesc", "What's the poll description/info?", TextInputStyle.PARAGRAPH)
			.setPlaceholder("Max of 2000 characters, minimum of 1. Formatting allowed.")
			.setMinLength(1).setMaxLength(1880).build();

		return Modal.create("modalDuoPollCreation", "Create a Poll").addComponents(ActionRow.of(voteTitle), ActionRow.of(voteDesc)).build();
	}

	public Modal TrioPoll(char type)
	{
		TextInput voteTitle = TextInput.create("modalPollTitle", "What's the title for the poll?", TextInputStyle.PARAGRAPH)
			.setPlaceholder("Max of 120 characters, minimum of 1. Formatting allowed.")
			.setMinLength(1).setMaxLength(120).build();

		TextInput voteDesc = TextInput.create("modalPollDesc", "What's the poll description/info?", TextInputStyle.PARAGRAPH)
			.setPlaceholder("Max of 2000 characters, minimum of 1. Formatting allowed.")
			.setMinLength(1).setMaxLength(1880).build();

		return Modal.create("modalTrioPollCreation" + type, "Create a Poll").addComponents(ActionRow.of(voteTitle), ActionRow.of(voteDesc)).build();
	}

	public Modal levelRoleCreationModal(Member member, Guild guild, StringSelectInteractionEvent event, int max, int current)
	{
		Modal modal = null;

		TextInput levelUpCreationAmount = TextInput.create("levelUpCreationAmount", "How many Level-Up roles should be added?", TextInputStyle.PARAGRAPH)
			.setPlaceholder("This guild has " + current + " level roles. How many more would you like to add? Remaining slots: " + (max-current))
			.setRequiredRange(1, max-current).build();

		modal = Modal.create("levelUpRoleCreator", "Level-Up Role Creator").addComponents(ActionRow.of(levelUpCreationAmount)).build();

		return modal;
	}
}