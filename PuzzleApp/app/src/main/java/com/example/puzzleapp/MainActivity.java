package com.example.puzzleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Point;
import android.icu.text.Transliterator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    public static int STATUS_BAR_HEIGHT = 24; // in dp
    public static int ACTION_BAR_HEIGHT = 56; //in dp
    private PuzzleView puzzleView;
    private Puzzle puzzle;
    private int statusBarHeight;
    private int actionBarHeight;
    private GestureDetector detector;

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        puzzle = new Puzzle();

        //getDefaultDisplay has been deprecated. Using getCurrentWindowMetrics.getBounds instead.
        int screenHeight = getWindowManager().getCurrentWindowMetrics().getBounds().height();
        int puzzleWidth = getWindowManager().getCurrentWindowMetrics().getBounds().width();

        Resources res = getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        float pixelDensity = metrics.density;

        int actionBarHeight = (int) (pixelDensity * ACTION_BAR_HEIGHT);
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize,
                tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    metrics);

        int statusBarHeight = (int) (pixelDensity * STATUS_BAR_HEIGHT);
        int resourceID =
                res.getIdentifier("status_bar_height", "dimen", "android");
        if(resourceID != 0) //found resource for status bar height
            statusBarHeight = res.getDimensionPixelSize(resourceID);

        int puzzleHeight = screenHeight - statusBarHeight - actionBarHeight;
        puzzleView = new PuzzleView(this, puzzleWidth, puzzleHeight,
                puzzle.getNumberOfParts());
        String [] scrambled = puzzle.scramble( );
        puzzleView.fillGui(scrambled);
        puzzleView.enableListener(this);
        setContentView(puzzleView);

        DoubleTapHandler dth = new DoubleTapHandler();
        detector = new GestureDetector( this, dth);
        detector.setOnDoubleTapListener(dth);

}

public boolean onTouchEvent (MotionEvent event) {
        detector.onTouchEvent(event);
        return true;
}

public boolean onTouch (View v, MotionEvent event) {
        int index = puzzleView.indexOfTextView (v);
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                //initialize data before move
                puzzleView.updateStartPositions (index, (int) event.getY());
                //bring v to front
                puzzleView.bringChildToFront( v );
                break;
            case MotionEvent.ACTION_MOVE:
                //update y position of TextView being moved
                puzzleView.moveTextViewVertically(index, (int) event.getY());
                break;
            case MotionEvent.ACTION_UP:
                //move is complete: swap the 2 TextViews
                int newPosition = puzzleView.tvPosition(index);
                puzzleView.placeTextViewAtPosition (index, newPosition);
                //if user just won, disable listener to stop the game
                if (puzzle.solved(puzzleView.currentSolution()))
                    puzzleView.disableListener();
                break;
                }
            return true;
}
    private class  DoubleTapHandler
            extends  GestureDetector.SimpleOnGestureListener {
        public boolean onDoubleTapEvent (MotionEvent event) {
            int touchY = (int) event.getRawY();
            //y coordinate of the touch within puzzleView is
            //touchY - actionBarHeight - statusBarHeight
            int index = puzzleView.indexOfTextView(touchY
                    - actionBarHeight - statusBarHeight);
            if (puzzleView.getTextViewText(index)
                    .equals(puzzle.wordToChange()))
                puzzleView.setTextViewText(index, puzzle.replacementWord());
            return true;
        }
    }
}