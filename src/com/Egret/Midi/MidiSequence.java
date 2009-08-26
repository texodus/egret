package com.Egret.Midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import java.util.List;
import com.Egret.Utils.Log;
import com.Egret.Model.Note;

public class MidiSequence {

	private static final int ticksPerMeter = 1000;
	private static int instrument = 0;
	
	public static Sequence generateSequence(List<Note> notes) {
		
		Sequence newSequence;
		MidiEvent event;
		ShortMessage message;
		long index;
		
		try {
			
			newSequence = new Sequence(Sequence.PPQ, ticksPerMeter/4);

			
        	// create a new midi message setting the instrument 
        	newSequence.createTrack();
	    	index = 0;
    	    message = new ShortMessage();
   	    	message.setMessage(192, instrument, 120);
   	    	event = new MidiEvent(message, index);
   	    	newSequence.getTracks()[newSequence.getTracks().length - 1].add(event);
   	    	
   	    	for(int j = 0; j < notes.size(); j ++) {
	    		
    			// write the start of the note
		    	message = new ShortMessage();
    	    	message.setMessage(
    	    			ShortMessage.NOTE_ON, 
    	    			Math.abs(notes.get(j).getPitch()), 
    	    			100
    	    	);
    	    		
    	    	MidiEvent startEvent = new MidiEvent(message, new Long(index).intValue());
    	    	index = index + (ticksPerMeter / 32);
    			
    	    	if (j < (notes.size() - 1)) {
    	    		try {
    	    			
    	    			String a = notes.get(j + 1).getNote();
    	    			String b = notes.get(j).getNote();
    	    			while (notes.get(j + 1).getNote().equals(notes.get(j).getNote())) {
    	    				index = index + (ticksPerMeter / 32);
    	    				j ++;
    	    			}
    	    		} catch (Exception e) {
    	    			Log.error("Array out of bounds!");
    	    		}
    	    	}
    	    	
    	    	// write the end of the note
    			message = new ShortMessage();
   	    		message.setMessage(
   	    				ShortMessage.NOTE_OFF, 
   	    				Math.abs(notes.get(j).getPitch()), 
   	    				100
   	    		);
   	    	
   	    		MidiEvent endEvent = new MidiEvent(message, new Long(index).intValue());

   	    		// write the notes to the track
   	    		if (!notes.get(j).isNull()) {
   	    			newSequence.getTracks()[newSequence.getTracks().length - 1].add(startEvent);
   	    			newSequence.getTracks()[newSequence.getTracks().length - 1].add(endEvent);
   	    		}
    			
   	    	}
   	    	return newSequence;
		} catch (InvalidMidiDataException e) {
			Log.append("Error writing to midi file; "+ e.getMessage());
			return null;
		}
    }
}
