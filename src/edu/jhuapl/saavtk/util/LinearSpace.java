package edu.jhuapl.saavtk.util;

public class LinearSpace
{
	static public double[] create(double a, double b, int n)
	{
		double[] arr=new double[n];
		for (int i=0; i<n; i++)
			arr[i]=(double)i/(double)(n-1)*(b-a)+a;
		return arr;
	}
	
	static public double[] createTruncated(double a, double b, int n)	// use i/n [instead of i/(n-1)] to generate values from a to a+(b-a)*(n-1)/n [instead of a to b]
	{
		double[] arr=new double[n];
		for (int i=0; i<n; i++)
			arr[i]=(double)i/(double)n*(b-a)+a;	// here is the difference with the plain create() method; the values here almost reach b but not quite, due to division by n instead of n+1
		return arr;		
	}
}
