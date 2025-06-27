package lanoxkododev.tigerguard;

import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class PermissionValidator {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	
	/**
	 * Check if the user meets the Mid-tier permission requirement for an administrative-like function calling this.
	 * 
	 * @param guild - the associated guild.
	 * @param member - the member which invoked whatever calls this method.
	 * @return
	 */
	public boolean administrativeAccessBase(Guild guild, Member member)
	{
		List<Role> roles = member.getRoles();
		Long roleAdmin = tigerguardDB.getGuildAdminRole(guild.getIdLong());
		Long rolePrimStaff = tigerguardDB.getGuildStaffRole(guild.getIdLong());
		Long roleSuppStaff = tigerguardDB.getGuildSupportingStaffRole(guild.getIdLong());
		
		if (member == guild.getOwner() || roleCheck(roles, guild, roleAdmin) ||
			roleCheck(roles, guild, rolePrimStaff) || roleCheck(roles, guild, roleSuppStaff)) return true;
		else return false;
	}
	
	/**
	 * Check if the user meets the High-tier permission requirement for an administrative-like function calling this.
	 * 
	 * @param guild - the associated guild.
	 * @param member - the member which invoked whatever calls this method.
	 * @return
	 */
	public boolean administrativeAccessElevated(Guild guild, Member member)
	{
		List<Role> roles = member.getRoles();
		Long roleAdmin = tigerguardDB.getGuildAdminRole(guild.getIdLong());
		Long rolePrimStaff = tigerguardDB.getGuildStaffRole(guild.getIdLong());
		
		if (member == guild.getOwner() || roleCheck(roles, guild, roleAdmin) || roleCheck(roles, guild, rolePrimStaff)) return true;
		else return false;
	}
	
	private boolean roleCheck(List<Role> roles, Guild guild, Long roleID)
	{
		if (roleID == null || roleID == 0L) return false;
		
		if (roles.contains(guild.getRoleById(roleID))) return true;
		else return false;
	}
}
