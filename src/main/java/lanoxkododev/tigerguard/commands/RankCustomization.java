package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import lanoxkododev.tigerguard.pagination.Pages;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

public class RankCustomization implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	Pages pages = Pages.getInstance();

	@Override
	public String getName()
	{
		return "rank-customization";
	}

	@Override
	public String getDescription()
	{
		return "Customize your rank card for this guild!";
	}

	@Override
	public boolean isNSFW()
	{
		return false;
	}

	@Override
	public List<OptionData> getOptions()
	{
		return null;
	}

	@Override
	public DefaultMemberPermissions getDefaultPermission()
	{
		return DefaultMemberPermissions.ENABLED;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		event.deferReply().setEphemeral(true).queue();
		TigerGuardDB.getTigerGuardDB().checkIfUserExistsInGlobalData(event.getMember().getIdLong());

		List<Button> buttons = new ArrayList<>();
		buttons.add(Button.secondary("rankcustom-start", "index").withEmoji(Emoji.fromFormatted("⏪")));
		buttons.add(Button.secondary("rankcustom-prev", "previous").withEmoji(Emoji.fromFormatted("◀")));
		buttons.add(Button.secondary("rankcustom-next", "next").withEmoji(Emoji.fromFormatted("▶")));
		buttons.add(Button.secondary("rankcustom-end", "last").withEmoji(Emoji.fromFormatted("⏩")));
		buttons.add(Button.success("rankcustom-select", "select current").withEmoji(Emoji.fromFormatted("⏺")));

		event.getHook().sendMessageEmbeds(embedder.paginater(pages.rankCustomizerPages(0))).setFiles(FileUpload.fromData(pages.getImageAsStream(0), "image.png")).setComponents(ActionRow.of(buttons)).queue();
	}
}
