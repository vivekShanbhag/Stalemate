/*
 *	AudioConverter.java
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
import javax.sound.sampled.UnsupportedAudioFileException;

/*	If the compilation fails because this class is not available,
	get gnu.getopt from the URL given in the comment below.
*/
import gnu.getopt.Getopt;


/**	<titleabbrev>AudioConverter</titleabbrev>
	<title>Converting audio files to different encodings, sample size, channels, sample rate</title>

	<formalpara><title>Purpose</title>
	<para>Converts audio files, changing the sample rate of the
	audio data.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java AudioConverter</command>
	<arg choice="plain"><option>-h</option></arg>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java AudioConverter</command>
	<arg choice="plain"><option>-l</option></arg>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java AudioConverter</command>
	<arg><option>-c <replaceable class="parameter">channels</replaceable></option></arg>
	<arg><option>-s <replaceable class="parameter">sample_size</replaceable></option></arg>
	<arg><option>-e <replaceable class="parameter">encoding</replaceable></option></arg>
	<arg><option>-f <replaceable class="parameter">sample_rate</replaceable></option></arg>
	<arg><option>-t <replaceable class="parameter">file_type</replaceable></option></arg>
	<group choice="opt">
	<arg><option>-B</option></arg>
	<arg><option>-L</option></arg>
	</group>
	<arg><option>-D</option></arg>
	<arg choice="plain"><replaceable class="parameter">sourcefile</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">targetfile</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option>-h</option></term>
	<listitem><para>prints usage information</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-l</option></term>
	<listitem><para>lists available file types</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">channels</replaceable></term>
	<listitem><para>the number of channels to convert to, for instance 1 for mono or 2 for stereo</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">sample_size</replaceable></term>
	<listitem><para>the sample size in bits to convert to, for instance 8 for 8 bit samples</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">encoding</replaceable></term>
	<listitem><para>the encoding name to convert to, for instance PCM_SIGNED, ULAW or VORBIS</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">sample_rate</replaceable></term>
	<listitem><para>the sample rate to convert to, for instance 44100 or 8000. Fractional values are allowed, for instance 8192.76</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">file_type</replaceable></term>
	<listitem><para>the file type to write the target file, for instance WAVE or AU.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">sourcefile</replaceable></term>
	<listitem><para>the file name of the audio file that should be read to get the source audio data</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">targetfile</replaceable></term>
	<listitem><para>the file name of the audio file that the converted audio data should be written to</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>Sample rate conversion can only be handled natively
	by <ulink url="http://www.tritonus.org/">Tritonus</ulink>.
	If you want to do sample rate conversion with the
	Sun jdk1.3/1.4/1.5, you have to install Tritonus' sample rate converter.
	Converting number of channels and sample size is only partly supported
	by the Sun JDK. To get more options, install Tritonus's PCM2PCM converter.
	Both are part of the 'Tritonus miscellaneous' plug-in. See <ulink url
	="http://www.tritonus.org/plugins.html">Tritonus Plug-ins</ulink>.
	</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="AudioConverter.java.html">AudioConverter.java</ulink>,
	<olink targetdocent="getopt">gnu.getopt.Getopt</olink>
	</para>
	</formalpara>

*/
// IDEA: name for Florian's program: BatchAudioConverter
public class AudioConverter
{
	/** Threshold for float comparisions.
		If the difference between two floats is smaller than DELTA, they
		are considered equal.
	 */
	private static final float DELTA = 1E-9F;

	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static boolean		DEBUG = false;


