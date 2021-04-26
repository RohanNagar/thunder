package com.sanctionco.thunder.util;

import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

public class FileUtilities {

  /**
   * Reads a file from the resources folder.
   *
   * @param fileName the name of the file
   * @return the file's contents
   * @throws IllegalStateException if the file was not found or there was an error reading the file
   */
  public static String readFileAsResources(String fileName) {
    try {
      return Resources.toString(Resources.getResource(fileName), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Error reading file from resources folder", e);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Default file not found in resources folder", e);
    }
  }

  /**
   * Reads an RSAPublicKey from a file at the given path. The file must be
   * in DER format.
   *
   * @param path the path of the file to read from
   * @return a new {@link RSAPublicKey} instance containing the value in the file
   * @throws RuntimeException if unable to read from the file
   */
  public static RSAPublicKey readPublicKeyFromPath(String path) {
    try {
      byte[] keyBytes = Files.readAllBytes(Path.of(path));

      X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) kf.generatePublic(spec);
    } catch (Exception e) {
      throw new RuntimeException("Unable to read RSA public key from path " + path, e);
    }
  }
}
