package lanoxkododev.tigerguard.audio;

import java.util.Optional;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;

public class GuildMusicManager {

	public final TrackScheduler scheduler = new TrackScheduler(this);
    private final long guildId;
    private final Guild guild;
    private final LavalinkClient lavalink;

    public GuildMusicManager(Guild guildIn, LavalinkClient lavalink)
    {
        this.lavalink = lavalink;
        this.guildId = guildIn.getIdLong();
        this.guild = guildIn;
    }

    public void stop()
    {
        scheduler.deleteMusicEmbed();
        scheduler.queue.clear();

        getPlayer().ifPresent(
        	(player) -> player.setPaused(false)
        		.setTrack(null)
        		.subscribe()
        );
        guild.getJDA().getDirectAudioController().disconnect(guild);
    }

    public void skip()
    {
    	scheduler.startNextTrack();
    }

    /**
     * Change whether the player is paused (true) or playing (false)
     *
     * @param status
     */
    public void setPauseStatus(boolean status)
    {
    	getLink().ifPresent(player -> player.createOrUpdatePlayer().setPaused(status).subscribe());
    }

    public Guild getGuild()
    {
    	return guild;
    }

    public Optional<Link> getLink()
    {
        return Optional.ofNullable(lavalink.getLinkIfCached(guildId));
    }

    public Optional<LavalinkPlayer> getPlayer()
    {
        return this.getLink().map(Link::getCachedPlayer);
    }
}