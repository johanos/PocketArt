package com.example.brandon.spirograph;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

public class CanvasScreen extends Activity implements AdapterView.OnItemSelectedListener {
    //large circle takes up 80% of the screen width
    final float OUTER_CIRCLE_SCREEN_RATIO = 0.4f;
    //ratio between seekbar and inner circle ratio
    final float MAX_INNER_CIRCLE_RATIO = 0.8f;
    //maximum brush size (50px)
    final float MAX_BRUSH_SIZE = 0.35f;
    //angle change
    final float THETA_STEP = (float)(Math.PI/128);

    //selecting colors and brush sizes
    private static final Integer[] colors = {
            R.drawable.colors_red,
            R.drawable.colors_orange,
            R.drawable.colors_yellow,
            R.drawable.colors_green,
            R.drawable.colors_blue,
            R.drawable.colors_purple
    };
    int brush_size = 25;
    int brush_color = colors[1];

    //used for updating DrawView
    int theta_iterations = 0;
    int last_period_index = 0;
    //used for calculations
    double dr, r, d = 0;

    enum RunStatus {Stopped, Paused, Running}
    RunStatus runStatus = RunStatus.Stopped;

    DrawView drawView;
    FrameLayout spirographPreview;
    LinearLayout canvasMenuBar;
    Button playPauseButton;

    private class ViewUpdater extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            while (true) {
                if(runStatus == RunStatus.Running) {
                    try {
                        Thread.sleep(10);
                        publishProgress();
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {}

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {
            calculateFigure(theta_iterations);
            drawView.invalidate();
            theta_iterations++;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas_screen);

        //slider for setting inner circle radius
        final SeekBar radiusSlide = (SeekBar)findViewById(R.id.gear_size_slide);

        canvasMenuBar = (LinearLayout)findViewById(R.id.canvas_menu_bar);

        playPauseButton = new Button(this);
        //playPauseButton.setText("► / ▌▌");
        playPauseButton.setBackgroundResource(R.drawable.play);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //make sure there is something to draw
                if(drawView.insideGear.drawPoints.size() > 0)
                {
                    if (runStatus == RunStatus.Stopped)
                    {
                        runStatus = RunStatus.Running;

                        radiusSlide.setEnabled(false);
                        //Start thread for updating drawView (just calls drawView.invalidate() every 100 ms or so)
                        ViewUpdater viewUpdater = new ViewUpdater();
                        viewUpdater.execute();
                    }
                    else if (runStatus == RunStatus.Paused)
                    {
                        runStatus = RunStatus.Running;
                        radiusSlide.setEnabled(false);
                    }
                    else if (runStatus == RunStatus.Running)
                    {
                        runStatus = RunStatus.Paused;
                        radiusSlide.setEnabled(true);
                    }
                }
            }
        });
        canvasMenuBar.addView(playPauseButton);

        //slider for selecting brush size
        SeekBar brushSlide = (SeekBar)findViewById(R.id.brush_size_slide);

        //spinner for selecting draw color
        Spinner colorSelector = (Spinner)findViewById(R.id.color_selector);
        colorSelector.setAdapter(new ColorSpinnerAdapter());
        colorSelector.setOnItemSelectedListener(this);

        //handle adding dots to inner circle upon tap
        spirographPreview = (FrameLayout)findViewById(R.id.spirograph_preview);
        spirographPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                drawView.nextPointsCalculated = false;
                //get the touched location on the screen
                pPoint touchPoint = new pPoint(event.getX(), event.getY());

                pPoint debugPoint = new pPoint(touchPoint.getX()-drawView.origin.getX(), -(touchPoint.getY()-drawView.origin.getY()));

                //only add a point if it's inside the inner circle
                if(pPoint.getDistance(touchPoint, drawView.insideGear.getCenter()) < drawView.insideGear.getRadius())
                {
                    Paint drawPaint = new Paint();
                    drawPaint.setColor(getColor(brush_color));
                    drawPaint.setStrokeWidth(brush_size);

                    //reset drawPoints
                    theta_iterations = 0;
                    last_period_index = 0;
                    drawView.insideGear.drawPoints.clear();
                    drawView.insideGear.drawPoints.add(touchPoint);
                    drawView.drawPaint = drawPaint;

                    //set calculation variables



                    dr = drawView.outsideRadius - r;
                    r = drawView.insideGear.getRadius();
                    d = pPoint.getDistance(touchPoint, drawView.insideGear.getCenter());
                    Log.v("inside onTouch", String.valueOf(drawView.outsideRadius) + " " + String.valueOf(dr)+ " " + String.valueOf(r) + " " + String.valueOf(d));

                    drawView.invalidate();
                }

