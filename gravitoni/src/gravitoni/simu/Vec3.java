package gravitoni.simu;

/*
 * To prevent unwanted garbage collection, the functions modify the object itself rather than returning a new one.
 * I don't know how much effect it has, but just to be safe...
 */
public class Vec3 {
	public double x, y, z;
	
	public Vec3() {
		x = 0;
		y = 0;
		z = 0;
	}

	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3(Vec3 other) {
		x = other.x;
		y = other.y;
		z = other.z;
	}

	public Vec3 clone() {
		return new Vec3(this);
	}
	
	public Vec3 set(Vec3 other) {
		x = other.x;
		y = other.y;
		z = other.z;
		return this;
	}

	public Vec3 add(Vec3 other) {
		x += other.x;
		y += other.y;
		z += other.z;
		return this;
	}

	public Vec3 neg() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	public Vec3 sub(Vec3 other) {
		x -= other.x;
		y -= other.y;
		z -= other.z;
		return this;
	}

	public double mul(Vec3 other) {
		return x * other.x + y * other.y + z * other.z;
	}

	public Vec3 mul(double a) {
		x *= a;
		y *= a;
		z *= a;
		return this;
	}
	
	public Vec3 unit() {
		double ilen = 1 / len();
		x *= ilen;
		y *= ilen;
		z *= ilen;
		return this;
	}
	
	public double len() {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	public String toString() {
		return x + "\t" + y + "\t" + z;
	}
}
