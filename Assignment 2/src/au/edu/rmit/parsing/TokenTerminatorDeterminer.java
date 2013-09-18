package au.edu.rmit.parsing;

interface TokenTerminatorDeterminer
{
	public boolean shouldTerminateToken(char ch);
}

class LetterContentTerminator implements TokenTerminatorDeterminer
{
	private boolean containHypens = false;
	

	@Override
	public boolean shouldTerminateToken(char ch)
	{
		if(ch == '-')
		{
			containHypens = true;
			return false;
		}
		
		return !Character.isLetterOrDigit(ch);
	}

	public boolean doesContainHypens()
	{
		return containHypens;
	}
}

class NumberContentTerminator implements TokenTerminatorDeterminer
{
	private boolean lastCharWasDotOrComma = false;
	
	@Override
	public boolean shouldTerminateToken(char ch)
	{
		if(Character.isLetterOrDigit(ch))
		{
			lastCharWasDotOrComma = false;
			return false;
		}
		else if(ch == '.' || ch == ',')
		{
			lastCharWasDotOrComma = true;
			return false;
		}
		
		return true;
	}

	public boolean wasLastCharWasDotOrComma()
	{
		return lastCharWasDotOrComma;
	}
}

class TagTerminator implements TokenTerminatorDeterminer
{
	@Override
	public boolean shouldTerminateToken(char ch)
	{
		return ch == '>';
	}
}

class WordTerminator implements TokenTerminatorDeterminer
{
	@Override
	public boolean shouldTerminateToken(char ch)
	{
		return ch == '<';
	}
}
