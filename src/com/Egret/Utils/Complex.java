package com.Egret.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: steinlink
 * Date: Dec 3, 2006
 * Time: 2:20:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class Complex {

    public double re;   // the real part
    public double im;   // the imaginary part

    // create a new object with the given real and imaginary parts
    public Complex(double real, double imag) {
        this.re = real;
        this.im = imag;
    }

    // return a string representation of the invoking object
    public String toString()  { return re + " + " + im + "i"; }

    // return |this|
    public double abs() { return Math.sqrt(re*re + im*im);  }

    // return a new object whose value is (this + b)
    public Complex plus(Complex b) {
        Complex a = this;             // invoking object
        double real = a.re + b.re;
        double imag = a.im + b.im;
        Complex sum = new Complex(real, imag);
        return sum;
    }

    // return a new object whose value is (this - b)
    public Complex minus(Complex b) {
        Complex a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        Complex diff = new Complex(real, imag);
        return diff;
    }

    // return a new object whose value is (this * b)
    public Complex times(Complex b) {
        Complex a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        Complex prod = new Complex(real, imag);
        return prod;
    }

    // return a new object whose value is (this * alpha)
    public Complex times(double alpha) {
        return new Complex(alpha * re, alpha * im);
    }

    // return a new object whose value is the conjugate of this
    public Complex conjugate() {  return new Complex(re, -im); }

    // a static version of plus
    public static Complex plus(Complex a, Complex b) {
        double real = a.re + b.re;
        double imag = a.im + b.im;
        Complex sum = new Complex(real, imag);
        return sum;
    }
}