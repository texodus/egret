package com.Egret.View;

import javax.swing.*;
import javax.sound.sampled.AudioFormat;
import com.Egret.Audio.Analyzer;
import com.Egret.Utils.Log;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.Vector;
import java.lang.reflect.Array;


/**
 * Created by IntelliJ IDEA.
 * User: steinlink
 * Date: Dec 4, 2006
 * Time: 2:31:21 AM
 * To change this template use File | Settings | File Templates.
 */
/**
 * Render a WaveForm.
 */
public class GraphPanel extends JPanel {

    Vector lines = new Vector();
    Color jfcBlue = new Color(204, 204, 255);

    int w = (Analyzer.sampleSize / 2);
    int h = 300;
     
    public GraphPanel() {
        setBackground(new Color(20, 20, 20));
    }

    public void createWaveForm(double[] audioData, AudioFormat format) {

        lines.removeAllElements();  // clear the old vector

        int frames_per_pixel = (Analyzer.sampleSize / 2) / w;
        double y_last = 0, max = 0;
        
        for (int i = (Analyzer.sampleSize / 100); i < Array.getLength(audioData); i ++) {
        	if (audioData[i] > max) max = audioData[i];
        }
        
        for (double x = 0; x < w && audioData != null; x++) {
            int idx = (int) (frames_per_pixel * x);
            double y_new = audioData[idx];
            y_new = 300 - (y_new * (h / max));
            lines.add(new Line2D.Double(x, y_last, x, y_new));
            y_last = y_new;
        }

        repaint();
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(getBackground());
        g2.clearRect(0, 0, w, h);
        g2.setColor(jfcBlue);
        for (int i = 1; i < lines.size(); i++) {
            g2.draw((Line2D) lines.get(i));
        }
    }
}
