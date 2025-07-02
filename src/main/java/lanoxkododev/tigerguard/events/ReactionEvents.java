package lanoxkododev.tigerguard.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lanoxkododev.tigerguard.ThreadUtilities;
import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
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

		if (member != null && !member.getUser().isBot())
		{
			Guild guild = event.getGuild();
			Long messageID = event.getMessageIdLong();
			
			event.getGuild().getTextChannelById(messageID);

			if (tigerguardDB.checkForTable(guild.getIdLong() + "embeds"))
			{
				if (tigerguardDB.checkRow(guild.getIdLong() + "embeds", "message", messageID))
				{
					reactionRoleResult(guild, member, event.getReaction().getEmoji().getFormatted(), messageID, "add");
				}
			}
			else if (tigerguardDB.checkRow("colorRoles", "guild", guild.getIdLong()) && tigerguardDB.getValueLong("colorRoles", "embed", "guild", guild.getIdLong()).equals(messageID))
			{
				colorProcess(event, member, guild);
			}
		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event)
	{
		Member member = event.getMember();
		
		if (member != null && !member.getUser().isBot())
		{
			Guild guild = event.getGuild();
			Long messageID = event.getMessageIdLong();
			
			if (tigerguardDB.checkRow(guild.getIdLong() + "embeds", "message", messageID))
			{
				reactionRoleResult(guild, member, event.getReaction().getEmoji().getFormatted(), messageID, "remove");
			}
		}
	}
	
	private void reactionRoleResult(Guild guild, Member member, String emoji, Long messageID, String eventType)
	{
		Role role = getRoleFromMessage(guild, messageID, emoji);
		
		if (role != null)
		{
			if (eventType.equals("add")) giveRole(guild, member, role);
			else takeRole(guild, member, role);
		}
	}
	
	private Role getRoleFromMessage(Guild guild, Long messageID, String emoji)
	{
		CompletableFuture<String> future = new CompletableFuture<>();
		String[][] dataParts = new String[1][];
		Long guildID = guild.getIdLong();
		
		if (tigerguardDB.checkIfValueExists(guildID + "embeds", "body", "message", messageID))
		{
			dataParts[0] = tigerguardDB.getEmbedBodyData(guildID, messageID).split("\\s+");
			future.complete(searchData(dataParts[0], emoji));
		}
		
		Long value;
		
		try
		{
			value = Long.parseLong(future.get());
		}
		catch (Exception e)
		{
			logger.logErr(LogType.ERROR, "Failure to find, or get, Role from message '" + messageID + " for guild '" + guildID + "'.", "Emoji searched with: " + emoji, e);
			return null;
		}
		
		return guild.getRoleById(value);
	}
	
	private String searchData(String[] dataParts, String emoji)
	{
		ArrayList<String> roles = new ArrayList<>();
		ArrayList<String> emojis = new ArrayList<>();
		
		for (String part : dataParts)
		{
			if (part.matches("\\p{So}+") && !part.equals(">")) emojis.add(part);
			else if (part.startsWith("<@&")) roles.add(part.substring(3, part.length() - 1));
		}
		
		for (int pos = 0; pos < emojis.size(); pos++)
		{
			if (emojis.get(pos).equals(emoji)) return roles.get(pos);
		}
		
		return null;
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
		ThreadUtilities.createGenericThread(_ -> {
			List<Role> memberRoles = member.getRoles();

			for (Long role : colorRoles)
			{
				if (memberRoles.contains(guild.getRoleById(role))) takeRole(guild, member, guild.getRoleById(role));
			}
			
			if (roleProvisioned != -1L) giveRole(guild, member, guild.getRoleById(roleProvisioned));
		}, null, null, false, false);
	}

	private void removeReaction(MessageReactionAddEvent event)
	{
		event.getReaction().removeReaction(event.getUser()).queue();
	}
	
	private void giveRole(Guild guild, Member member, Role role)
	{
		guild.addRoleToMember(member, role).queue();
	}
	
	private void takeRole(Guild guild, Member member, Role role)
	{
		guild.removeRoleFromMember(member, role).queue();
	}
}