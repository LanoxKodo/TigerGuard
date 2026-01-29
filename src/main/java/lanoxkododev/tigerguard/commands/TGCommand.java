package lanoxkododev.tigerguard.commands;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public interface TGCommand {

	String getName();
	String getDescription();
	
	default boolean isNSFWRelated()
	{
		return false;
	}

	/**
	 * For commands that use PRIVATE_CHANNEL context, this will be unusable until TigerGuard is approved (configured?)
	 *  to use Activities or such, might also just be something reserved for larger bots, ie the 75 to 100 server verification range.
	 * 
	 * @return
	 */
	default EnumSet<InteractionContextType> getContexts()
	{
		return EnumSet.of(InteractionContextType.GUILD);
	}
	
	default DefaultMemberPermissions getDefaultPermission()
	{
		return DefaultMemberPermissions.ENABLED;
	}
	
	default List<SubcommandData> getSubcommands()
	{
		return Collections.emptyList();
	}

	default List<OptionData> getOptions()
	{
		return Collections.emptyList();
	};

	void execute(SlashCommandInteractionEvent event);
}
