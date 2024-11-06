package lanoxkododev.tigerguard.messages;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Quartet;

import lanoxkododev.tigerguard.TigerGuardDB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class EmbedCreationThreader extends Thread {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	SlashCommandInteractionEvent event;
	Guild guild;

	public EmbedCreationThreader(SlashCommandInteractionEvent eventIn)
	{
		guild = eventIn.getGuild();
		event = eventIn;
	}

	@Override
	public void run()
	{
		review();
	}

	private void review()
	{
		event.deferReply().queue();

		if (!tigerGuardDB.checkForTable(event.getGuild().getIdLong() + "embeds"))
		{
			tigerGuardDB.createTable(event.getGuild().getIdLong() + "embeds (name varchar(20), type varchar(10), id varchar(45), title varchar(100), color varchar(7), body varchar(1900));");
		}

		if (!tigerGuardDB.checkRow("tempEmbedData", "id", event.getGuild().getIdLong()))
		{
			tigerGuardDB.firstInsertion("INSERT INTO tigerguarddb.tempEmbedData (id, name, title, color, body) VALUES (" + event.getGuild().getIdLong() + ", null, null, null, null);");
		}

		if (tigerGuardDB.countRows(event.getGuild().getIdLong() + "embeds") == 3 && !tigerGuardDB.getGuildPremiumStatus(event.getGuild().getIdLong()))
		{
			event.replyEmbeds(embedder.simpleEmbed("Pardon, this command is experiemental and partially restricted.", null, null, ColorCodes.UNABLE, "For non-donation servers, while this command is experimental, there is a limit of 3 customizable embeds.\nFor servers that support the bot this restrction is lifted.")).queue();
		}
		else
		{
			EmbedBuilder embed = new EmbedBuilder();

			ArrayList<String> roles = new ArrayList<>();
			ArrayList<String> emojis = new ArrayList<>();

			String[] dataParts = event.getOption("data").getAsString().split("\\s+");
			String str = "";

			boolean validInputs = true;
			for (String check : dataParts)
			{
				//Check if invalid input was given by checking if string begins with <> symbols or is an emoji item which would only have at most 2 codepoints iirc.
				if (check.length() > 60 || ((check.charAt(0) == ':' && check.charAt(check.length()-1) == ':') && (check.charAt(0) == '<' && check.charAt(check.length()-1) == '>' && check.codePoints().count() > 2)))
				{
					event.getInteraction().getHook().sendMessageEmbeds(embedder.simpleEmbed("Problem with input, incorrect data supplied or char length issue", null, null, ColorCodes.ERROR, "Sorry but there appears to be an issue, as a guard here are some things I check for:"
						+ "\n\nIn the data option, I am expecting at most 20 Roles and 20 Emojis to be provided in alterating order seperated by spaces, which Discord automatically does for you. Examples:\n@Role1 Emoji1 @Role2 Emoji2...\nOR\nEmoji1 @Role1 Emoji2 @Role2..."
						+ "\n\nI am further expecting each piece of data entered to be <= 60 characters.\n**Emoji Format Length(s):**\n-Custom: *<:emojiname:###################>*\n-Animated: *<a:emojiname:###################>*\n-Unicode: *'as-is'*\n**Role Format Length:**\n<@&###################>"
						+ "\nI count __each__ character involved, thus if the role or emoji has a lengthy name per the above format examples, please shorten it where possible. This should only affect emojis if this is the case."
						+ "\n\nAll else, the data provided may not even be a role or emoji, for security reasons I obviously must deny such data."
						+ "\n\nWith all that, here is what you provided, please modify as needed then repeat the command again:\n" + event.getOption("data").getAsString()
						+ "\n\nIf there somehow is not an issue with your input then please reach out on my support server for deeper inspection for it could have been a gnarly yarn ball full of bugs that snuck in when I dozed.")).queue();
					validInputs = false;
					break;
				}
			}

			if (validInputs)
			{
				if (dataParts.length % 2 == 0)
				{
					boolean continuePermitted = true;

					if (dataParts[0].contains("<@&")) //If first index is a role
					{
						for (int a = 0; a < dataParts.length; a++)
						{
							if (a % 2 == 0) roles.add(dataParts[a]);
							else emojis.add(dataParts[a]);
						}

						continuePermitted = verifyList(roles, emojis, true);
					}
					else //If first index is an emoji
					{
						for (int a = 0; a < dataParts.length; a++)
						{
							if (a % 2 == 0) emojis.add(dataParts[a]);
							else roles.add(dataParts[a]);
						}

						continuePermitted = verifyList(emojis, roles, false);
					}

					if (continuePermitted)
					{
						for (int a = 0; a < roles.size(); a++)
						{
							str += "\n" + emojis.get(a) + " ➤ " + roles.get(a);
						}
						
						String color = event.getOption("color").getAsString();
						if (event.getOption("color").getAsString() != null)
						{
							String colorTemp = event.getOption("color").getAsString();
							if (colorTemp.chars().count() < 6 || colorTemp.chars().count() > 7)
							{
								event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Unable to parse input", null, null, ColorCodes.UNABLE, "Cannot determined color from the input of '" + colorTemp + "'. Input not a hex value between #000000 and #FFFFFF.")).queue();
							}
							else
							{
								if (colorTemp.charAt(0) != '#') color = "#" + colorTemp;
								else color = colorTemp;
							}
						}

						embed.setColor(Color.decode(color.toString()));
						embed.setTitle(event.getOption("title").getAsString().replace("\\n", "\n"));

						if (event.getOption("description") != null)
						{
							String conjoin = event.getOption("description").getAsString() + "\n" + str;
							embed.setDescription(conjoin.replace("\\n", "\n"));
						}
						else embed.setDescription(str);

						Long divider = null;
						if (event.getOption("divider") != null)
						{
							divider = event.getOption("divider").getAsLong();
						}
						System.out.println(divider);

						embed.setFooter("Embed preview; Confirm to save embed : Cancel to not save this embed.\n\nNote: Emoji-reactions will appear in the final embed when it's printed out later on.\n"
							+ "Note: Attempting to create another embed before confirming or canceling this one will result in this one being nullified!");

						String name = event.getOption("name").getAsString();
						if (tigerGuardDB.checkRow(guild.getIdLong() + "embeds", "name", name))
						{
							boolean checkNameFinished = false;
							int a = 1;
							while (!checkNameFinished)
							{
								if (tigerGuardDB.checkRow(guild.getIdLong() + "embeds", "name", name + a)) a++;
								else
								{
									checkNameFinished = true;
									name += a;
									break;
								}
							}
						}

						List<Button> buttons = new ArrayList<>();
						buttons.add(Button.success("roleembed-confirm", "Confirm").withEmoji(Emoji.fromFormatted("✔️")));
						buttons.add(Button.danger("roleembed-cancel", "Cancel").withEmoji(Emoji.fromFormatted("✖️")));

						event.getInteraction().getHook().sendMessageEmbeds(embed.build()).setActionRow(buttons).queue();

						tigerGuardDB.setEmbedTempData(Quartet.with(name, event.getOption("title").getAsString(), color, event.getOption("data").getAsString()), event.getGuild().getIdLong());
					}
					else
					{
						event.getInteraction().getHook().sendMessageEmbeds(embedder.simpleEmbed("Problem with input, please review and try again", null, null, ColorCodes.ERROR, "It appears the provided input for the data option did not match my format expectations. Ensure that the input is in alternating pattern with spaces between each role and emoji."
							+ "\nExample: @Role1 Emoji1 @Role2 Emoji2\nOR\nEmoji1 @Role1 Emoji2 @Role2\n\nReview the following and try again:\n" + event.getOption("data").getAsString())).queue();
					}
				}
				else
				{
					event.getInteraction().getHook().sendMessageEmbeds(embedder.simpleEmbed("Problem with input, please review and try again", null, null, ColorCodes.ERROR, "It appears there is a problem with this input, ensure there are an equal number of roles and emojis provided "
						+ "(please avoid duplicate emojis for I won't fix that. I'm a guard, not a secretary afterall!)\n\nReview the following:\n" + event.getOption("data").getAsString())).queue();
				}
			}
		}
	}

	private boolean verifyList(ArrayList<String> set1, ArrayList<String> set2, boolean rolesInFirst)
	{
		if (rolesInFirst) //roles array is the first set, check to ensure all items in this array begin with the role markers and that all items in the other do not.
		{
			for (int a = 0; a < set1.size(); a++)
			{
				set1.get(a);
				set2.get(a);
				if (!set1.get(a).contains("<@&") || set2.get(a).contains("<@&")) return false;
			}
		}
		else //emojis array is first set, check to ensure all items in this array do not begin with the role number and that all items in the other do.
		{
			for (int a = 0; a < set1.size(); a++)
			{
				set1.get(a);
				set2.get(a);
				if (set1.get(a).contains("<@&") || !set2.get(a).contains("<@&")) return false;
			}
		}

		return true;
	}
}
