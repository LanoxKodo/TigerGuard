package lanoxkododev.tigerguard.pagination;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.javatuples.Quintet;

import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;

public class Pages {

	public static Pages pages;
	TigerLogs logger = new TigerLogs();

	String[] imageList = {
		"/assets/backgrounds/index.png",
		"/assets/backgrounds/cherryTree.png",
		"/assets/backgrounds/colorfulFlowerGarden.png",
		"/assets/backgrounds/fantasyNatureTown01.png",
		"/assets/backgrounds/fantasyNatureTown02.png",
		"/assets/backgrounds/koiPond.png"
		};

	public Pages()
	{
		pages = this;
	}

	public static Pages getInstance()
	{
		return pages;
	}

	public int imageListSize()
	{
		return imageList.length - 1;
	}

	/*
	 * Embed Format: Title, ColorCode, Image, Description, PageNumber
	 */
	public Quintet<String, ColorCodes, String, String, String> rankCustomizerPages(Integer num)
	{
		String[] desc = { "Welcome to the rank customization menu! Here you can use the buttons "
			+ "at the bottom to navigate through this embed and select an option. The buttons you'll see on each page are as follows:\n\n"
			+ "⏪ | Move to the starting page.\n"
			+ "◀️ | Move to the previous page.\n"
			+ "▶️ | Move to the next page.\n"
			+ "⏩ | Move to the last page.\n"
			+ "⏺️ | Choose the option on the current page.\n\n"
			+ "The images that are shown in this embed series (except the one on this page) are globally set; so the image you set here will be shown in other server's where TigerGuard is active in when rank-processes occur there!"
			+ " This may change in the future."
			+ "\n\nAt any time, when you are finished, simply dimiss the embed using the 'dismiss message' text below! Let's begin with customization, shall we?",
			"Looking towards a Cherry Blossom Tree",
			"Colorful nature scene",
			"Waterside town of sorts, A",
			"Waterside town of sorts, B",
			"A pond featuring many Koi fish"};

		String image = "";

		try
		{
			image = getImageAsStream(num).toString();
		}
		catch (Exception e)
		{
			logger.logErr(LogType.ERROR, "Error converting image instance into string", null, e);
		}

		return Quintet.with("Rank Card Customization", ColorCodes.TIGER_FUR, image, desc[num], "Page " + num.toString());
	}

	public BufferedImage getBufferedImage(Integer num) throws IOException
	{
		return ImageIO.read(getClass().getResource(imageList[num]));
	}

	public InputStream getImageAsStream(Integer num)
	{
		InputStream inputImage = null;

		try
		{
			inputImage = getClass().getResourceAsStream(imageList[num]);
		}
		catch (Exception e)
		{
			logger.logErr(LogType.ERROR, "Failure in processing image for '" + imageList[num] + "'", "InputStream = " + inputImage, e);
		}

		return inputImage;
	}
}