/*
 *	CddaRipper.java
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



/**	<titleabbrev>CddaRipper</titleabbrev>
	<title>Ripping an audio CD</title>

	<formalpara><title>Purpose</title>
	<para>
	Reads data digitally from an audio CD and saves them as an audio file
	(output.wav).
	</para></formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java CddaRipper</command>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java CddaRipper</command>
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
	<para>The output filename and file type are hardcoded.
	</para>
	<para>It is reported that there are problems with mixed mode CDs.
	</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="CddaRipper.java.html">CddaRipper.java</ulink>
	</para>
	</formalpara>
	  
*/
public class CddaRipper
{
	static
	{
		URL.setURLStreamHandlerFactory(new CddaURLStreamHandlerFactory());
	}

	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static boolean	DEBUG = false;



	public static void main(String[] args)
	{
		/**	CDROM drive number.
			Defaults to first drive. [how is the order constituted?]
			Not used for now. Hardcoded default to /dev/cdrom.
		*/
		int		nDrive = 0;

		boolean		bTocOnly = true;
		int		nTrack = 0;
		File		outputFile = new File("output.wav");

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

			try
			{
				if (DEBUG) { out("before write()"); }
				int	nWritten = AudioSystem.write(
					audioInputStream,
					AudioFileFormat.Type.WAVE,
					outputFile);
				if (DEBUG) { out("after write()"); }
			}
			catch (IOException e)
			{
				e.printStackTrace();
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


/*** CddaRipper.java ****/
