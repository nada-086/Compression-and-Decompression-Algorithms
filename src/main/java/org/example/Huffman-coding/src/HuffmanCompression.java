import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HuffmanCompression {

    public static void encodeHuffmanTree(Node root, String code, Map<Character, String> encodings) {
        if (root == null) {
            return;
        }

        if (Node.isLeaf(root)) {
            if (!code.isEmpty()) {
                encodings.put(root.character, code);
            } else {
                encodings.put(root.character, "1");
            }
        }

        encodeHuffmanTree(root.leftChild, code + '0', encodings);
        encodeHuffmanTree(root.rightChild, code + '1', encodings);
    }

    public static void compressFile(Node root, char[] characters) throws IOException {
        Map<Character, String> huffmanCodes = new HashMap<>();
        encodeHuffmanTree(root, "", huffmanCodes);

        System.out.println("\n**Huffman codes**");
        for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

        StringBuilder encodedString = new StringBuilder();
        for (char c : characters) {
            encodedString.append(huffmanCodes.get(c));
        }

        int zeroPaddingNum = 0;
        if ((encodedString.length() % 8) != 0) {
            zeroPaddingNum = 8 - (encodedString.length() % 8);
            for (int i = 0; i < zeroPaddingNum; i++) {
                encodedString.append("0");
            }
        }

        StringBuilder ascii = new StringBuilder();
        for (int j = 0; j < encodedString.length() / 8; j++) {
            int binary = Integer.parseInt(encodedString.substring(8 * j, (j + 1) * 8), 2);
            char character = (char) binary;
            ascii.append(character);
        }

        writeCompressedFile(huffmanCodes, zeroPaddingNum, ascii);
    }

    public static void writeCompressedFile(Map<Character, String> huffmanCode, int padding, StringBuilder ascii) throws IOException {
        File file = new File("CompressedFile.txt");
        if (!file.exists()) {
            file.createNewFile();
        }

        Writer outputStream = new OutputStreamWriter(new FileOutputStream(file.getName(), false), "ISO_8859_1");
        outputStream.write(padding + "\n");
        outputStream.write(huffmanCode.size() + "\n");

        for (Map.Entry<Character, String> entry : huffmanCode.entrySet()) {
            outputStream.write(entry.getKey() + entry.getValue() + "\n");
        }

        outputStream.write(String.valueOf(ascii));
        outputStream.flush();
        outputStream.close();
    }
}
