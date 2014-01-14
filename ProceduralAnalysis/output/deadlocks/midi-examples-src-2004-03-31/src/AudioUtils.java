/*
 *	AudioUtils.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 2000 by Matthias Pfisterer
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
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;


/**
 *	For an example of usage, see AudioStream.java.
 */
public class AudioUtils
{
	private static final boolean	DEBUG = true;



	public static Iterator getSupportedSourceDataLineFormats(Mixer mixer)
	{
		Line.Info[] aLineInfos = mixer.getSourceLineInfo(new Line.Info(SourceDataLine.class));
		return getSupportedSourceDataLineFormatsImpl(aLineInfos);
	}



	public static Iterator getSupportedSourceDataLineFormats()
	{
		Line.Info[] aLineInfos = AudioSystem.getSourceLineInfo(new Line.Info(SourceDataLine.class));
		return getSupportedSourceDataLineFormatsImpl(aLineInfos);
	}



	private static Iterator getSupportedSourceDataLineFormatsImpl(Line.Info[] aLineInfos)
	{
		ArrayList	formats = new ArrayList();
		for (int i = 0; i < aLineInfos.length; i++)
		{
			if (aLineInfos[i] instanceof DataLine.Info)
			{
				AudioFormat[]	aFormats = ((DataLine.Info) aLineInfos[i]).getFormats();
				for (int nFormat = 0; nFormat < aFormats.length; nFormat++)
				{
					if (! formats.contains(aFormats[nFormat]))
					{
						formats.add(aFormats[nFormat]);
					}
				}
			}
			else
			{
				/*
				 *	No chance to get useful information,
				 *	so do nothing.
				 */
			}
		}
		return formats.iterator();
	}



	public static AudioFormat getSuitableTargetFormat(
		AudioFormat sourceFormat)
	{
		Iterator	possibleFormats = getSupportedSourceDataLineFormats();
		return getSuitableTargetFormatImpl(possibleFormats, sourceFormat);
	}



	public static AudioFormat getSuitableTargetFormat(
		Mixer mixer,
		AudioFormat sourceFormat)
	{
		Iterator	possibleFormats = getSupportedSourceDataLineFormats(mixer);
		return getSuitableTargetFormatImpl(possibleFormats, sourceFormat);
	}



	public static AudioFormat getSuitableTargetFormatImpl(
		Iterator possibleTargetFormats,
		AudioFormat sourceFormat)
	{
		// TODO: should use some preference algorithm to use best possible formats.
		while (possibleTargetFormats.hasNext())
		{
			AudioFormat	possibleTargetFormat = (AudioFormat) possibleTargetFormats.next();
			if (AudioSystem.isConversionSupported(possibleTargetFormat, sourceFormat))
			{
				return possibleTargetFormat;
			}
		}
		/*
		 *	No suitable format found.
		 */
		return null;
	}



	public static AudioInputStream getSuitableAudioInputStream(
		AudioInputStream sourceAudioInputStream)
	{
		AudioFormat	targetFormat = getSuitableTargetFormat(
			sourceAudioInputStream.getFormat());
		return getSuitableAudioInputStreamImpl(
			sourceAudioInputStream,
			targetFormat);
	}



	public static AudioInputStream getSuitableAudioInputStream(
		Mixer mixer,
		AudioInputStream sourceAudioInputStream)
	{
		AudioFormat	targetFormat = getSuitableTargetFormat(
			mixer,
			sourceAudioInputStream.getFormat());
		return getSuitableAudioInputStreamImpl(
			sourceAudioInputStream,
			targetFormat);
	}



	public static AudioInputStream getSuitableAudioInputStreamImpl(
		AudioInputStream sourceAudioInputStream,
		AudioFormat targetFormat)
	{
		if (DEBUG)
		{
			out("AudioUtils.getSuitableAudioInputStreamImpl(): target format: " + targetFormat);
		}
		if (targetFormat != null)
		{
			if (DEBUG)
			{
				out("AudioUtils.getSuitableAudioInputStreamImpl(): trying to do a conversion.");
			}
			return AudioSystem.getAudioInputStream(
				targetFormat,
				sourceAudioInputStream);
		}
		else
		{
			if (DEBUG)
			{
				out("AudioUtils.getSuitableAudioInputStreamImpl(): returning null as resulting AudioInputStream.");
			}
			return null;
		}
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** AudioUtils.java ***/
