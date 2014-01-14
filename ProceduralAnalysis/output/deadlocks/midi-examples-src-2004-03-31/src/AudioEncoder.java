/*
 *	AudioEncoder.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2004 by Matthias Pfisterer
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

import java.io.IOException;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;



/**	<titleabbrev>AudioEncoder</titleabbrev>
	<title>Encoding an audio file</title>

	<formalpara><title>Purpose</title>
	<para>
	Encodes a PCM audio file, writes the result as an
	encoded audio file.
	</para></formalpara>

	<formalpara>
	<title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java AudioEncoder</command>
	<arg choice="plain"><replaceable class="parameter">pcm_file</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">encoded_file</replaceable></arg>
	</cmdsynopsis>
	</para>
	</formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option><replaceable class="parameter">pcm_file</replaceable></option></term>
	<listitem><para>the name of the PCM input file.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option><replaceable class="parameter">encoded_file</replaceable></option></term>
	<listitem><para>the name of the encoded output file.</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>
	To work cleanly, this program requires JDK 1.5.0 or the latest version of Tritonus.

	Several formats, e.g. Ogg vorbis and GSM, can only be handled natively
	by Tritonus. If you want to use this format with the
	Sun jdk1.3/1.4, you have to install the respective plug-in
	from <ulink url
	="http://www.tritonus.org/plugins.html">Tritonus
	Plug-ins</ulink>.
	</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="AudioEncoder.java.html">AudioEncoder.java</ulink>
	</para>
	</formalpara>

*/
public class AudioEncoder
{
	private static final boolean	DEBUG = true;


	public static void main(String[] args)
	{
		AudioFormat.Encoding	targetEncoding = null;
		AudioFileFormat.Type	fileType = null;
		int	nNextArgIndex = -1;
		if (args.length == 3)
		{
			targetEncoding = new AudioFormat.Encoding(args[0]);
			fileType = AudioFileFormat.Type.WAVE;
			nNextArgIndex = 1;
		}
		else if (args.length == 5)
		{
			targetEncoding = new AudioFormat.Encoding(args[0]);
			fileType = new AudioFileFormat.Type(args[1], args[2]);
			nNextArgIndex = 3;
		}
		else
		{
			printUsageAndExit();
		}
		File	pcmFile = new File(args[nNextArgIndex]);
		File	encodedFile = new File(args[nNextArgIndex + 1]);

		AudioInputStream	ais = null;
		try
		{
			ais = AudioSystem.getAudioInputStream(pcmFile);
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
		AudioInputStream	encodedAudioInputStream = AudioSystem.getAudioInputStream(targetEncoding, ais);
		if (DEBUG) { out("encoded stream: " + encodedAudioInputStream); }
		if (DEBUG) { out("encoded stream's format: " + encodedAudioInputStream.getFormat()); }
		int	nWrittenFrames = 0;
		try
		{
			nWrittenFrames = AudioSystem.write(encodedAudioInputStream, fileType, encodedFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}



	private static void printUsageAndExit()
	{
			out("AudioEncoder: usage:");
			out("\tjava AudioEncoder <encoding> [<filetype> <fileextension>] <pcm_file> <encoded_file>");
			System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** AudioEncoder.java ***/

