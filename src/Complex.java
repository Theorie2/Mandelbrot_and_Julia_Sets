import java.util.concurrent.atomic.AtomicBoolean;

public class Complex {
    private double re;
    private double im;
    public Complex(double re, double im){
        this.re = re;
        this.im = im;
    }

    public void setReal(double re){
        this.re = re;
    }
    public void setImag(double im){
        this.im = im;
    }
    public Complex multipliziere(Complex c){
        double newRe = re * c.getRe() - im * c.getIm();
        double newIm = re * c.getIm() + im * c.getRe();
        re = newRe;
        im = newIm;
        return this;
    }
    public Complex summiere(Complex c){
        re += c.getRe();
        im += c.getIm();
        return this;
    }
    public Complex subtrahiere(Complex c){
        re -= c.getRe();
        im -= c.getIm();
        return this;
    }
    public Complex dividiere(Complex c) {
        double denominator = c.getRe() * c.getRe() + c.getIm() * c.getIm();
        if (denominator == 0) {
            throw new ArithmeticException("Division durch null ist nicht erlaubt.");
        }
        double real = (re * c.getRe() + im * c.getIm()) / denominator;
        double imaginary = (im * c.getRe() - re * c.getIm()) / denominator;
        re = real;
        im = imaginary;
        return this;
    }
    public Complex potenz(double exponent) {
        double r_pow = Math.pow(Math.sqrt(re * re + im * im), exponent);
        double newTheta = Math.atan2(im, re) * exponent;
        re = r_pow * Math.cos(newTheta);
        im = r_pow * Math.sin(newTheta);
        return this;
    }
    public Complex potenz(int exponent) {
        double r = Math.sqrt(re * re + im * im);
        double theta = Math.atan2(im, re);
        double r_pow = Math.exp(exponent * Math.log(r));
        double newTheta = theta * exponent;

        re = r_pow * Math.cos(newTheta);
        im = r_pow * Math.sin(newTheta);
        return this;
    }
    //Nur für Kleine Potenzen benutzen sonst sehr Uneffizient
    public Complex potenzDurchIteration(int exponent){
        Complex result = new Complex(1, 0);
        Complex base = new Complex(re, im);
        int absExponent = Math.abs(exponent);

        while (absExponent > 0) {
            if ((absExponent & 1) == 1) {
                result.multipliziere(base);
            }
            base.multipliziere(base);
            absExponent >>= 1;
        }

        if (exponent < 0) {
            result = new Complex(1, 0).dividiere(result);
        }

        this.re = result.getRe();
        this.im = result.getIm();
        /*Complex c = new Complex(re,im);
        for (int i = 0; i < exponent; i++){
            this.multipliziere(c);
        }*/
        return this;
    }
    public double getRe(){
        return re;
    }
    public double getIm(){
        return im;
    }
    public boolean betragIstKleinerAls(double c) {
        return (re * re + im * im) < (c * c);
    }

    public double getBetrag(){
        return Math.sqrt(re*re+im*im);
    }

    @Override
    public String toString() {
        return String.format("%.2f + %.2fi", re, im);
    }
}
