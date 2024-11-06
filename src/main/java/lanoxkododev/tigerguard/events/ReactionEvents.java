package lanoxkododev.tigerguard.events;

import java.util.ArrayList;
import java.util.List;
import lanoxkododev.tigerguard.ThreadUtilities;
import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionEvents extends ListenerAdapter {

	TigerGuard tigerGuard;
	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	EmbedMessageFactory embedder = new EmbedMessageFactory();

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event)
	{
		Member member = event.getMember();

		if (member != null)
		{
			if (!member.getUser().isBot())
			{
				Guild guild = event.getGuild();
				Long messageID = event.getMessageIdLong();

				if (tigerguardDB.checkRow("colorRoles", "id", guild.getIdLong()) && tigerguardDB.basicSelectLong("colorRoles", "embed", "id", guild.getIdLong()).equals(messageID))
				{
					colorProcess(event, member, guild);
				}
			}
		}
	}

	private void colorProcess(MessageReactionAddEvent event, Member member, Guild guild)
	{
		ArrayList<Long> colorRoles = tigerguardDB.selectColorRoles(guild.getIdLong());

		Long roleVal = 0L;
		switch (event.getReaction().getEmoji().getName())
		{
			case "🍎":
				roleVal = colorRoles.get(0);
				break;
			case "🥭":
				roleVal = colorRoles.get(1);
				break;
			case "🍊":
				roleVal = colorRoles.get(2);
				break;
			case "🍋":
				roleVal = colorRoles.get(3);
				break;
			case "🍌":
				roleVal = colorRoles.get(4);
				break;
			case "🍈":
				roleVal = colorRoles.get(5);
				break;
			case "🥦":
				roleVal = colorRoles.get(6);
				break;
			case "🧊":
				roleVal = colorRoles.get(7);
				break;
			case "🍶":
				roleVal = colorRoles.get(8);
				break;
			case "🍦":
				roleVal = colorRoles.get(9);
				break;
			case "🍇":
				roleVal = colorRoles.get(10);
				break;
			case "🎂":
				roleVal = colorRoles.get(11);
				break;
			case "🍑":
				roleVal = colorRoles.get(12);
				break;
			case "🍓":
				roleVal = colorRoles.get(13);
				break;
			case "🥥":
				roleVal = colorRoles.get(14);
				break;
			case "🥑":
				roleVal = colorRoles.get(15);
				break;
			case "🍧":
				roleVal = colorRoles.get(16);
				break;
			case "🍆":
				roleVal = colorRoles.get(17);
				break;
			case "🧽":
				roleVal = -1L;
				break;
		}

		handleColorChange(roleVal, member, guild, colorRoles);
		removeReaction(event);
	}
	
	private void handleColorChange(Long roleProvisioned, Member member, Guild guild, ArrayList<Long> colorRoles)
	{
		ThreadUtilities.createGenericThread(a -> {
			List<Role> memberRoles = member.getRoles();
			
			for (Long role : colorRoles)
			{
				if (memberRoles.contains(guild.getRoleById(role)))
				{
					guild.removeRoleFromMember(member, guild.getRoleById(role)).queue();
				}
			}
			//If role selected was not the sponge, then provision the selected role.
			if (roleProvisioned != -1L) guild.addRoleToMember(member, guild.getRoleById(roleProvisioned)).queue();
		}, null, null, false, false);
	}

	@SuppressWarnings("unused")
	private void detectColorsForRemoval(Long roleProvisioned, Member member, Guild guild, ArrayList<Long> roles)
	{
		ThreadUtilities.createGenericThread(a -> {
			for (Long roleCheck : roles)
			{
				List<Role> memberRoles = member.getRoles();

				if (roleCheck != -1L)
				{
					if (memberRoles.contains(guild.getRoleById(roleCheck)))
					{
						guild.removeRoleFromMember(member, guild.getRoleById(roleCheck)).queue();
					}
				}
				else
				{
					if (memberRoles.contains(guild.getRoleById(roleCheck)))
					{
						guild.removeRoleFromMember(member, guild.getRoleById(roleCheck)).queue();
					}
				}
			}

			if (roleProvisioned != -1L) guild.addRoleToMember(member, guild.getRoleById(roleProvisioned)).queue();
		}, null, null, false, false);
	}

	private void removeReaction(MessageReactionAddEvent event)
	{
		event.getReaction().removeReaction(event.getUser()).queue();
	}

	@SuppressWarnings("unused")
	private Role provideRoleFromString(String roleName, Member member)
	{
		Role sample = null;

		List<Role> roles = member.getRoles();
		sample = roles.stream().filter(role -> role.getName().equals(roleName)).findFirst().orElse(null);

		return sample;
	}

	@SuppressWarnings("unused")
	private Role provideRole(Guild guild, Long roleId, Member member)
	{
		Role sample = null;

		List<Role> roles = member.getRoles();
		if (roles.contains(guild.getRoleById(roleId)))
		{
			sample = guild.getRoleById(roleId);
		}

		return sample;
	}
}