/*
 *	RealTimeSequencerTest4.java
 *
 *	This file is derived from MidiPlayer.java, part of jsresources.org
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

/*	If the compilation fails because this class is not available,
	get gnu.getopt from the URL given in the comment below.
*/
import gnu.getopt.Getopt;

public class RealTimeSequencerTest4 {
	/**	Flag for debugging messages.
	 	If true, some messages are dumped to the console
	 	during operation.
	*/
	private static boolean		DEBUG = true;
	private static Sequencer	sm_sequencer = null;

	/**	List of opened MidiDevices.
		This stores references to all MidiDevices that we've
		opened except the sequencer.
		It is used to close them properly on exit.
	*/
	private static List	sm_openedMidiDeviceList;

	private static boolean	sm_bFinished = false;

	public static void before(String[] args) {
		//String[] args = new String [3];
		//args[0] = "-S";
		//args[1] = "Real Time Sequencer";
		//args[1] = "/home/vivek/Downloads/reimei_koi.mid";

		/*
		 *	Set when the sequence should be played on the default
		 *	internal synthesizer.
		 */
		boolean	bUseSynthesizer = false;

		/*
		 *	Set when the sequence should be played on the default
		 *	external MIDI port.
		 */
		boolean	bUseMidiPort = false;

		/*
		 *	Set when the sequence should be played on a MidiDevice
		 *	whose name is in strDeviceName. This can be any device,
		 *	including internal or external synthesizers, MIDI ports
		 *	or even sequencers.
		 */
		boolean bUseDevice = false;

		/*
		 *	Set when the sequence should be dumped in the console window
		 *	(or whereever the standard output is routed to). This gives
		 *	detailed information about each MIDI event.
		 */
		boolean bUseConsoleDump = false;

		/*
		 *	The device name to use when bUseDevice is set.
		 */
		String	strDeviceName = null;

		/*
		 *	The name of the sequencer to use. This is optional. If not
		 *	set, the default sequencer is used.
		 */
		String	strSequencerName = null;

		/*
		 *	Parsing of command-line options takes place...
		 */
		Getopt	g = new Getopt("MidiPlayer", args, "hlsmd:cS:D");
		int	c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'h': printUsageAndExit();
			case 'l': MidiCommon.listDevicesAndExit(false, true);
			case 's': bUseSynthesizer = true; break;
			case 'm': bUseMidiPort = true; break;
			case 'd': bUseDevice = true;
				strDeviceName = g.getOptarg();
				if (DEBUG) {
					out("MidiPlayer.main(): device name: " + strDeviceName);
				}
				break;
			case 'c': bUseConsoleDump = true; break;
			case 'S': strSequencerName = g.getOptarg();
				if (DEBUG) {
					out("MidiPlayer.main(): sequencer name: " + strSequencerName);
				}
				break;
			case 'D': DEBUG = true; break;
			case '?': printUsageAndExit();
			default: out("getopt() returned " + c); break;
			}
		}

		/*
		 *	If no destination option is choosen at all,
		 *	we default to playing on the internal synthesizer.
		 */
		if (!(bUseSynthesizer | bUseMidiPort | bUseDevice | bUseConsoleDump)) {
			if (DEBUG) out("using default synthesizer because no other option was given");
			bUseSynthesizer = true;
		}

		/*
		 *	We make shure that there is only one more argument, which
		 *	we take as the filename of the MIDI file we want to play.
		 */
		String	strFilename = null;
		for (int i = g.getOptind(); i < args.length; i++) {
			if (strFilename == null) {
				strFilename = args[i];
			} else {
				printUsageAndExit();
			}
		}
		if (strFilename == null) {
			printUsageAndExit();
		}
		File	midiFile = new File(strFilename);

		/*
		 * We create a Sequence object from the input file.  This is
		 * set later at the Sequencer as its sequence.
		 *
		 * We create an (File)InputStream and decorate it with a
		 * buffered stream. This is set later at the Sequencer as the
		 * source of a sequence.
		
		 * There is another programming technique: Creating an
		 * (File)InputStream and set this at the sequencer as the
		 * source of a sequence. While this technique seems somewhat
		 * unnatural, it in fact is more efficient on Sun's
		 * implementation of the Java Sound API up to version 1.4.2 of
		 * the JDK. Furthermore, it allows playback of RMF files.
		 *
		 * However, the technique used above should be considered the
		 * standard technique. It is especially appropriate if the JDK
		 * 1.5.0 or Tritonus is used.
		 */
		Sequence sequence = null;
		try {
			if (DEBUG) out("before MIDI file reading.");
			sequence = MidiSystem.getSequence(midiFile);
			if (DEBUG) out("MIDI file read.");
		} catch (InvalidMidiDataException e) {
			printExceptionAndExit(e);
		} catch (IOException e) {
			printExceptionAndExit(e);
		}

		/*
		 *	Now, we need a Sequencer to play the sequence.
		 *	In case we have passed a sequencer name on the command line,
		 *	we try to get that specific sequencer.
		 *	Otherwise, we simply request the default sequencer.
		 */
		try {
			if (strSequencerName != null) {
				MidiDevice.Info	seqInfo = MidiCommon.getMidiDeviceInfo(strSequencerName, true);
				if (seqInfo == null) {
					out("Cannot find device " + strSequencerName);
					System.exit(1);
				}
				sm_sequencer = (Sequencer) MidiSystem.getMidiDevice(seqInfo);
				if (DEBUG) out("Sequencer: " + sm_sequencer);
			} else {
				sm_sequencer = MidiSystem.getSequencer();
			}
		} catch (MidiUnavailableException e) {
			printExceptionAndExit(e);
		}
		if (sm_sequencer == null) {
			out("MidiPlayer.main(): can't get a Sequencer");
			System.exit(1);
		}

		/*
		 *	There is a bug in the Sun jdk1.3/1.4.
		 *	It prevents correct termination of the VM.
		 *	So we have to exit ourselves.
		 *	To accomplish this, we register a Listener to the Sequencer.
		 *	It is called when there are "meta" events. Meta event
		 *	47 is end of track.
		 *
		 *	Thanks to Espen Riskedal for finding this trick.
		 */
		sm_sequencer.addMetaEventListener(new MetaEventListener() {
			public void meta(MetaMessage event) {
				if (event.getType() == 47) {
					if (DEBUG) {
						out("MidiPlayer.<...>.meta(): end of track message received,"+
						" closing sequencer and attached MidiDevices...");
					}
					sm_sequencer.close();
					Iterator iterator = sm_openedMidiDeviceList.iterator();
					while (iterator.hasNext()) {
						MidiDevice	device = (MidiDevice) iterator.next();
						device.close();
					}
					if (DEBUG) { out("MidiPlayer.<...>.meta(): ...closed, now exiting"); }
					sm_bFinished = true;
				}
			}
		});

		/*
		 *	If we are in debug mode, we set additional listeners
		 *	to produce interesting (?) debugging output.
		 */
		if (DEBUG) {
			sm_sequencer.addMetaEventListener(
				new MetaEventListener() {
					public void meta(MetaMessage message) {
						out("%%% MetaMessage: " + message);
						out("%%% MetaMessage type: " + message.getType());
						out("%%% MetaMessage length: " + message.getLength());
					}
			});

			int[] anControllers = new int[128];
			for (int i = 0; i < anControllers.length; i++) {
				anControllers[i] = i;
			}
			sm_sequencer.addControllerEventListener(
				new ControllerEventListener() {
					public void controlChange(ShortMessage message) {
						out("%%% ShortMessage: " + message);
						out("%%% ShortMessage controller: " + message.getData1());
						out("%%% ShortMessage value: " + message.getData2());
					}
			}, anControllers);
		}

		/*
		 *	The Sequencer is still a dead object.
		 *	We have to open() it to become live.
		 *	This is necessary to allocate some ressources in
		 *	the native part.
		 */
		try {
			sm_sequencer.open();
		} catch (MidiUnavailableException e) {
			printExceptionAndExit(e);
		}
		if (DEBUG) out("Sequencer opened.");

		/*
		 *	Next step is to tell the Sequencer which
		 *	Sequence it has to play. In this case, we
		 *	set it as the InputStream created above.
		 */
		try {
			sm_sequencer.setSequence(sequence);
		} catch (InvalidMidiDataException e) {
			printExceptionAndExit(e);
		}
		if (DEBUG) out("Sequence set.");

		/*
		 *	Now, we set up the destinations the Sequence should be
		 *	played on.
		 */
		sm_openedMidiDeviceList = new ArrayList();
		if (bUseSynthesizer) {

			/* For the Sun implementation of Java Sound (up to 1.4.2),
			   the default Sequencer is also a
			   Synthesizer. So to play only on the default
			   Synthesizer, no further actions are
			   required. However, this is
			   implementation-specific behaviour. To write
			   portable programs, it is strongly recommanded
			   to follow the programming technique shown
			   below.
			*/
			if (sm_sequencer instanceof Synthesizer) {
				/* Sun implementation; no action required. */
			} else {

				/*
				 *	We try to get the default synthesizer, open()
				 *	it and chain it to the sequencer with a
				 *	Transmitter-Receiver pair.
				 */
				try {
					Synthesizer	synth = MidiSystem.getSynthesizer();
					synth.open();
					sm_openedMidiDeviceList.add(synth);
					Receiver	synthReceiver = synth.getReceiver();
					Transmitter	seqTransmitter = sm_sequencer.getTransmitter();
					seqTransmitter.setReceiver(synthReceiver);
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}
			}
		}

		if (bUseMidiPort) {
			/*
			 *	We try to get a Receiver which is already
			 *	associated with the default MIDI port.
			 *	It is then linked to a sequencer's
			 *	Transmitter.
			 */
			try {
				Receiver	midiReceiver = MidiSystem.getReceiver();
				Transmitter	midiTransmitter = sm_sequencer.getTransmitter();
				midiTransmitter.setReceiver(midiReceiver);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}

		if (bUseDevice) {
			/*	Here, we try to use a MidiDevice as destination
			 *	whose name was passed on the command line.
			 *	It is then linked to a sequencer's
			 *	Transmitter.
			 */
			MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
			MidiDevice.Info		info = MidiCommon.getMidiDeviceInfo(strDeviceName, true);
			if (info == null) {
				out("Cannot find device " + strDeviceName);
			}
			try {
				MidiDevice	midiDevice = MidiSystem.getMidiDevice(info);
				midiDevice.open();
				sm_openedMidiDeviceList.add(midiDevice);
				Receiver	midiReceiver = midiDevice.getReceiver();
				Transmitter	midiTransmitter = sm_sequencer.getTransmitter();
				midiTransmitter.setReceiver(midiReceiver);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}

		if (bUseConsoleDump) {
			/*
			 *	We allocate a DumpReceiver object. Its job
			 *	is to print information on all received events
			 *	to the console.
			 *	It is then linked to a sequencer's
			 *	Transmitter.
			 */
			try {
				Receiver	dumpReceiver = new DumpReceiver(System.out);
				Transmitter	dumpTransmitter = sm_sequencer.getTransmitter();
				dumpTransmitter.setReceiver(dumpReceiver);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		before (args);
		if (DEBUG) { out("MidiPlayer.main(): starting sequencer..."); }
		sm_sequencer.start();
		if (DEBUG) { out("MidiPlayer.main(): ...started"); }
		if (DEBUG) { out("MidiPlayer.main(): starting keeda..."); }
		new Thread () {
		  public void run () {
		    sm_sequencer.setMicrosecondPosition (500);
		  };
		}.start ();
		Thread.sleep(1000);
                sm_sequencer.setTrackMute (5, true);
		while (! sm_bFinished) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// IGNORE
			}
		}
	}

	private static void printUsageAndExit() {
		out("MidiPlayer: usage:");
		out("  java MidiPlayer -h");
		out("    gives help information");
		out("  java MidiPlayer -l");
		out("    lists available MIDI devices");
		out("  java MidiPlayer [-s] [-m] [-d <output device name>] [-c] [-S <sequencer name>] [-D] <midifile>");
		out("    -s\tplays on the default synthesizer");
		out("    -m\tplays on the MIDI port");
		out("    -d <output device name>\toutputs to named device (see '-l')");
		out("    -c\tdumps to the console");
		out("    -S <sequencer name>\tuses named sequencer (see '-l')");
		out("    -D\tenables debugging output");
		out("All options may be used together.");
		out("No option is equal to giving -s.");
		System.exit(1);
	}

	/** Handle Exception.
	 *	In case of an exception, we dump the exception
	 *	including the stack trace to the console
	 *	output. Then, we exit the program.
	 */
	private static void printExceptionAndExit(Exception e) {
		e.printStackTrace();
		System.exit(1);
	}

	private static void listDevicesAndExit(boolean forInput, boolean forOutput) {
		if (forInput && !forOutput) {
			out("Available MIDI IN Devices:");
		}
		else if (!forInput && forOutput) {
			out("Available MIDI OUT Devices:");
		} else {
			out("Available MIDI Devices:");
		}

		MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < aInfos.length; i++) {
			try {
				MidiDevice	device = MidiSystem.getMidiDevice(aInfos[i]);
				boolean		bAllowsInput = (device.getMaxTransmitters() != 0);
				boolean		bAllowsOutput = (device.getMaxReceivers() != 0);
				if ((bAllowsInput && forInput) || (bAllowsOutput && forOutput)) {
					out(""+i+"  "
					    +(bAllowsInput?"IN ":"   ")
					    +(bAllowsOutput?"OUT ":"    ")
					    +aInfos[i].getName()+", "
					    +aInfos[i].getVendor()+", "
					    +aInfos[i].getVersion()+", "
					    +aInfos[i].getDescription());
				}
			} catch (MidiUnavailableException e) {
				// device is obviously not available...
			}
		}
		if (aInfos.length == 0) {
			out("[No devices available]");
		}
		System.exit(0);
	}

	/*
	 *	This method tries to return a MidiDevice.Info whose name
	 *	matches the passed name. If no matching MidiDevice.Info is
	 *	found, null is returned.
	 *	If forOutput is true, then only output devices are searched,
	 *	otherwise only input devices.
	 */
	// TODO: check against MidiCommon
	private static MidiDevice.Info getMidiDeviceInfo(String strDeviceName, boolean forOutput) {
		MidiDevice.Info[]	aInfos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < aInfos.length; i++) {
			if (aInfos[i].getName().equals(strDeviceName)) {
				try {
					MidiDevice device = MidiSystem.getMidiDevice(aInfos[i]);
					boolean	bAllowsInput = (device.getMaxTransmitters() != 0);
					boolean	bAllowsOutput = (device.getMaxReceivers() != 0);
					if ((bAllowsOutput && forOutput) || (bAllowsInput && !forOutput)) {
						return aInfos[i];
					}
				} catch (MidiUnavailableException mue) {}
			}
		}
		return null;
	}

	private static void out(String strMessage) {
		System.out.println(strMessage);
	}
}
/*** RealTimeSequencerTest4.java ***/
