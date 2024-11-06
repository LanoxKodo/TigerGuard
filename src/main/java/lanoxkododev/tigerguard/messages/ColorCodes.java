package lanoxkododev.tigerguard.messages;

public enum ColorCodes {

	INFO(0x134f5c), 		//Seaworld (Teal)
	JOIN(0x18a00a),			//Green
	FINISHED(0x00ff00),		//Lime
	MEH_NOTICE(0xb3b300),	//Dim-Yellow
	TIGER_FUR(0xff9900),	//Orange
	FLARE(0xff471a),		//Red-Orange
	UNABLE(0xcc6666),		//Dim-Red
	ERROR(0x9F1A1A),		//Red
	LEAVE(0xffe6e6),		//Deep Red
	NSFW(0xC20056),			//Fuschia
	MUSIC(0x730099),		//Purple
	POLL(0x13266e),			//Pure-Midnight
	CONFIRMATION(0x2649d3),	//Blue/Blue-Purple
	TESTING(0x00ffff),		//Turqouise
	N_A(0x000000);			//Black

	public final Integer value;

    private ColorCodes(int i)
    {
        this.value = i;
    }
    
    /*
     * Return hex-value of color.
     */
    public String toHexString()
    {
    	return String.format("#%06X", (0xFFFFFF & value));
    }
}
