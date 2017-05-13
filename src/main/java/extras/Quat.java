package extras;

import edu.wpi.rail.jrosbridge.messages.geometry.Quaternion;

import static java.lang.Math.*;

/**
 * Created by arthur on 13.05.17.
 */
public class Quat{

    public static Quaternion toQuaternion(double pitch, double roll, double yaw)
    {
        double t0 = cos(yaw * 0.5);
        double t1 = sin(yaw * 0.5);
        double t2 = cos(roll * 0.5);
        double t3 = sin(roll * 0.5);
        double t4 = cos(pitch * 0.5);
        double t5 = sin(pitch * 0.5);

        return new Quaternion(t0 * t3 * t4 - t1 * t2 * t5,
                t0 * t2 * t5 + t1 * t3 * t4,
                t1 * t2 * t4 - t0 * t3 * t5,
                t0 * t2 * t4 + t1 * t3 * t5
        );
    }
    
    public static double[] toEulerianAngle(Quaternion q)
    {
        double ysqr = q.getY() * q.getY();
        double roll, pitch, yaw;

        // roll (x-axis rotation)
        double t0 = +2.0 * (q.getW() * q.getX() + q.getY() * q.getZ());
        double t1 = +1.0 - 2.0 * (q.getX() * q.getX() + ysqr);
        roll = atan2(t0, t1);

        // pitch (y-axis rotation)
        double t2 = +2.0 * (q.getW() * q.getY() - q.getZ() * q.getX());
        t2 = t2 > 1.0 ? 1.0 : t2;
        t2 = t2 < -1.0 ? -1.0 : t2;
        pitch = asin(t2);

        // yaw (z-axis rotation)
        double t3 = +2.0 * (q.getW() * q.getZ() + q.getX() * q.getY());
        double t4 = +1.0 - 2.0 * (ysqr + q.getZ() * q.getZ());
        yaw = atan2(t3, t4);
        
        return new double[]{roll, pitch, yaw};
    }
}