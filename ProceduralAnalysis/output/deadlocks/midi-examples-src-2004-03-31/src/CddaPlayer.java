/*
 *	CddaPlayer.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 2001 by Matthias Pfisterer
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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

import org.tritonus.sampled.cdda.CddaURLStreamHandlerFactory;



/**	<titleabbrev>CddaPlayer</titleabbrev>
	<title>Playing an audio CD</title>

	<formalpara><title>Purpose</title>
	<para>
	Reads data digitally from an audio CD and plays them via Java Sound.
	</para></formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java CddaPlayer</command>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java CddaPlayer</command>
	<arg choice="plain"><replaceable class="parameter">tracknumber</replaceable></arg>
	</cmdsynopsis>
	</para>
	</formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option><replaceable class="parameter">tracknumber</replaceable></option></term>
	<listitem><para>the number of the track on the audio CD that should be played (starting with 1).</para></listitem>
	</varlistentry>
	</variablelist>
	<para>If no argument is given, the table of content is displayed.
	</para>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>The CDROM drive is hardcoded to '/dev/cdrom'.
	</para>
	<para>It is reported that there are problems with mixed mode CDs.
	</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="CddaPlayer.java.html">CddaPlayer.java</ulink>
	</para>
	</formalpara>
	  
*/
public class CddaPlayer
{
	static
	{
		URL.setURLStreamHandlerFactory(new CddaURLStreamHandlerFactory());
	}

	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static boolean	DEBUG = true;

	private static int	DEFAULT_EXTERNAL_BUFFER_SIZE = 128000;



	public static void main(String[] args)
	{
		int	nExternalBufferSize = DEFAULT_EXTERNAL_BUFFER_SIZE;
		int	nInternalBufferSize = AudioSystem.NOT_SPECIFIED;

		/**	CDROM drive number.
			Defaults to first drive. [how is the order constituted?]
			Not used for now. Hardcoded default to /dev/cdrom.
		*/
		int		nDrive = 0;

		boolean		bTocOnly = true;
		int		nTrack = 0;

		if (args.length < 1)
		{
			bTocOnly = true;
		}
		else if (args.length == 1)
		{
			nTrack = Integer.parseInt(args[0]);
			bTocOnly = false;
		}

		// TODO: should not be hardcoded
		String	strDrive = "/dev/cdrom";

		if (bTocOnly)
		{
			URL	tocURL = null;
			try
			{
				tocURL = new URL("cdda:" + strDrive);
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
			InputStream	tocInputStream = null;
			try
			{
				tocInputStream = tocURL.openStream();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			output(tocInputStream);
		}

 		if (! bTocOnly)
		{
			URL	trackURL = null;
			try
			{
				trackURL = new URL("cdda://" + strDrive + "#" + nTrack);
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
			InputStream	trackInputStream = null;
			try
			{
				trackInputStream = trackURL.openStream();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			AudioInputStream	audioInputStream = (AudioInputStream) trackInputStream;

 			SourceDataLine	line = null;
 			AudioFormat	audioFormat = audioInputStream.getFormat();
 			Line.Info	info = new DataLine.Info(SourceDataLine.class, audioFormat);

 			try
 			{
 				line = (SourceDataLine) AudioSystem.getLine(info);
 				line.open();
 				line.start();
 			}
 			catch (LineUnavailableException e)
 			{
 				e.printStackTrace();
 			}

			int	nBytesRead = 0;
			byte[]	abData = new byte[nExternalBufferSize];
			while (nBytesRead != -1)
			{
				try
				{
					nBytesRead = audioInputStream.read(abData, 0, abData.length);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				if (DEBUG)
				{
					out("AudioPlayer.main(): read from AudioInputStream (bytes): " + nBytesRead);
				}
				if (nBytesRead >= 0)
				{
					int	nBytesWritten = line.write(abData, 0, nBytesRead);
					if (DEBUG)
					{
						out("AudioPlayer.main(): written to SourceDataLine (bytes): " + nBytesWritten);
					}
				}
			}
		}
	}


	private static void output(InputStream inputStream)
	{
		byte[]	buffer = new byte[4096];
		OutputStream	outputStream = System.out;

		try
		{
			int	nBytesRead = 0;
			nBytesRead = inputStream.read(buffer);
			while (nBytesRead >= 0)
			{
				outputStream.write(buffer, 0, nBytesRead);
				nBytesRead = inputStream.read(buffer);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}


/*** CddaPlayer.java ****/
