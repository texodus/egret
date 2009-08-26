package com.Egret.Midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import com.Egret.Utils.Log;


public class MidiPlayer {
	
	private static Sequencer	sm_sequencer = null;
	private static Synthesizer	sm_synthesizer = null;

	public static Sequencer play(Sequence sequence, MidiDevice.Info seq, float tempo){
 
		// Get a Sequencer
		try{
			sm_sequencer = MidiSystem.getSequencer(false);
			MidiDevice device = MidiSystem.getMidiDevice(seq);
			sm_sequencer.getTransmitter().setReceiver(device.getReceiver());
		} catch (MidiUnavailableException e){
			e.printStackTrace();
		}
     
		if (sm_sequencer == null){
			Log.append("SimpleMidiPlayer.main(): can't get a Sequencer");
			return null;
		}

		try {
			sm_sequencer.open();
			sm_sequencer.setSequence(sequence);
		} catch (InvalidMidiDataException e){
			e.printStackTrace();
		} catch (MidiUnavailableException e){
			e.printStackTrace();
		}

		if (!(sm_sequencer instanceof Synthesizer)){
			try {
				sm_synthesizer = MidiSystem.getSynthesizer();
				sm_synthesizer.open();
				sm_sequencer.setTempoInBPM(tempo);
				Receiver	synthReceiver = sm_synthesizer.getReceiver();
				Transmitter	seqTransmitter = sm_sequencer.getTransmitter();
				seqTransmitter.setReceiver(synthReceiver);
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
		sm_sequencer.start();
		return sm_sequencer;
	}
}