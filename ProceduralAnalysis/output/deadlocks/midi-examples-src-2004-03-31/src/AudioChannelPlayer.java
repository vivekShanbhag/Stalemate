/*
 *	AudioChannelPlayer.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 2000 by Matthias Pfisterer
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.BooleanControl;


/**	<titleabbrev>AudioChannelPlayer</titleabbrev>
	<title>Plays several soundfiles in sequence</title>
   
OLD DOCUMENTATION:
	<td bgcolor="orange">
	  <h2>AudioChannel -- playing sound files in a sequence</h2>

	  <p>
	    AudioChannel manages a queue of AudioInputStreams to send
	    them to the same line.  In the constructor of
	    AudioChannel, an AudioFormat has to be passed. A Line is
	    retrieved for this format. AudioInputStreams having a
	    different format are automatically converted during
	    playback.  This class is in an experimental state. Please
	    report problems.
	  </p>
	  <p>
	    AudioChannelPlayer is a command-line program that shows
	    how to use AudioChannel. It plays all sound files whose
	    names are given as command line arguments in sequence.
	  </p>
	  <p>
	    <strong>Source code:</strong> <a href="AudioChannel.java.html">AudioChannel.java</a>,
	    <strong>Source code:</strong> <a href="AudioChannelPlayer.java.html">AudioChannelPlayer.java</a>
	  </p>

	</td>

 */
public class AudioChannelPlayer
{
	private static final boolean	DEBUG = true;
	private static final int		BUFFER_SIZE = 16384;



	public static void main(String[] args)
	{
		// TODO: set AudioFormat after the first soundfile
		AudioFormat	audioFormat = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED,
			44100.0F, 16, 2, 4, 44100.0F, true);
		SourceDataLine		line = null;

		try
		{
			DataLine.Info	info = new DataLine.Info(SourceDataLine.class, audioFormat);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat, line.getBufferSize());
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}
		line.start();
		AudioChannel	channel = new AudioChannel(line);
		channel.start();
		for (int nArgPos = 0; nArgPos < args.length; nArgPos++)
		{
			if (args[nArgPos].startsWith("-s"))
			{
				String	strDuration = args[nArgPos].substring(2);
				int	nDuration = Integer.parseInt(strDuration);
				handleSilence(nDuration, channel);
			}
			else
			{
				handleFile(args[nArgPos], channel);
			}

		}
		// TODO: instead of waiting a fixed amount of time, wait until the queue of AudioChannel is empty.
		try
		{
			Thread.sleep(10000);
		}
		catch (InterruptedException e)
		{
		}
	}



	private static void handleFile(String strFilename, AudioChannel channel)
	{
		File	audioFile = new File(strFilename);
		AudioInputStream	audioInputStream = null;
		try
		{
			audioInputStream = AudioSystem.getAudioInputStream(audioFile);
		}
		catch (Exception e)
		{
			/*
			 *	In case of an exception, we dump the exception
			 *	including the stack trace to the console output.
			 *	Then, we exit the program.
			 */
			e.printStackTrace();
			System.exit(1);
		}
		if (audioInputStream != null)
		{
			boolean	bSuccessfull = channel.addAudioInputStream(audioInputStream);
			if (! bSuccessfull)
			{
				out("Warning: could not enqueue AudioInputStream; presumably formats don't match!");
			}
		}
	}



	private static void handleSilence(int nDuration, AudioChannel channel)
	{
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}


/*** AudioChannelPlayer.java ***/

