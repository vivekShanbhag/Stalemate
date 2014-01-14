/*
 *	ProgramChangeMidi.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 2002 by Matthias Pfisterer
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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;



/**	<titleabbrev>ProgramChangeMidi</titleabbrev>

Shows how to do a program change using MIDI messages.
    Just a code fragment...
 */
public class ProgramChangeMidi
{
	public static void main(String[] args)
		throws MidiUnavailableException, InvalidMidiDataException
	{
		int nChannel = 0;		// MIDI channel
		int	nNoteNumber = 66;	// MIDI key number
		int	nVelocity = 100;	// MIDI note on velocity

		/*
		 *	Time between note on and note off event in
		 *	milliseconds. Note that on most systems, the
		 *	best resolution you can expect are 10 ms.
		 */
		int	nDuration = 2000;
		ShortMessage sm = new ShortMessage();

		int nBank = Integer.parseInt(args[0]);
		int nProgram = Integer.parseInt(args[1]);

		MidiDevice midiDevice = MidiSystem.getSynthesizer();
		out("Synthsizer: " + midiDevice);
		/* Don't forget to open the MidiDevice!
		   (One of the most common mistakes.)
		*/
		midiDevice.open();
		Receiver receiver = midiDevice.getReceiver();
		//out("Program before: " + channels[0].getProgram() + ", " + channels[0].getController(0) + ", " + channels[0].getController(20));
		sm.setMessage(ShortMessage.CONTROL_CHANGE, nChannel, 0, nBank);
		receiver.send(sm, -1);
		sm.setMessage(ShortMessage.PROGRAM_CHANGE, nChannel, nProgram, 0);
		receiver.send(sm, -1);
		//out("Program after: " + channels[0].getProgram() + ", " + channels[0].getController(0) + ", " + channels[0].getController(20));

		/*
		 *	Turn the note on on MIDI channel 1.
		 *	(Index zero means MIDI channel 1)
		 */
		sm.setMessage(ShortMessage.NOTE_ON, nChannel, nNoteNumber, nVelocity);
		receiver.send(sm, -1);

		/*
		 *	Wait for the specified amount of time
		 *	(the duration of the note).
		 */
		sleep(nDuration);

		/*
		 *	Turn the note off.
		 */
		sm.setMessage(ShortMessage.NOTE_OFF, nChannel, nNoteNumber, 0);
		receiver.send(sm, -1);

		/* Wait some time before exiting.
		 */
		sleep(200);

		receiver.close();
		midiDevice.close();
	}


	private static void sleep(int nDuration)
	{
		try
		{
			Thread.sleep(nDuration);
		}
		catch (InterruptedException e)
		{
		}
	}


	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}


/*** ProgramChangeMidi.java ***/
