/*
 *	AudioFileSplit.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 by Matthias Pfisterer
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent;



/**	<titleabbrev>AudioFileSplit</titleabbrev>
	<title>Converting between audio file types</title>

	<formalpara><title>Purpose</title>
	<para>Converts audio files between different file types.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java AudioFileSplit</command>
	<arg choice="plain"><option>-l</option></arg>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java AudioFileSplit</command>
	<arg choice="plain"><option>-t <replaceable class="parameter">targettype</replaceable></option></arg>
	<arg choice="plain"><replaceable class="parameter">audiofile</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option>-l</option></term>
	<listitem><para>lists the available target file types</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-t <replaceable class="parameter">targettype</replaceable></option></term>
	<listitem><para>the extension of the target file type that should be converted to (e.g. <filename>.wav</filename>)</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">audiofile</replaceable></term>
	<listitem><para>the file name of the audio file that information should be displayed for. Note: The output filename is derived from the input filename by replacing the extension.</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>This should
	really be a program that also converts between different
	encodings and sampling rates.</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="AudioFileSplit.java.html">AudioFileSplit.java</ulink>
	</para>
	</formalpara>

*/
public class AudioFileSplit
{
	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static final boolean	DEBUG = false;



	public static void main(String[] args)
	{
		if (args.length == 1)
		{
			if (args[0].equals("-h"))
			{
				printUsageAndExit();
			}
			else if (args[0].equals("-l"))
			{
				AudioCommon.listSupportedTargetTypes();
				System.exit(0);
			}
			else
			{
				printUsageAndExit();
			}
		}
		else if (args.length == 3)
		{
			if (! args[0].equals("-t"))
			{
				printUsageAndExit();
			}
			String	strExtension = args[1];
			AudioFileFormat.Type	targetFileType = AudioCommon.findTargetType(strExtension);
			if (targetFileType == null)
			{
				out("Unknown target file type. Check with 'AudioFileSplit -l'.");
				System.exit(1);
			}
			String	strFilename = args[2];
			File	file = new File(strFilename);
			AudioInputStream	ais = null;
			try
			{
				ais = AudioSystem.getAudioInputStream(file);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (ais == null)
			{
				out("cannot open audio file");
				System.exit(1);
			}
			int	nDotPos = strFilename.lastIndexOf('.');
			String	strTargetFilename = null;
			if (nDotPos == -1)
			{
				strTargetFilename = strFilename + targetFileType.getExtension();
			}
			else
			{
				strTargetFilename = strFilename.substring(0, nDotPos) + targetFileType.getExtension();
			}
			if (DEBUG)
			{
				out("Target filename: " + strTargetFilename);
			}
			int	nWrittenBytes = 0;
			try
			{
				nWrittenBytes = AudioSystem.write(ais, targetFileType, new File(strTargetFilename));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (DEBUG)
			{
				out("Written bytes: " + nWrittenBytes);
			}
		}
		else	// args.length != 3
		{
			printUsageAndExit();
		}
	}



	private static void printUsageAndExit()
	{
		out("AudioFileSplit: usage:");
		out("\tjava AudioFileSplit -l");
		out("\tjava AudioFileSplit [-t <targettype>]<soundfile>");
		System.exit(0);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** AudioFileSplit.java ***/

