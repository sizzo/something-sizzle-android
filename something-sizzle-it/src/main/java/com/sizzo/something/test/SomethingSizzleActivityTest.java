package com.sizzo.something.test;

import android.test.ActivityInstrumentationTestCase2;
import com.sizzo.something.*;

public class SomethingSizzleActivityTest extends ActivityInstrumentationTestCase2<PeersActivity> {

    public SomethingSizzleActivityTest() {
        super(PeersActivity.class); 
    }

    public void testActivity() {
        PeersActivity activity = getActivity();
        assertNotNull(activity);
    }
}

