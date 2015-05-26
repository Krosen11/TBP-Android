package com.example.kevin.tbptexasalpha;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

//Remember to cite the graph thing according to Apache License
public class CandidatePage extends Activity {

    private String name;
    private double[] values;

    private static final String[] REQUIREMENTS = new String[]{"Informal Interview", "Resume", "Catalog Cards",
        "Dues", "Scrapbook", "Bent", "Quiz", "Dress Up", "Sig Block", "Meetings", "Firesides", "Socials",
        "Service", "Team Points"};
    private static final int NUM_MEETINGS = 5;
    private static final int NUM_FIRESIDES = 1;
    private static final int HOURS_SOCIAL = 5;
    private static final int HOURS_SERVICE = 10;
    private static final int TEAM_POINTS = 0;//For now

    private static final int[] EXPECTED_RESULTS = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, NUM_MEETINGS,
            NUM_FIRESIDES, HOURS_SOCIAL, HOURS_SERVICE, 200};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_page);

        values = new double[14];
        int counter = 0;

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        String[] tempVals = intent.getStringArrayExtra("values");
        for (String val : tempVals)
        {
            String current;
            if (val.equals("X")) {current = "1";}
            else {current = val;}
            values[counter] = Double.parseDouble(current);
            counter++;
        }

        //TODO: Add transparency to the checkmark picture and maybe the TBP Logo picture (xmark already has it)

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.rel_layout);

        //Let's first output a textview for the heading
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView view = new TextView(this);
        view.setText(name);
        view.setTypeface(null, Typeface.BOLD);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        view.setSingleLine(true);
        int viewId = View.generateViewId();
        view.setId(viewId);

        TextView view2 = new TextView(this);
        view2.setText("Progress Towards Candidate Requirements");
        view2.setTypeface(null, Typeface.ITALIC);

        linearLayout.addView(view);
        linearLayout.addView(view2);
        layout.addView(linearLayout);

        /*
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layout.addView(view, params);


        TextView view2 = new TextView(this);
        //view2.setText("Progress Towards Candidate Requirements");
        view2.setText("B");
        view2.setTypeface(null, Typeface.ITALIC);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
        //params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, view.getId());
        layout.addView(view2, params);

        view = new TextView(this);
        view.setText("");
        layout.addView(view);

        //NOTE: No longer doing a graph!
        //Time to create the graph
        //createGraph();
        Candidate candidate = new Candidate();
        Thread thread = new Thread(candidate);
        thread.start();
        */
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

    /*
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
    */

    public void outputInfo(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.rel_layout);

                for (int i = 0; i < 14; i++) {
                    //loop through every requirement
                    String output = REQUIREMENTS[i] + ": ";
                    output += Double.toString(values[i]) + " / " + EXPECTED_RESULTS[i];

                    TextView view = new TextView(CandidatePage.this);
                    view.setText(output);
                    layout.addView(view);
                }
            }
        });
    }

    private class Candidate implements Runnable{
        @Override
        public void run() {
            outputInfo();
        }
    }
}
