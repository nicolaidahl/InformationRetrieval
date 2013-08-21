package au.edu.rmit.stopping;

public class DummyStopperModule implements StopperModule
{

	@Override
	public boolean isStopWord(String word)
	{
		return false;
	}

}
