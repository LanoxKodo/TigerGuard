package lanoxkododev.tigerguard.events;

import java.util.List;
import java.util.Random;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelEvents extends ListenerAdapter {

	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();

	@Override
	public void onChannelCreate(ChannelCreateEvent event)
	{
		event.getChannel().asGuildChannel().getPermissionContainer().getManager().putRolePermissionOverride(event.getGuild().getBotRole().getIdLong(), 8L, 0L).queue();
	}

	private void deleteVC(AudioChannelUnion vc)
	{
		if (vc.getIdLong() == tigerGuardDB.getGuildCustomvcChannel(vc.getGuild().getIdLong()))
		{
			logger.log(LogType.WARNING, "Bot tried to delete customVC channel for guild " + vc.getGuild().getIdLong() + ", denying deletion.");
		}
		else
		{
			vc.delete().queue();
		}
	}

	@Override
	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event)
	{
		if (!event.getMember().getUser().isBot())
		{
			Guild guild = event.getGuild();

			Long guildCustomvcCategory = tigerGuardDB.getGuildCustomvcCategory(guild.getIdLong());
			Long guildCustomvcChannel = tigerGuardDB.getGuildCustomvcChannel(guild.getIdLong());

			if (guildCustomvcChannel != 0 || guildCustomvcChannel != null)
			{
				if (event.getChannelJoined() != null)
				{
					AudioChannelUnion vcJoined = event.getChannelJoined();

					if (vcJoined.getIdLong() == guildCustomvcChannel)
					{
						createNewVoiceChannelFromJoin(event);
					}
					else
					{
						if (tigerGuardDB.checkRow("levelRoles", "guild", guild.getIdLong()))
						{
							tigerGuardDB.voiceStatusBegin(event.getMember().getIdLong(), guild.getIdLong());
						}
					}
				}
				else
				{
					AudioChannelUnion vcLeft = event.getChannelLeft();

					if (!guildCustomvcChannel.equals(vcLeft.getIdLong()))
					{
						totalTimeInVC(event, event.getMember());
					}
					else if (vcLeft.getIdLong() != guildCustomvcChannel && vcLeft.getMembers().size() == 1)
					{
						List<Member> memberRemaining = vcLeft.getMembers();
						String name = memberRemaining.get(0).getEffectiveName();

						//Check if remaining user is TigerGuard.
						if (name.equals(TigerGuard.getTigerGuard().getName()))
						{
							//PlayerManager.getINSTANCE().safetyStop(event.getGuild(), false);
							EmbedBuilder embed = new EmbedBuilder();
							SelfUser bot = TigerGuard.getTigerGuard().getSelf();
							embed.setColor(0x730099);
							embed.setAuthor("║ Leaving channel due to empty queue or due to being last one in session.\n║Destroying audio playlist to start a fresh one next time I am called.", null, bot.getEffectiveAvatarUrl());
							guild.getTextChannelById(tigerGuardDB.getGuildMusicChannel(guild.getIdLong())).sendMessageEmbeds(embed.build()).queue();
							guild.getJDA().getDirectAudioController().disconnect(guild);
						}
					}

					if (vcLeft.getParentCategory().getIdLong() == guildCustomvcCategory && vcLeft.getIdLong() != guildCustomvcChannel)
					{
						deleteVC(vcLeft);
					}
				}
			}
		}
	}

	/**
	 * Method for handling xp provision from duration in voice channels.
	 *
	 * Requirement: vc time must at minimum be 15 minutes.
	 * Step calculations (calc): each 15 minute intervals up to a max of 56 (14 hours).
	 *
	 * @param event - the event attached to which the user left a voice channel.
	 * @param member - the member invoking the event.
	 */
	private void totalTimeInVC(GuildVoiceUpdateEvent event, Member member)
	{
		long timeBegan = tigerGuardDB.voiceStatusEnd(member.getIdLong());

		if (timeBegan != 0)
		{
			double total = System.currentTimeMillis() - timeBegan;
			double calc = total / 900000;

			if (calc >= 1)
			{
				if (!tigerGuardDB.checkRow(event.getGuild().getIdLong() + "xp", "member", member.getIdLong()))
				{
					tigerGuardDB.insertUserIntoGuildXPTable(event.getGuild().getIdLong() + "xp", member.getIdLong());
				}

				if (calc <= 56)
				{
					tigerGuardDB.updateGuildRankXp(event.getGuild(), member, (int)Math.round(15 * calc), null, event);
				}
				else
				{
					tigerGuardDB.updateGuildRankXp(event.getGuild(), member, (int)Math.round(15 * 56), null, event);
				}
			}
		}
	}

	private void createNewVoiceChannelFromJoin(GuildVoiceUpdateEvent event)
	{
		String[] names = {"Digital Hub", "Fancy Circle", "Unfancy Triangle", "Square Fancy!", "(✿◠‿◠)", "Eventful Peeps", "Nice Folks", "Chatter Crew"};
		int rand = new Random().nextInt(names.length);

		Guild guild = event.getGuild();
		Member member = event.getMember();

		guild.getCategoryById(tigerGuardDB.getGuildCustomvcCategory(guild.getIdLong())).createVoiceChannel(names[rand])
			.addRolePermissionOverride(guild.getPublicRole().getIdLong(), Permission.VOICE_CONNECT.getRawValue(), 0L).queue(channel ->
				guild.moveVoiceMember(member, guild.getVoiceChannelById(channel.getIdLong())).queue());
				if (!member.getUser().isBot())
				{
					tigerGuardDB.voiceStatusBegin(member.getIdLong(), guild.getIdLong());
				}
	}
}