package raytrace;

import SimServer.Robot;
import clients.RealClient;
import extras.Quat;
import javafx.geometry.Point3D;
import msgs.LaserScan;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by the following students at the University of Antwerp
 * Faculty of Applied Engineering: Electronics and ICT
 * Janssens Arthur, De Laet Jan & Verhoeven Peter.
 **/
public class RayTracer{

    //This value is supposed to be divided by -pi/4, but it generates an offset if you don't substract 0.1
    private final static double angleStartRad = Math.toRadians(-135); //-0.28;
    //private final static double angleEndRad = Math.PI-angleStartRad;
    private final static double angleDiffRad = Math.toRadians(270)/(1080);

    public static float[] rayTrace(RealClient client, LaserScan laserScan, int length){
        float[] data = new float[length];
        int cores = Runtime.getRuntime().availableProcessors();
        ArrayList<RayTraceThread> rayTraceThreads = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        ArrayList<Hit> hits = new ArrayList<>();

        //Get robot pose and direction
        if(client.ownedRobots.size() != 0)
        {
            Robot robot = client.ownedRobots.get(0);
            double position[] =  new double[]{robot.pose.getPosition().getX(),robot.pose.getPosition().getY(),robot.pose.getPosition().getZ()};
            float[] ranges = laserScan.getRanges();

            //get external robots
            List<Robot> externalRobots;
            synchronized (client.externalRobots)
            {
                externalRobots = client.externalRobots;

                double current = angleStartRad;
                double currentCarAngleRad = Quat.toEulerianAngle(robot.pose.getOrientation())[2];

                int i = 0;

                ArrayList<Segment[]> segments = new ArrayList<>();
                for(Robot r : externalRobots)
                {
                    segments.add(r.getSegments());
                }

                while (i < length) // length = amount of rays (1080)
                {
                    for(int m = 0; m<cores; m++)
                    {
                        rayTraceThreads.add(new RayTraceThread(new Point3D(position[0],position[1],position[2]), current, currentCarAngleRad+angleDiffRad*m, segments));
                        threads.add(new Thread(rayTraceThreads.get(m)));
                        threads.get(m).start();
                    }

                    try
                    {
                        for(int j = 0; j< cores; j++){
                            threads.get(j).join();
                            hits.add(rayTraceThreads.get(j).hit);
                        }

                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    for(int k = 0; k<cores; k++){
                        Hit hit = rayTraceThreads.get(k).hit;
                        if (hit != null)
                        {
                            if (hit.getTime() < ranges[i+k])
                                data[i+k] = (float) hit.getTime();
                        }
                        else
                        {
                            if(i+k+1 <= length)
                                data[i+k] = ranges[i+k];
                        }
                    }

                    current += angleDiffRad*cores;
                    i += cores;
                }
            }
        }

        //return modified array
        return data;
    }

    // Conversion Angles to X-th ray
    private static int mappingAngle(double angle)
    {
        angle += 3/4*Math.PI;
        angle = angle*180/Math.PI;
        return (int) Math.ceil(angle * 4);
    }
}


