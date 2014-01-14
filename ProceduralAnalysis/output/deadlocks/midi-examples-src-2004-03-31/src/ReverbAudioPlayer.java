/*
 *	ReverbAudioPlayer.java
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

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.EnumControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.ReverbType;
import javax.sound.sampled.SourceDataLine;


/**	<titleabbrev>ReverbAudioPlayer</titleabbrev>
	<title>Playing an audio file with added reverb effect</title>

	<formalpara><title>Purpose</title> <para>Plays a single audio
	file. One of the available reverb types can be cosen. The chosen
	type is applied to the audio data before
	playback.</para></formalpara>

	<formalpara><title>Usage</title>
	<cmdsynopsis>
	<command>java ReverbAudioPlayer</command>
	<replaceable class="parameter">audiofile</replaceable>
	</cmdsynopsis>
	</formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option><replaceable class="parameter">audiofile</replaceable></option></term>
	<listitem><para>the name of the
	audio file that should be played</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>This example does not work with the JDK 1.5.0, as the
	"Java Sound Audio Engine" is no longer the default Mixer. In general,
	it is recommended to write your reverberation algorithm yourself.
	</para>
	</formalpara>
		
	<formalpara><title>Source code</title>
	<para>
	<ulink url="ReverbAudioPlayer.java.html">ReverbAudioPlayer.java</ulink>
	</para>
	</formalpara>

*/
public class ReverbAudioPlayer
{
	private static boolean DEBUG = true;

	private static final int	EXTERNAL_BUFFER_SIZE = 128000;



