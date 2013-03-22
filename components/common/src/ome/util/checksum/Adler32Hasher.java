package ome.util.checksum;

import java.nio.charset.Charset;
import java.util.zip.Adler32;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;

class Adler32Hasher implements Hasher {

    private Adler32 adler32;

    public Adler32Hasher() {
        this.adler32 = new Adler32();
    }

    public HashCode hash() {
        // TODO Auto-generated method stub
        return null;
    }

    public Hasher putBoolean(boolean arg0) {
        throw new UnsupportedOperationException();
    }

    public Hasher putByte(byte arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Hasher putBytes(byte[] arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Hasher putBytes(byte[] arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public Hasher putChar(char arg0) {
        throw new UnsupportedOperationException();
    }

    public Hasher putDouble(double arg0) {
        throw new UnsupportedOperationException();
    }

    public Hasher putFloat(float arg0) {
        throw new UnsupportedOperationException();
    }

    public Hasher putInt(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Hasher putLong(long arg0) {
        throw new UnsupportedOperationException();
    }

    public <T> Hasher putObject(T arg0, Funnel<? super T> arg1) {
        throw new UnsupportedOperationException();
    }

    public Hasher putShort(short arg0) {
        throw new UnsupportedOperationException();
    }

    public Hasher putString(CharSequence arg0) {
        throw new UnsupportedOperationException();
    }

    public Hasher putString(CharSequence arg0, Charset arg1) {
        throw new UnsupportedOperationException();
    }

}