package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import lanoxkododev.tigerguard.audio.AudioComplex;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

/*
 * This file is pending an overhaul, pending changes to come in the future for dynamic handling of commands when I find
 * a way that best fits my preferred use-case. 
 */

class CommandComparator implements Comparator<Command>
{
    @Override
    public int compare(Command com1, Command com2)
    {
        return com1.getName().compareTo(com2.getName());
    }
}

public class CommandCenter extends ListenerAdapter {

	TigerLogs logger = new TigerLogs();
	private List<TGCommand> tigerCommands = new ArrayList<>();
	
	private final AudioComplex ac;
	
	public CommandCenter(AudioComplex ac)
	{
		this.ac = ac;
	}
	
	@Override
	public void onReady(@NotNull ReadyEvent event)
	{
		super.onReady(event);
		tigerCommands.add(new Pause(ac));
		tigerCommands.add(new PingZeTiger());
		tigerCommands.add(new Play(ac));
		tigerCommands.add(new Poll());
		tigerCommands.add(new Rank());
		tigerCommands.add(new RankCustomization());
		tigerCommands.add(new Resume(ac));
		tigerCommands.add(new Roll());
		tigerCommands.add(new Skip(ac));
		tigerCommands.add(new Stop(ac));
		tigerCommands.add(new TgCreateEmbed());
		tigerCommands.add(new TgCreateReactionEmbed());
		tigerCommands.add(new TgHelp());
		tigerCommands.add(new TgUpdateConfig());
		tigerCommands.add(new TgViewConfig());
		tigerCommands.add(new VcLimit());
		tigerCommands.add(new VcName());
		tigerCommands.add(new VcNSFW());
		tigerCommands.add(new VcSFW());
		
		boolean doUpdate = false;
		if (doUpdate == true)
		{
			logger.log(LogType.INFO, "Updating commands...");
			for (TGCommand command : tigerCommands)
			{
				if (command.getName().equalsIgnoreCase("play"))
				{
					event.getJDA().editCommandById(1101427159664447498L).clearOptions().queue(a -> {
						a.editCommand().addOptions(command.getOptions()).setDefaultPermissions(command.getDefaultPermission()).queue();
					});
				}
			}
		}
		
		List<Command> jdaCommands = event.getJDA().retrieveCommands().complete();
		Collections.sort(jdaCommands, new CommandComparator());
		logger.log(LogType.INFO, "Available Commands:");
		jdaCommands.forEach(a -> {
			System.out.println(String.format("%-25s %-25s %s %s" , a.getName(), a.getIdLong(), "|", a.getDescription()));
		});
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event)
	{
		for (TGCommand command : tigerCommands)
		{
			if (command.getName().equals(event.getName()))
			{
				try
				{
					command.execute(event);
				}
				catch (Exception e)
				{
					logger.logErr(LogType.ERROR, "Failure executing task for command " + command.getName(), null, e);
				}
				return;
			}
		}
	}
}