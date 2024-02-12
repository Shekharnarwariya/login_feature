package com.hti.smpp.common.database;

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.data.general.PieDataset;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;

public class DlrPieCustom implements JRChartCustomizer {

    @Override
    public void customize(JFreeChart jfc, JRChart jrc) {
        // JRPropertiesMap pm=jrc.getDataset();

        Plot plot = jfc.getPlot();
        if (plot instanceof PiePlot) {
            PiePlot piePlot = (PiePlot) plot;
            PieDataset dataset = piePlot.getDataset();
            //System.out.println("DS count : " + dataset.getItemCount());
            for (int i = 0; i < dataset.getItemCount(); i++) {
                String key = (String) dataset.getKey(i);
                //System.out.println("-> " + key);
                 if (key.startsWith("DELIV")) {
                    piePlot.setSectionPaint(key, Color.decode("#008000"));
                } else if (key.startsWith("UNDELIV")) {
                    piePlot.setSectionPaint(key, Color.decode("#a52a2a"));
                } else if (key.startsWith("ACCEPT")) {
                    piePlot.setSectionPaint(key, Color.decode("#ff8c00"));
                } else if (key.startsWith("UNKNOW")) {
                    piePlot.setSectionPaint(key, Color.decode("#00bfff"));
                } else if (key.startsWith("ATES")) {
                    piePlot.setSectionPaint(key, Color.decode("#ffff00"));
                } else if (key.startsWith("REJEC")) {
                    piePlot.setSectionPaint(key, Color.decode("#ffc0cb"));
                } else if (key.startsWith("ERR_RESP")) {
                    piePlot.setSectionPaint(key, Color.decode("#ff0000"));
                } else if (key.startsWith("EXPIR")) {
                    piePlot.setSectionPaint(key, Color.decode("#9932cc"));
                }else if (key.startsWith("NO_RESP")) {
                    piePlot.setSectionPaint(key, Color.decode("#0000ff"));
                }else if (key.startsWith("QUEUED")) {
                    piePlot.setSectionPaint(key, Color.decode("#fff8dc"));
                }
            }
        }
    }

}