package au.com.addstar.copycat.commands;

public class CopyCatCommand extends RootCommandDispatcher
{
	public CopyCatCommand()
	{
		super("");
		
		registerCommand(new CreateCommand());
	}
}