package lanoxkododev.tigerguard;

import java.util.ArrayList;
import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB.DB_Enums;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class PermissionValidator {

	TigerGuardDB tgdb = TigerGuardDB.getTigerGuardDB();
	
	/**
	 * Check whether the member can use the function that invokes this method.
	 * @param guild - the guild the command is used in
	 * @param member - the member invoking the request
	 * @param lowestPermitted - whether the lowest perm usage (DB_Enums.MOD) should be used
	 * @return
	 */
	public boolean canAccess(Guild guild, Member member, boolean lowestPermitted)
	{
		if (member == guild.getOwner()) return true;
		else
		{
			Long guildID = guild.getIdLong();
			
			List<Role> memberRoles = member.getRoles();
			ArrayList<Long> permRoles = new ArrayList<>();
			permRoles.add(tgdb.getValue(DB_Enums.ADMIN, "guild", guildID));
			permRoles.add(tgdb.getValue(DB_Enums.STAFF, "guild", guildID));
			
			if (lowestPermitted) permRoles.add(tgdb.getValue(DB_Enums.MOD, "guild", guildID));
			
			for (Long role : permRoles)
			{
				if (role != null && memberRoles.contains(guild.getRoleById(role))) return true;
			}
		}
		
		return false;
	}
}
