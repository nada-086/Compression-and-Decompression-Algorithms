package org.example.ArithmeticCoding;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


public class Compression {
    Map<Character, BigDecimal> probabilities;
    MathContext mc = null;
    int precision = 0;

    public Compression(Map<Character, BigDecimal> probabilities, int precision) {
        this.probabilities = probabilities;
        showProb();
        this.mc = new MathContext(precision, RoundingMode.HALF_UP);
        this.precision = precision;
    }

    private void showProb() {
        System.out.println("COMPRESSION");
        for (Map.Entry<Character, BigDecimal> entry : probabilities.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    private BigDecimal commulativeProb(Character c) {
        BigDecimal val = new BigDecimal("0.0");
        for (Character curr : probabilities.keySet()) {
            if (curr < c && curr != '$') {
                val = val.add(probabilities.get(curr));
            }
        }
        return val;
    }

    public BigDecimal compress(String message) {
        BigDecimal low = new BigDecimal("0.0");
        BigDecimal high = new BigDecimal("1.0");
        BigDecimal range = new BigDecimal("1.0");

        for (int i = 0; i < message.length(); i++) {
            Character symbol = message.charAt(i);

            BigDecimal commutativeVal = commulativeProb(symbol);
            BigDecimal range_high = commutativeVal.add(probabilities.get(symbol));

            high = (range.multiply(range_high)).add(low);
            low = (range.multiply(commutativeVal).add(low));
            range = high.subtract(low);
        }
        return (low.add(high)).divide(new BigDecimal("2.0", mc));
    }

    public void writeCompressed(String outputFileName, BigDecimal compressed, int msgLen) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(outputFileName));
        output.writeObject(compressed.setScale(precision, RoundingMode.HALF_UP));
        output.writeObject(msgLen);
        output.writeObject(probabilities);
        output.close();
    }
}