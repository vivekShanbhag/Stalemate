/*
 *	AudioStreamPlayer.java
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

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;



/**	<titleabbrev>AudioStreamPlayer</titleabbrev>
	<title>AudioStream - encapsulating audio file playback</title>

	<formalpara><title>Purpose</title>
	<para>AudioStream hides the details of loading an audio file, requesting a Line and feeding the data to the line. It includes support for changing gain and pan. AudioStreamPlayer is a command-line application that shows how to use the basic features of AudioStream. This class is in an experimental state. Please report problems.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java AudioStreamPlayer</command>
	<arg choice="plain"><replaceable>audiofile</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><replaceable>audiofile</replaceable></term>
	<listitem><para>the name of the audio file to play</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>Not well-tested</para></formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="AudioStreamPlayer.java.html">AudioStreamPlayer.java</ulink>,
	<ulink url="SimpleAudioStream.java.html">SimpleAudioStream.java</ulink>,
	<ulink url="BaseAudioStream.java.html">BaseAudioStream.java</ulink>
	</para>
	</formalpara>

*/
public class AudioStreamPlayer
{
	public static void main(String[] args)
	{
		/*
		 *	We check that there is exactely one command-line
		 *	argument. If not, we display the usage message and
		 *	exit.
		 */
		if (args.length != 1)
		{
			printUsageAndExit();
		}
		/*
		 *	Now, that we're shure there is an argument, we take
		 *	it as the filename of the soundfile we want to play.
		 */
		String	strFilename = args[0];
		File	soundFile = new File(strFilename);

		/*
		 *	We just create a SimpleAudioStream by passing a
		 *	File object for the soundfile to the constructor.
		 *	All hairy details are handled inside of this class.
		 */
		SimpleAudioStream	audioStream = null;
		try
		{
			audioStream = new SimpleAudioStream(soundFile);
		}
		catch (LineUnavailableException e)
		{
			/*
			 *	In case of an exception, we dump the exception
			 *	including the stack trace to the console
			 *	output. Then, we exit the program.
			 */
			e.printStackTrace();
			System.exit(1);
		}
		catch (UnsupportedAudioFileException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		/*
		 *	We start the playback.
		 */
		audioStream.start();
		/*
		 *	TODO: use some (yet to be defined) function in
		 *	SimpleAudioStream to wait until it is finished, then
		 *	exit the VM.
		 */
	}



	private static void printUsageAndExit()
	{
		out("AudioStreamPlayer: usage:");
		out("\tjava AudioStreamPlayer <soundfile>");
		System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** AudioStreamPlayer.java ***/
