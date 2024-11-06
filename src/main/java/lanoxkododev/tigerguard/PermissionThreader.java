package lanoxkododev.tigerguard;

import java.util.List;

import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class PermissionThreader extends Thread {

	TigerLogs logger = new TigerLogs();
	Guild guild;

	public PermissionThreader(Guild eventIn)
	{
		guild = eventIn;
	}

	@Override
	public void run()
	{
		updatePerms();
	}

	private void updatePerms()
	{
		List<GuildChannel> channels = guild.getChannels();

		//Can't do this - access issue - reason: Permission is blocked due to access-escalation, you cannot make yourself higher otherwise permissions would be useless
		//guild.modifyRolePositions().selectPosition(guild.getBotRole()).swapPosition(guild.getRoles().size()-2).queue();

		channels.forEach(channel -> {
			channel.getPermissionContainer().getManager().putRolePermissionOverride(guild.getBotRole().getIdLong(), 8L, 0L).queue();
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		});

		logger.log(LogType.INFO, "Finished processing perm changes for server " + guild.getIdLong());
	}
}