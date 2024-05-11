package org.example.ArithmeticCoding;

import java.io.*;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.math.BigDecimal;

public class Main {
    public static final int PRECISION = 10500;
    public static  String INPUT_FILE = "input.txt";
    public static final String COMPRESSED_FILE = "compressed.dat";
    public static final String OUTPUT_FILE = "decompressed.txt";

    private static String readFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
    private static Map<Character, BigDecimal> estimateProbabilities(String message){
        Hashtable<Character,Integer> alphaCount = new Hashtable<>();
        for(int i = 0 ;i < message.length();i++){
            Character c = message.charAt(i);
            if(alphaCount.containsKey(c))
                alphaCount.put(c,alphaCount.get(c)+1);
            else alphaCount.put(c,1);
        }
        double msgLength = message.length()+ 0.0;
        Map<Character,BigDecimal> prob = new Hashtable<>();
        for(Character c : alphaCount.keySet()){
            prob.put(c,new BigDecimal(alphaCount.get(c)/msgLength));
        }
        return prob;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String message = readFile(INPUT_FILE);
        Map<Character, BigDecimal> symbolProbabilities = estimateProbabilities(message);

        Compression compression = new Compression(symbolProbabilities,PRECISION);
        BigDecimal compressed = compression.compress(message);
        System.out.println("Got the compression ready");
        System.out.println("______________________________________________________________");
        compression.writeCompressed(COMPRESSED_FILE, compressed,message.length());

        Decompression decompression = new Decompression(PRECISION);
        String decoded = decompression.decompress(COMPRESSED_FILE);
        System.out.println("Got the decompression ready");
        System.out.println("______________________________________________________________");
        decompression. writeToFile(OUTPUT_FILE,decoded);

        long inBytes = Files.size(Paths.get(INPUT_FILE));
        long outBytes = Files.size(Paths.get(COMPRESSED_FILE));
        System.out.println("Input file size in bytes "+inBytes);
        System.out.println("Compressed file size in bytes "+outBytes);
        System.out.println("______________________________________________________________");

        double compressionRatio = inBytes/(outBytes+0.0);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        String formattedComRatio = decimalFormat.format(compressionRatio);
        System.out.println("Compression ratio "+ formattedComRatio);
        System.out.println("This means that the compressed file is "+formattedComRatio+" times smaller than the original file.");
        System.out.println("______________________________________________________________");

    }
}