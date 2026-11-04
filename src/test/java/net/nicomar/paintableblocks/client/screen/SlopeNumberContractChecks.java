package net.nicomar.paintableblocks.client.screen;

/** Prepared only; no test process is launched by the coding task. */
public final class SlopeNumberContractChecks {
    private static void check(boolean v){if(!v)throw new AssertionError();}
    private static void reject(String s){try{SlopeNumberScreen.parse(s,0,256);throw new AssertionError("Invalid number accepted: "+s);}catch(IllegalArgumentException|ArithmeticException expected){}}
    public static void run(){check(SlopeNumberScreen.parse("0,5",0,256)==8);check(SlopeNumberScreen.parse("2.75",0,256)==44);check(SlopeNumberScreen.parse("1/8",0,256)==2);check(SlopeNumberScreen.parse("16",0,256)==256);reject("1/3");reject("0.1");reject("1/0");reject("-1");reject("17");reject("NaN");}
}
