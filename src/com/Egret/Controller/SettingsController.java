package com.Egret.Controller;

import javax.sound.sampled.AudioFormat;

public class SettingsController {
	
	private static AudioFormat format = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44100.0F,
            16,
            2,
            4,
            44100.0F,
            true
    );
	
	public static AudioFormat getFormat() {
		return format;
	}
}
