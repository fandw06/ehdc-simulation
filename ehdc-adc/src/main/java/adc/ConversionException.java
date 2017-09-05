package adc;

/**
 * Exception for AD conversion.
 */
public class ConversionException extends RuntimeException {
    public ConversionException() {
        super();
    }

    public ConversionException(String msg) {
        super(msg);
    }
}
