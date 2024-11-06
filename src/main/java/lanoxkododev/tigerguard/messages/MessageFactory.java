package lanoxkododev.tigerguard.messages;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.pagination.Pages;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class MessageFactory {
	
	Font font40p = new Font("Fira Sans SemiBold", Font.PLAIN, 40);
    Font font30p = new Font("Fira Sans SemiBold", Font.PLAIN, 30);
    Font font30i = new Font("Fira Sans SemiBold", Font.ITALIC, 30);
    Font font28i = new Font("Fira Sans SemiBold", Font.ITALIC, 28);
    Font font25i = new Font("Fira Sans SemiBold", Font.ITALIC, 25);
    Color underlay = new Color(13, 13, 13, 40);
    Color underlayStrong = new Color(13, 13, 13, 60);
    Color underlayDark = new Color(13, 13, 13, 200);

    public byte[] createRanklevelUpImage(Guild guild, Member member, String newLevelRole, int newLevel, boolean test) throws IOException
    {
    	BufferedImage background = resize(new Pages().getBufferedImage(TigerGuardDB.getTigerGuardDB().getUserRankImage(member.getIdLong())), 700, 300);
        BufferedImage base = new BufferedImage(700, 300, BufferedImage.TYPE_INT_ARGB);

        List<Role> role = guild.getRolesByName(newLevelRole, true);
        Color roleColor = role.get(0).getColor();
        String roleName = role.get(0).getName();

        BufferedImage memberImage = scaleAndCircleize(ImageIO.read(uri2url(member.getEffectiveAvatarUrl())));

        Graphics2D base2D = base.createGraphics();

        //Background
        base2D.setComposite(AlphaComposite.Src);
        base2D.drawImage(background, null, 0, 0);
        base2D.setColor(Color.WHITE);
        base2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        base2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        String nameRevised = "";
        String roleRevised = roleTrimmer(roleName);

        if (!test)
        {
        	nameRevised = nameTrimmer(member.getEffectiveName()) + "has leveled up!";
        }
        else
        {
        	int rng = new Random().nextInt(0,4);
	        String testName = "Tester";

	        for (int a = 0; a < rng; a++)
	        {
	        	testName += "0";
	        }

        	nameRevised = nameTrimmer(testName) + "has leveled up!";
        }

        FontMetrics f30p_metric = base2D.getFontMetrics(font30p);

        //Draw dimmed layer over background
        base2D.setComposite(AlphaComposite.SrcAtop);
        base2D.setColor(underlayStrong);
        base2D.fillRect(0, 0, background.getWidth(), background.getHeight());

        base2D.setColor(underlayDark);
        base2D.fillRoundRect((base.getWidth()/2-((int)(f30p_metric.getStringBounds(nameRevised, base2D).getWidth() + 10)/2)), 194, (int)(f30p_metric.getStringBounds(nameRevised, base2D)).getWidth() + 10, (int)(f30p_metric.getStringBounds(nameRevised, base2D)).getHeight(), 5, 5);
        base2D.fillRoundRect((base.getWidth()/2-((int)(f30p_metric.getStringBounds(roleRevised, base2D).getWidth() + 10)/2)), 234, (int)(f30p_metric.getStringBounds(roleRevised, base2D)).getWidth() + 10, (int)(f30p_metric.getStringBounds(roleRevised, base2D)).getHeight(), 5, 5);

        //User's image
        base2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        base2D.drawImage(memberImage, null, (base.getWidth()/2)-80, 20);

        //graphics2D.setColor(new Color(252, 194, 0));
        base2D.setColor(member.getColor());
        base2D.setFont(font30p);
        base2D.drawString(nameRevised, (base.getWidth()/2-(base2D.getFontMetrics().stringWidth(nameRevised)/2)), 220);


        base2D.setColor(roleColor);
        base2D.setFont(font30i);
        base2D.drawString(roleRevised, (base.getWidth()/2-(base2D.getFontMetrics().stringWidth(roleRevised)/2)), 260);

    	try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            ImageIO.write(base, "PNG", outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Create the rank card for the user.
     *
     * @param member	  - The member the rank card is being made for.
     * @param level		  - The level of the member.
     * @param xp		  - The xp of the member.
     * @param nextXP	  - The level-up xp requirement value.
     * @param isMaxLevel  - Boolean flag, true for user is max level, false for less than max level.
     * @return
     * @throws IOException
     */
	public byte[] createRankImage(Member member, String levelRoleName, int level, int xp, int nextXP, int prevXP, boolean isMaxLevel) throws IOException
	{
        if (member == null)
            return new byte[128];

        BufferedImage background = resize(new Pages().getBufferedImage(TigerGuardDB.getTigerGuardDB().getUserRankImage(member.getIdLong())), 700, 300);
        BufferedImage base = new BufferedImage(700, 300, BufferedImage.TYPE_INT_ARGB);
        Color userColor;

        try
        {
        	userColor = member.getColor();
        }
        catch (Exception e)
        {
        	userColor = Color.WHITE;
        }

        BufferedImage memberImage = scaleAndCircleize(ImageIO.read(uri2url(member.getEffectiveAvatarUrl())));

        Graphics2D base2D = base.createGraphics();

        base2D.setComposite(AlphaComposite.Clear);
        base2D.fillRect(0, 0, base.getWidth(), base.getHeight());

        //Background
        base2D.setComposite(AlphaComposite.Src);
        base2D.drawImage(background, null, 0, 0);
        base2D.setColor(Color.WHITE);
        base2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        base2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        String nameRevised = nameTrimmer(member.getEffectiveName());

        FontMetrics f40_metric = base2D.getFontMetrics(font40p);
        FontMetrics f25i_metric = base2D.getFontMetrics(font25i);
        FontMetrics f30p_metric = base2D.getFontMetrics(font30p);

        Rectangle2D f40_bounder = f40_metric.getStringBounds(nameRevised, base2D);
        Rectangle2D f25i_bounder = f25i_metric.getStringBounds(levelRoleName, base2D);

        //Draw base-dimmed layer over background
        base2D.setComposite(AlphaComposite.SrcAtop);
        base2D.setColor(underlay);
        base2D.fillRect(0, 0, background.getWidth(), background.getHeight());

        //Draw dimmed parts for next layers for visibility reasons
        base2D.setColor(underlayDark);
        base2D.setComposite(AlphaComposite.SrcAtop);
        base2D.fillRoundRect(5, 5, (int)f40_bounder.getWidth(), (int)f40_bounder.getHeight(), 5, 5);
        base2D.fillRoundRect(5, 56, (int)f25i_bounder.getWidth() + 15, (int)f25i_bounder.getHeight(), 5, 5);
        base2D.fillRoundRect(415, 12, (int)(f25i_metric.getStringBounds("LEVEL  ", base2D).getWidth() + f30p_metric.getStringBounds("" + level, base2D).getWidth()), (int)f30p_metric.getStringBounds("" + level, base2D).getHeight(), 5, 5);
        base2D.fillRoundRect((base.getWidth()/2)-(base2D.getFontMetrics().stringWidth(xp+"/"+nextXP+"xp"))-27, (base.getHeight()-60),
        		(int)(f30p_metric.getStringBounds(xp+"/"+nextXP+"xp", base2D).getWidth()+20), (int)(f30p_metric.getStringBounds(xp+"/"+nextXP+"xp", base2D).getHeight() + 10), 5, 5);

        //User's image
        base2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        base2D.drawImage(memberImage, null, 530, 10);

        //User's icons - these are the images from /assets/rankIcons/ - this was a test field that may be implemented later
        //graphics2D.drawImage(iconA, null, 540, 180);
        //graphics2D.drawImage(iconB, null, 585, 180);

        //User's name
        base2D.setFont(font40p);
        base2D.setColor(userColor);
        base2D.drawString(nameRevised, 10, 40);

        //User's highest levelUp-role
        base2D.setFont(font25i);
        base2D.drawString(levelRoleName, 10, 80);

        //Level stats
        base2D.setFont(font25i);
        base2D.drawString("LEVEL ", 420, 40);
        base2D.setFont(font30p);
        base2D.drawString("" + level, 487, 40);

        //If user is max level
        if (isMaxLevel)
        {
        	base2D.setColor(underlayDark);
        	Rectangle2D f28i_bounder = base2D.getFontMetrics(font28i).getStringBounds("MAX!", base2D);
        	base2D.fillRoundRect(450, 50, (int)f28i_bounder.getWidth() + 10, (int)f28i_bounder.getHeight(), 5, 5);
        	base2D.setColor(userColor);
        	base2D.setFont(font28i);
        	base2D.drawString("MAX!", 455, 75);
        }

        //XP parts
        base2D.setFont(font30p);
        int xpCenterX = base.getWidth();
        int xpCenterY = base.getHeight();

        base2D.drawString(xp+"/"+nextXP+"xp", (xpCenterX/2)-(base2D.getFontMetrics().stringWidth(xp+"/"+nextXP+"xp")/2), (xpCenterY-30));

        int barWidth = (int)(xpCenterX*.9);
        double progressPercent = 0.0;

        if (xp != 0) progressPercent = 1 - (((double)xp-nextXP)/(prevXP-nextXP));

        //Base bar
        base2D.setColor(Color.BLACK);
        base2D.fillRoundRect((base.getWidth()-barWidth)/2, xpCenterY-20, barWidth, 15, 5, 5);
        //Progress bar
        base2D.setColor(userColor);
        base2D.fillRoundRect((base.getWidth()-barWidth)/2, xpCenterY-20, (int)(barWidth * progressPercent), 15, 5, 5);

        // Close the Graphics2D instance.
        base2D.dispose();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            ImageIO.write(base, "PNG", outputStream);
            return outputStream.toByteArray();
        }
    }

	private String nameTrimmer(String inputName)
	{
		String membername = inputName;

        if (membername.length() > 20)
        {
            membername = membername.substring(0, 16);
        }

        Pattern emojiExcluderRegex = Pattern.compile("[a-zA-z0-9!@#$%&*() _+=|<>?{}\\[\\]~-]");
        String nameRevised = "";

        boolean matchedEvent = false;

        for (int a = 0; a < membername.length(); a++)
        {
        	Matcher found = null;

        	try
        	{
        		found = emojiExcluderRegex.matcher(membername.charAt(a) + "");
        	}
        	catch (Exception e) {}

        	if (!found.find()) matchedEvent = true;
        	else nameRevised += membername.charAt(a);
        }

        if (!matchedEvent)
        {
        	nameRevised += " ";
        }

        return nameRevised;
	}

	private String roleTrimmer(String inputRole)
	{
		String rolename = "";

		for (int a = 0; a < inputRole.length(); a++)
		{
			int type = Character.getType(inputRole.charAt(a));
			if (type != Character.SURROGATE && type != Character.OTHER_SYMBOL)
			{
				rolename += inputRole.charAt(a);
			}
		}

		return rolename;
	}

	private static BufferedImage scaleAndCircleize(BufferedImage item)
	{
        //Scale and Transform
		int w = item.getWidth();
        int h = item.getHeight();
        int w2 = (int) (w * 1.25);
        int h2 = (int) (h * 1.25);
        BufferedImage modded = new BufferedImage(w2, h2, item.getType());
        AffineTransform scaleInstance = AffineTransform.getScaleInstance(1.25, 1.25);
        AffineTransformOp scaleOp = new AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BICUBIC);
        modded = scaleOp.filter(item, modded);

        //Make as circle
        BufferedImage output = new BufferedImage(modded.getWidth(), modded.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();

        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Float(0, 0, modded.getWidth(), modded.getHeight()));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(modded, 0, 0, null);
        g2.dispose();

        return output;
	}

	public static BufferedImage resize(BufferedImage inputImage, int scaledWidth, int scaledHeight)
	{
        if (inputImage == null)
            return null;

        if (inputImage.getWidth() == scaledWidth && inputImage.getHeight() == scaledHeight) {
            return inputImage;
        }

        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return outputImage;
    }

	private URL uri2url(String input)
	{
		URL url = null;
		try
		{
			url = new URI(input).toURL();
		}
		catch (Exception e)
		{
			new TigerLogs().logErr(LogType.ERROR, "Failure converting uri to url with the provided input", input , e);
		}

		return url;
	}
}