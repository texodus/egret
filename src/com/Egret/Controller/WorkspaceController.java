package com.Egret.Controller;

import com.Egret.Midi.MidiPlayer;
import com.Egret.Midi.MidiSequence;
import com.Egret.Model.Note;
import com.Egret.Utils.Log;
import com.Egret.View.GraphPanel;
import com.Egret.View.Workspace;
import com.Egret.Audio.Analyzer;
import com.Egret.Audio.Recorder;



import javax.swing.*;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: steinlink
 * Date: Dec 3, 2006
 * Time: 4:32:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkspaceController extends Workspace implements ActionListener {

    private boolean isRecording = false;
    private Recorder recorder = new Recorder();

    public WorkspaceController() {
        getRecordButton().addActionListener(this);
        getRecordButton().setActionCommand("RECORD");

        JFrame frame = new JFrame("Workspace");
        frame.setContentPane(getPanel1());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {

        String cmd = actionEvent.getActionCommand();

        // Record button clicked
        if (cmd == getRecordButton().getActionCommand()) {

            if (!isRecording) {
                isRecording = true;
                getRecordButton().setText("Stop");
                recorder.startRecording();
            } else {
                isRecording = false;
                getRecordButton().setText("Record");
                recorder.stopRecording();
                
                byte[] bytes = recorder.getRecordedBytes();

                Analyzer analyze = new Analyzer(SettingsController.getFormat());
                
                List<Note> notes = analyze.computePhrase(bytes);
                String sequence = "";
               
                for (Iterator i = notes.iterator(); i.hasNext();) {
                	sequence += ((Note)i.next()).getNote() + " ";
                }
                
                Log.append(sequence);
                
    			Sequencer player = MidiPlayer.play(
    					MidiSequence.generateSequence(notes), 
    					MidiSystem.getMidiDeviceInfo()[0], 
    					120
    			);
            }
        }
    }
}
