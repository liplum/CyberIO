package net.liplum.utils;

import arc.math.Mathf;

import static arc.math.Angles.forwardDistance;
import static arc.math.Mathf.PI2;
import static java.lang.Math.abs;

public class MathU {

    public static float angleDistRad(float a, float b) {
        return Math.min((a - b) < 0 ? a - b + PI2 : a - b, (b - a) < 0 ? b - a + PI2 : b - a);
    }

    public static float backwardDstRad(float angle1, float angle2) {
        return PI2 - Math.abs(angle1 - angle2);
    }

    public static float moveTowardRad(float angle, float to, float speed) {
        if (abs(angleDistRad(angle, to)) < speed) return to;
        angle = Mathf.mod(angle, PI2);
        to = Mathf.mod(to, PI2);

        if (angle > to == backwardDstRad(angle, to) > forwardDistance(angle, to)) {
            angle -= speed;
        } else {
            angle += speed;
        }

        return angle;
    }

    public static int towardRad(float angle, float to) {
        angle = Mathf.mod(angle, PI2);
        to = Mathf.mod(to, PI2);

        if (angle > to == backwardDstRad(angle, to) > forwardDistance(angle, to)) {
            return -1;
        } else {
            return 1;
        }
    }
}