                return false;
            }
        });


        radiusSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //resize the circle
                int percentage = (int) (10 + (MAX_INNER_CIRCLE_RATIO * progress));
                drawView.setInsideGearRadius((int) (.01 * percentage * drawView.outsideRadius));

                //clear any points drawn inside the circle
                theta_iterations = 0;
                last_period_index = 0;
                drawView.insideGear.drawPoints.clear();

                drawView.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        brushSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //resize the brush
                brush_size = (int) (5 + (MAX_BRUSH_SIZE * progress));
                drawView.drawPaint.setStrokeWidth(brush_size);

                drawView.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Point size = getScreenSize();

        drawView = new DrawView(this);
        //drawView.setBackgroundColor(Color.WHITE);
        drawView.origin = new pPoint((float)(size.x/2), (float)(size.y/2));
        drawView.showGears = true;
        drawView.outsideRadius = (int)(OUTER_CIRCLE_SCREEN_RATIO*size.x);
        drawView.setInsideGearRadius((int)(0.5* drawView.outsideRadius));

        spirographPreview.addView(drawView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_canvas_screen, menu);
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

    public int getColor(int colorId){
        int colorAsInt;

        switch(colorId) {
            case 0:
                colorAsInt = Color.RED;
                break;
            case 1:
                colorAsInt = Color.parseColor("#FF8000");
                break;
            case 2:
                colorAsInt = Color.YELLOW;
                break;
            case 3:
                colorAsInt = Color.parseColor("#196014");
                break;
            case 4:
                colorAsInt = Color.BLUE;
                break;
            case 5:
                colorAsInt = Color.parseColor("#4B0082");
                break;
            default:
                colorAsInt = Color.WHITE;
                break;
        }
        return colorAsInt;
    }

    private void calculateFigure(int drawPointIndex)
    {
        pPoint currentPoint, nextPoint;

        currentPoint = drawView.insideGear.drawPoints.get(drawPointIndex);

        nextPoint = getNextPoint(currentPoint);

        //check for start of last period
        if(theta_iterations*THETA_STEP == 2*Math.PI)
        {
            nextPoint.setX(nextPoint.getX() + drawView.insideGear.drawPoints.get(last_period_index).getX());
            nextPoint.setY(nextPoint.getY() + drawView.insideGear.drawPoints.get(last_period_index).getY());

            last_period_index = drawView.insideGear.drawPoints.size() - 1;
        }
        drawView.insideGear.drawPoints.add(nextPoint);

        Log.v("on this one" , "Current Point = (" + currentPoint.getX() + ", " + currentPoint.getY() + ")\nNext Point = (" + nextPoint.getX() + ", " + nextPoint.getY());
        drawView.nextPointsCalculated = true;
    }

    private pPoint getNextPoint(pPoint point)
    {
        //double d = pPoint.getDistance(point, drawView.insideGear.getCenter());
        //transform around screen's origin (top left corner) for calculations
        point.setX(point.getX() - drawView.origin.getX());
        point.setY(-(point.getY() - drawView.origin.getY()));
        drawView.insideGear.getCenter().setX(drawView.insideGear.getCenter().getX() - drawView.origin.getX());
        drawView.insideGear.getCenter().setY(-(drawView.insideGear.getCenter().getY() - drawView.origin.getY()));

        double t = drawView.insideGear.getCenter().getTheta() + THETA_STEP;
        drawView.insideGear.getCenter().setTheta(t);

        //force positive angle
        if(t < 0)
            t += 2*Math.PI;

        double x =  drawView.origin.getX() + (dr*Math.cos(t) + d*Math.cos((dr / r) * t));
        double y =  drawView.origin.getY() - (dr*Math.sin(t) + d*Math.sin((dr / r) * t));

        //transform back to drawView's origin
        point.setX(point.getX() + drawView.origin.getX());
        point.setY(-point.getY() + drawView.origin.getY());
        drawView.insideGear.getCenter().setX(drawView.insideGear.getCenter().getX() + drawView.origin.getX());
        drawView.insideGear.getCenter().setY(-drawView.insideGear.getCenter().getY() + drawView.origin.getY());

        return new pPoint(x, y);
    }

    //returns the screen size as a point (x and y pixel coordinates of bottom right corner)
    private Point getScreenSize()
    {
        Point size = new Point();
        WindowManager wm = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getSize(size);

        return size;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        brush_color = position;
        drawView.drawPaint.setColor(getColor(brush_color));
        drawView.invalidate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //nothing selected
    }

    private static class ViewHolder {
        ImageView colorImageView;
    }

    private class ColorSpinnerAdapter extends BaseAdapter {

        public int getCount() {
            return colors.length;
        }

        @Override
        public Integer getItem(int position) {
            return colors[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            ViewHolder colorViewHolder;
            if (convertView == null){
                itemView = getLayoutInflater().inflate(R.layout.row, parent, false);
                colorViewHolder = new ViewHolder();
                colorViewHolder.colorImageView = (ImageView) itemView.findViewById(R.id.spinnerImage);
                itemView.setTag(colorViewHolder);
            } else {
                colorViewHolder = (ViewHolder) itemView.getTag();
            }

            colorViewHolder.colorImageView
                    .setImageDrawable(getResources()
                            .getDrawable(colors[position]));
            return itemView;
        }
    }
}