	public static void main(String[] args)
		throws Exception
	{
		/*
		  We check that there is exactely one command-line
		  argument.
		  If not, we display the usage message and exit.
		*/
		if (args.length == 1)
		{
			if (args[0].equals("-l"))
			{
				printAvailableReverbTypesAndExit();
			}
			else
			{
				printUsageAndExit();
			}
		}
		else if (args.length != 2)
		{
			printUsageAndExit();
		}

		/*
		  Now, that we're shure there are two arguments, we
		  take them as the reverb type name and the filename of the
		  soundfile we want to play.
		*/
		String strReverbTypeName = args[0];
		File soundFile = new File(args[1]);
	
		/*
		  We have to read in the sound file.
		*/
		AudioInputStream	audioInputStream = null;
		try
		{
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		}
		catch (Exception e)
		{
			/*
			  In case of an exception, we dump the exception
			  including the stack trace to the console output.
			  Then, we exit the program.
			*/
			e.printStackTrace();
			System.exit(1);
		}

		/*
		  From the AudioInputStream, i.e. from the sound file,
		  we fetch information about the format of the
		  audio data.
		  These information include the sampling frequency,
		  the number of
		  channels and the size of the samples.
		  These information
		  are needed to ask Java Sound for a suitable output line
		  for this audio file.
		*/
		AudioFormat	audioFormat = audioInputStream.getFormat();

		/*
		  Asking for a line is a rather tricky thing.
		  We have to construct an Info object that specifies
		  the desired properties for the line.
		  First, we have to say which kind of line we want. The
		  possibilities are: SourceDataLine (for playback), Clip
		  (for repeated playback)	and TargetDataLine (for
		  recording).
		  Here, we want to do normal playback, so we ask for
		  a SourceDataLine.
		  Then, we have to pass an AudioFormat object, so that
		  the Line knows which format the data passed to it
		  will have.
		  Furthermore, we can give Java Sound a hint about how
		  big the internal buffer for the line should be. This
		  isn't used here, signaling that we
		  don't care about the exact size. Java Sound will use
		  some default value for the buffer size.
		*/
		SourceDataLine	line = null;
		Line.Info info = new DataLine.Info(SourceDataLine.class,
										   audioFormat);
		Mixer mixer = getReverberatingMixer();
		try
		{
			line = (SourceDataLine) mixer.getLine(info);
			if (DEBUG) out("line: " + line);

			/*
			  The line is there, but it is not yet ready to
			  receive audio data. We have to open the line.
			*/
			line.open(audioFormat);
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		javax.sound.sampled.Control[] controls = line.getControls();
		for (int i = 0; i < controls.length; i++)
		{
			out(controls[i].toString());
		}
		setReverbType(args[0]);

		/*
		  Still not enough. The line now can receive data,
		  but will not pass them on to the audio output device
		  (which means to your sound card). This has to be
		  activated.
		*/
		line.start();

		/*
		  Ok, finally the line is prepared. Now comes the real
		  job: we have to write data to the line. We do this
		  in a loop. First, we read data from the
		  AudioInputStream to a buffer. Then, we write from
		  this buffer to the Line. This is done until the end
		  of the file is reached, which is detected by a
		  return value of -1 from the read method of the
		  AudioInputStream.
		*/
		int	nBytesRead = 0;
		byte[]	abData = new byte[EXTERNAL_BUFFER_SIZE];
		while (nBytesRead != -1)
		{
			try
			{
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (nBytesRead >= 0)
			{
				int	nBytesWritten = line.write(abData, 0, nBytesRead);
			}
		}

		/*
		  Wait until all data are played.
		  This is only necessary because of the bug noted below.
		  (If we do not wait, we would interrupt the playback by
		  prematurely closing the line and exiting the VM.)
		 
		  Thanks to Margie Fitch for bringing me on the right
		  path to this solution.
		*/
		line.drain();

		/*
		  All data are played. We can close the shop.
		*/
		line.close();
	}


	private static void printAvailableReverbTypesAndExit()
		throws LineUnavailableException
	{
		Object[] aTypes = getReverbTypes();
		if (aTypes == null)
		{
			out("Reverb is not supported!");
		}
		else
		{
			out("Supported Reverb Types:");
			for (int i = 0; i < aTypes.length; i++)
			{
				ReverbType type = (ReverbType) aTypes[i];
				// with 1.5.0:
				//out(type.getName());
				out(type.toString());
			}
		}
		System.exit(1);
	}


	private static void setReverbType(String strReverbTypeName)
		throws LineUnavailableException
	{
		ReverbType desiredType = null;
		Object[] aTypes = getReverbTypes();
		if (aTypes != null)
		{
			for (int i = 0; i < aTypes.length; i++)
			{
				ReverbType type = (ReverbType) aTypes[i];
				// with 1.5.0:
				// if (type.getName().equals(strReverbTypeName))
				if (type.toString().startsWith(strReverbTypeName))
				{
					desiredType = type;
				}
			}
			if (DEBUG) out("desiredType: " + desiredType);
			Mixer mixer = getReverberatingMixer();
			mixer.open();
			if (desiredType != null && mixer != null)
			{
				EnumControl reverbControl = (EnumControl)
					mixer.getControl(EnumControl.Type.REVERB);
				reverbControl.setValue(desiredType);
				out("active reverb type:" + reverbControl.getValue());
			}
		}
	}


	private static Object[] getReverbTypes()
		throws LineUnavailableException
	{
		Mixer mixer = getReverberatingMixer();
		if (DEBUG) out("Mixer: " + mixer);
		if (mixer == null ||
			! mixer.isControlSupported(EnumControl.Type.REVERB))
		{
			return null;
		}
		else
		{
			EnumControl reverbControl = (EnumControl)
				mixer.getControl(EnumControl.Type.REVERB);
			return reverbControl.getValues();
		}
	}


	/** Obtain a Mixer instance that supports reverberating.
		@return an appropriate Mixer instance or null if no Mixer that
		supports reverberating is available
	*/
	private static Mixer getReverberatingMixer()
		throws LineUnavailableException
	{
		Mixer.Info[] aInfos = AudioSystem.getMixerInfo();
		for (int i = 0; i < aInfos.length; i++)
		{
			Mixer mixer = AudioSystem.getMixer(aInfos[i]);
			if (mixer.isControlSupported(EnumControl.Type.REVERB))
			{
				if (DEBUG) out("mixer: " + mixer);
				return mixer;
			}
		}
		return null;
	}


	private static void printUsageAndExit()
	{
		out("ReverbAudioPlayer: usage:");
		out("\tjava ReverbAudioPlayer -l");
		out("\tjava ReverbAudioPlayer <reverb_type> <soundfile>");
		System.exit(1);
	}


	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** ReverbAudioPlayer.java ***/
