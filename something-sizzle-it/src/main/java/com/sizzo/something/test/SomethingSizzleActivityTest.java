package com.sizzo.something.test;

import android.test.ActivityInstrumentationTestCase2;
import com.sizzo.something.*;

public class SomethingSizzleActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public SomethingSizzleActivityTest() {
        super(MainActivity.class); 
    }

    public void testActivity() {
        MainActivity activity = getActivity();
        assertNotNull(activity);
    }
}

