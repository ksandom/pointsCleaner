package com.funnyhacks.sensors2;

/**
 * Created by ksandom on 24/11/16.
 */
public class PointsCleaner {
    private float lowPassFilterThreshold1; // tunable: (radians) How big the delta must be from the last accepted value. (filter 1)
    private float lowPassFilterThreshold2; // tunable: (radians) How big the delta must be from the last accepted value. (filter 2)
    private float percentOfDeltasMustMatch; // tunable: (percentage 0-1) How many deltas must be in the same direction.
    private int howManyDeltasMustMatch; // derived: from percentOfDeltasMustMatch How many deltas must be in the same direction.
    private float reactionSpeed; // tunable: (percentage 0-1) How much of the average, of the agreeing deltas to apply.
    private int howManyDeltas; // tunable: Use this change the length of the window. Shorter makes it more reactive. Longer makes it more steady.
    private int deltaPosition; // Where we are currently working in the recent deltas.
    private float[] recentDeltas; // The most recent changes.
    private float lastValue; // Last accepted value.
    private float newValue; // New value to be accepted or ignored.

    public PointsCleaner(int deltas) { // tunable: Use howManyDeltas to change the length of the window. Shorter makes it more reactive. Longer makes it more steady.

        // Set the number of deltas
        howManyDeltas = deltas;

        // tunable: (radians) How big the delta must be from the last accepted value. (filter 1)
        setThreshold1((float)0.15);

        // tunable: (radians) How big the delta must be from the last accepted value. (filter 2)
        setThreshold2((float)0.17);

        // tunable: (percentage 0-1) How many deltas must be in the same direction.
        setPercentOfDeltasMustMatch((float)0.98);

        // tunable: (percentage 0-1) How much of the average, of the agreeing deltas to apply.
        setReactionSpeed((float)0.2);


        // internal: Where are we up to?
        deltaPosition = 0;

        // internal: Delta tracking
        recentDeltas = new float[howManyDeltas];

        // Work out how many deltas must match.
        setHowManyDeltasMustMatch((int)percentOfDeltasMustMatch * howManyDeltas);


        lastValue = 0; // Last accepted value.
        newValue = 0; // New value to be accepted or ignored.

    }

    public void setThreshold1(float threshold1) { // Filter1: How much the value must change before it will be considered a change.
        lowPassFilterThreshold1 = threshold1;
    }

    public void setThreshold2(float threshold2) { // Filter1: How much the value must change before it will be considered a change.
        lowPassFilterThreshold2 = threshold2;
    }

    public void setPercentOfDeltasMustMatch(float percentage) {
        percentOfDeltasMustMatch = percentage;
    }

    private void setHowManyDeltasMustMatch(int howMany) {
        howManyDeltasMustMatch = howMany;
    }

    public void setReactionSpeed(float speed) {
        reactionSpeed = speed;
    }


    // protected float basicLowPassFilter(float lastSavedValue, float currentValue, float threshold) {
    //     float delta=currentValue-lastSavedValue;
    //     if (Math.abs(delta) > threshold) {
    //         return currentValue;
    //     }
    //     else {
    //         return lastSavedValue;
    //     }
    // }

    public float processPoint(float newValue) {
        float delta=newValue-lastValue;

        // Is the change big enough that we just let it through
        if (Math.abs(delta) > lowPassFilterThreshold1) {
            /*
            * Note that we do not do any statistics of the data that is big enough to just let it through, because we don't want it to add noise to the stable state.
            * */

            lastValue=newValue;
        }
        else {
            // Move the position in the delta buffer.
            deltaPosition ++;
            deltaPosition = deltaPosition%howManyDeltas;

            // Assign delta
            recentDeltas[deltaPosition]=delta;

            // Find polarity
            if (delta > 0) {
                final int polarity = 1;
            }
            else {
                final int polarity = -1;
            }

            // Count deltas with matching polarity
            int matches = 0;
            for (int i=0; i<howManyDeltas; i++) {
                if ( ! ((delta > 0) ^ (recentDeltas[i] > 0))) {
                    matches ++;
                }
            }

            // Do we have enough matches?
            if (matches >= howManyDeltasMustMatch) {
                // Average only the deltas matching the current polarity so we can give a more realistic average.
                float totalOfMatchingDeltas = 0;
                for (int j=0; j<howManyDeltas; j++) {
                    if ( ! ((delta > 0) ^ (recentDeltas[j] > 0))) {
                        totalOfMatchingDeltas += recentDeltas[j];
                    }
                }
                float averageOfMatchingDeltas = totalOfMatchingDeltas/matches;

                // Calculate how much change we actually want to apply.
                float finalDelta = averageOfMatchingDeltas * reactionSpeed;

                if (Math.abs(finalDelta) > lowPassFilterThreshold2) {
                    // Apply new delta to output
                    lastValue = lastValue + finalDelta;
                }
            }
        }

        return lastValue;
    }
}
