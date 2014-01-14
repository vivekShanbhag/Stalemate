/*
 *	SynthNote.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2003 by Matthias Pfisterer
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

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;


/**	<titleabbrev>SynthNote</titleabbrev>
	<title>Playing a note on the synthesizer</title>

	<formalpara><title>Purpose</title>
	<para>Plays a single note on the synthesizer.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis><command>java SynthNote</command>
	<arg choice="plain"><replaceable class="parameter">keynumber</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">velocity</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">duration</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><replaceable class="parameter">keynumber</replaceable></term>
	<listitem><para>the MIDI key number</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">velocity</replaceable></term>
	<listitem><para>the velocity</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">duration</replaceable></term>
	<listitem><para>the duration in milliseconds</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>The precision of the duration depends on the precision
	of <function>Thread.sleep()</function>, which in turn depends on
	the precision of the system time and the latency of th
	thread scheduling of the Java VM. For many VMs, this
	means about 20 ms. When playing multiple notes, it is
	recommended to use a <classname>Sequence</classname> and the
	<classname>Sequencer</classname>, which is supposed to give better
	timing.</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="SynthNote.java.html">SynthNote.java</ulink>
	</para>
	</formalpara>

*/
public class SynthNote
{
	private static boolean	DEBUG = true;


	public static void main(String[] args)
	{
		/** The MIDI channel to use for playing the note. */
		int nChannelNumber = 0;
		int	nNoteNumber = 0;	// MIDI key number
		int	nVelocity = 0;

		/*
		 *	Time between note on and note off event in
		 *	milliseconds. Note that on most systems, the
		 *	best resolution you can expect are 10 ms.
		 */
		int	nDuration = 0;
		int nNoteNumberArgIndex = 0;
		switch (args.length)
		{
		case 4:
			nChannelNumber = Integer.parseInt(args[0]) - 1;
			nChannelNumber = Math.min(15, Math.max(0, nChannelNumber));
			nNoteNumberArgIndex = 1;
			// FALL THROUGH

		case 3:
			nNoteNumber = Integer.parseInt(args[nNoteNumberArgIndex]);
			nNoteNumber = Math.min(127, Math.max(0, nNoteNumber));
			nVelocity = Integer.parseInt(args[nNoteNumberArgIndex + 1]);
			nVelocity = Math.min(127, Math.max(0, nVelocity));
			nDuration = Integer.parseInt(args[nNoteNumberArgIndex + 2]);
			nDuration = Math.max(0, nDuration);
			break;

		default:
			printUsageAndExit();
		}

		/*
		 *	We need a synthesizer to play the note on.
		 *	Here, we simply request the default
		 *	synthesizer.
		 */
		Synthesizer	synth = null;
		try
		{
			synth = MidiSystem.getSynthesizer();
		}
		catch (MidiUnavailableException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		if (DEBUG) out("Synthesizer: " + synth);

		/*
		 *	Of course, we have to open the synthesizer to
		 *	produce any sound for us.
		 */
		try
		{
			synth.open();
		}
		catch (MidiUnavailableException e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		/*
		 *	Turn the note on on MIDI channel 1.
		 *	(Index zero means MIDI channel 1)
		 */
		MidiChannel[]	channels = synth.getChannels();
		MidiChannel	channel = channels[nChannelNumber];
		if (DEBUG) out("MidiChannel: " + channel);
		channel.noteOn(nNoteNumber, nVelocity);

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
		channel.noteOff(nNoteNumber);

		/* Close the synthesizer.
		 */
		synth.close();
	}


	private static void printUsageAndExit()
	{
		out("SynthNote: usage:");
		out("java SynthNote [<channel>] <note_number> <velocity> <duration>");
		System.exit(1);
	}


	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** SynthNote.java ***/
