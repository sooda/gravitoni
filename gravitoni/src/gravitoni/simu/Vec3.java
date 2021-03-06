package gravitoni.simu;

/** A 3D vector with basic operations. The operations modify this object itself and return a reference to this. */
public class Vec3 {
	public double x, y, z;
	
	/** Construct a zero vector */
	public Vec3() {
		x = 0;
		y = 0;
		z = 0;
	}

	/** Construct a vector with the given coordinates */
	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/** Copy constructor */
	public Vec3(Vec3 other) {
		x = other.x;
		y = other.y;
		z = other.z;
	}

	/** Clone this */
	public Vec3 clone() {
		return new Vec3(this);
	}
	
	/** Assign this to the same as other
	 * @return this */
	public Vec3 set(Vec3 other) {
		x = other.x;
		y = other.y;
		z = other.z;
		return this;
	}

	/** Add other to this
	 * @return this */
	public Vec3 add(Vec3 other) {
		x += other.x;
		y += other.y;
		z += other.z;
		return this;
	}

	/** Negate
	 * @return this */
	public Vec3 neg() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	/** Subtract other from this
	 * @return this */
	public Vec3 sub(Vec3 other) {
		x -= other.x;
		y -= other.y;
		z -= other.z;
		return this;
	}

	/** Dot product */
	public double dot(Vec3 other) {
		return x * other.x + y * other.y + z * other.z;
	}
	
	/** Cross product with other.
	 * @return this
	 */
	public Vec3 cross(Vec3 other) {
		double x = this.y * other.z - this.z * other.y;
		double y = this.z * other.x - this.x * other.z;
		double z = this.x * other.y - this.y * other.x;
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/** Multiply this by a
	 * @return this */
	public Vec3 mul(double a) {
		x *= a;
		y *= a;
		z *= a;
		return this;
	}
	
	/** Make this a unit vector
	 * @return this */
	public Vec3 unit() {
		double ilen = 1 / len();
		x *= ilen;
		y *= ilen;
		z *= ilen;
		return this;
	}
	
	/** Get the length of this vector */
	public double len() {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	/** Print it out */
	public String toString() {
		return x + "\t" + y + "\t" + z;
	}
	
	/** Get one component of this vector
	 * 
	 * @param idx the id to return
	 * @return idx==0?x, idx==1?y, idx==2?z
	 */
	public double component(int idx) {
		switch (idx) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		default:
			return z;
		}
	}
	
	/** Parse str and try to find a Vec3 in the form x,y,z */
	public static Vec3 parse(String str) throws NumberFormatException {
		String[] parts = str.split(" *, *");
		if (parts.length != 3) throw new NumberFormatException("Bad string for vector: " + str);
		return new Vec3(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
	}
}
