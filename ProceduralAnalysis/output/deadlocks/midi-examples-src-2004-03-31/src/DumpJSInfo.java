/*
 *	DumpJSInfo.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2002 by Matthias Pfisterer
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

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/*	If the compilation fails because this class is not available,
	get gnu.getopt from the URL given in the comment below.
*/
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;


// TODO: adapt doc
/**	<titleabbrev>DumpJSInfo</titleabbrev>
	<title>Displaying information about the Java Sound implementation/installation</title>

	<formalpara><title>Purpose</title>
	<para>
	This program can display information about available Mixers, Controls,
	MidiDevices and installed service providers.
	</para></formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java DumpJSInfo</command>
	<arg choice="plain"><option>-h, --help</option></arg>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java DumpJSInfo</command>
	<arg choice="plain"><option>-V, --version</option></arg>
	</cmdsynopsis>
	<cmdsynopsis>
	<command>java DumpJSInfo</command>
	<arg choice="opt"><option>-D, --debug</option></arg>
	<arg choice="opt"><option>--mixers</option></arg>
	<arg choice="opt"><option>--controls</option></arg>
	<arg choice="opt"><option>--mididevices</option></arg>
	<arg choice="opt"><option>--providers</option></arg>
	<arg choice="opt"><option>-a, --all</option></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option>-l</option></term>
	<listitem><para>lists the available mixers</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-M <replaceable>mixername</replaceable></option></term>
	<listitem><para>selects a mixer to play on</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-e <replaceable>buffersize</replaceable></option></term>
	<listitem><para>the buffer size to use in the application ("extern")</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-i <replaceable>buffersize</replaceable></option></term>
	<listitem><para>the buffer size to use in Java Sound ("intern")</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option><replaceable>audiofile</replaceable></option></term>
	<listitem><para>the file name of the audio file to play</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>
	<para>
	Not known.
	</para>
	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="DumpJSInfo.java.html">DumpJSInfo.java</ulink>,
	<olink targetdocent="getopt">gnu.getopt.Getopt</olink>
	</para>
	</formalpara>

*/
public class DumpJSInfo
{
	private static final String	BASE_NAME = "META-INF/services/";


	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static boolean	DEBUG = false;



	private static final String[]	INDENTATION_TABLE =
	{
		"",
		"    ",
		"        ",
		"            ",
		"                ",
		"                    ",
		"                        ",
		"                            ",
		"                                ",
	};


	// TODO: rename to CONFIGURATION_SOURCES
	public static final Object[]	sm_configurationSources = new Object[]
	{
		// for Sun jdk1.3/1.4
		new String[]{"sun.misc.Service", "providers"},
		// for Tritonus
		new String[]{"org.tritonus.core.Service", "providers"},
	};



