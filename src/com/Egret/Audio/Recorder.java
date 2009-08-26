package com.Egret.Audio;

import javax.sound.sampled.*;

import com.Egret.Controller.SettingsController;
import com.Egret.Utils.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

public class Recorder {

	private RecorderThread recorder;
    private Line.Info[] infos;
    private byte[] audioBytes;
    boolean filled = false;

    AudioFormat audioFormat = SettingsController.getFormat();
    
    public void startRecording() {


		DataLine.Info	info = new DataLine.Info(TargetDataLine.class, audioFormat);
		TargetDataLine targetDataLine = null;

        try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			targetDataLine.open(audioFormat, targetDataLine.getBufferSize());
		} catch (Exception e) {
			e.printStackTrace();
            return;
        }

		recorder = new RecorderThread(targetDataLine);
		recorder.startRecord();
	}

	public void stopRecording() {
		recorder.stopRecord();
	}

	public byte[] getRecordedBytes() {
		while (!filled);
        filled = false;
        return audioBytes;
	}

	private class RecorderThread extends Thread {

		private TargetDataLine		line;
        private boolean running;

        public RecorderThread(TargetDataLine dataline) {
			line = dataline;
		}

		public void run() {
			try	{

                // play back the captured audio data
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int frameSizeInBytes = 4;
                int bufferLengthInFrames = line.getBufferSize() / 8;
                int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
                byte[] data = new byte[bufferLengthInBytes];
                int numBytesRead;

                line.start();

                Log.append("Started Recording ...");

                while (running) {
                    if((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                        break;
                    }
                    out.write(data, 0, numBytesRead);
                }

                Log.append("Stopped Recording");
                
                // we reached the end of the stream.  stop and close the line.
                line.stop();
                line.close();
                line = null;

                // stop and close the output stream
                try {
                    out.flush();
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // load bytes into the audio input stream for playback

                audioBytes = out.toByteArray();
                Log.append("Captured " + Array.getLength(audioBytes) + " bytes");
                filled = true;
            } catch (Exception e)	{
				e.printStackTrace();
			}
		}

		public void startRecord() {
			running = true;
			super.start();
		}

		public void stopRecord() {
            running = false;
		}
	}
}
