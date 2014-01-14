/*
 *	LoadSoundbank.java
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

import java.io.File;
import java.io.IOException;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;


/**	<titleabbrev>LoadSoundbank</titleabbrev>
	<title>Using custom Soundbanks</title>

	<formalpara><title>Purpose</title>
	<para>Loads a custom soundbank and uses its instruments. One note is
	played to verify successful loading.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis><command>java LoadSoundbank</command>
	<arg choice="opt"><replaceable class="parameter">soundbank</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><replaceable class="parameter">soundbank</replaceable></term>
	<listitem><para>the filename of a custom soundbank to be loaded</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>Using a custom soundbank even if no default soundbank is
	available only works with JDK 1.5.0 and later.</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="LoadSoundbank.java.html">LoadSoundbank.java</ulink>
	</para>
	</formalpara>

*/
public class LoadSoundbank
{
    private static final boolean DEBUG = true;


	public static void main(String[] args)
	    throws MidiUnavailableException, InvalidMidiDataException,
	    IOException
	{
		int	nNoteNumber = 66;	// MIDI key number
		int	nVelocity = 100;	// MIDI note on velocity

		/*
		 *	Time between note on and note off event in
		 *	milliseconds. Note that on most systems, the
		 *	best resolution you can expect are 10 ms.
		 */
		int	nDuration = 2000;

		Soundbank soundbank = null;
		if (args.length == 1)
		{
			File file = new File(args[0]);
			soundbank = MidiSystem.getSoundbank(file);
			if (DEBUG) out("Soundbank: " + soundbank);
		}
		else if (args.length > 1)
		{
			printUsageAndExit();
		}


		/*
		 *	We need a synthesizer to play the note on.
		 *	Here, we simply request the default
		 *	synthesizer.
		 */
		Synthesizer	synth = null;
		synth = MidiSystem.getSynthesizer();
		if (DEBUG) out("Synthesizer: " + synth);

		/*
		 *	Of course, we have to open the synthesizer to
		 *	produce any sound for us.
		 */
		synth.open();
		if (DEBUG) out("Defaut soundbank: " + synth.getDefaultSoundbank());

		if (soundbank != null)
		{
			out("soundbank supported: " + synth.isSoundbankSupported(soundbank));
 			boolean bInstrumentsLoaded = synth.loadAllInstruments(soundbank);
 			if (DEBUG) out("Instruments loaded: " + bInstrumentsLoaded);
		}
		/*
		 *	Turn the note on on MIDI channel 1.
		 *	(Index zero means MIDI channel 1)
		 */
		MidiChannel[]	channels = synth.getChannels();
		channels[0].noteOn(nNoteNumber, nVelocity);

		/*
		 *	Wait for the specified amount of time
		 *	(the duration of the note).
		 */
		try
		{
			Thread.sleep(nDuration);
		}
		catch (InterruptedException e)
		{
		}

		/*
		 *	Turn the note off.
		 */
		channels[0].noteOff(nNoteNumber);
	}


    private static void printUsageAndExit()
    {
		out("LoadSoundbank: usage:");
		out("java LoadSoundbank [<soundbankfilename>]");
		System.exit(1);
    }


	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** LoadSoundbank.java ***/
