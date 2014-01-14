/*
 *	AudioFileInfo.java
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

import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;



/**	<titleabbrev>AudioFileInfo</titleabbrev>
	<title>Getting information about an audio file</title>

	<formalpara><title>Purpose</title>
	<para>Displays general information about an audio file: file type,
	format of audio data, length of audio data, total length of the
	file.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java AudioFileInfo</command>
	<group>
	<arg><option>-f</option></arg>
	<arg><option>-u</option></arg>
	<arg><option>-s</option></arg>
	</group>
	<arg><option>-i</option></arg>
	<arg><replaceable class="parameter">audiofile</replaceable></arg>
	</cmdsynopsis>
	</para>
	</formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option>-s</option></term>
	<listitem><para>use standard input as source for the audio file. If this option is given, <replaceable class="parameter">audiofile</replaceable> is not required.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-f</option></term>
	<listitem><para>interpret <replaceable class="parameter">audiofile</replaceable> as filename. If this option is given, <replaceable class="parameter">audiofile</replaceable> is required.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-u</option></term>
	<listitem><para>interpret <replaceable class="parameter">audiofile</replaceable> as URL. If this option is given, <replaceable class="parameter">audiofile</replaceable> is required.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-i</option></term>
	<listitem><para>display additional information</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">audiofile</replaceable></term>
	<listitem><para>the file name  or URL of the audio
	file that information should be displayed for. This is required if
	<option>-s</option> is not given.</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>
	Some combination of options do not work. Both Sun's
	implementation and <ulink url="http://www.tritonus.org/">Tritonus</ulink> show some
	information only with option <option>-i</option>.
	</para></formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="AudioFileInfo.java.html">AudioFileInfo.java</ulink>
	</para></formalpara>
	  
*/
public class AudioFileInfo
{
	private static final int	LOAD_METHOD_STREAM = 1;
	private static final int	LOAD_METHOD_FILE = 2;
	private static final int	LOAD_METHOD_URL = 3;



	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			printUsageAndExit();
		}
		int	nLoadMethod = LOAD_METHOD_FILE;
		boolean	bCheckAudioInputStream = false;
		int	nCurrentArg = 0;
		while (nCurrentArg < args.length)
		{
			if (args[nCurrentArg].equals("-h"))
			{
				printUsageAndExit();
			}
			else if (args[nCurrentArg].equals("-s"))
			{
				nLoadMethod = LOAD_METHOD_STREAM;
			}
			else if (args[nCurrentArg].equals("-f"))
			{
				nLoadMethod = LOAD_METHOD_FILE;
			}
			else if (args[nCurrentArg].equals("-u"))
			{
				nLoadMethod = LOAD_METHOD_URL;
			}
			else if (args[nCurrentArg].equals("-i"))
			{
				bCheckAudioInputStream = true;
			}

			nCurrentArg++;
		}
		String	strSource = args[nCurrentArg - 1];
		String	strFilename = null;
		AudioFileFormat	aff = null;
		AudioInputStream ais = null;
		try
		{
			switch (nLoadMethod)
			{
			case LOAD_METHOD_STREAM:
				InputStream	inputStream = System.in;
				aff = AudioSystem.getAudioFileFormat(inputStream);
				strFilename = "<standard input>";
				if (bCheckAudioInputStream)
				{
					ais = AudioSystem.getAudioInputStream(inputStream);
				}
				break;

			case LOAD_METHOD_FILE:
				File	file = new File(strSource);
				aff = AudioSystem.getAudioFileFormat(file);
				strFilename = file.getCanonicalPath();
				if (bCheckAudioInputStream)
				{
					ais = AudioSystem.getAudioInputStream(file);
				}
				break;

			case LOAD_METHOD_URL:
				URL	url = new URL(strSource);
				aff = AudioSystem.getAudioFileFormat(url);
				strFilename = url.toString();
				if (bCheckAudioInputStream)
				{
					ais = AudioSystem.getAudioInputStream(url);
				}
				break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		if (aff == null)
		{
			out("Cannot determine format");
		}
		else
		{
			AudioFormat format = aff.getFormat();
			out("---------------------------------------------------------------------------");
			out("Source: " + strFilename);
			out("Type: " + aff.getType());
			out("AudioFormat: " + format);
			out("---------------------------------------------------------------------------");
			String	strAudioLength = null;
			if (aff.getFrameLength() != AudioSystem.NOT_SPECIFIED)
			{
				strAudioLength = "" + aff.getFrameLength() + " frames, " + aff.getFrameLength() * format.getFrameSize() + " bytes, " + (aff.getFrameLength() / format.getFrameRate()) + " seconds";
			}
			else
			{
				strAudioLength = "unknown";
			}
			out("Length of audio data: " + strAudioLength);
			String	strFileLength = null;
			if (aff.getByteLength() != AudioSystem.NOT_SPECIFIED)
			{
				strFileLength = "" + aff.getByteLength() + " bytes";
			}
			else
			{
				strFileLength = "unknown";
			}
			out("Total length of file (including headers): " + strFileLength);
			if (bCheckAudioInputStream)
			{
				strAudioLength = null;
				if (ais.getFrameLength() != AudioSystem.NOT_SPECIFIED)
				{
					strAudioLength = "" + ais.getFrameLength() + " frames (= " + ais.getFrameLength() * ais.getFormat().getFrameSize() + " bytes)";
				}
				else
				{
					strAudioLength = "unknown";
				}
				out("[AudioInputStream says:] Length of audio data: " + strAudioLength);
			}
			out("---------------------------------------------------------------------------");
		}
	}


	private static void printUsageAndExit()
	{
		out("AudioFileInfo: usage:");
		out("\tjava AudioFileInfo [-s|-f|-u] [-i] <audiofile>");
		System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** AudioFileInfo.java ***/
