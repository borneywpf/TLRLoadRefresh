package com.think.tlr;

/**
 * Created by borney on 4/27/17.
 */
class Operate {
    private static final String TAG = "Operate";

    enum State {
        INITIAL,
        START,
        PROCESSING,
        COMPLETE,
        NONE
    }

    private Operate() {

    }

    public static final Operate REFREASH = new Operate() {
        @Override
        public String toString() {
            return "REFREASH";
        }
    };

    public static final Operate LOAD = new Operate() {
        @Override
        public String toString() {
            return "LOAD";
        }
    };

    public static final Operate NONE = new Operate() {
        @Override
        public String toString() {
            return "NONE";
        }
    };

    private State mState = State.NONE;

    boolean setState(State state) {
        if (mState == state) {
            return false;
        }
        mState = state;
        Log.d(toString() + " set state ==> " + state);
        return true;
    }

    public State state() {
        return mState;
    }
}
