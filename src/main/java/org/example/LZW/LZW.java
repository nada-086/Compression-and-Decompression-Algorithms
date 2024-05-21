package org.example.LZW;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LZW {

    private static final int BYTE_SIZE = 3; // Number of bytes used to store dictionary codes

    public static byte[] compress(byte[] uncompressed) throws IOException {
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }

        int dictSize = 256;
        StringBuilder w = new StringBuilder();
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        for (byte b : uncompressed) {
            char c = (char) (b & 0xFF);
            w.append(c);

            if (!dictionary.containsKey(w.toString())) {
                w.setLength(w.length() - 1);
                result.write(toBytes(dictionary.get(w.toString()), BYTE_SIZE));
                if (dictSize < (1 << (BYTE_SIZE * 8))) {
                    dictionary.put(w.toString() + c, dictSize++);
                }
                w.setLength(0);
                w.append(c);
            }
        }

        if (w.length() > 0) {
            result.write(toBytes(dictionary.get(w.toString()), BYTE_SIZE));
        }

        return result.toByteArray();
    }

    public static byte[] decompress(byte[] compressed) throws IOException {
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }

        int dictSize = 256;
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int index = 0;
        int prevCode = toInt(compressed, index, BYTE_SIZE);
        index += BYTE_SIZE;
        result.write(dictionary.get(prevCode).getBytes());

        while (index < compressed.length) {
            int currCode = toInt(compressed, index, BYTE_SIZE);
            index += BYTE_SIZE;

            String entry;
            if (dictionary.containsKey(currCode)) {
                entry = dictionary.get(currCode);
            } else if (currCode == dictSize) {
                entry = dictionary.get(prevCode) + dictionary.get(prevCode).charAt(0);
            } else {
                throw new IllegalArgumentException("Bad compressed code: " + currCode);
            }

            result.write(entry.getBytes());

            if (dictSize < (1 << (BYTE_SIZE * 8))) {
                dictionary.put(dictSize++, dictionary.get(prevCode) + entry.charAt(0));
            }

            prevCode = currCode;
        }

        return result.toByteArray();
    }

    private static byte[] toBytes(int value, int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[size - i - 1] = (byte) (value >>> (i * 8));
        }
        return result;
    }

    private static int toInt(byte[] bytes, int offset, int length) {
        int result = 0;
        for (int i = 0; i < length; i++) {
            result = (result << 8) | (bytes[offset + i] & 0xFF);
        }
        return result;
    }

    public static void compressFile(String inputFile, String outputFile) {
        try (InputStream inputStream = new FileInputStream(inputFile);
             OutputStream outputStream = new FileOutputStream(outputFile)) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            byte[] compressed = compress(byteArrayOutputStream.toByteArray());
            outputStream.write(compressed);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decompressFile(String inputFile, String outputFile) {
        try (InputStream inputStream = new FileInputStream(inputFile);
             OutputStream outputStream = new FileOutputStream(outputFile)) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            byte[] decompressed = decompress(byteArrayOutputStream.toByteArray());
            outputStream.write(decompressed);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String calculateFileHash(String filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] bytesBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }
        }
        byte[] hashedBytes = digest.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for (byte hashedByte : hashedBytes) {
            stringBuilder.append(Integer.toString((hashedByte & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String inputFilePath = "file";
        String compressedFilePath = "comp.bin";
        String outputFilePath = "out.txt";

        System.out.println("Start compression ...");
        long compressStartTime = System.nanoTime();
        compressFile(inputFilePath, compressedFilePath);
        long compressEndTime = System.nanoTime();
        System.out.println("Compressing " + inputFilePath + " is done successfully");

        System.out.println("Start decompression ...");
        long decompressStartTime = System.nanoTime();
        decompressFile(compressedFilePath, outputFilePath);
        long decompressEndTime = System.nanoTime();
        System.out.println("Decompressing " + compressedFilePath + " is done successfully");

        long compressDuration = compressEndTime - compressStartTime;
        long decompressDuration = decompressEndTime - decompressStartTime;

        System.out.println("Compression time: " + compressDuration / 1_000_000.0 + " ms");
        System.out.println("Decompression time: " + decompressDuration / 1_000_000.0 + " ms");

        long originalFileSize = Files.size(Paths.get(inputFilePath));
        long encodedFileSize = Files.size(Paths.get(compressedFilePath));
        long decodedFileSize = Files.size(Paths.get(outputFilePath));

        System.out.println("Original file size: " + originalFileSize + " bytes");
        System.out.println("Encoded file size: " + encodedFileSize + " bytes");
        System.out.println("Decoded file size: " + decodedFileSize + " bytes");

        double compressionRatio = (double) originalFileSize / encodedFileSize;
        System.out.println("Compression ratio: " + compressionRatio);

        String originalFileHash = calculateFileHash(inputFilePath);
        String decodedFileHash = calculateFileHash(outputFilePath);

        if (!originalFileHash.equals(decodedFileHash)) {
            throw new AssertionError("Hash of decoded file is not the same as the original");
        }
        System.out.println("File hashes match");
        System.out.println("Decompressed file matches original");
    }
}
