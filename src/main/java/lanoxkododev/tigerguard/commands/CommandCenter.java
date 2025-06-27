package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import lanoxkododev.tigerguard.audio.AudioComplex;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Option;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
		
		registerCommands();

		List<Command> discordCommands = event.getJDA().retrieveCommands().complete();
		Map<String, Command> discordMap = new HashMap<>();
		for (Command c : discordCommands) discordMap.put(c.getName(), c);
		
		for (TGCommand localCommand : tigerCommands)
		{
			Command discordCommand = discordMap.get(localCommand.getName());
			
			if (discordCommand == null)
			{
				event.getJDA().upsertCommand(localCommand.getName(), localCommand.getDescription())
					.addOptions(localCommand.getOptions())
					.setDefaultPermissions(localCommand.getDefaultPermission())
					.queue(cmd -> logger.log(LogType.INFO, "Created command: " + cmd.getName()));
			}
			else
			{
				if (isCommandDifferent(discordCommand, localCommand))
				{
					logger.log(LogType.INFO, "Details for command " + localCommand.getName() + " do not match between local and Discord's endpoint, updating...");
					
					if (localCommand.getOptions() == null)
					{
						event.getJDA().editCommandById(Type.SLASH, discordCommand.getIdLong())
							.setName(localCommand.getName())
							.setDescription(localCommand.getDescription())
							.setContexts(localCommand.getContexts())
							.setDefaultPermissions(localCommand.getDefaultPermission())
							.queue();
					}
					else
					{
						event.getJDA().editCommandById(Type.SLASH, discordCommand.getIdLong())
							.setName(localCommand.getName())
							.setDescription(localCommand.getDescription())
							.setContexts(localCommand.getContexts())
							.setDefaultPermissions(localCommand.getDefaultPermission())
							.addOptions(localCommand.getOptions())
							.queue();
					}
				}
			}
		}
		
		Set<String> localCommandNames = tigerCommands.stream().map(TGCommand::getName).collect(Collectors.toSet());
		
		for (Command discordCommand : discordCommands)
		{
			if (!localCommandNames.contains(discordCommand.getName()))
			{
				logger.log(LogType.INFO, "Queueing deletion of unused command: " + discordCommand.getName());
				event.getJDA().deleteCommandById(discordCommand.getIdLong()).queue();
				discordCommands.remove(discordCommand);
			}
		}
		
		List<Command> availableCommands = event.getJDA().retrieveCommands().complete();
		Collections.sort(availableCommands, new CommandComparator());
		logger.log(LogType.INFO, "Available Commands:");
		availableCommands.forEach(cmd -> logger.log(String.format("%-25s %-25s | %s" , cmd.getName(), cmd.getIdLong(), cmd.getDescription())));
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
	
	private void registerCommands()
	{
		tigerCommands.add(new Info());
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
	}
	
	private boolean isCommandDifferent(Command discordCommand, TGCommand localCommand)
	{
		if (!Objects.equals(discordCommand.getDescription(), localCommand.getDescription())) return true;
		if (!Objects.equals(discordCommand.getDefaultPermissions().getPermissionsRaw(), localCommand.getDefaultPermission().getPermissionsRaw())) return true;
		if (!new HashSet<>(discordCommand.getContexts()).equals(localCommand.getContexts())) return true;
		
		List<Option> discordOptions = discordCommand.getOptions();
		List<OptionData> localOptions = localCommand.getOptions();
		if (discordOptions == null) discordOptions = Collections.emptyList();
		if (localOptions == null) localOptions = Collections.emptyList();
		if (discordOptions.size() != localOptions.size()) return true;
		
		for (int i = 0; i < discordOptions.size(); i++)
		{
			Option dOpt = discordOptions.get(i);
			OptionData lOpt = localOptions.get(i);
			
			if (!dOpt.getType().equals(lOpt.getType())) return true;
			if (!Objects.equals(dOpt.getName(), lOpt.getName())) return true;
			if (!Objects.equals(dOpt.getDescription(), lOpt.getDescription())) return true;
			if (dOpt.isRequired() != lOpt.isRequired()) return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused") //Leaving for now, used for occasional debugging.
	private String formatData(List<?> options)
	{
		if (options == null || options.isEmpty()) return "[]";
		
		return options.stream().map(option -> {
			if (option instanceof OptionData opt)
			{
				return String.format("OptionInfo[%s](name=%s, desc=%s, required=%s)", opt.getType(), opt.getName(), opt.getDescription(), opt.isRequired());
			}
			if (option instanceof Option opt)
			{
				return String.format("OptionInfo[%s](name=%s, desc=%s, required=%s)", opt.getType(), opt.getName(), opt.getDescription(), opt.isRequired());
			}
			return "[]";
		}).collect(Collectors.joining(", ", "[", "]"));
	}
}