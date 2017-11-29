package emsec.korea.exceptions;

public class DisconnectException extends Exception {
	
	private static final long serialVersionUID = -4389165213157664020L;

	public DisconnectException()
	{
		super("Disconected...");
	}
	
	public DisconnectException(String message)
	{
		super(message);
	}

}
