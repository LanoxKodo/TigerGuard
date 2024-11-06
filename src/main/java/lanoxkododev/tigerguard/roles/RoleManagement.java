package lanoxkododev.tigerguard.roles;

import java.util.ArrayList;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class RoleManagement {

	protected static RoleManagement INSTANCE;

	/**
	 * Method for initiating the level role setup for a Guild.
	 *
	 * @param event
	 * @param neededInput
	 * @param knownLevelRoles
	 */
	public void initiateLevelRoleSetup(ModalInteractionEvent event, int neededInput, int knownLevelRoles)
	{
		new Thread (new LevelRoleAdderThreader(event, event.getGuild(), neededInput, knownLevelRoles), "LevelUpRole Thread").start();
	}

	public void deleteRoles(ButtonInteractionEvent event, String type)
	{
		switch (type)
		{
			case "levels":
				new LevelRoleDeleteThreader(event, event.getGuild()).start();
				break;
			case "colors":
				new ColorRoleDeleteThreader(event, event.getGuild(), colorInfo()).start();
				break;
		}
	}

	/**
	 * Method for StringSelectionEvents to proceed to in a new thread.
	 *
	 * @param event - The StringSelectionInteractionEvent
	 * @param type  - The name of the event to lead to
	 */
	public void stringSelectAction(StringSelectInteractionEvent event, String type)
	{
		switch (type)
		{
			case "createColor":
				new Thread(new ColorRoleAdderThreader(event, event.getGuild(), colorInfo())).start();
				break;
			case "repairLevel":
				new Thread(new LevelRoleRepairThreader(event, event.getGuild())).start();
				break;
			case "repairColor":
				new Thread(new ColorRoleRepairThreader(event, event.getGuild(), colorInfo())).start();
				break;
			case "color":
				new Thread(new PromptColorThreader(event, colorInfo())).start();
				break;
			case "nsfw":
				new Thread(new PromptNSFWThreader(event)).start();
				break;
			case "roles":
				new Thread(new PromptRolesThreader(event, false)).start();
				break;
			case "rolesRedo":
				new Thread(new PromptRolesThreader(event, true)).start();
				break;
		}
	}

	private ArrayList<String[]> colorInfo()
	{
		ArrayList<String[]> colorData = new ArrayList<>();
		String[] titles = {"Red", "Red Orange", "Orange", "Orange Yellow", "Yellow", "Lime", "Green", "Aqua", "Turquoise", "Blue", "Purple", "Pink", "Fuchsia", "Brown", "Dark Red", "Dark Green", "Dark Blue", "Dark Purple"};
		String[] values = {"#ff0000", "#ff4600", "#ff6600", "#ff9900", "#ffff00", "#9bff00", "#00ad00", "#00ffff", "#30d5c8", "#0000ff", "#9900ff", "#ff00ff", "#ff0099", "#562200", "#7f0000", "#003d00", "#000071", "#700070"};
		colorData.add(titles);
		colorData.add(values);
		return colorData;
	}

	public static RoleManagement getINSTANCE()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new RoleManagement();
		}

		return INSTANCE;
	}
}