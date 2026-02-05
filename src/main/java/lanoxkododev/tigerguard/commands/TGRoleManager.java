package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.List;

import lanoxkododev.tigerguard.PermissionValidator;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.TigerGuardDB.DB_Enums;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class TGRoleManager implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tgdb = TigerGuardDB.getTigerGuardDB();
	PermissionValidator permValidator = new PermissionValidator();
	
	@Override
	public String getName()
	{
		return "tg-role-manager";
	}
	
	@Override
	public String getDescription()
	{
		return "Configure various role associations/features";
	}
	
	@Override
	public List<SubcommandData> getSubcommands()
	{
		List<SubcommandData> subcommands = new ArrayList<>();
		subcommands.add(new SubcommandData("admin_role", "Set the Admin role")
			.addOption(OptionType.ROLE, "role", "role for administrator(s) - highest perms.", true));
		subcommands.add(new SubcommandData("staff_role", "Set the Staff role")
			.addOption(OptionType.ROLE, "role", "role for staff users - mid perms.", true));
		subcommands.add(new SubcommandData("mod_role", "Set the Moderator role")
			.addOption(OptionType.ROLE, "role", "role for moderators - low perms.", true));
		subcommands.add(new SubcommandData("new_user_role", "Set the role that is given to users joining the server")
			.addOption(OptionType.ROLE, "role", "role for new users", true));
		subcommands.add(new SubcommandData("vetted_user_role", "Set the role that is given to users whom are approved or whatnot")
			.addOption(OptionType.ROLE, "role", "role for regular members (post join, not new users)", true));
		subcommands.add(new SubcommandData("nsfw_access_role", "Set the role that is related to NSFW permission logic")
			.addOption(OptionType.ROLE, "role", "role for nsfw handling", true));
		
		return subcommands;
	}
	
	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		if (permValidator.canAccess(event.getGuild(), event.getMember(), false))
		{
			String subcommand = event.getSubcommandName();
			
			if (subcommand == null)
			{
				event.replyEmbeds(embedder.simpleEmbed("Invalid command usage", null, null, ColorCodes.ERROR,
					"Your input for the command appears to be wrong. Please try again")).setEphemeral(true).queue();
				return;
			}
			
			event.deferReply().setEphemeral(true).queue();
			
			long guildID = event.getGuild().getIdLong();
			long roleID = 0;
			
			Role role = event.getOption("role").getAsRole();
			if (role != null) roleID = role.getIdLong();
			
			if (roleID != 0)
			{
				try
				{
					DB_Enums enumType = null;
					switch (subcommand)
					{
						case "admin_role" -> enumType = DB_Enums.ADMIN;
						case "staff_role" -> enumType = DB_Enums.STAFF;
						case "mod_role" -> enumType = DB_Enums.MOD;
						case "new_user_role" -> enumType = DB_Enums.ENTRANT;
						case "vetted_user_role" -> enumType = DB_Enums.MEMBER;
						case "nsfw_access_role" -> enumType = DB_Enums.NSFW;
						default -> {}
					}
					
					if (enumType != null)
					{
						tgdb.setValue(enumType, roleID, "guild", guildID);
						
						event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Data written", null, null, ColorCodes.FINISHED,
							"The role has been set and is able to be used by my functionality.")).queue();
					}
					else
					{
						event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Failure during processing", null, null, ColorCodes.ERROR,
							"A failure has occurred in saving the role data as requested.")).queue();
					}
				}
				catch (Exception e)
				{
					event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Failure saving data", null, null, ColorCodes.ERROR,
						"Backend failure occurred while trying to save data.")).queue();
				}
			}
			else
			{
				event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Unknown role", null, null, ColorCodes.ERROR,
					"It appears the provided detail for a role either does not exist or some other error occurred.")).queue();
			}
		}
		else event.getHook().sendMessageEmbeds(embedder.accessErrorEmbed()).queue();
	}
}