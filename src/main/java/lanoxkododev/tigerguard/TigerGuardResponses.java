package lanoxkododev.tigerguard;

import java.util.Random;

public class TigerGuardResponses {

	/**
	 * Method for returning a string for non-available events.
	 * 0:Not Ready. 1:Depreciation
	 * @param a - the int to pull from the string[a]
	 * @return the resulting string
	 */
	public String notAvailable(int a)
	{
		String[] responses = {"Oohh, trying to get something out of this command, eh? Well I must hold my tongue for this request at this time. Stay alert for when this command is ready!\n"
				+ "Until then, I must attend to our community and look out for wild yarn; they come with strings attached which usually have nasty bugs we don't want here!",
				"This command or action is going undergoing depreciation. It's functionality has either been removed or relocated to another feature, most likely "
						+ "the latter. My dev probably left this in as a short-lived flag to remove later on, or it's a bug from somewhere in the code, er, yarn ball. They have strings attached everywhere and thus bugs!"};


		return responses[a];
	}
	
	public String banter(String memberTag)
	{
		String[] banterText = {
				"Oi, hello there " + memberTag + "! That isn't yarn that I see on you is it?",
				"Good day " + memberTag + ". I am on guard watching for yarn. We mustn't let them get through for they are nothing but trouble with strings attached. Hah!",
				"***AHH!*** " + memberTag + " you can't sneak up on me like that! I get spooked occassionally from the likes of that interaction...",
				"Reporting no anomalies currently. The sky is blissful and the wind is passing by. Continuing my watch."};
		Random inputRoll = new Random();


		return banterText[inputRoll.nextInt(banterText.length)];
	}
}