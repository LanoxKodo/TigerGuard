package lanoxkododev.tigerguard;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.WebSocketClosedEvent;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import lanoxkododev.tigerguard.audio.AudioComplex;
import lanoxkododev.tigerguard.commands.CommandCenter;
import lanoxkododev.tigerguard.events.ButtonClickEvents;
import lanoxkododev.tigerguard.events.ChannelEvents;
import lanoxkododev.tigerguard.events.JoinLeaveEvents;
import lanoxkododev.tigerguard.events.MessageEvents;
import lanoxkododev.tigerguard.events.ModalEvents;
import lanoxkododev.tigerguard.events.ReactionEvents;
import lanoxkododev.tigerguard.events.SelectEvents;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.pagination.Pages;
import lanoxkododev.tigerguard.time.TimingThread;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * @author LanoxKodo (Formerly SirBassington)
 *
 * TigerGuard is a Discord Bot made to assist in general server management and processes for server engagement.
 */
public class TigerGuard {

	TerminalListener terminalThread = new TerminalListener();
	TigerLogs logger = new TigerLogs();
	TimingThread timeThread;
	Utils util = new Utils();

	public static TigerGuard TigerGuardInstance;
	public static JDA jda;
	public static String prefix = "~tg"; //Debug prefix for certain commands

	private static boolean DEBUG_MODE = true;
	protected static boolean STOP = false;

    private static final int SESSION_INVALID = 4006;
    private final AudioPlayerManager playerManager;

	public static void main(String[] args) throws InterruptedException
	{
		new TigerGuard();
	}

	public TigerGuard() throws InterruptedException
	{
		TigerGuardInstance = this;
		STOP = false;

		playerManager = new DefaultAudioPlayerManager();
		
    	if (util.verifyConfig("TigerGuardConfig.txt"))
    	{
    		final String botToken = util.getValue("botToken");
    		
        	YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager();
        	
        	String ytToken = util.getValue("youtubeOAuthRefreshToken");
        	if (ytToken != null && (ytToken.isEmpty() || ytToken.equalsIgnoreCase("YOUR_YT_OAUTH_REFRESH_TOKEN"))) yt.useOauth2(null, false);
        	else yt.useOauth2(ytToken, true);
        	
        	playerManager.registerSourceManager(yt);

    		LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(botToken));
    		client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());

    		final var audioComplex = new AudioComplex(client, "TG_Audio");

    		//Command setup - Note: while functional, file needs to be revised updating/removing/adding commands in a more dynamic way, lower priority as no commands are in design or needing immediate changes.
    		final var commandCenter = new CommandCenter(audioComplex);

    		preJDALoad();

    		//Will need to use '.useSharding(#, #)' if the bot becomes bogged from higher usage than it currently sees.
    		jda = JDABuilder.createDefault(botToken).setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(client))
    			.enableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_EXPRESSIONS,
    			GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
    			.enableCache(CacheFlag.VOICE_STATE).setMemberCachePolicy(MemberCachePolicy.ALL).setActivity(Activity.watching("that evil yarn 🧶")).setStatus(OnlineStatus.ONLINE)
    			.addEventListeners(commandCenter, new JoinLeaveEvents(), new ButtonClickEvents(), new ChannelEvents(),
    				new SelectEvents(), new ModalEvents(), new MessageEvents(), new ReactionEvents()).build().awaitReady();

    		timeThread = new TimingThread();
    		timeThread.start();
    		terminalThread.start();
    		guildReview();

    		client.on(WebSocketClosedEvent.class).subscribe((event) -> {
                if (event.getCode() == SESSION_INVALID) {
                	
                    final var guild = jda.getGuildById(event.getGuildId());
                    if (guild == null) return;
                    
                    final var connectedChannel = guild.getSelfMember().getVoiceState().getChannel();
                    if (connectedChannel == null) return;

                    jda.getDirectAudioController().reconnect(connectedChannel);
                }
            });

    		logger.log(LogType.INFO, "Initialization complete!");
    	}
		else
		{
			logger.log(LogType.WARNING, "Stopping due to TigerGuardConfig.txt file either just being created with blank values or it is not filled in fully.");
		}
	}

	private void preJDALoad()
	{
		new Pages();
		new TigerGuardDB(util.getValue("address") + ':' + util.getValue("port"), util.getValue("databaseName"), util.getValue("databaseUsername"), util.getValue("databasePassword"));
	}

	//Diagnostic for reviewal purposes, helps identify DB errors if any occur
	private void guildReview()
	{
		if (jda.getGuilds().size() >= 20)
		{
			logger.log(LogType.INFO, "I am connected to a total of " + jda.getGuilds().size() + " guilds.");
		}
		else
		{
			logger.log(LogType.INFO, "Guilds I am connected to:");
			jda.getGuilds().forEach(guild -> {
				logger.log(LogType.INFO, "Guild: " + guild.getIdLong() + " | " + guild.getName());
			});
		}
	}

	/**
	 * Safely shut down the database connection by committing and DB changes and then turn off the bot.
	 * @throws InterruptedException
	 */
	protected void TigerGuardStop(boolean fullstop)
	{
		logger.log(LogType.INFO, "Stop signal requested. Shutting down instances.");
		timeThread.updateStopStatus();
		TigerGuardDB.getTigerGuardDB().closeConnection();
		jda.shutdown();

		if (fullstop)
		{
			System.exit(0);
		}
		else
		{
			try {
				Thread.sleep(3000);
				new TigerGuard();
			} catch (InterruptedException e) {}
		}
	}

	//Return the bot's instance of JDA
	public SelfUser getSelf()
	{
		return jda.getSelfUser();
	}

	//Return the JDA instance
	public JDA getJDA()
	{
		return jda;
	}

	public String getName()
	{
		return "TigerGuard";
	}

	public static TigerGuard getTigerGuard()
	{
		return TigerGuardInstance;
	}

	public static boolean isDebugMode()
	{
		return DEBUG_MODE;
	}
}