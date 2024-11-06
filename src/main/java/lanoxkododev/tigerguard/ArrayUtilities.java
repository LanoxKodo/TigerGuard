package lanoxkododev.tigerguard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class ArrayUtilities {

	//Storage for userID's to mitigate spam-messages for level boosting from every individual message.
	public static final List<Member> xpThrottle = new ArrayList<>();

	//Storage for users when they've engaged an xp event via sending a message. Guild portion involves the guild as well so a person can gain xp per each guild that opt-into the leveling system
	public static final Map<Member, Guild> xpThrottleTest = new HashMap<>();

	//Storage for guild's when a user joins or leaves a server so we don't update the memberCount vc too often by abusing JDA/Discord API. This will be set to a 10-minute cooldown.
	public static final List<Guild> guildMemberCounter = new ArrayList<>();
}