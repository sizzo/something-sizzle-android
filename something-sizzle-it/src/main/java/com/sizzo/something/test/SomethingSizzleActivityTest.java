package com.sizzo.something.test;

import android.test.ActivityInstrumentationTestCase2;
import com.sizzo.something.*;

public class SomethingSizzleActivityTest extends ActivityInstrumentationTestCase2<WifiActivity> {

    public SomethingSizzleActivityTest() {
        super(WifiActivity.class); 
    }

    public void testActivity() {
        WifiActivity activity = getActivity();
        assertNotNull(activity);
    }
}

