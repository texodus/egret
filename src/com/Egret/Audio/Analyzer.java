package com.Egret.Audio;

import com.Egret.Model.Note;
import com.Egret.Utils.Log;
import com.Egret.Utils.MathUtils;
import com.Egret.Utils.Complex;
import com.Egret.View.GraphPanel;

import javax.sound.sampled.AudioFormat;
import javax.swing.JFrame;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: steinlink
 * Date: Dec 2, 2006
 * Time: 11:39:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class Analyzer {

	// this must be a power of two or the algorithim won't work!
	public static int sampleSize = 2048;
    private static int downsamples = 3;
    private static boolean debug = true;
    private static boolean autocorrelate = false;
    private int iterations = 128;
	private int amplitudeThreshold = 1500;
	private AudioFormat format;
	private static int spread = 20;
	private String message;
	double sampleRate;

	/**
	 * Constructor, needs an AudioFormat for several calculations ...
	 * @param f
	 */
	public Analyzer(AudioFormat f) {
		format = f;
	}
	
	/**
	 * Compute a phrase (list of notes) from a byte[]
	 * @param bytes
	 * @return
	 */
	public List<Note> computePhrase(byte[] bytes) {
		
		int[] samples = byteArrayToIntArray(bytes);
		
		// if the format is greater than 11025, downsample so the algorithm is fast;
		// after all, we don't need to analyze frequencies over ~5000hz
		sampleRate = format.getSampleRate();
		
		while(sampleRate > 11025) {
			int[] tempSamples = new int[(Array.getLength(samples) / 2) + 1];
			for (int i = 0; i < Array.getLength(samples) + 1; i += 2) tempSamples[i / 2] = (samples[i] + samples[i + 1]) / 2;
			samples = tempSamples;
			sampleRate /= 2;
		}
		
		int numberOfSegments =  (int)(getLength(samples) * 32);
		double segmentLength = sampleRate / 16;
		boolean leading = false;
		List<Note> notes = new LinkedList<Note>();
		
		Log.append("Computing phrase from " + Array.getLength(samples) + " samples of captured data ...");
		Log.append("This sample is " + getLength(samples) + "s at " + sampleRate + ";  using " + numberOfSegments + " segments of " + segmentLength + " samples");

		
		// divide the sample into segments
		for (int i = 0; i < numberOfSegments; i ++) {
			try {
				int[] segment = getSubArray(samples, segmentLength, i);
				message = "Segment "+ i + ": " + (i * segmentLength) + "-" + ((i + 1) * segmentLength) + " is ";
				
				if ((getAverageAmplitude(segment) < amplitudeThreshold) && !leading) {
					// this is leading dead space, don't report it
					notes.add(new Note("leading null"));
				} else {
					
					// we found a note, so don't skip any more segments
					leading = true;
					int aa = getAverageAmplitude(segment);
					message += aa + " average amplitude ";
					if (aa < amplitudeThreshold) {
						// this is a dead note after the leading dead space, record it as a null
						notes.add(new Note("null"));
					} else {
						notes.add(guessNote(segment, i));
					}
				}
				
				Log.append(message + " " + notes.get(notes.size() - 1).getNote());
			
			} catch (ArrayIndexOutOfBoundsException e) {
				// the segment was too short, decrement the numberOfSegments
				Log.append("Caught a short segment, using " + numberOfSegments);
				numberOfSegments --;
			}
		}
		
		// Go backwards and replace nulls with trailing nulls
		for (int i = notes.size() - 1; i >= 0; i --) {
			if (notes.get(i).isNull()) {
				notes.set(i, new Note("trailing null"));
			} else break;
		}
		
		// Process the list of note segments and remove/replace
		notes = autoFix(notes);
		
		Log.append("Phrase successfully computed!");
		return notes;
	}
	
	private List<Note> autoFix(List<Note> notes) {
		
		LinkedList<Note> newNotes = new LinkedList<Note>();
		
		if (notes.size() <= 1) return notes;

        // normalize in the frequency domain
        double current = notes.get(0).getFrequency();
		for (int i = 1; i < notes.size(); i ++) {
        	if ((notes.get(i).getFrequency() > (current * 0.94)) && (notes.get(i).getFrequency() < (current * 1.06))) {
        		notes.set(i, new Note(current));
        	} else if (notes.get(i).getFrequency() != 0){
        		current = notes.get(i).getFrequency();
        	}
        	newNotes.add(notes.get(i));
        }
		
		// now delete all leading and trailing nulls
		for (int i = 0; i < newNotes.size(); i ++) {
			
			if ((newNotes.get(i).getNote() == "leading null") || (newNotes.get(i).getNote() == "trailing null")) {
				newNotes.remove(i);
				i --;
			}
		}
		
		return newNotes;
	}
	
	/**
	 * get the average amplitude from a sample
	 * @param samples
	 * @return
	 */
	private int getAverageAmplitude(int[] samples) {
		long average = 0;
		for (int i = 0; i < Array.getLength(samples); i ++) average += Math.abs(samples[i]);
		average = average / Array.getLength(samples);
		return (int) average;
	}
	
	/**
	 * Determine the pitch of the selected sample through autocorreclation algorithm
	 * @param notes
	 * @return
	 */
	private int autoCorrelate(int[] notes, int x) {
		
		int[] sample = getSubArray(notes, Array.getLength(notes) / 2, 0);
		double[] r = new double[Array.getLength(notes)];
		int guess = 0;
		double last = -1;
		boolean mark = false;
		
		// calculate the differences
		for (int i = 0; i < (Array.getLength(notes) / 2); i ++) {
			double diff = 0;
			for (int j = 0; j < Array.getLength(sample); j ++) {
				diff = diff + (Math.abs((notes[j + i] - sample[j])));
			}
			if (last == -1) {
				// do nothing
			} else if (diff < last) {
				mark = true;
			} else if ((diff > last) && mark) {
				guess = i;
				break;
			}
			last = (double)diff;
			r[i] = (double)diff;
		}
		
		if (debug) {
			GraphPanel panel = new GraphPanel();
			
           	panel.createWaveForm(r, format);
            
        	JFrame frame = new JFrame();
        	frame.setTitle(x + "");
        	frame.add(panel);
        	frame.setSize((Array.getLength(r)) + 10, 330);
        	frame.setVisible(true);
		}
		
		return guess;
	}
	
	/**
	 * Get a sub array of length starting from (seg * length)
	 * @param samples
	 * @param length
	 * @param seg
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 */
	private int[] getSubArray(int[] samples, double length, int seg) throws ArrayIndexOutOfBoundsException {
		int[] newSamples = new int[(int) length];
		for (int i = (seg * (int)length); i < ((seg + 1) * (int)length); i ++) {
			newSamples[i - (seg * (int)length)] = samples[i];
		}
		return newSamples;
	}
	
	/**
	 * Make a guess at the note in this sample
	 * @param samples
	 * @return
	 */
	private Note guessNote(int[] samples, int x) {

        double frequency = 0;

        if (autocorrelate) {
            frequency = 1 / (autoCorrelate(samples, x) / (sampleRate * 4));
        } else {

            double[] fft = computeFFT(samples, generateHammingWindow(Array.getLength(samples)), 0);
            int count = 0;

            // get the average of several sample sets
            for (int i = 1; i < Array.getLength(samples) - sampleSize; i += spread) {
                count ++;
                double[] instantFFT = computeFFT(samples, generateHammingWindow(Array.getLength(samples)), i);
                for (int j = 0; j < Array.getLength(fft); j ++) {
                    fft[j] += instantFFT[j];
                }
            }

            for (int j = 0; j < Array.getLength(fft); j ++) {
                fft[j] = fft[j] / count;
            }

            // downsample & divide to get the peaks
            double[] downsampled = fft.clone();

            for (int i = 0; i < downsamples; i ++) {

                downsampled = compress(downsampled);
                for (int j = 0; j < Array.getLength(fft); j ++) {
                    fft[j] += downsampled[j];
                }

            }

            for (int j = 0; j < Array.getLength(fft); j ++) {
                fft[j] = fft[j] / downsamples;
            }

            // assume the max is the frequency
            double guess = 0;
            for (int i = 0; i < Array.getLength(fft); i ++) {
                if (fft[i] > frequency) {
                    frequency = fft[i];
                    guess = i;
                }
            }

            frequency = (guess / Array.getLength(fft)) * (sampleRate / 2);

            if (debug) {
                GraphPanel panel = new GraphPanel();

                panel.createWaveForm(fft, format);

                JFrame frame = new JFrame();
                frame.setTitle(x + "");
                frame.add(panel);
                frame.setSize((Array.getLength(fft)) + 10, 330);
                frame.setVisible(true);
            }


        }
        message += frequency + "hz";
		return new Note(frequency);
	}

    private double[] compress(double[] samples) {

        double[] compressed = new double[Array.getLength(samples)];

        for (int i = 0; i < Array.getLength(samples); i ++) {
            if (Array.getLength(samples) > (i * 2) + 1) {
                compressed[i] = (samples[i * 2] + samples[(i * 2) + 1]) / 2;
            } else {
                compressed[i] = 0;
            }
        }

        return compressed;
    }

    /**
     * Get the length in seconds of a byte[]
     * @param samples
     * @return
     */
    private double getLength(int[] samples) {
    	double length = Array.getLength(samples) / (sampleRate * 2);
    	return length;
    }
    
    /**
     * Translate a byte array into an int array of amplitudes
     * @param bytes
     * @return
     */
    public int[] byteArrayToIntArray(byte[] bytes) {
    	
    	int[] audioData = null;
        if (format.getSampleSizeInBits() == 16) {
        
        	int nlengthInSamples = bytes.length / 2;
            audioData = new int[nlengthInSamples];
            
            if (format.isBigEndian()) {
            	for (int i = 0; i < nlengthInSamples; i++) {
            		int MSB = (int) bytes[2*i];
                    int LSB = (int) bytes[2*i+1];
                    audioData[i] = MSB << 8 | (255 & LSB);
                }
            } else {
                for (int i = 0; i < nlengthInSamples; i++) {
                    int LSB = (int) bytes[2*i];
                    int MSB = (int) bytes[2*i+1];
                    audioData[i] = MSB << 8 | (255 & LSB);
                }
            }
        
        } else if (format.getSampleSizeInBits() == 8) {
            
        	int nlengthInSamples = bytes.length;
            audioData = new int[nlengthInSamples];
            
            if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
            	for (int i = 0; i < bytes.length; i++) {
            		audioData[i] = bytes[i];
                }
            } else {
            	for (int i = 0; i < bytes.length; i++) {
            		audioData[i] = bytes[i] - 128;
                }
            }
        }

        return audioData;
    }
    
    ///
    ///  Fast Fourier Transform Section ... this is no longer used
    ///
    ///
    
    
    /**
     * Translate an int[] to a complex[], apply a filter window and offset, and compute
     * the Fast Fourier Transform complex[], convert to polar coordinates and return the
     * magnitude array. 
     * @param audio
     * @param window
     * @param offset
     * @return
     */
    private double[] computeFFT(int[] audio, double[] window, int offset) {

    	// translate into complex array
        Complex[] complexAudio = new Complex[sampleSize];

        for (int i = 0; i < sampleSize; i ++) {
            complexAudio[i] = new Complex(window[i] * audio[i + offset], 0);
        }

        Complex[] spectrum = MathUtils.fft(complexAudio);
        int size = Array.getLength(spectrum);
        double[] array = new double[size / 2];

        for (int i = 0; i < size / 2; i ++) {
            array[i] = polarMagnitude(spectrum[i]);
        }

        return array;
    }
    
    private double polarMagnitude(Complex c) {
    	return Math.sqrt((c.re * c.re) + (c.im * c.im));
    }
    
	/**
	 * Computes the Frequency Spectrum given a byte array of audio data
	 * @param bytes a byte[] of audio data
	 * @return
	 */
    public double[] computeFrequencySpectrum(byte[] bytes) {
        int[] audio;
        Log.append((sampleSize + (iterations * spread)) + " samples (" + ((sampleSize + (iterations * spread)) / sampleRate) + "s) are needed");
        if (Array.getLength(bytes) < (sampleSize + (spread * iterations))) {
            Log.error("The sample was too short at " + Array.getLength(bytes) + "; ");
            
            return null;
        }

        audio = byteArrayToIntArray(bytes);
        double[] results = new double[sampleSize / 2];
        double[] hammingWindow = generateHammingWindow(sampleSize);
        
        for (int i = 0; i < (sampleSize / 2); i ++) results[i] = 0;
        for (int i = 0; i < iterations; i ++) {
        	double[] temp = computeFFT(audio, hammingWindow, i * spread);
        	for(int j = 0; j < (sampleSize / 2); j ++) {
        		results[j] += temp[j];
        	}
        }
        
        for (int i = 0; i < (sampleSize / 2); i ++) results[i] = results[i] / iterations;
        return results;
    }

    /**
     * Generate a hamming window of a specified size
     * @param size
     * @return
     */
    private double[] generateHammingWindow(int size) {
    	double[] window = new double[size];
    	for (int i = 0; i < size; i ++) {
    		window[i] = 0.54 - (0.46 * Math.cos((2 * Math.PI * i) / size));
    	}
    	return window;
    }
}
