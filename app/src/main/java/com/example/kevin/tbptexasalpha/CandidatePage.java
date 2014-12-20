package com.example.kevin.tbptexasalpha;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

//Remember to cite the graph thing according to Apache License
public class CandidatePage extends Activity {

    private String name;
    private int[] values;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_page);

        values = new int[14];
        int counter = 0;

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        String[] tempVals = intent.getStringArrayExtra("values");
        for (String val : tempVals)
        {
            String current;
            if (val.equals("X")) {current = "1";}
            else {current = val;}
            values[counter] = Integer.parseInt(current);
            counter++;
        }

        //Time to create the graph
        createGraph();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_candidate_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void createGraph()
    {
        XYSeries candidateGraph = new XYSeries("Candidate");
        for (int i = 0; i < values.length; i++)
        {
            candidateGraph.add(i, values[i]);
        }

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(candidateGraph);

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(Color.CYAN);
        renderer.setFillPoints(true);
        renderer.setLineWidth(2);
        renderer.setDisplayChartValues(true);
        renderer.setDisplayChartValuesDistance(10);

        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.VERTICAL);
        multiRenderer.setXLabels(0);
        multiRenderer.setChartTitle("Candidate Information");
        multiRenderer.setXTitle("Block");
        multiRenderer.setYTitle("Value");

    }
}
