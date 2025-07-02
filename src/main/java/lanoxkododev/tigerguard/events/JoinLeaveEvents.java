package lanoxkododev.tigerguard.events;

import java.time.Duration;

import lanoxkododev.tigerguard.ArrayUtilities;
import lanoxkododev.tigerguard.PermissionThreader;
import lanoxkododev.tigerguard.ThreadUtilities;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class JoinLeaveEvents extends ListenerAdapter {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event)
	{
		Guild guild = event.getGuild();
		Member member = event.getMember();

		if (tigerguardDB.getGuildBotSpamChannel(guild.getIdLong()) != null)
		{
			TextChannel channel = guild.getTextChannelById(tigerguardDB.getGuildBotSpamChannel(guild.getIdLong()));

			if (channel != null)
			{
				String intro = member.getEffectiveName() + " has joined!";

				if (member.getUser().isBot())
				{
					channel.sendMessageEmbeds(embedder.simpleEmbed(null, intro, member.getEffectiveAvatarUrl(), ColorCodes.JOIN,
						"A bot of some kind. Be advised that I cannot vet bots for their safety features.")).queue();
				}
				else
				{
					channel.sendMessageEmbeds(embedder.simpleEmbed(null, intro, member.getEffectiveAvatarUrl(), ColorCodes.JOIN, null)).queue();
				}
			}
		}

		if (!member.getUser().isBot())
		{
			Long memberCheck = tigerguardDB.getGuildMemberRole(guild.getIdLong());
			
			if (memberCheck != null && guild.getRoleById(memberCheck) != null || guild.getRoleById(memberCheck).getIdLong() != 0)
			{
				guild.addRoleToMember(member, guild.getRoleById(memberCheck)).queue();
			}

			if (!ArrayUtilities.guildMemberCounter.contains(guild))
			{
				ArrayUtilities.guildMemberCounter.add(guild);
				ThreadUtilities.createNewThread(_ -> updateMemberCountChannel(guild), _ -> logger.log(LogType.ERROR, "Issue involving guildMemberCounter arraylist."), Duration.ofSeconds(600), false, false);
			}
		}
		else
		{
			long memberRole = tigerguardDB.getGuildMemberRole(guild.getIdLong());
			if (memberRole != 0)
			{
				try
				{
					guild.addRoleToMember(member, guild.getRoleById(memberRole)).queue();
				}
				catch (Exception e)
				{
					logger.logErr(LogType.ERROR, "Failure applying level role " + memberRole + " for guild " + guild.getIdLong(), null, e);
				}
			}
		}
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event)
	{
		Guild guild = event.getGuild();
		User user = event.getUser();

		if (tigerguardDB.getGuildBotSpamChannel(guild.getIdLong()) != null)
		{
			TextChannel channel = guild.getTextChannelById(tigerguardDB.getGuildBotSpamChannel(guild.getIdLong()));

			channel.sendMessageEmbeds(embedder.simpleEmbed(user.getName() + " has departed.", null, null, ColorCodes.LEAVE, null)).queue();
		}
		else
		{
			logger.log(LogType.WARNING, "Failure with JoinLeaveEvents.onGuildMemberRemove(). Regular botChannel statement partially worked.\nResults:"
				+ user.getName() + " (" + user.getIdLong() + ") left from server " + guild.getIdLong());
		}

		if (!user.isBot())
		{
			if (!ArrayUtilities.guildMemberCounter.contains(guild))
			{
				ArrayUtilities.guildMemberCounter.add(guild);
				ThreadUtilities.createNewThread(_ -> updateMemberCountChannel(guild), _ -> logger.log(LogType.ERROR, "Issue involving guildMemberCounter arraylist."), Duration.ofSeconds(600), false, false);
			}
		}
	}

	protected void updateMemberCountChannel(Guild guild)
	{
		guild.loadMembers().onSuccess(members ->
		{
			long memberCount = members.stream().filter(b -> !b.getUser().isBot()).count();

			guild.getVoiceChannelById(tigerguardDB.getGuildSizeChannel(guild.getIdLong())).getManager().setName("Members " + memberCount).queue();
			ArrayUtilities.guildMemberCounter.remove(guild);
		});
	}

	@Override
	public void onGuildJoin(GuildJoinEvent event)
	{
		logger.log(LogType.INFO, "Server added bot! Sever " + event.getGuild().getId() + ", " + event.getGuild().getName());

		if (!tigerguardDB.checkRow("guildInfo", "id", event.getGuild().getIdLong()))
		{
			tigerguardDB.newGuildEntry(event.getGuild().getIdLong());
		}

		if (!tigerguardDB.checkForTable(event.getGuild().getIdLong() + "embeds"))
		{
			tigerguardDB.createTable("CREATE TABLE tigerguard_db." + event.getGuild().getIdLong() +
				"embeds (name varchar(25), type varchar(10), id varchar(45), title varchar(200), color varchar(7), datas varchar(1800), descr varchar(200));");
		}

		new PermissionThreader(event.getGuild()).start();

		event.getGuild().getDefaultChannel().asTextChannel().sendMessageEmbeds(embedder.simpleEmbed("Thank you for inviting me, allow me to introduce myself briefly!", null, null, ColorCodes.TIGER_FUR,
			"I am a multi-purpose bot that aims to assist in server management flow with support of dynamicVC logic, music support, reaction role handling, a server-level/level-role system, poll systems and more growing over time!\n\n" +
			"I am designed as an opt-in bot, this means nearly everything I provide must first be explicitly setup before my handling for certain interactions can work.\n\n**To start, server admin:**\nRun my command **/tg-view-config** to " +
			"see what this server can have configured! And run **/tg-update-config** to see what all might need details I can work with so I may work closer with your server!\n\nIf there are ever any issues with setting up something to work " +
			"with this server, please reach out on my support server, https://discord.gg/Gd8NDkyu4V, or via the developer, <@200305907572146177>, directly if in the server!")).queue();
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event)
	{
		logger.log(LogType.INFO, "TigerGuard was removed from " + event.getGuild());
	}
}