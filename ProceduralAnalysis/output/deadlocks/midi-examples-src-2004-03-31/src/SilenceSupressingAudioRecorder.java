/*
 *	SilenceSupressingAudioRecorder.java
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

import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.AudioFileFormat;

/*
 *      Tritonus classes.
 *      Using these makes the program not portable to other
 *      Java Sound implementations.
 */
import  org.tritonus.share.sampled.AudioSystemShadow;
import  org.tritonus.share.sampled.file.AudioOutputStream;
import  org.tritonus.share.sampled.file.TDataOutputStream;


/**	<titleabbrev>SilenceSupressingAudioRecorder</titleabbrev>
	<title>Recording to an audio file (simple version)</title>

	<formalpara><title>Purpose</title>
	<para>Records audio data and stores it in a file. The data is
	recorded in CD quality (44.1 kHz, 16 bit linear, stereo) and
	stored in a <filename>.wav</filename> file.</para></formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java SilenceSupressingAudioRecorder</command>
	<arg choice="plain"><option>-h</option></arg>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java SilenceSupressingAudioRecorder</command>
	<arg choice="plain"><replaceable>audiofile</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option>-h</option></term>
	<listitem><para>print usage information, then exit</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option><replaceable>audiofile</replaceable></option></term>
	<listitem><para>the file name of the
	audio file that should be produced from the recorded data</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>
	You cannot select audio formats and the audio file type
	on the command line. See
	AudioRecorder for a version that has more advanced options.
	Due to a bug in the Sun jdk1.3/1.4, this program does not work
	with it.
	</para></formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="SilenceSupressingAudioRecorder.java.html">SilenceSupressingAudioRecorder.java</ulink>
	</para>
	</formalpara>

*/
public class SilenceSupressingAudioRecorder
extends Thread
{
	private static boolean DEBUG = true;

	private TargetDataLine		m_line;
	private AudioOutputStream	m_audioOutputStream;


	public SilenceSupressingAudioRecorder(TargetDataLine line,
										  AudioFileFormat.Type targetType,
										  File file)
	{
		m_line = line;
		TDataOutputStream	dataOutputStream = null;
		try
		{
			// dataOutputStream = new TSeekableDataOutputStream(targetFile);
			dataOutputStream = AudioSystemShadow.getDataOutputStream(file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		m_audioOutputStream = AudioSystemShadow.getAudioOutputStream(
			targetType,
			m_line.getFormat(),
			AudioSystem.NOT_SPECIFIED,
			dataOutputStream);
	}



	/** Starts the recording.
	    To accomplish this, (i) the line is started and (ii) the
	    thread is started.
	*/
	public void start()
	{
		/* Starting the TargetDataLine. It tells the line that
		   we now want to read data from it. If this method
		   isn't called, we won't
		   be able to read data from the line at all.
		*/
		m_line.start();

		/* Starting the thread. This call results in the
		   method 'run()' (see below) being called. There, the
		   data is actually read from the line.
		*/
		super.start();
	}


	/** Stops the recording.

	Note that stopping the thread explicitely is not necessary. Once
	no more data can be read from the TargetDataLine, no more data
	be read from our AudioInputStream. And if there is no more
	data from the AudioInputStream, the method 'AudioSystem.write()'
	(called in 'run()' returns. Returning from 'AudioSystem.write()'
	is followed by returning from 'run()', and thus, the thread
	is terminated automatically.

	It's not a good idea to call this method just 'stop()'
	because stop() is a (deprecated) method of the class 'Thread'.
	And we don't want to override this method.
	*/
	public void stopRecording()
	{
		m_line.stop();
		m_line.close();
	}




	/** Main working method.
	    You may be surprised that here, just 'AudioSystem.write()' is
	    called. But internally, it works like this: AudioSystem.write()
	    contains a loop that is trying to read from the passed
	    AudioInputStream. Since we have a special AudioInputStream
	    that gets its data from a TargetDataLine, reading from the
	    AudioInputStream leads to reading from the TargetDataLine. The
	    data read this way is then written to the passed File. Before
	    writing of audio data starts, a header is written according
	    to the desired audio file type. Reading continues untill no
	    more data can be read from the AudioInputStream. In our case,
	    this happens if no more data can be read from the TargetDataLine.
	    This, in turn, happens if the TargetDataLine is stopped or closed
	    (which implies stopping). (Also see the comment above.) Then,
	    the file is closed and 'AudioSystem.write()' returns.
	*/
	public void run()
	{
		AudioFormat format = m_line.getFormat();
		int nFrameSize = format.getFrameSize();
		float fBlockLengthInSeconds = 0.01F;
		float fBufferLengthInSeconds = 0.5F;
		int nBlockCount = (int)(fBufferLengthInSeconds / fBlockLengthInSeconds);
		int nBlockLengthInFrames = (int) (format.getFrameRate() * fBlockLengthInSeconds);
		int nBlockLengthInBytes = nBlockLengthInFrames * nFrameSize;
		int nBufferSize = nBlockLengthInBytes * nBlockCount;
		byte[]	abData = new byte[nBufferSize];
		int nThreshold = 25;
		try
		{
			/*
			 */
			while (true)
			{
				int	nBytesRead = m_line.read(abData, 0, abData.length);
				if (nBytesRead > 0)
				{
					for (int nBlockBaseIndex = 0;
						 nBlockBaseIndex < nBytesRead;
						 nBlockBaseIndex += nBlockLengthInBytes)
					{
						/* The last block may be shorter than
						   nBlockLengthInBytes if read() does not return
						   a complete buffer.
						*/
						int nLengthInBytes = Math.min(nBlockLengthInBytes, nBytesRead - nBlockBaseIndex);
						int nLengthInFrames = nLengthInBytes / nFrameSize;
						int nSum = 0;
						for (int nFrameBaseIndex = nBlockBaseIndex;
							 nFrameBaseIndex < nBlockBaseIndex + nLengthInBytes;
							 nFrameBaseIndex += nFrameSize)
						{
							// 16 bit LE (mono) hardcoded:
							int nSample = (abData[nFrameBaseIndex] & 0xFF) | (abData[nFrameBaseIndex + 1] << 8);
							nSum += (nSample * nSample);
						}
						if (DEBUG) out("sum: " + nSum);
						int nAverage = (int) Math.sqrt(nSum / nLengthInFrames);
						if (DEBUG) out("average: " + nAverage);
						if (nAverage >= nThreshold)
						{
							if (DEBUG) out("writing block");
							m_audioOutputStream.write(abData, nBlockBaseIndex, nLengthInBytes);
						}
						else
						{
							if (DEBUG) out("discarding block");
						}
					}
				}
				else
				{
					break;
				}
			}

			/*
			 *	All data are transfered. We can close the shop.
			 *	Note that this is important to do backpatching of the
			 *	header, if possible.
			 */
			m_audioOutputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}



	public static void main(String[] args)
	{
		if (args.length != 1 || args[0].equals("-h"))
		{
			printUsageAndExit();
		}

		/* We have made shure that there is only one command line
		   argument. This is taken as the filename of the soundfile
		   to store to.
		*/
		String	strFilename = args[0];
		File	outputFile = new File(strFilename);

		/* For simplicity, the audio data format used for recording
		   is hardcoded here. We use PCM 44.1 kHz, 16 bit signed,
		   mono.
		*/
		AudioFormat	audioFormat = new AudioFormat(
			44100.0F, 16, 1, true, false);

		/* Now, we are trying to get a TargetDataLine. The
		   TargetDataLine is used later to read audio data from it.
		   If requesting the line was successful, we are opening
		   it (important!).
		*/
		DataLine.Info	info = new DataLine.Info(TargetDataLine.class, audioFormat);
		TargetDataLine	targetDataLine = null;
		try
		{
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			targetDataLine.open(audioFormat);
		}
		catch (LineUnavailableException e)
		{
			out("unable to get a recording line");
			e.printStackTrace();
			System.exit(1);
		}

		/* Again for simplicity, we've hardcoded the audio file
		   type, too.
		*/
		AudioFileFormat.Type	targetType = AudioFileFormat.Type.WAVE;

		/* Now, we are creating an SilenceSupressingAudioRecorder object. It
		   contains the logic of starting and stopping the
		   recording, reading audio data from the TargetDataLine
		   and writing the data to a file.
		*/
		SilenceSupressingAudioRecorder	recorder = new SilenceSupressingAudioRecorder(
			targetDataLine,
			targetType,
			outputFile);

		/* We are waiting for the user to press ENTER to
		   start the recording. (You might find it
		   inconvenient if recording starts immediately.)
		*/
		out("Press ENTER to start the recording.");
		try
		{
			System.in.read();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		/* Here, the recording is actually started.
		 */
		recorder.start();
		out("Recording...");

		/* And now, we are waiting again for the user to press ENTER,
		   this time to signal that the recording should be stopped.
		*/
		out("Press ENTER to stop the recording.");
		try
		{
			System.in.read();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		/* Here, the recording is actually stopped.
		 */
		recorder.stopRecording();
		out("Recording stopped.");
	}



	private static void printUsageAndExit()
	{
		out("SilenceSupressingAudioRecorder: usage:");
		out("\tjava SilenceSupressingAudioRecorder -h");
		out("\tjava SilenceSupressingAudioRecorder <audiofile>");
		System.exit(0);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** SilenceSupressingAudioRecorder.java ***/

