package rtg.util;

/**
 * @author KdotJPG
 *
 * Generates 2D Simplex-cellular noise.
 *
 * Simplex-cellular noise is cellular noise implemented using the lattice
 * of Simplex noise.
 *
 * In this case the point contribution determination is implemented using
 * a lookup scheme inspired by DigitalShadow's optimized implementation of
 * OpenSimplex noise, and a permutation table of size 1024 is used instead
 * of the traditional 256.
 *
 * Each point set is defined as the directions from the center to the
 * vertices of the normalized expanded vertex figure of the lattice for
 * the given dimensionality. These point sets are symmetric with the
 * lattice, but don't include directions that follow edges or facets.
 *
 * Supports multi-evaluation with F1 and F2 values.
 *
 * Version 02/05/2015
 */

public class SimplexCellularOctave {

	private short[] perm;
	private short[] perm2D;
    private int f1Index = 0;
    private int f2Index =1;

	public SimplexCellularOctave(long seed) {
		perm = new short[1024];
		perm2D = new short[1024];
		short[] source = new short[1024];
		for (short i = 0; i < 1024; i++)
			source[i] = i;
		for (int i = 1023; i >= 0; i--) {
			seed = seed * 6364136223846793005L + 1442695040888963407L;
			int r = (int)((seed + 31) % (i + 1));
			if (r < 0)
				r += (i + 1);
			perm[i] = source[r];
			perm2D[i] = (short)((perm[i] % 12) * 2);
			source[r] = source[i];
		}
	}

	/*
	 * 2D multi-instance evaluation function
	 */

	//2D Simplex-Cellular noise (Multi-eval)
	public double [] eval(double x, double y) {

        double [] results = new double[2];
        results[0] = Double.POSITIVE_INFINITY;
        results[1] = Double.POSITIVE_INFINITY;

		//Get points for A2* lattice
		double s = 0.366025403784439 * (x + y);
		double xs = x + s, ys = y + s;


		//Get base points and offsets
		int xsb = fastFloor(xs), ysb = fastFloor(ys);
		double xsi = xs - xsb, ysi = ys - ysb;

		//Index to point list
		int index =
			((int)(xsi + ysi) * 9) +
			((int)(xsi * 2 - ysi + 1) * 9 * 2) +
			((int)(ysi * 2 - xsi + 1) * 9 * 6);

		//Offsets in input space
		double ssi = (xsi + ysi) * -0.211324865405187;
		double xi = xsi + ssi, yi = ysi + ssi;

		//Point contributions
		for (int i = 0; i < 9; i++) {
        LatticePoint2D c = LOOKUP_2D[index + i];

        int pxm = (xsb + c.xsv) & 1023, pym = (ysb + c.ysv) & 1023;
            int ji = perm2D[perm[pxm] ^ pym];
            double jx = JITTER_2D[ji + 0], jy = JITTER_2D[ji + 1];
            double djx = jx - (c.dx + xi),
                    djy = jy - (c.dy + yi);
            double distance = Math.sqrt(djx * djx + djy * djy);

            if (f2Index >= 0) {
                if (distance < results[f2Index]) {
                    results[f2Index] = distance;
                    if (distance < results[f1Index]) {
                        results[f2Index] = results[f1Index];
                        results[f1Index] = distance;
                    }
                }
            } else if (f1Index >= 0) {
                if (distance < results[f1Index]) {
                    results[f1Index] = distance;
                }
            }
		}
        return results;
	}

	/*
	 * Init functions
	 */

	public static double[] initResultArray(NoiseInstance2[] instances) {
		int max = 0;
		for (NoiseInstance2 instance : instances) {
			if (instance.f1Index > max) max = instance.f1Index;
			if (instance.f2Index > max) max = instance.f2Index;
		}
		double[] destination = new double[max + 1];
		return destination;
	}

	public static void resetResultArray(NoiseInstance2[] instances, double[] results) {
		for (NoiseInstance2 instance : instances) {
			if (instance.f1Index >= 0) {
				results[instance.f1Index] = Double.POSITIVE_INFINITY;
			}
			if (instance.f2Index >= 0) {
				results[instance.f2Index] = Double.POSITIVE_INFINITY;
			}
		}
	}

	/*
	 * Utility
	 */

	private static int fastFloor(double x) {
		int xi = (int)x;
		return x < xi ? xi - 1 : xi;
	}

	/*
	 * Definitions
	 */

	private static final LatticePoint2D[] LOOKUP_2D;
	static {
		LOOKUP_2D = new LatticePoint2D[18 * 9];

		for (int i = 0; i < 18; i++) {
			int i1, j1, i2, j2, i3, j3, i4, j4, i5, j5;
			int a = (i & 1);
			int b = (i / 2) % 3;
			int c = (i / 6) % 3;
			if (a == 0) { i1 = -1; j1 = -1; } else { i1 = 2; j1 = 2; }
			if (b < 2) { i2 = -1; j2 = 0; } else { i2 = 2; j2 = 0; }
			if (b < 1) { i3 = -1; j3 = 1; } else { i3 = 2; j3 = 1; }
			if (c < 2) { i4 = 0; j4 = -1; } else { i4 = 0; j4 = 2; }
			if (c < 1) { i5 = 1; j5 = -1; } else { i5 = 1; j5 = 2; }
			LOOKUP_2D[i * 9 + 0] = new LatticePoint2D(0, 0);
			LOOKUP_2D[i * 9 + 1] = new LatticePoint2D(1, 0);
			LOOKUP_2D[i * 9 + 2] = new LatticePoint2D(0, 1);
			LOOKUP_2D[i * 9 + 3] = new LatticePoint2D(1, 1);
			LOOKUP_2D[i * 9 + 4] = new LatticePoint2D(i1, j1);
			LOOKUP_2D[i * 9 + 5] = new LatticePoint2D(i2, j2);
			LOOKUP_2D[i * 9 + 6] = new LatticePoint2D(i3, j3);
			LOOKUP_2D[i * 9 + 7] = new LatticePoint2D(i4, j4);
			LOOKUP_2D[i * 9 + 8] = new LatticePoint2D(i5, j5);
		}
	}

	//2D Points: Dodecagon
	private static final double[] JITTER_2D = new double[] {
		                 0,		 0.408248290463863,
		 0.204124145231932,		 0.353553390593274,
		 0.353553390593274,		 0.204124145231932,
		 0.408248290463863,		                 0,
		 0.353553390593274,		-0.204124145231932,
		 0.204124145231932,		-0.353553390593274,
		                 0,		-0.408248290463863,
		-0.204124145231932,		-0.353553390593274,
		-0.353553390593274,		-0.204124145231932,
		-0.408248290463863,		                 0,
		-0.353553390593274,		 0.204124145231932,
		-0.204124145231932,		 0.353553390593274
	};

    public float noise(double x, double z, double depth) {
        return (float)eval(x,z)[0];
    }

	private static class LatticePoint2D {
		public int xsv, ysv;
		public double dx, dy;
		public LatticePoint2D(int xsv, int ysv) {
			this.xsv = xsv; this.ysv = ysv;
			double ssv = (xsv + ysv) * -0.211324865405187;
			this.dx = -xsv - ssv;
			this.dy = -ysv - ssv;
		}
	}

	public static class NoiseInstance2 {
		public NoiseInstance2(SimplexCellularOctave noise, int f1Index,
				int f2Index) {
			this.noise = noise;
			this.f1Index = f1Index;
			this.f2Index = f2Index;
		}
		public SimplexCellularOctave noise;
		public int f1Index;
		public int f2Index;
	}
}