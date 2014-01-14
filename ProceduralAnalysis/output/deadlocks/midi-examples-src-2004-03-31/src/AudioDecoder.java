/*
 *	AudioDecoder.java
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

import java.io.IOException;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**	<titleabbrev>AudioDecoder</titleabbrev>
	<title>Decoding an encoded audio file</title>

	<formalpara><title>Purpose</title>
	<para>
	Decodes an encoded audio file, writes the result as a
	PCM file.
	</para></formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java AudioDecoder</command>
	<arg choice="plain"><replaceable class="parameter">encodedfile</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">pcmfile</replaceable></arg>
	</cmdsynopsis>
	</para>
	</formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option><replaceable class="parameter">encodedfile</replaceable></option></term>
	<listitem><para>the name of the encoded input file.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option><replaceable class="parameter">pcmfile</replaceable></option></term>
	<listitem><para>the name of the PCM output file.</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>The output file type and audio format can't be selected.
	</para>
	<para>
	Compressed formats can be handled depending on the
	capabilities of the Java Sound implementation it is run
	with.  A-law and &mu;-law can be handled in any known Java
	Sound implementation. Ogg vorbis, mp3 and GSM 06.10 can be handled
	by Tritonus. If you want to play these formats with the
	Sun jdk1.3/1.4, you have to install the respective plug-ins
	from <ulink url
	="http://www.tritonus.org/plugins.html">Tritonus
	Plug-ins</ulink>.
	</para></formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="AudioDecoder.java.html">AudioDecoder.java</ulink>
	</para>
	</formalpara>
	  
*/
public class AudioDecoder
{
	private static final boolean	DEBUG = false;



	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			printUsageAndExit();
		}
		File	encodedFile = new File(args[0]);
		File	pcmFile = new File(args[1]);
		AudioInputStream	ais = null;
		try
		{
			ais = AudioSystem.getAudioInputStream(encodedFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (ais == null)
		{
			out("cannot open input file");
			System.exit(1);
		}
		if (DEBUG) { out("AudioDecoder: ais: " + ais); }
		if (DEBUG) { out("AudioDecoder: ais AudioFormat: " + ais.getFormat()); }
		if (DEBUG) { out("AudioDecoder: ais length (frames): " + ais.getFrameLength()); }
		AudioFormat.Encoding	targetEncoding = AudioFormat.Encoding.PCM_SIGNED;
		AudioInputStream	pcmAIS = AudioSystem.getAudioInputStream(targetEncoding, ais);
		if (DEBUG) { out("AudioDecoder: pcmAIS: " + pcmAIS); }
		if (DEBUG) { out("AudioDecoder: pcmAIS AudioFormat: " + pcmAIS.getFormat()); }
		if (DEBUG) { out("AudioDecoder: pcmAIS length (frames): " + pcmAIS.getFrameLength()); }
		AudioFileFormat.Type	fileType = AudioFileFormat.Type.AU;
		int	nWrittenBytes = 0;
		try
		{
			nWrittenBytes = AudioSystem.write(pcmAIS, fileType, pcmFile);
			if (DEBUG) out("AudioDecoder: written (bytes): " + nWrittenBytes);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}



	private static void printUsageAndExit()
	{
			out("AudioDecoder: usage:");
			out("\tjava AudioDecoder <encodedfile> <pcmfile>");
			System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** AudioDecoder.java ***/

