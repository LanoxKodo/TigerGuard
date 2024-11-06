package lanoxkododev.tigerguard;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;

public class ThreadUtilities {

	static TigerLogs logger = new TigerLogs();

	//Thread pool used for Asyncronous thread handling.
	static ExecutorService executorPool = Executors.newFixedThreadPool(10);

	public static Future<?> genericThread(Consumer<Void> success, String failure)
	{
		return createGenericThread(success, failure, null, false, true);
	}

	public static Future<?> createThread(Consumer<Void> success, Consumer<Throwable> failure)
	{
        return createNewThread(success, failure, null, false, true);
    }

	/**
	 * Creates a new thread with a consumer to act with.
	 *
	 * @param success      - The consumer that will be executed when the thread finishes.
	 * @param failure	   - The consumer that will be executed when the thread fails.
	 * @param timeDuration - The delay for the thread to act with.
	 * @param toLoop	   - Boolean flag for if the thread should be looped through.
	 * @param acceptBefore - Boolean flag for if the thread should execute the consumer at the start or end of the thread lifetime, regardless of loop status.
	 * @return
	 */
	public static Future<?> createGenericThread(Consumer<Void> success, String failure, Duration sleepDuration, boolean toLoop, boolean acceptBefore)
	{
		return Executors.newSingleThreadExecutor().submit(() -> {
			while (!Thread.currentThread().isInterrupted())
			{
				if (acceptBefore)
				{
					success.accept(null);
					if (!toLoop) Thread.currentThread().interrupt();
				}

				if (sleepDuration != null)
				{
					try
					{
						Thread.sleep(sleepDuration.toMillis());
					}
					catch (Exception e)
					{
						if (failure != null) logger.logErr(LogType.ERROR, failure, null, e);
						else logger.logErr(LogType.ERROR, "ThreadUtilities executor encountered a failure while trying to sleep", null, e);
						
						Thread.currentThread().interrupt();
					}
				}

				if (!acceptBefore)
				{
					success.accept(null);
					if (!toLoop) Thread.currentThread().interrupt();
				}
			}
		});
	}

	/**
	 * Creat a new thread with a consumer to act with.
	 *
	 * @param success      - The consumer that will be executed when the thread finishes.
	 * @param failure	   - The consumer that will be executed when the thread fails.
	 * @param timeDuration - The delay for the thread to act with.
	 * @param toLoop	   - Boolean flag for if the thread should be looped through.
	 * @param acceptBefore - Boolean flag for if the thread should execute the consumer at the start or end of the thread lifetime, regardless of loop status.
	 * @return
	 */
	public static Future<?> createNewThread(Consumer<Void> success, Consumer<Throwable> failureReason, Duration sleepDuration, boolean toLoop, boolean doBefore)
	{
		return executorPool.submit(() -> {
			while (!Thread.currentThread().isInterrupted())
			{
				if (doBefore)
				{
					success.accept(null);
					if (!toLoop) Thread.currentThread().interrupt();
				}

				try
				{
					if (sleepDuration != null) Thread.sleep(sleepDuration.toMillis());
				}
				catch (Exception e)
				{
					if (failureReason == null) logger.logErr(LogType.ERROR, "ThreadUtilities executor thread encountered a failure", null, e);
					else failureReason.accept(e);

					Thread.currentThread().interrupt();
				}

				if (!doBefore)
				{
					success.accept(null);
					if (!toLoop)
					{
						Thread.currentThread().interrupt();
					}
				}
			}
		});
	}
}
