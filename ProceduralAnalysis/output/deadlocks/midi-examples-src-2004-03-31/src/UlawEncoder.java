/*
 *	UlawEncoder.java
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



/**	<titleabbrev>UlawEncoder</titleabbrev>
	<title>Encoding an audio file to &mu;-law</title>

	<formalpara><title>Purpose</title>
	<para>Encodes a PCM audio file, writes the result as an
	<filename>.au</filename>-file, &mu;-law encoded.
	</para></formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java UlawEncoder</command>
	<arg choice="plain"><replaceable class="parameter">pcmfile</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">ulawfile</replaceable></arg>
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
	<term><option><replaceable class="parameter">ulawfile</replaceable></option></term>
	<listitem><para>the name of the &mu;-law output file.</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>Does not work with the Sun jdk1.3 (see bug #4391108).
	According to Florian, this bug is fixed in jdk1.4.</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="UlawEncoder.java.html">UlawEncoder.java</ulink>
	</para>
	</formalpara>

*/
public class UlawEncoder
{
	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			printUsageAndExit();
		}
		File	pcmFile = new File(args[0]);
		File	ulawFile = new File(args[1]);
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
		AudioFormat.Encoding	targetEncoding = AudioFormat.Encoding.ULAW;
		AudioInputStream	ulawAudioInputStreamAIS = AudioSystem.getAudioInputStream(targetEncoding, ais);
		AudioFileFormat.Type	fileType = AudioFileFormat.Type.WAVE;
		int	nWrittenFrames = 0;
		try
		{
			nWrittenFrames = AudioSystem.write(ulawAudioInputStreamAIS, fileType, ulawFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}



	private static void printUsageAndExit()
	{
			out("UlawEncoder: usage:");
			out("\tjava UlawEncoder <pcmfile> <ulawfile>");
			System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** UlawEncoder.java ***/

