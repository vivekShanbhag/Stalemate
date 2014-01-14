/*
 *	MultiAudioStreamPlayer.java
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

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.SourceDataLine;

// import AudioStream;



/**	<titleabbrev>MultiAudioStreamPlayer</titleabbrev>
	<title>Playing multiple audio files concurrently</title>

	<formalpara><title>Purpose</title>
	<para>This program plays multiple audio files
	concurrently.
	It opens each file given on the command line and starts it.
	The program uses the class <classname>AudioStream</classname>.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java MultiAudioStreamPlayer</command>
	<arg choice="plain" rep="repeat"><replaceable>audiofile</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><replaceable>audiofile</replaceable></term>
	<listitem><para>the name(s) of the audio file(s) to play</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>Not well-tested</para></formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="MultiAudioStreamPlayer.java.html">MultiAudioStreamPlayer.java</ulink>,
	<ulink url="SimpleAudioStream.java.html">SimpleAudioStream.java</ulink>,
	<ulink url="BaseAudioStream.java.html">BaseAudioStream.java</ulink>
	</para>
	</formalpara>

*/
public class MultiAudioStreamPlayer
{
	public static void main(String[] args)
	{
		/*
		 *	We check that there is at least one command-line
		 *	argument. If not, we display the usage message and
		 *	exit.
		 */
		if (args.length < 1)
		{
			out("MultiAudioStreamPlayer: usage:");
			out("\tjava MultiAudioStreamPlayer <soundfile1> <soundfile2> ...");
			System.exit(1);
		}
		/*
		 *	Now, that we're shure there is at least one argument,
		 *	we take each argument as the filename of the soundfile
		 *	we want to play.
		 */
		for (int i = 0; i < args.length; i++)
		{
			String	strFilename = args[i];
			File	soundFile = new File(strFilename);

			/*
			 *	We just create an AudioStream object by
			 *	passing a File object for the soundfile to
			 *	the constructor. All hairy details are
			 *	handled inside of AudioStream.
			 */
			SimpleAudioStream	audioStream = null;
			try
			{
				audioStream = new SimpleAudioStream(soundFile);
			}
			catch (LineUnavailableException e)
			{
				/*
				 *	In case of an exception, we dump the
				 *	exception including the stack trace
				 *	to the console output. Then, we exit
				 *	the program.
				 */
				e.printStackTrace();
				System.exit(1);
			}
			catch (UnsupportedAudioFileException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}

			/*
			 *	We start the playback.
			 */
			audioStream.start();
		}
		/*
		 *	TODO: use some (yet to be defined) function in
		 *	AudioStream to wait until it is finished, then
		 *	exit the VM.
		 */
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** MultiAudioStreamPlayer.java ***/
