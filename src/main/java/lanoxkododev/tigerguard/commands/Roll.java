package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lanoxkododev.tigerguard.ThreadUtilities;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Roll implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();

	@Override
	public String getName()
	{
		return "roll";
	}

	@Override
	public String getDescription()
	{
		return "Roll a die or few and see your rng in action!";
	}

	@Override
	public boolean isNSFW()
	{
		return false;
	}

	@Override
	public List<OptionData> getOptions()
	{
		List<OptionData> options = new ArrayList<>();
		options.add(new OptionData(OptionType.BOOLEAN, "private", "True/False. Do the results need to be private?", true));
		options.add(new OptionData(OptionType.INTEGER, "d4", "How many d4's do you need? Max:30", false).setMaxValue(30));
		options.add(new OptionData(OptionType.INTEGER, "d6", "How many d6's do you need? Max:30", false).setMaxValue(30));
		options.add(new OptionData(OptionType.INTEGER, "d8", "How many d8's do you need? Max:30", false).setMaxValue(30));
		options.add(new OptionData(OptionType.INTEGER, "d10", "How many d10's do you need? Max:30", false).setMaxValue(30));
		options.add(new OptionData(OptionType.INTEGER, "d12", "How many d12's do you need? Max:30", false).setMaxValue(30));
		options.add(new OptionData(OptionType.INTEGER, "d20", "How many d20's do you need? Max:30", false).setMaxValue(30));
		options.add(new OptionData(OptionType.INTEGER, "d100", "How many d100's do you need? Max:30", false).setMaxValue(30));

		return options;
	}
	
	@Override
	public DefaultMemberPermissions getDefaultPermission()
	{
		return DefaultMemberPermissions.ENABLED;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		event.deferReply().setEphemeral(event.getOption("private").getAsBoolean()).queue();
		ThreadUtilities.createGenericThread(a -> {
			class DieRoll {
				private int sides;
				private int result;

				public DieRoll(int sides)
				{
					this.sides = sides;
					roll();
				}

				private void roll()
				{
					Random random = new Random();
					result = random.nextInt(sides) + 1;
				}

				public int getRollResult()
				{
					return result;
				}
			}
			
			class DiceRollCollection {
				private Map<Integer, List<DieRoll>> rollSets;

				public DiceRollCollection()
				{
					rollSets = new HashMap<>();
				}

				public void rollDice(int[] sides, int[] diceCount)
				{
					for (int s = 0; s < sides.length; s++)
					{
						for (int c = 0; c < diceCount[s]; c++)
						{
							int dieSides = sides[s];
							DieRoll dieRoll = new DieRoll(dieSides);
							rollSets.computeIfAbsent(dieSides, k -> new ArrayList<>()).add(dieRoll);
						}
					}
				}

				public Map<Integer, List<DieRoll>> getRollSets()
				{
					return rollSets;
				}
			}
			
			Integer d4 = null;
			Integer d6 = null;
			Integer d8 = null;
			Integer d10 = null;
			Integer d12 = null;
			Integer d20 = null;
			Integer d100 = null;
			
			ArrayList<Integer> validTypes = new ArrayList<>();
			ArrayList<Integer> validCounts = new ArrayList<>();
			
			if (event.getOption("d4") != null)
			{
				d4 = event.getOption("d4").getAsInt();
				validTypes.add(4);
				validCounts.add(d4);
			}
			if (event.getOption("d6") != null)
			{
				d6 = event.getOption("d6").getAsInt();
				validTypes.add(6);
				validCounts.add(d6);
			}
			if (event.getOption("d8") != null)
			{
				d8 = event.getOption("d8").getAsInt();
				validTypes.add(8);
				validCounts.add(d8);
			}
			if (event.getOption("d10") != null)
			{
				d10 = event.getOption("d10").getAsInt();
				validTypes.add(10);
				validCounts.add(d10);
			}
			if (event.getOption("d12") != null)
			{
				d12 = event.getOption("d12").getAsInt();
				validTypes.add(12);
				validCounts.add(d12);
			}
			if (event.getOption("d20") != null)
			{
				d20 = event.getOption("d20").getAsInt();
				validTypes.add(20);
				validCounts.add(d20);
			}
			if (event.getOption("d100") != null)
			{
				d100 = event.getOption("d100").getAsInt();
				validTypes.add(100);
				validCounts.add(d100);
			}
			
			if ((d4 == null) && (d6 == null) && (d8 == null) && (d10 == null) && (d12 == null) && (d20 == null) && (d100 == null))
			{
				event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Well, umm, what am I rolling?", null, null, ColorCodes.UNABLE,
					"It appears you provided me nothing to roll; this command has the dice parts as optional, you need to specify which one you need, for example 'd4' so you can then enter a number for the amount of dice for that type.")).setEphemeral(true).queue();
			}
			else
			{
				int[] diceTypes = new int[validTypes.size()];
				int[] diceCount = new int[validCounts.size()];
				for (int b = 0; b < validTypes.size(); b++)
				{
					diceTypes[b] = validTypes.get(b);
					diceCount[b] = validCounts.get(b);
				}

				DiceRollCollection drc = new DiceRollCollection();
				drc.rollDice(diceTypes, diceCount);
				
				Map<Integer, List<DieRoll>> rollsByDie = drc.getRollSets();

				List<Map.Entry<Integer, List<DieRoll>>> sortedList = new ArrayList<>(rollsByDie.entrySet());
				Collections.sort(sortedList, Comparator.comparingInt(Map.Entry::getKey));
				
				String finalMessage = "";
				int absoluteTotal = 0;
		        for (Map.Entry<Integer, List<DieRoll>> entry : sortedList)
		        {
		        	int dieSides = entry.getKey();
		            List<DieRoll> rolls = entry.getValue();
		            
		            String statement = "**" + rolls.size() + "D" + dieSides + ":** ";
		            String calc = "";
		            String temp = "";
		            int calcTotal = 0;
		            int counter = 0;
		            for (DieRoll roll : rolls)
		            {
		            	int rolledValue = roll.getRollResult();
		            	temp += rolledValue;
		            	calcTotal += rolledValue;
		            	
		            	if (counter != rolls.size()-1) temp += ", ";
		            	else
		            	{
		            		calc = "***" + calcTotal + "***";
		            	}
		            	counter++;
		            }
		            //System.out.println(statement + temp);
		            finalMessage += String.format("%10s %10s %-30s", statement, calc, ("[" + temp + "]\n"));
		            absoluteTotal += calcTotal;
		        }
		        finalMessage += "\n**Total:** ***" + absoluteTotal + "***";

				if (event.getOption("private").getAsBoolean()) event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Roll results:", null, null, ColorCodes.FINISHED, finalMessage)).setEphemeral(true).queue();
				else event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Roll results:", null, null, ColorCodes.FINISHED, finalMessage)).queue();
			}
		}, null, null, false, false);
	}
}