	public static void main(String[] args)
		throws Exception
	{
		boolean	bDisplayMixers = false;
		boolean	bDisplayControls = false;
		boolean	bDisplayMidiDevices = false;
		boolean	bDisplayProviders = false;

		/*
		 *	Parsing of command-line options takes place...
		 */
		LongOpt[]	aLongOpts = new LongOpt[]{
			new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
			new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'D'),
			new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'V'),
			new LongOpt("mixers", LongOpt.NO_ARGUMENT, null, 1),
			new LongOpt("controls", LongOpt.NO_ARGUMENT, null, 2),
			new LongOpt("mididevices", LongOpt.NO_ARGUMENT, null, 3),
			new LongOpt("providers", LongOpt.NO_ARGUMENT, null, 4),
			new LongOpt("all", LongOpt.NO_ARGUMENT, null, 'a'),
		};
		Getopt	g = new Getopt("DumpJSInfo", args, "hDVa", aLongOpts);
		int	c;
		while ((c = g.getopt()) != -1)
		{
			switch (c)
			{
			case 'h':
				printUsageAndExit();

			case 'V':
				printVersionAndExit();

			case 'D':
				DEBUG = true;
				break;

			case 1:
				bDisplayMixers = true;
				break;

			case 2:
				bDisplayControls = true;
				break;

			case 3:
				bDisplayMidiDevices = true;
				break;

			case 4:
				bDisplayProviders = true;
				break;

			case 'a':
				bDisplayMixers = true;
				bDisplayControls = true;
				bDisplayMidiDevices = true;
				bDisplayProviders = true;
				break;

			case '?':
				printUsageAndExit();

			default:
				out("getopt() returned " + c);
				break;
			}
		}

		/* If no display option is selected, select all.
		 */
		if (! (bDisplayMixers | bDisplayControls |
		       bDisplayMidiDevices | bDisplayProviders))
		{
			bDisplayMixers = true;
			bDisplayControls = true;
			bDisplayMidiDevices = true;
			bDisplayProviders = true;
		}

		if (bDisplayMixers)
		{
			displayMixers();
		}
		if (bDisplayControls)
		{
			displayControls();
		}
		if (bDisplayMidiDevices)
		{
			displayMidiDevices();
		}
		if (bDisplayProviders)
		{
			displayServiceProviders();
		}

		System.exit(0);
	}



	private static void displayMixers()
		throws Exception
	{
	}



	private static void displayControls()
		throws Exception
	{
	}



	private static void displayMidiDevices()
		throws Exception
	{
	}



	private static void displayServiceProviders()
		throws Exception
	{
		final String[]	astrCategories =
			{
				"javax.sound.midi.spi.MidiDeviceProvider",
				"javax.sound.midi.spi.MidiFileReader",
				"javax.sound.midi.spi.MidiFileWriter",
				"javax.sound.midi.spi.SoundbankReader",
				"javax.sound.sampled.spi.AudioFileReader",
				"javax.sound.sampled.spi.AudioFileWriter",
				"javax.sound.sampled.spi.FormatConversionProvider",
				"javax.sound.sampled.spi.MixerProvider",
			};


		out(0, "Service Providers");
		for (int nProviderType = 0;
		     nProviderType < astrCategories.length;
		     nProviderType++)
		{
			String	strProviderTypeName = astrCategories[nProviderType];
			out(1, "");
			out(1, strProviderTypeName);
			out(2, "Available Providers");
			displayProviders(strProviderTypeName);
			out(2, "Configuration Files");
			displayConfigurationFiles(strProviderTypeName);
		}
	}



	private static void displayProviders(String strProviderTypeName)
	{
		Class		providerClass = null;
		Method		providersMethod = null;

		for (int i = 0; i < sm_configurationSources.length; i++)
		{
			String	strServiceClassName = ((String[]) sm_configurationSources[i])[0];
			String	strProvidersMethodName = ((String[]) sm_configurationSources[i])[1];
			try
			{
				Class	serviceClass = Class.forName(strServiceClassName);
				providersMethod = serviceClass.getMethod(strProvidersMethodName, new Class[]{Class.class});
				break;
			}
			catch (Exception e)
			{
				if (DEBUG) { out(e); }
			}
		}
		try
		{
			providerClass = Class.forName(strProviderTypeName);
		}
		catch (ClassNotFoundException e)
		{
			out(e);
		}
		Iterator	services = null;
		try
		{
			services = (Iterator) providersMethod.invoke(null, new Object[]{providerClass});
		}
		catch (Throwable e)
		{
			out(e);
		}
		if (services != null)
		{
			while (services.hasNext())
			{
				Object	provider = services.next();
				out(3, provider.getClass().getName());
			}
		}
	}



	private static void displayConfigurationFiles(String strProviderTypeName)
	{
		String	strFullName = BASE_NAME + strProviderTypeName;
		Class	providerClass = null;
		Enumeration	configs = null;
		try
		{
			providerClass = Class.forName(strProviderTypeName);
			configs = ClassLoader.getSystemResources(strFullName);
		}
		catch (Exception e)
		{
			out(e);
		}
		// if (configs != null)
		// {
		while (configs.hasMoreElements())
		{
			Object	config = configs.nextElement();
			out(3, config.toString());
		}
		// }
	}




	private static void printUsageAndExit()
	{
		out("DumpJSInfo: usage:");
		out("\tjava DumpJSInfo -h, --help");
		out("\tjava DumpJSInfo -V, --version");
		out("\tjava DumpJSInfo [-D, --debug] [--mixers] [--controls] [--mididevices] [--providers] [-a, --all]");
		System.exit(0);
	}



	private static void printVersionAndExit()
	{
		out("DumpJSInfo version 0.1.0");
		out("Copyright (c) 1999 - 2003 by Matthias Pfisterer");
		System.exit(0);
	}



	private static void out(int nIndentationStep, String strMessage)
	{
		String	strIndentation = INDENTATION_TABLE[nIndentationStep];
		System.out.println(strIndentation + strMessage);
	}



	private static void out(String strMessage)
	{
		out(0, strMessage);
	}



	private static void out(Throwable throwable)
	{
		throwable.printStackTrace();
	}
}



/*** DumpJSInfo.java ***/
