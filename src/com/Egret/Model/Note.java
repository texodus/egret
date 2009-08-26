package com.Egret.Model;

public class Note {
	
	private String note;
    private double _freq;
    private int pitch;
	private double octave;
	private boolean isNull = false; 
	
	public Note(double frequency) {
		setNote(frequency);
        _freq = frequency;
    }
	
	public Note(String n, int oct) {
		note = n;
		octave = oct;
	}
	
	public Note(String x) {
		isNull = true;
		note = x;
	}
	
	public String getNote() {
		return note;
	}

    public double getFrequency() {
        return _freq;
    }

    public int getPitch() {
		return pitch;
	}
	
	public int getOctave() {
		if(!isNull) {
			return (int) octave;
		} else {
			return 0;
		}
	}
	
	private static double lognote(double freq)	{
		return (Math.log(freq) - Math.log(440))	/ Math.log(2) + 4.0;
	}

	private void setNote(double freq) {
		
		double lnote = lognote(freq);
		octave = Math.floor(lnote);
		double cents = 1200 * (lnote - octave);
		String noteTable = "A A#B C C#D D#E F F#G G#";
		String pitchTable = "1 2 3 4 5 6 7 8 9 101112";
		double offset = 50.0;
		int x = 2;

		if (cents < 50) {
			note = "A ";
            pitch = 13 * (int)octave;
        } else if (cents >= 1150)	{
			note = "A ";
			cents -= 1200;
			octave++;
            pitch = 13 * (int)octave;

        } else {
			for (int j = 1 ; j <= 11 ; j++ ) {
				if (cents >= offset && cents < (offset + 100)) {
					note = noteTable.substring(x, x+1);
					pitch = Integer.parseInt(pitchTable.substring(x, x+1)) + (13 * (int)octave);
					cents -= (j * 100);
					break;
				}
				offset += 100;
				x += 2;
			}
		}
		
		
	}

	public boolean isNull() {
		return isNull;
	}
}
