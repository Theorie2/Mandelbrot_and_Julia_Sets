public class ZahlenfolgenRechner {
    public static final int ANZAHL_ITERATIONEN = 500;
    private final Complex juliaConstant;
    private final double exp;
    private final Complex z; // Reusable Complex object for calculations
    private final Complex current; // Reusable Complex object for Julia set

    public ZahlenfolgenRechner() {
        this.exp = 2;
        this.juliaConstant = new Complex(1, 1);
        this.z = new Complex(0, 0);
        this.current = new Complex(0, 0);
    }

    public ZahlenfolgenRechner(double exp) {
        this.exp = exp;
        this.juliaConstant = new Complex(1, 1);
        this.z = new Complex(0, 0);
        this.current = new Complex(0, 0);
    }

    public ZahlenfolgenRechner(double exp, Complex juliaConstant) {
        this.exp = exp;
        this.juliaConstant = juliaConstant;
        this.z = new Complex(0, 0);
        this.current = new Complex(0, 0);
    }

    public int zahlInMandelbrotmenge(Complex com) {
        // Reset z to 0+0i
        z.setReal(0);
        z.setImag(0);

        for (int i = 0; i < ANZAHL_ITERATIONEN; i++) {
            if (z.betragIstKleinerAls(2)) {
                z.potenzDurchIteration((int)exp).summiere(com);
            } else {
                return i;
            }
        }
        return ANZAHL_ITERATIONEN;
    }

    public int zahlInMandelbrotmengeF(Complex com) {
        // Reset z to 0+0i
        z.setReal(0);
        z.setImag(0);

        for (int i = 0; i < ANZAHL_ITERATIONEN; i++) {
            if (z.betragIstKleinerAls(2)) {
                z.potenz(exp).summiere(com);
            } else {
                return i;
            }
        }
        return ANZAHL_ITERATIONEN;
    }

    public int zahlInJuliaMenge(Complex z) {
        // Copy input values to current
        current.setReal(z.getRe());
        current.setImag(z.getIm());

        for (int i = 0; i < ANZAHL_ITERATIONEN; i++) {
            if (!current.betragIstKleinerAls(2.0)) {
                return i;
            }
            current.potenzDurchIteration((int)exp).summiere(juliaConstant);
        }

        return ANZAHL_ITERATIONEN;
    }

    public int zahlInJuliaMengeF(Complex z) {
        // Copy input values to current
        current.setReal(z.getRe());
        current.setImag(z.getIm());

        for (int i = 0; i < ANZAHL_ITERATIONEN; i++) {
            if (!current.betragIstKleinerAls(2.0)) {
                return i;
            }
            current.potenz(exp).summiere(juliaConstant);
        }

        return ANZAHL_ITERATIONEN;
    }

    public void setJuliaConstant(Complex newJuliaConstant) {
        juliaConstant.setReal(newJuliaConstant.getRe());
        juliaConstant.setImag(newJuliaConstant.getIm());
    }
}