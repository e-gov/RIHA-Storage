package ee.eesti.riha.rest.logic.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Calculates {@link InputStream} size while it is being read.
 */
public class LengthCalculatingInputStream extends FilterInputStream {

    private long length = 0;

    public LengthCalculatingInputStream(InputStream in) {
        super(in);
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Override
    public int read() throws IOException {
        int result = super.read();
        if (result != -1) {
            this.length += 1;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result != -1) {
            this.length += result;
        }
        return result;
    }

}
