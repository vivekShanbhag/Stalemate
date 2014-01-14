/*
 *	RawAudioDataConverter.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 2003 by Matthias Pfisterer
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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/*	If the compilation fails because this class is not available,
	get gnu.getopt from the URL given in the comment below.
*/
import gnu.getopt.Getopt;


/**	<titleabbrev>RawAudioDataConverter</titleabbrev>
	<title>Converting raw data (headerless) files</title>

	<formalpara><title>Purpose</title>
	<para>Converts a file with raw
	audio data without header (input file) to an audio file
	(outputfile). Since the format of the input data cannot be derived
	from the input file, it has to be specified on the command
	line.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java RawAudioDataConverter</command>
	<arg choice="plain"><option>-h</option></arg>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java RawAudioDataConverter</command>
	<arg choice="plain"><option>-l</option></arg>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java RawAudioDataConverter</command>
	<arg>
	<group>
	<arg>-s</arg>
	<arg>-u</arg>
	</group>
	</arg>
	<arg choice="plain">
	<group>
	<arg>-L</arg>
	<arg>-B</arg>
	</group>
	</arg>
	<arg choice="req">-b <replaceable class="parameter">sample_size_in_bits</replaceable></arg>
	<arg choice="req">-c <replaceable class="parameter">channels</replaceable></arg>
	<arg choice="req">-r <replaceable class="parameter">sample_rate</replaceable></arg>
	<arg choice="req">-t <replaceable class="parameter">target_type</replaceable></arg>
	<arg choice="req"><replaceable class="parameter">input_file</replaceable></arg>
	<arg choice="req"><replaceable class="parameter">output_file</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option>-h</option></term>
	<listitem><para>output usage information, then exit</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-l</option></term>
	<listitem><para>lists the available target file types</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-u</option></term>
	<listitem><para>data is unsigned</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-s</option></term>
	<listitem><para>data is signed</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-L</option></term>
	<listitem><para>data is little-endian</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-B</option></term>
	<listitem><para>data is big-endian</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-b <replaceable class="parameter">sample_size_in_bits</replaceable></option></term>
	<listitem><para>the size of one (mono) sample in bits. This is typicall 8, 16, 24 or 32.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-c <replaceable class="parameter">channels</replaceable></option></term>
	<listitem><para>the number of channels</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-r <replaceable class="parameter">sample_rate</replaceable></option></term>
	<listitem><para>the sample rate of the input date</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-t <replaceable class="parameter">target_type</replaceable></option></term>
	<listitem><para>the extension of the target file type that should be converted to (e.g. <filename>wav</filename>). The available target types can be listed with the <option>-l</option> option.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">input_file</replaceable></term>
	<listitem><para>the file name of the raw data (headerless) file that should be read.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">output_file</replaceable></term>
	<listitem><para>the file name of the audio file that the audio data should be written to.</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>

	<para>This program can only process PCM data. Trying to store
	big-endian data in a <filename>wav</filename> file with the JDK
	1.4.2 results in a file with only the header. Presumably, this is
	a bug in the JDK.</para>
	</formalpara>

	<formalpara><title>Source code</title>

	<para>
	<ulink url="RawAudioDataConverter.java.html">RawAudioDataConverter.java</ulink>,
	<ulink url="AudioCommon.java.html">AudioCommon.java</ulink>,
	<olink targetdocent="getopt">gnu.getopt.Getopt</olink>
	</para>
	</formalpara>

*/
public class RawAudioDataConverter
{
	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static boolean	DEBUG = false;

	private static final int UNKNOWN = -1;



