package gravitoni.gfx;

import gravitoni.simu.Vec3;

public class Mat44 {
	public double[][] data = new double[4][4];
	
	public Mat44() {
	}
	
	public Mat44(double d) {
		data[0][0] = data[1][1] = data[2][2] = data[3][3] = d;
	}
	
	public Mat44(double a, double b, double c, double d) {
		data[0][0] = a;
		data[1][1] = b;
		data[2][2] = c;
		data[3][3] = d;
	}
	
	public Mat44(Mat44 b) {
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				data[i][j] = b.data[i][j];
	}
	
	public static void main(String[] args) {
		Mat44 mat = new Mat44(1, 2, 3, 4);
		mat.data[0][1] = 1;
		System.out.println(mat);
		System.out.println(mat.mul(new Vec3(10, 100, 1000))); 
	}
	
	public Mat44 mul(Mat44 b) {
		double[][] newdata = new double[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				double sum = 0;
				for (int k = 0; k < 4; k++) {
					sum += data[i][k] * b.data[k][j];
				}
				newdata[i][j] = sum;
			}
		}
		data = newdata;
		return this;
	}
	
	public Vec3 mul(Vec3 b) {
		Vec3 v = new Vec3();
		double x = b.x, y = b.y, z = b.z;
		v.x = data[0][0] * x + data[0][1] * y + data[0][2] * z;
		v.y = data[1][0] * x + data[1][1] * y + data[1][2] * z;
		v.z = data[2][0] * x + data[2][1] * y + data[2][2] * z;
		return v;
	}
	
	public Mat44 add(Mat44 b) {
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				data[i][j] += b.data[i][j];
		return this;
	}
	
	public Mat44 sub(Mat44 b) {
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				data[i][j] -= b.data[i][j];
		return this;
	}
	
	public Mat44 mul(double a) {
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				data[i][j] *= a;
		return this;
	}
	
	public String toString() {
		String x = "";
		x += data[0][0] + "\t" + data[0][1] + "\t" + data[0][2] + "\t" + data[0][3] + "\n";
		x += data[1][0] + "\t" + data[1][1] + "\t" + data[1][2] + "\t" + data[1][3] + "\n";
		x += data[2][0] + "\t" + data[2][1] + "\t" + data[2][2] + "\t" + data[2][3] + "\n";
		x += data[3][0] + "\t" + data[3][1] + "\t" + data[3][2] + "\t" + data[3][3];
		return x;
	}
	
	public static Mat44 rx(double a) {
		Mat44 mat = new Mat44(1);
		mat.data[1][1] = Math.cos(a);
		mat.data[1][2] = -Math.sin(a);
		mat.data[2][1] = Math.sin(a);
		mat.data[2][2] = Math.cos(a);
		return mat;
	}
	public static Mat44 ry(double a) {
		Mat44 mat = new Mat44(1);
		mat.data[0][0] = Math.cos(a);
		mat.data[0][2] = Math.sin(a);
		mat.data[2][0] = -Math.sin(a);
		mat.data[2][2] = Math.cos(a);
		return mat;
	}
	public static Mat44 rz(double a) {
		Mat44 mat = new Mat44(1);
		mat.data[0][0] = Math.cos(a);
		mat.data[0][1] = -Math.sin(a);
		mat.data[1][0] = Math.sin(a);
		mat.data[1][1] = Math.cos(a);
		return mat;
	}
	public static Mat44 rxyz(double a, double b, double c) {
		Mat44 x = rx(a);
		Mat44 y = ry(a);
		Mat44 z = rz(a);
		return x.mul(y).mul(z);
	}
}
