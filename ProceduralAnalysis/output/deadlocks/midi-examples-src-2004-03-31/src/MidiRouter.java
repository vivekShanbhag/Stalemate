/*
 *	MidiRouter.java
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;


/**	<titleabbrev>MidiRouter</titleabbrev>
	<title>OBSOLETE: Plays a single MIDI or RMF file</title>

	<formalpara><title>Purpose</title>
	<para>Plays a single MIDI or RMF file, then exits.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para><synopsis>java MidiRouter [-s] [-m] [-d] &lt;midifile&gt;</synopsis></para>
	</formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option>-s</option></term>
	<listitem><para>play on the default
	synthesizer</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-m</option></term>
	<listitem><para>play on the MIDI port</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-d</option></term>
	<listitem><para>dump on the console</para></listitem>

	All options may be used together.
	No option is equal to giving <option>-s</option>.

	</varlistentry>
	<varlistentry>
	<term><option>&lt;midifile&gt;</option></term>
	<listitem><para>the file
	name of the MIDI or RMF file that should be
	played</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>For Sun's implementation of Java Sound, playing to
	the MIDI port and dumping to the console do not
	work. For Tritonus, playing RMF files does not
	work.</para></formalpara>

*/
public class MidiRouter
{
	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static boolean		DEBUG = true;
	// private static Sequencer	sm_sequencer = null;



	public static void main(String[] args)
	{
		/*
		 *	We check if there is no command-line argument at all
		 *	or the first one is '-h'.
		 *	If so, we display the usage message and
		 *	exit.
		 */
/*
		if (args.length < 1 || args[0].equals("-h"))
		{
			printUsageAndExit();
		}
*/
		boolean	bUseSynthesizer = false;
		boolean	bUseMidiPort = false;
		boolean bUseConsoleDump = false;

		int	nArgumentIndex;
		for (nArgumentIndex = 0; nArgumentIndex < args.length; nArgumentIndex++)
		{
			String	strArgument = args[nArgumentIndex];
			if (strArgument.equals("-s"))
			{
				bUseSynthesizer = true;
			}
			else if (strArgument.equals("-m"))
			{
				bUseMidiPort = true;
			}
			else if (strArgument.equals("-d"))
			{
				bUseConsoleDump = true;
			}
			else
			{
				break;
			}
		}

		/*
		 *	If no destination option is choosen at all,
		 *	we default to playing on the internal synthesizer.
		 */
		if (!(bUseSynthesizer | bUseMidiPort | bUseConsoleDump))
		{
			bUseSynthesizer = true;
		}


		/*
		 *	Now, we set up the destinations the Sequence should be
		 *	played on.
		 */
		if (bUseSynthesizer)
		{
			/*
			 *	We try to get the default synthesizer, open()
			 *	it and chain it to the sequencer with a
			 *	Transmitter-Receiver pair.
			 */
			try
			{
				Synthesizer	synth = MidiSystem.getSynthesizer();
				synth.open();
				Receiver	synthReceiver = synth.getReceiver();
				Transmitter	seqTransmitter = MidiSystem.getTransmitter();
				seqTransmitter.setReceiver(synthReceiver);
			}
			catch (MidiUnavailableException e)
			{
				e.printStackTrace();
			}
		}

		if (bUseMidiPort)
		{
			/*
			 *	We try to get a Receiver which is already
			 *	associated with the default MIDI port.
			 *	It is then linked to a sequencer's
			 *	Transmitter.
			 */
			try
			{
				Receiver	midiReceiver = MidiSystem.getReceiver();
				Transmitter	midiTransmitter = MidiSystem.getTransmitter();
				midiTransmitter.setReceiver(midiReceiver);
			}
			catch (MidiUnavailableException e)
			{
				e.printStackTrace();
			}
		}

		if (bUseConsoleDump)
		{
			/*
			 *	We allocate a DumpReceiver object. Its job
			 *	is to print information on all received events
			 *	to the console.
			 *	It is then linked to a sequencer's
			 *	Transmitter.
			 */
			try
			{
				Receiver	dumpReceiver = new DumpReceiver(System.out);
				Transmitter	dumpTransmitter = MidiSystem.getTransmitter();
				dumpTransmitter.setReceiver(dumpReceiver);
			}
			catch (MidiUnavailableException e)
			{
				e.printStackTrace();
			}
		}


		out("If you are done with this programm, terminate it by pressing ctrl-C");

		/*
		 *	Now, we wait forever.
		 */
		while (true)
		{
			try
			{
				Thread.sleep(10000);
			}
			catch (InterruptedException e)
			{
			}
		}
	}



	private static void printUsageAndExit()
	{
			out("MidiRouter: usage:");
			out("\tjava MidiRouter [-s] [-m] [-d]");
			out("\t-s\troutes to the internal synthesizer");
			out("\t-m\troutes to the the MIDI port (MIDI through)");
			out("\t-d\tdump on the console");
			out("All options may be used together.");
			out("No option is equal to giving -s.");
			System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** MidiRouter.java ***/