	public static void main(String[] args)
	{
		boolean bSigned = false;
		boolean bSignednessSpecified = false;
		boolean bBigEndian = false;
		boolean bEndianessSpecified = false;
		int nSampleSizeInBits = UNKNOWN;
		int nChannels = UNKNOWN;
		float fSampleRate = UNKNOWN;
		AudioFileFormat.Type targetFileType = AudioFileFormat.Type.WAVE;

		/*
		 *	Parsing of command-line options takes place...
		 */
		Getopt	g = new Getopt("RawAudioDataConverter", args, "hlusBLb:c:r:t:D");
		int	c;
		while ((c = g.getopt()) != -1)
		{
			switch (c)
			{
			case 'h':
				printUsageAndExit();

			case 'l':
				AudioCommon.listSupportedTargetTypes();
				System.exit(0);

			case 'u': // unsigned
				bSigned = false;
				bSignednessSpecified = true;
				break;

			case 's': // signed
				bSigned = true;
				bSignednessSpecified = true;
				break;

			case 'B': // big endian
				bBigEndian = true;
				bEndianessSpecified = true;
				break;

			case 'L': // little endian
				bBigEndian = false;
				bEndianessSpecified = true;
				break;

			case 'b': // bits per sample
				nSampleSizeInBits = Integer.parseInt(g.getOptarg());
				break;

			case 'c': // channels
				nChannels = Integer.parseInt(g.getOptarg());
				break;

			case 'r': // (sample) rate
				fSampleRate = Float.parseFloat(g.getOptarg());
				break;

			case 't': // target type
				targetFileType = AudioCommon.findTargetType(g.getOptarg());
				if (targetFileType == null)
				{
					out("Unknown target file type. Check with 'RawAudioDataConverter -l'.");
					System.exit(1);
				}
				break;

			case 'D':
				DEBUG = true;
				break;

			case '?':
				printUsageAndExit();

			default:
				out("getopt() returned " + c);
				break;
			}
		}

		/* Make sure all AudioFormat params are given. */
		if (! bSignednessSpecified)
		{
			out("signedness parameter (-u or -s) missing");
			printUsageAndExit();
		}
		if (! bEndianessSpecified && nSampleSizeInBits > 8)
		{
			out("endianess parameter (-L or -B) missing");
			printUsageAndExit();
		}
		if (nSampleSizeInBits == UNKNOWN)
		{
			out("sample size parameter (-b) missing");
			printUsageAndExit();
		}
		if (nChannels == UNKNOWN)
		{
			out("channels parameter (-c) missing");
			printUsageAndExit();
		}
		if (fSampleRate == UNKNOWN)
		{
			out("sample rate parameter (-r) missing");
			printUsageAndExit();
		}

		/* We make shure that there are only two more arguments, which
		   we take as the input and output filenames. */
		String	strInputFilename = null;
		String	strOutputFilename = null;
		if (args.length - g.getOptind() < 2)
		{
			printUsageAndExit();
		}
		strInputFilename = args[g.getOptind()];
		strOutputFilename = args[g.getOptind() + 1];

		File inputFile = new File(strInputFilename);
		File outputFile = new File(strOutputFilename);

		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(inputFile);
		}
		catch (FileNotFoundException e)
		{
			out("cannot load input file " + strInputFilename);
			System.exit(1);
		}
		inputStream = new BufferedInputStream(inputStream);

		AudioFormat format = new AudioFormat(fSampleRate,
											 nSampleSizeInBits,
											 nChannels,
											 bSigned,
											 bBigEndian);
		if (DEBUG) out("AudioFormat: " + format);
		long lLengthInFrames = inputFile.length() / format.getFrameSize();
		AudioInputStream ais = new AudioInputStream(inputStream,
													format,
													lLengthInFrames);

		int	nWrittenBytes = 0;
		try
		{
			nWrittenBytes = AudioSystem.write(ais, targetFileType, outputFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		if (DEBUG) out("Written bytes: " + nWrittenBytes);
	}



	private static void printUsageAndExit()
	{
		out("RawAudioDataConverter: usage:");
		out("\tjava RawAudioDataConverter -l");
		out("\tjava RawAudioDataConverter -u|-s -L|-B -b <bits per sample> -c <channels> -r <sample rate> -t <targettype> <inputfile> <outputfile>");
		System.exit(0);
	}


	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** RawAudioDataConverter.java ***/
