package org.firstinspires.ftc.teamcode;

class Singleton {
    double currentHeading;

    public double getHeading() {
        return currentHeading;
    }

    public void setHeading(double heading) {
        currentHeading = heading;
    }
}
public class ContextSingleton {
    static Singleton obj= new Singleton();

    static public Singleton getContext() {
        return obj;
    }

}
