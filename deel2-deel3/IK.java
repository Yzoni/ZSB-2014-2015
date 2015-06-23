/*
 * IK.java
 * Assignment for the Inverse Kinematics part of the ZSB lab course.
 *
 * 21 juli 2015
 * Y. J. I. de Boer - 10786015
 * J. Main - 10541578
 *
 * ************************************************************
 *
 * This Java introduction was written for the 2001/2002 course.
 * Matthijs Spaan <mtjspaan@science.uva.nl>
 * $Id: Week1.java,v 1.9 2008/06/10 10:21:36 obooij Exp $
 *
 */

import java.lang.*;
import java.util.Vector;

public class IK {

    // Class containing the Denavit-Hartenberg representation of the robot arm
    private static RobotJoints robotJoints;

    /* Calculate roll, pitch, and yaw for the gripper of the robot arm plus
     * the value of the gripper itself.
     */
    private static void handJointCalculation(GripperPosition pos,
                                             JointValues j) {
        j.roll = 0;
        j.pitch = -90;
        j.yaw = 0; // Keep on 0 for simplicity
        j.grip = pos.grip;
    }

    /* Calculate the wrist coordinates from the hand coordinates.
     * If the robot's last link has some length and the tip of the robot grabs
     * the piece. At what height is the start of the robot's last link?
     */
    private static Point wristCoordinatesCalculation(GripperPosition pos) {

        Point c = new Point(pos.coords.x, pos.coords.y, pos.coords.z + robotJoints.get("roll").d);
        return (c);
    }

    /* Calculate the arm joints from the (x,y,z) coordinates of the wrist (the
     * last link).
     */
    private static void armJointCalculation(Point wristCoords,
                                            JointValues j) {

        j.zed = wristCoords.z + robotJoints.get("elbow").d + robotJoints.get("shoulder").d;

        // Robot has left handed coordinate system so switch x and y
        double x = wristCoords.y;
        double y = wristCoords.x;

        double l1 = robotJoints.get("shoulder").a;
        double l2 = robotJoints.get("elbow").a;

        double c2 = (Math.pow(x, 2) + Math.pow(y, 2) - Math.pow(l1, 2)
                - Math.pow(l2, 2)) / (2 * l1 * l2);

        double s2 = Math.sqrt(1 - Math.pow(c2, 2));

        double theta1 = Math.atan2(y, x) - Math.atan2(l2 * s2, l1 + l2 * c2);
        double theta1Deg = Math.toDegrees(theta1);

        double theta2 = Math.atan2(s2, c2);
        double theta2Deg = Math.toDegrees(theta2);

        j.shoulder = theta1Deg;
        j.elbow = theta2Deg;

    }

    /* Calculate the appropriate values for all joints for position pos.
     */
    private static JointValues jointCalculation(GripperPosition pos) {
        JointValues j = new JointValues();
        Point wristCoords;
        handJointCalculation(pos, j);
        wristCoords = wristCoordinatesCalculation(pos);
        armJointCalculation(wristCoords, j);

        return (j);
    }

    private static void inverseKinematics(Vector<GripperPosition> p, Vector<JointValues> j) {

        // initialize the Denavit-Hartenberg representation
        robotJoints = new RobotJoints();

        for (int i = 0; i < p.size(); i++) {
            GripperPosition pos = (GripperPosition) p.elementAt(i);
      /* correct for errors in the arm*/
            // if left on the board then assume left-hand configuration
            // if right on the board then assume right-hand configuration
            if (pos.coords.x < 0)
                RobotJoints.correctCartesian(pos, 0);
            else
                RobotJoints.correctCartesian(pos, 1);
            j.addElement(jointCalculation(pos));
        }
    }

    public static void main(String[] args) {
        Vector<GripperPosition> p = new Vector<GripperPosition>();
        Vector<JointValues> j = new Vector<JointValues>();

        System.out.println("**** THIS IS THE STUDENT IK MODULE IN JAVA\n");

        // read the gripper positions as produced by PP.java
        GripperPosition.read(p);

        inverseKinematics(p, j);

        //for (int i = 0; i < j.size(); i++)
        //    System.out.println((JointValues) j.get(i));

        JointValues.write(j);
    }
}