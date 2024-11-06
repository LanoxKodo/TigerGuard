package lanoxkododev.tigerguard.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TigerLogs {

	/**
	 * Simple system.out.println helper method. Pass an Enum<LogType> to display the severity level of the printed statement and the String of whatever needs to be appended.
	 *
	 * @param logVariant - The log severity type.
	 * @param toLog		 - The string to print after the log type.
	 */
	public void log(@NotNull Enum<LogType> logVariant, @NotNull String logStatement)
	{
		if (logVariant.equals(LogType.DEBUG))
		{
			//[HH:mm:ss DEBUG] <Message>
			//┗@: <Location>
			logStatement += "\n┗@: " + Thread.currentThread().getStackTrace()[2].getClassName() + "." +
				Thread.currentThread().getStackTrace()[2].getMethodName() + "()";
		}
		
		System.out.println(String.format("[%s %s] %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), logVariant, logStatement));
	}

	/**
	 * Log method for errors.
	 *
	 * @param logVariant 	- The log severity type.
	 * @param initialError	- The string to print after the log type.
	 * @param optStatement	- An optional message to help define an error.
	 * @param e			 	- The exception being passed to print the stacktrace from.
	 */
	public void logErr(@NotNull Enum<LogType> logVariant, @NotNull String initialError, @Nullable String optStatement, @NotNull Exception e)
	{
		/*
		 * [HH:mm:ss ErrorType] <Error statement>
		 * .@: <Location>
		 * .X: optStatement
		 */
		String lineA = String.format("[%s %s] %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), logVariant, initialError);
		String end = "\n┗";
		String lineB = Thread.currentThread().getStackTrace()[2].getClassName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName() + "()";
		
		if (optStatement == null || optStatement.isEmpty()) System.out.println(lineA + end + "@: " + lineB);
		else System.out.println(lineA + "\n┣@: " + lineB + end + "X: " + optStatement);
		e.printStackTrace();
	}
}