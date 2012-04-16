package com.sizzo.something.test;

import android.test.ActivityInstrumentationTestCase2;
import com.sizzo.something.*;

public class SomethingSizzleActivityTest extends ActivityInstrumentationTestCase2<SomethingSizzleActivity> {

    public SomethingSizzleActivityTest() {
        super(SomethingSizzleActivity.class); 
    }

    public void testActivity() {
        SomethingSizzleActivity activity = getActivity();
        assertNotNull(activity);
    }
}

