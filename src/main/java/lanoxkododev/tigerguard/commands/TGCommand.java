package lanoxkododev.tigerguard.commands;

import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface TGCommand {

	String getName();
	String getDescription();
	boolean isNSFW();

	List<OptionData> getOptions();
	DefaultMemberPermissions getDefaultPermission();

	void execute(SlashCommandInteractionEvent event);
}
