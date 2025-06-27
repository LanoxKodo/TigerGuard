package lanoxkododev.tigerguard.commands;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public interface TGCommand {

	String getName();
	String getDescription();
	
	default boolean isNSFWRelated()
	{
		return false;
	}

	default EnumSet<InteractionContextType> getContexts()
	{
		return EnumSet.of(InteractionContextType.GUILD);
	}
	
	default DefaultMemberPermissions getDefaultPermission()
	{
		return DefaultMemberPermissions.ENABLED;
	}

	default List<OptionData> getOptions()
	{
		return Collections.emptyList();
	};

	void execute(SlashCommandInteractionEvent event);
}
