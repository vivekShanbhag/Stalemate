/*
 *	MidiLoop.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 2002 by Florian Bomers
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
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;


/**	<titleabbrev>MidiLoop</titleabbrev>
	<title>Receiving and sending MIDI (MIDI thru)</title>

	<formalpara><title>Purpose</title>
	<para>Outputs all MIDI events that arrive at a MIDI IN port
	to a MIDI OUT port.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis><command>java MidiLoop</command>
	<arg choice="plain"><replaceable class="parameter">-l</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">input device</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">output device</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><replaceable class="parameter">-l</replaceable></term>
	<listitem><para>List available MIDI devices</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">input device</replaceable></term>
	<listitem><para>MIDI IN device name</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">output device</replaceable></term>
	<listitem><para>MIDI OUT device</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>None.</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="MidiLoop.java.html">MidiLoop.java</ulink>,
	<ulink url="MidiCommon.java.html">MidiCommon.java</ulink>
	</para>
	</formalpara>

*/
public class MidiLoop {

	public static void main(String[] args) {
		try {
			if (args.length > 0) {
				if (args[0].equals("-l")) {
					MidiCommon.listDevicesAndExit(true, true);
				}
			}

			if (args.length != 2) {
				printUsageAndExit();
			}
			String	inDeviceName = args[0];
			String	outDeviceName = args[1];

			out("getting input device '"+inDeviceName+"'...");
			MidiDevice.Info	info = MidiCommon.getMidiDeviceInfo(inDeviceName, false);
			if (info == null) {
				err("no input device found for name " + inDeviceName);
			}
			MidiDevice inputDevice = MidiSystem.getMidiDevice(info);
			out("opening input device '"+inDeviceName+"'...");
			inputDevice.open();
			try {
				out("getting output device '"+outDeviceName+"'...");
				info = MidiCommon.getMidiDeviceInfo(outDeviceName, true);
				if (info == null) {
					err("no output device found for name " + outDeviceName);
				}
				MidiDevice outputDevice = MidiSystem.getMidiDevice(info);
				out("opening output device '"+outDeviceName+"'...");
				outputDevice.open();
				try {
					out("connecting input with output...");
					inputDevice.getTransmitter().setReceiver(outputDevice.getReceiver());

					out("connected. Press ENTER to quit.");
					System.in.read();
				} finally {
					out("Closing output device...");
					outputDevice.close();
				}
			} finally {
				out("Closing input device...");
				inputDevice.close();
			}
		} catch (IOException ioe) {
			out(ioe);
		} catch (MidiUnavailableException mue) {
			out(mue);
		}
		System.exit(0);
	}



	private static void printUsageAndExit() {
		out("MidiLoop usage:");
		out("  java MidiLoop [-l] <input device name> <output device name>");
		out("    -l\tlist available devices and exit");
		out("    <input device name>\tinput to named device");
		out("    <output device name>\toutput to named device");
		System.exit(0);
	}



	private static void err(String strMessage) {
		out(strMessage);
		System.exit(1);
	}

	private static void out(String strMessage) {
		System.out.println(strMessage);
	}

	private static void out(Throwable t) {
		t.printStackTrace();
	}
}



/*** MidiLoop.java ***/
