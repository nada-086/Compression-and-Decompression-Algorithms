package org.example.ArithmeticCoding;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Decompression {
    private Map<Character, BigDecimal> probabilities;
    MathContext mc;

    public Decompression(int precision) {
        this.mc = new MathContext(precision, RoundingMode.HALF_UP);
    }

    private void showProb() {
        System.out.println("DECOMPRESSION");
        for (Map.Entry<Character, BigDecimal> entry : probabilities.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    private BigDecimal commutativeProb(Character c  ){
        BigDecimal val = new BigDecimal("0.0");
        for (Character curr : probabilities.keySet()){
            if( curr < c){
                val = val.add(probabilities.get(curr));
            }
        }
        return val;
    }

    public String decompress(String outputFileName) throws IOException, ClassNotFoundException {
        ObjectInputStream input = new ObjectInputStream(new FileInputStream(outputFileName));
        BigDecimal compressed = (BigDecimal) input.readObject();
        int msgLen = (int) input.readObject();
        probabilities = (Map<Character, BigDecimal>) input.readObject();
        showProb();
        input.close();

        BigDecimal low, high, range;
        BigDecimal value = compressed;

        StringBuilder sb = new StringBuilder();

        while (sb.length() < msgLen) {

            for (char symbol : probabilities.keySet()) {

                BigDecimal commutativeVal = commutativeProb(symbol);
                BigDecimal range_high = commutativeVal.add(probabilities.get(symbol));

                if (value.compareTo(commutativeVal) >= 0 && value.compareTo(range_high) < 0 ) {
                    sb.append(symbol);
                    low = commutativeVal;
                    high = range_high;
                    range = high .subtract(low) ;
                    value =  (value.subtract(low)).divide(range, mc);
                    break;
                }
            }
        }
        return sb.toString();
    }

    public void writeToFile(String fileName, String message){
        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write(message);
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
