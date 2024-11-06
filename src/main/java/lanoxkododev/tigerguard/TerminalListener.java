package lanoxkododev.tigerguard;

import java.util.Scanner;

public class TerminalListener extends Thread {

	TigerGuard tigerGuard;

	public void terminalListener() {}

	@Override
	public void run()
	{
		terminalReader();
	}

	private void terminalReader()
	{
		Scanner input = new Scanner(System.in);

		while (!TigerGuard.STOP)
		{
			switch (input.next())
			{
				case "reboot":
				case "restart":
					TigerGuard.TigerGuardInstance.TigerGuardStop(false);
					break;
				case "stop":
				case "exit":
					TigerGuard.TigerGuardInstance.TigerGuardStop(true);
					break;
			}
		}

		input.close();
	}
}
