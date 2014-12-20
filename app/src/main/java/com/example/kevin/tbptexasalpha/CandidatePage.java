package com.example.kevin.tbptexasalpha;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

//Remember to cite the graph thing according to Apache License
public class CandidatePage extends Activity {

    private String name;
    private int[] values;

    private static final String[] REQUIREMENTS = new String[]{"Informal Interview", "Resume", "Catalog Cards",
        "Dues", "Scrapbook", "Bent", "Quiz", "Dress Up", "Sig Block", "Meetings", "Firesides", "Socials",
        "Service", "Team Points"};
    private static final int NUM_MEETINGS = 5;
    private static final int NUM_FIRESIDES = 1;
    private static final int HOURS_SOCIAL = 5;
    private static final int HOURS_SERVICE = 10;
    private static final int TEAM_POINTS = 0;//For now

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
        XYSeries expectedValues = new XYSeries("Expected");
        int[] expectedResults = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, NUM_MEETINGS,
                NUM_FIRESIDES, HOURS_SOCIAL, HOURS_SERVICE, 200};
        for (int i = 0; i < values.length; i++)
        {
            candidateGraph.add(i, values[i]);
        }

        //Now to get the maximum X value based on # team points earned
        int team_points = values[values.length - 1];
        int remainder = team_points % 100;
        int newMax = (team_points > TEAM_POINTS) ? team_points - remainder + 100 : TEAM_POINTS + 50;

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(candidateGraph);
        dataset.addSeries(expectedValues);

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(Color.CYAN);
        renderer.setFillPoints(true);
        renderer.setLineWidth(2);
        renderer.setDisplayChartValues(true);
        renderer.setDisplayChartValuesDistance(10);

        XYSeriesRenderer e_renderer = new XYSeriesRenderer();
        e_renderer.setColor(Color.RED);
        e_renderer.setFillPoints(true);
        e_renderer.setLineWidth(2);
        e_renderer.setDisplayChartValues(true);

        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
        multiRenderer.setOrientation(XYMultipleSeriesRenderer.Orientation.VERTICAL);
        multiRenderer.setXLabels(0);
        multiRenderer.setChartTitle("Candidate Information");
        multiRenderer.setXTitle("Requirements");
        multiRenderer.setYTitle("Value");

        multiRenderer.setChartTitleTextSize(28);
        multiRenderer.setAxisTitleTextSize(24);
        multiRenderer.setLabelsTextSize(24);
        multiRenderer.setZoomButtonsVisible(false);
        multiRenderer.setPanEnabled(false, false);
        multiRenderer.setClickEnabled(false);
        multiRenderer.setZoomEnabled(false, false);
        multiRenderer.setShowGridY(false);
        multiRenderer.setShowGridX(false);
        multiRenderer.setFitLegend(true);
        multiRenderer.setShowGrid(false);
        multiRenderer.setZoomEnabled(false);
        multiRenderer.setExternalZoomEnabled(false);
        multiRenderer.setAntialiasing(false);
        multiRenderer.setInScroll(false);
        multiRenderer.setLegendHeight(30);
        multiRenderer.setXLabelsAlign(Paint.Align.CENTER);
        multiRenderer.setXLabelsColor(Color.BLACK);
        multiRenderer.setLabelsColor(Color.BLACK);
        multiRenderer.setYLabelsColor(0, Color.BLACK);
        multiRenderer.setYLabelsAlign(Paint.Align.LEFT);
        multiRenderer.setTextTypeface("sans_serif", Typeface.NORMAL);
        multiRenderer.setYLabels(10);
        multiRenderer.setYAxisMax(newMax);
        multiRenderer.setXAxisMin(0);
        multiRenderer.setXAxisMax(13);
        multiRenderer.setBarSpacing(0.5);
        multiRenderer.setBackgroundColor(Color.TRANSPARENT);
        multiRenderer.setMarginsColor(getResources().getColor(R.color.transparent_background));
        multiRenderer.setApplyBackgroundColor(true);

        multiRenderer.setMargins(new int[]{5, 30, 200, 10});

        for (int i = 0; i < values.length; i++)
        {
            multiRenderer.addXTextLabel(i, REQUIREMENTS[i]);
        }

        multiRenderer.addSeriesRenderer(renderer);
        multiRenderer.addSeriesRenderer(e_renderer);

        LinearLayout chart = (LinearLayout) findViewById(R.id.chart);
        chart.removeAllViews();
        View newChart = ChartFactory.getBarChartView(CandidatePage.this, dataset, multiRenderer, BarChart.Type.DEFAULT);
        chart.addView(newChart);
    }
}
