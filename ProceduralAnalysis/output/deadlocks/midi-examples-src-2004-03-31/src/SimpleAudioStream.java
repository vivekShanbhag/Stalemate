/*
 *	SimpleAudioStream.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999, 2000 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


import java.io.File;
import java.io.IOException;

import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.FloatControl;



public class SimpleAudioStream
	extends	BaseAudioStream
{
	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static boolean	DEBUG = true;

	// private static final int	EXTERNAL_BUFFER_SIZE = 16384;



	/**
	 *	This variable is used to distinguish stopped state from
	 *	paused state. In case of paused state, m_bRunning is still
	 *	true. In case of stopped state, it is set to false. Doing so
	 *	will terminate the thread.
	 */
	private boolean			m_bRunning;


	public SimpleAudioStream()
	{
		super();
		// m_dataSource = null;
	}



	public SimpleAudioStream(File file)
		throws	UnsupportedAudioFileException, LineUnavailableException, IOException
	{
		this();
		setDataSource(file);
		initLine();
	}



	public SimpleAudioStream(URL url)
		throws	UnsupportedAudioFileException, LineUnavailableException, IOException
	{
		this();
		setDataSource(url);
		initLine();
	}




	public AudioFormat getFormat()
	{
		// TODO: have to check that AudioInputStream (or Line?) is initialized
		return super.getFormat();
	}


/*
	public void start()
	{
		if (DEBUG)
		{
			out("start() called");
		}
		m_thread = new Thread(this);
		m_thread.start();
		if (DEBUG)
		{
			out("additional thread started");
		}
		m_line.start();
		if (DEBUG)
		{
			out("started line");
		}
	}



	public void stop()
	{
		m_line.stop();
		m_line.flush();
		m_bRunning = false;
	}



	public void pause()
	{
		m_line.stop();
	}



	public void resume()
	{
		m_line.start();
	}
*/


/*
	public void run()
	{
		if (DEBUG)
		{
			out("thread start");
		}
		int	nBytesRead = 0;
		m_bRunning = true;
		byte[]	abData = new byte[EXTERNAL_BUFFER_SIZE];
		int	nFrameSize = m_line.getFormat().getFrameSize();
		while (nBytesRead != -1 && m_bRunning)
		{
			try
			{
				nBytesRead = m_audioInputStream.read(abData, 0, abData.length);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (nBytesRead >= 0)
			{
				int	nRemainingBytes = nBytesRead;
				//	while (nRemainingBytes > 0)
				//{
					if (DEBUG)
					{
						out("Line status (active): " + m_line.isActive());
						out("Line status (running): " + m_line.isRunning());
						out("Trying to write (bytes): " + nBytesRead);
					}
					int	nBytesWritten = m_line.write(abData, 0, nBytesRead);
					if (DEBUG)
					{
						out("Written (bytes): " + nBytesWritten);
					}
					nRemainingBytes -= nBytesWritten;
				//}
			}
		}
		if (DEBUG)
		{
			out("after main loop");
		}
	}
*/

	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** SimpleAudioStream.java ***/

