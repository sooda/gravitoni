package gravitoni.gfx;

import gravitoni.simu.Vec3;
import java.awt.*;

/**
 * Rotation "arcball" handler for rotating the 3D scene.
 * 
 * This is mostly by some "pepijn" in 2005. I adapted it to use Vec3's.
 */
class ArcBall {
    private static final float Epsilon = 1.0e-5f;

    Vec3 StVec;          //Saved click vector
    Vec3 EnVec;          //Saved drag vector
    float adjustWidth;       //Mouse bounds width
    float adjustHeight;      //Mouse bounds height

    public ArcBall(float NewWidth, float NewHeight) {
        StVec = new Vec3();
        EnVec = new Vec3();
        setBounds(NewWidth, NewHeight);
    }

    public void mapToSphere(Point point, Vec3 vector) {
        //Copy paramter into temp point
        Vec3 tempPoint = new Vec3(point.x, point.y, 0);

        //Adjust point coords and scale down to range of [-1 ... 1]
        tempPoint.x = (tempPoint.x * this.adjustWidth) - 1.0f;
        tempPoint.y = 1.0f - (tempPoint.y * this.adjustHeight);

        //Compute the square of the length of the vector to the point from the center
        double length = (tempPoint.x * tempPoint.x) + (tempPoint.y * tempPoint.y);

        //If the point is mapped outside of the sphere... (length > radius squared)
        if (length > 1.0f) {
            //Compute a normalizing factor (radius / sqrt(length))
            float norm = (float) (1.0 / Math.sqrt(length));

            //Return the "normalized" vector, a point on the sphere
            vector.x = tempPoint.x * norm;
            vector.y = tempPoint.y * norm;
            vector.z = 0.0f;
        } else    //Else it's on the inside
        {
            //Return a vector to a point mapped inside the sphere sqrt(radius squared - length)
            vector.x = tempPoint.x;
            vector.y = tempPoint.y;
            vector.z = (float) Math.sqrt(1.0f - length);
        }

    }

    public void setBounds(float NewWidth, float NewHeight) {
        assert((NewWidth > 1.0f) && (NewHeight > 1.0f));

        //Set adjustment factor for width/height
        adjustWidth = 1.0f / ((NewWidth - 1.0f) * 0.5f);
        adjustHeight = 1.0f / ((NewHeight - 1.0f) * 0.5f);
    }

    //Mouse down
    public void click(Point NewPt) {
        mapToSphere(NewPt, this.StVec);

    }

    //Mouse drag, calculate rotation
    public void drag(Point NewPt, Quat NewRot) {
        //Map the point to the sphere
        this.mapToSphere(NewPt, EnVec);

        //Return the quaternion equivalent to the rotation
        if (NewRot != null) {
            Vec3 Perp;

            //Compute the vector perpendicular to the begin and end vectors
            Perp = StVec.clone().cross(EnVec);

            //Compute the length of the perpendicular vector
            if (Perp.len() > Epsilon)    //if its non-zero
            {
                //We're ok, so return the perpendicular vector as the transform after all
                NewRot.x = Perp.x;
                NewRot.y = Perp.y;
                NewRot.z = Perp.z;
                //In the quaternion values, w is cosine (theta / 2), where theta is rotation angle
                NewRot.w = StVec.clone().dot(EnVec); // Vector3f.dot(StVec, EnVec);
            } else                                    //if its zero
            {
                //The begin and end vectors coincide, so return an identity transform
                NewRot.x = NewRot.y = NewRot.z = NewRot.w = 0.0f;
            }
        }
    }

}
