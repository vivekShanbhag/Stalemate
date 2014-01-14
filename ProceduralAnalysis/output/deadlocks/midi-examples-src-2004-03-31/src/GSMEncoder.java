/*
 *	GSMEncoder.java
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



// TODO: try a single conversion to 8kHz, 16 bit linear signed, mono


/**	<titleabbrev>GSMEncoder</titleabbrev>
	<title>Encoding an audio file to GSM 06.10</title>

	<formalpara><title>Purpose</title>
	<para>
	Encodes a PCM audio file, writes the result as a
	GSM 06.10 file.
	</para></formalpara>

	<formalpara>
	<title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java GSMEncoder</command>
	<arg choice="plain"><replaceable class="parameter">pcmfile</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">gsmfile</replaceable></arg>
	</cmdsynopsis>
	</para>
	</formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option><replaceable class="parameter">pcmfile</replaceable></option></term>
	<listitem><para>the name of the PCM input file.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option><replaceable class="parameter">gsmfile</replaceable></option></term>
	<listitem><para>the name of the GSM output file.</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>
	To work cleanly, this program requires JDK 1.5.0 or the latest version of Tritonus.
	The input file has to be 8 kHz, 16 bit linear signed, mono.
	GSM 06.10 can only be handled natively
	by Tritonus. If you want to use this format with the
	Sun jdk1.3/1.4, you have to install the respective plug-in
	from <ulink url
	="http://www.tritonus.org/plugins.html">Tritonus
	Plug-ins</ulink>.
	</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="GSMEncoder.java.html">GSMEncoder.java</ulink>
	</para>
	</formalpara>

*/
public class GSMEncoder
{
	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			printUsageAndExit();
		}
		File	pcmFile = new File(args[0]);
		File	gsmFile = new File(args[1]);
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

		/** We check if the input file has a format that can be converted
			to GSM.
		*/
		AudioFormat sourceFormat = ais.getFormat();
		if (! sourceFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) ||
			sourceFormat.getSampleRate() != 8000.0F ||
			sourceFormat.getSampleSizeInBits() != 16 ||
			sourceFormat.getChannels() != 1)
		{
			out("The format of the input data has to be PCM 8 kHz 16 bit mono");
			System.exit(1);
		}
		AudioFormat.Encoding	targetEncoding = new AudioFormat.Encoding("GSM0610");
		AudioInputStream	gsmAIS = AudioSystem.getAudioInputStream(targetEncoding, ais);
		AudioFileFormat.Type	fileType = new AudioFileFormat.Type("GSM", ".gsm");
		int	nWrittenFrames = 0;
		try
		{
			nWrittenFrames = AudioSystem.write(gsmAIS, fileType, gsmFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}



	private static void printUsageAndExit()
	{
			out("GSMEncoder: usage:");
			out("\tjava GSMEncoder <pcmfile> <gsmfile>");
			System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** GSMEncoder.java ***/

