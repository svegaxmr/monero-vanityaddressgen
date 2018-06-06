package com.svega.vanitygen.ge;

public interface Sha512 {

  public void calculateDigest(byte[] out, byte[] in, long length);

}