	public static void main(String[] args)
		throws UnsupportedAudioFileException, IOException
	{
		/** The number of channels the audio data should be converted to.
			The value is initialized to -1. This is used to represent the
			condition that conversion of channels is not requested on the
			command line.
		*/
		int nDesiredChannels = AudioSystem.NOT_SPECIFIED;

		/** The sample size in bits the audio data should be converted to.
		*/
		int nDesiredSampleSizeInBits = AudioSystem.NOT_SPECIFIED;

		/** The encoding the audio data should be converted to.
		*/
		AudioFormat.Encoding desiredEncoding = null;

		/** The sample rate the audio data should be converted to.
		*/
		float fDesiredSampleRate = AudioSystem.NOT_SPECIFIED;

		/** The file type that should be used to write the audio data.
		*/
		AudioFileFormat.Type desiredFileType = null;

		/** The endianess the audio data should be converted to.
			This is only used if bIsEndianessDesired is true.
		*/
		boolean bDesiredBigEndian = false;

		/** Whether conversion of endianess is desired. This flag is
			necessary because the boolean variable bDesiredBigEndian
			has no 'unspecified' value to signal that endianess
			conversion is not desired.
		*/
		boolean bIsEndianessDesired = false;

		/*
		 * Parsing of command-line options takes place...
		 */
		Getopt	g = new Getopt("AudioConverter", args, "hlc:s:e:f:t:BLD");
		int	c;
		while ((c = g.getopt()) != -1)
		{
			switch (c)
			{
			case 'h':
				printUsageAndExit();

			case 'l':
				// TODO: listSupportedEncodings();
				AudioCommon.listSupportedTargetTypes();
				System.exit(0);

			case 'c':
				nDesiredChannels = Integer.parseInt(g.getOptarg());
				break;

			case 's':
				nDesiredSampleSizeInBits = Integer.parseInt(g.getOptarg());
				break;

			case 'e':
				String strEncodingName = g.getOptarg();
				desiredEncoding = new AudioFormat.Encoding(strEncodingName);
				break;

			case 'f':
				fDesiredSampleRate = Float.parseFloat(g.getOptarg());
				break;

			case 't':
				String	strExtension = g.getOptarg();
				desiredFileType = AudioCommon.findTargetType(strExtension);
				if (desiredFileType == null)
				{
					out("Unknown target file type. Check with 'AudioConverter -l'.");
					System.exit(1);
				}
				break;

			case 'B':
				bDesiredBigEndian = true;
				bIsEndianessDesired = true;
				break;

			case 'L':
				bDesiredBigEndian = true;
				bIsEndianessDesired = true;
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

		/* We make shure that there are only two more arguments, which
		   we take as the input and output filenames. */
		if (args.length - g.getOptind() < 2)
		{
			printUsageAndExit();
		}

		File inputFile = new File(args[g.getOptind()]);
		File outputFile = new File(args[g.getOptind() + 1]);

		/* As default for the audio file type we use the one of the
		   source file. So we first have to find out about the source
		   file's properties.
		*/
		AudioFileFormat		inputFileFormat = AudioSystem.getAudioFileFormat(inputFile);
		AudioFileFormat.Type	defaultFileType = inputFileFormat.getType();

		/* Here, we are reading the source file.
		 */
		AudioInputStream	stream = null;
		stream = AudioSystem.getAudioInputStream(inputFile);
		AudioFormat	format = stream.getFormat();
		if (DEBUG) out("source format: " + format);
		AudioFormat targetFormat = null;

		/* To ease our job later, we fill the values of the AudioFormat
		   properties not required with the values of the source format.
		 */
		if (desiredEncoding == null)
		{
			desiredEncoding = format.getEncoding();
		}
		if (fDesiredSampleRate == AudioSystem.NOT_SPECIFIED)
		{
			fDesiredSampleRate = format.getSampleRate();
		}
		if (nDesiredSampleSizeInBits == AudioSystem.NOT_SPECIFIED)
		{
			nDesiredSampleSizeInBits = format.getSampleSizeInBits();
		}
		if (nDesiredChannels == AudioSystem.NOT_SPECIFIED)
		{
			nDesiredChannels = format.getChannels();
		}
		if (! bIsEndianessDesired)
		{
			bDesiredBigEndian = format.isBigEndian();
		}

		/* Step 1: convert to PCM, if necessary.
		*/
		if (! AudioCommon.isPcm(format.getEncoding()))
		{
			if (DEBUG) out("converting to PCM...");
			/* The following is a heuristics: normally (but not always),
			   8 bit audio data are unsigned, while 16 bit data are signed.
			*/
			AudioFormat.Encoding targetEncoding =
				(format.getSampleSizeInBits() == 8) ?
				AudioFormat.Encoding.PCM_UNSIGNED :
				AudioFormat.Encoding.PCM_SIGNED;
			stream = convertEncoding(targetEncoding, stream);
			if (DEBUG) out("stream: " + stream);
			if (DEBUG) out("format: " + stream.getFormat());

			/* Here, we handle a special case: some compressed formats
			   do not state a sample size (but AudioSystem.NOT_SPECIFIED)
			   because its unknown how long
			   the samples are after decoding. If no sample size has been
			   requested with a command line option,
			   In this case, nDesiredSampleSizeInBits still has the value
			   AudioSystem.NOT_SPECIFIED despite the filling with default
			   values above.
			*/
			if (nDesiredSampleSizeInBits == AudioSystem.NOT_SPECIFIED)
			{
				nDesiredSampleSizeInBits = format.getSampleSizeInBits();
			}
		}

		/* Step 2: convert number of channels, if necessary.
		*/
		if (stream.getFormat().getChannels() != nDesiredChannels)
		{
			if (DEBUG) out("converting channels...");
			stream = convertChannels(nDesiredChannels, stream);
			if (DEBUG) out("stream: " + stream);
			if (DEBUG) out("format: " + stream.getFormat());
		}

		/* Step 3: convert sample size and endianess, if necessary.
		*/
		boolean bDoConvertSampleSize =
			(stream.getFormat().getSampleSizeInBits() != nDesiredSampleSizeInBits);
		boolean bDoConvertEndianess =
			(stream.getFormat().isBigEndian() != bDesiredBigEndian);
		if (bDoConvertSampleSize || bDoConvertEndianess)
		{
			if (DEBUG) out("converting sample size and endianess...");
			stream = convertSampleSizeAndEndianess(nDesiredSampleSizeInBits,
												   bDesiredBigEndian, stream);
			if (DEBUG) out("stream: " + stream);
			if (DEBUG) out("format: " + stream.getFormat());
		}

		/* Step 4: convert sample rate, if necessary.
		*/
		if (! equals(stream.getFormat().getSampleRate(), fDesiredSampleRate))
		{
			if (DEBUG) out("converting sample rate...");
			stream = convertSampleRate(fDesiredSampleRate, stream);
			if (DEBUG) out("stream: " + stream);
			if (DEBUG) out("format: " + stream.getFormat());
		}

		/* Step 5: convert to non-PCM encoding, if necessary.
		*/
		if (! stream.getFormat().getEncoding().equals(desiredEncoding))
		{
			if (DEBUG) out("converting to " + desiredEncoding + "...");
			stream = convertEncoding(desiredEncoding, stream);
			if (DEBUG) out("stream: " + stream);
			if (DEBUG) out("format: " + stream.getFormat());
		}

		/* Since we now know that we are dealing with PCM, we know
		   that the frame rate is the same as the sample rate.
		*/
// 		float		fTargetFrameRate = fTargetSampleRate;

// 		/* Here, we are constructing the desired format of the
// 		   audio data (as the result of the conversion should be).
// 		   We take over all values besides the sample/frame rate.
// 		*/

		/* And finally, we are trying to write the converted audio
		   data to a new file.
		*/
		int	nWrittenBytes = 0;
		AudioFileFormat.Type targetFileType = (desiredFileType != null) ?
			desiredFileType : defaultFileType;
		nWrittenBytes = AudioSystem.write(stream, targetFileType, outputFile);
		if (DEBUG) out("Written bytes: " + nWrittenBytes);
	}



	private static AudioInputStream convertEncoding(
		AudioFormat.Encoding targetEncoding,
		AudioInputStream sourceStream)
	{
		return AudioSystem.getAudioInputStream(targetEncoding,
											   sourceStream);
	}


	private static AudioInputStream convertChannels(
		int nChannels,
		AudioInputStream sourceStream)
	{
		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioFormat targetFormat = new AudioFormat(
			sourceFormat.getEncoding(),
			sourceFormat.getSampleRate(),
			sourceFormat.getSampleSizeInBits(),
			nChannels,
			calculateFrameSize(nChannels,
							   sourceFormat.getSampleSizeInBits()),
			sourceFormat.getFrameRate(),
			sourceFormat.isBigEndian());
		return AudioSystem.getAudioInputStream(targetFormat,
											   sourceStream);
	}


	private static AudioInputStream convertSampleSizeAndEndianess(
		int nSampleSizeInBits,
		boolean bBigEndian,
		AudioInputStream sourceStream)
	{
		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioFormat targetFormat = new AudioFormat(
			sourceFormat.getEncoding(),
			sourceFormat.getSampleRate(),
			nSampleSizeInBits,
			sourceFormat.getChannels(),
			calculateFrameSize(sourceFormat.getChannels(),
							   nSampleSizeInBits),
			sourceFormat.getFrameRate(),
			bBigEndian);
		return AudioSystem.getAudioInputStream(targetFormat,
											   sourceStream);
	}


	private static AudioInputStream convertSampleRate(
		float fSampleRate,
		AudioInputStream sourceStream)
	{
		AudioFormat sourceFormat = sourceStream.getFormat();
		AudioFormat targetFormat = new AudioFormat(
			sourceFormat.getEncoding(),
			fSampleRate,
			sourceFormat.getSampleSizeInBits(),
			sourceFormat.getChannels(),
			sourceFormat.getFrameSize(),
			fSampleRate,
			sourceFormat.isBigEndian());
		return AudioSystem.getAudioInputStream(targetFormat,
											   sourceStream);
	}


	private static int calculateFrameSize(int nChannels, int nSampleSizeInBits)
	{
		return ((nSampleSizeInBits + 7) / 8) * nChannels;
	}


	/** Compares two float values for equality.
	 */
	private static boolean equals(float f1, float f2)
	{
		return (Math.abs(f1 - f2) < DELTA);
	}



	private static void printUsageAndExit()
	{
		out("AudioConverter: usage:");
		out("\tjava AudioConverter -h");
		out("\tjava AudioConverter -l");
		out("\tjava AudioConverter");
		out("\t\t[-c <channels>]");
		out("\t\t[-s <sample_size_in_bits>]");
		out("\t\t[-e <encoding>]");
		out("\t\t[-f <sample_rate>]");
		out("\t\t[-t <file_type>]");
		out("\t\t[-B|-L] [-D]");
		out("\t\t<sourcefile> <targetfile>");
		System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** AudioConverter.java ***/

