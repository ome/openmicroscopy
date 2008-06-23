package org.openmicroscopy.shoola.env.data;

public class DSAccessException
	extends Exception
{

	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message	Short explanation of the problem.
	 */
	public DSAccessException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * 
	 * @param message	Short explanation of the problem.
	 * @param cause		The exception that caused this one to be risen.
	 */
	public DSAccessException(String message, Throwable cause) 
	{
		super(message, cause);
	}

}