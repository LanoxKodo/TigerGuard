package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.List;

import lanoxkododev.tigerguard.messages.EmbedCreationThreader;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TgCreateReactionEmbed implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();

	@Override
	public String getName()
	{
		return "tg-create-reaction-embed";
	}

	@Override
	public String getDescription()
	{
		return "Create a reaction-role embed";
	}

	@Override
	public boolean isNSFW()
	{
		return false;
	}

	@Override
	public List<OptionData> getOptions()
	{
		List<OptionData> options = new ArrayList<>();
		options.add(new OptionData(OptionType.STRING, "name", "The name the embed will be recognized as. Example: EventRoles", true).setMaxLength(25));
		options.add(new OptionData(OptionType.STRING, "title", "The title the embed will show. Supports formatting/multilines.", true).setMaxLength(200));
		options.add(new OptionData(OptionType.STRING, "color", "The Hex color value the embed will show. Example: #00b389", true).setMinLength(6).setMaxLength(7));
		options.add(new OptionData(OptionType.STRING, "data", "The data for the embed: Role (space) Emoji. Ex: @Role1 Emoji1 @Role2 Emoji2", true).setMinLength(1).setMaxLength(1800));
		options.add(new OptionData(OptionType.STRING, "description", "The description-header contents the embed will show. Supports formatting/multilines.", false).setMaxLength(200));
		options.add(new OptionData(OptionType.ROLE, "divider", "Used for making role lists look nicer if desired. Adds this role too when reaction is added.", false));

		return options;
	}
	
	@Override
	public DefaultMemberPermissions getDefaultPermission()
	{
		return DefaultMemberPermissions.DISABLED;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		new Thread(new EmbedCreationThreader(event)).start();
	}
}
