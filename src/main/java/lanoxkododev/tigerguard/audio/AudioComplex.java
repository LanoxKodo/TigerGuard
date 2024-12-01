package lanoxkododev.tigerguard.audio;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.EmittedEvent;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import lanoxkododev.tigerguard.TigerGuard;
import net.dv8tion.jda.api.entities.Guild;

public class AudioComplex {

	static AudioComplex instance;
	private final LavalinkClient client;
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    public AudioComplex(LavalinkClient client)
    {
        this.client = client;
        instance = this;

        registerLavalinkNodes();
		registerLavalinkListeners();
    }

    public GuildMusicManager acquireMusicManager(Guild guildId)
    {
        synchronized (this)
        {
            var mng = this.musicManagers.get(guildId.getIdLong());
            if (mng == null)
            {
                mng = new GuildMusicManager(guildId, this.client);
                this.musicManagers.put(guildId.getIdLong(), mng);
            }
            return mng;
        }
    }

    public LavalinkClient getClient()
    {
    	return client;
    }

    public Map<Long, GuildMusicManager> getMusicManagers()
    {
    	return musicManagers;
    }

    public static AudioComplex getInstance()
    {
    	return instance;
    }

    private void registerLavalinkNodes()
	{
		List.of(client.addNode(new NodeOptions.Builder("TigerGuardLavalink", URI.create("ws://localhost:2333"), "youshallnotpass", RegionGroup.US, 5000L)
			.build())).forEach((node) -> {
				node.on(TrackStartEvent.class).subscribe();
				node.on(TrackEndEvent.class).subscribe();
		});
	}

	private void registerLavalinkListeners()
	{
        client.on(dev.arbjerg.lavalink.client.event.ReadyEvent.class).subscribe((event) -> {
            final LavalinkNode node = event.getNode();

            System.out.printf("Node '%s' is ready, session id is '%s'!%n", node.getName(), event.getSessionId());
        });

        client.on(EmittedEvent.class).subscribe((event) -> {
            if (event instanceof TrackStartEvent) {}
            else if (event instanceof TrackEndEvent)
			{
            	acquireMusicManager(TigerGuard.getTigerGuard().getJDA().getGuildById(event.getGuildId())).scheduler.onTrackEnd(true);
			}
        });
    }
}
