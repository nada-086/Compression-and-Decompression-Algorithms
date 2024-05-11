package LZW;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LZW {
    public static List<Integer> compress(String uncompressed) {
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }

        String current = "";
        List<Integer> result = new ArrayList<>();
        for (char c : uncompressed.toCharArray()) {
            String combined = current + c;
            if (dictionary.containsKey(combined)) {
                current = combined;
            } else {
                result.add(dictionary.get(current));
                dictionary.put(combined, dictionary.size());
                current = "" + c;
            }
        }

        if (!current.equals("")) {
            result.add(dictionary.get(current));
        }

        return result;
    }

    public static String decompress(List<Integer> compressed) {
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }

        StringBuilder result = new StringBuilder();
        int oldCode = compressed.get(0);
        String current = dictionary.get(oldCode);
        result.append(current);
        for (int i = 1; i < compressed.size(); i++) {
            int code = compressed.get(i);
            String entry;
            if (dictionary.containsKey(code)) {
                entry = dictionary.get(code);
            } else if (code == dictionary.size()) {
                entry = current + current.charAt(0);
            } else {
                throw new IllegalArgumentException("Bad compressed code: " + code);
            }

            result.append(entry);
            dictionary.put(dictionary.size(), current + entry.charAt(0));
            current = entry;
        }
        return result.toString();
    }

    public static void compressFile(String inputFile, String outputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFile))) {

            StringBuilder uncompressed = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                uncompressed.append(line).append("\r\n");
            }

            List<Integer> compressed = compress(uncompressed.toString());
            for (Integer value : compressed) {
                outputStream.writeInt(value);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decompressFile(String inputFile, String outputFile) {

        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            List<Integer> compressed = new ArrayList<>();
            while (inputStream.available() > 0) {
                compressed.add(inputStream.readInt());
            }

            String decompressed = decompress(compressed);
            // Remove last line
            int lastNewLineIndex = decompressed.lastIndexOf("\r\n");
            if (lastNewLineIndex != -1) {
                decompressed = decompressed.substring(0, lastNewLineIndex);
            }

            writer.write(decompressed);


        } catch (IOException e) {
            e.printStackTrace();
        }
        // Ensure writer is closed even if an exception occurs

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
        String inputFilePath = "input.txt";
        String compressedFilePath = "compressed.bin";
        String outputFilePath = "output.txt";

        System.out.println("Start compression ...");
        // Compress input file
        compressFile(inputFilePath, compressedFilePath);
        System.out.println("Compressing " + inputFilePath + " is Done successfully");

        System.out.println("Start decompression ...");
        // Decompress compressed file
        decompressFile(compressedFilePath, outputFilePath);
        System.out.println("Decompressing " + compressedFilePath + " is Done successfully");


        /////////////////////////////////////


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


        /////////////////////////////////////////////////////

        if (!originalFileHash.equals(decodedFileHash)) {
            throw new AssertionError("Hash of decoded file is not the same as the original");
        }
        System.out.println("File hashes match");
        System.out.println("Decompressed file matches original");

    }
}