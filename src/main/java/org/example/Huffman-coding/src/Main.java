import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void compress(String fileContent) {
        buildHuffmanTree(fileContent);
    }

    public static void decompress(String filename) throws IOException {
        HuffmanDecompression.decompressFile(filename);
    }

    public static String readFile(String filename) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static Node buildHuffmanTree(String str) {
        Map<Character, Integer> frequencyMap = new HashMap<>();
        char[] characters = str.toCharArray();

        for (char c : characters) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<Node> queue = new PriorityQueue<>(frequencyMap.size() + 1, new FrequencyComparator());

        frequencyMap.forEach((key, value) -> {
            Node node = new Node();
            node.character = key;
            node.frequency = value;
            node.leftChild = null;
            node.rightChild = null;
            queue.add(node);
        });

        Node root;
        while (queue.size() > 1) {
            Node n1 = queue.poll();
            Node n2 = queue.poll();
            Node sum = new Node();
            sum.character = 0;
            sum.frequency = n1.frequency + n2.frequency;
            sum.leftChild = n1;
            sum.rightChild = n2;
            queue.add(sum);
        }
        root = queue.peek();

        return root;
    }

    static class FrequencyComparator implements Comparator<Node> {
        public int compare(Node node1, Node node2) {
            return node1.frequency - node2.frequency;
        }
    }

    public static void main(String[] args) throws IOException {
        while (true) {
            String defaultInputFileName = "input.txt";
            String filename;
            Scanner scanner = new Scanner(System.in);

            System.out.println("**************************************************\n");
            System.out.println("Enter File Name :");
            filename = scanner.nextLine();

            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("File does not exist!");
                return;
            }

            System.out.println("Choose one option :");
            System.out.println("1. Compress file ");
            System.out.println("2. Decompress file ");
            System.out.println("3. Exit \n");

            long startTime;
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    String content = readFile(defaultInputFileName);
                    Node root = buildHuffmanTree(content);
                    char[] characters = content.toCharArray();
                    startTime = System.nanoTime();
                    HuffmanCompression.compressFile(root, characters);
                    compress(content);
                    long compressionTime = System.nanoTime() - startTime;
                    System.out.println("Total execution time: " + compressionTime / 1000000 + "ms");
                    long originalFileSize = new File(defaultInputFileName).length();
                    System.out.println("Original file size: " + originalFileSize + " bytes");
                    File compressedFile = new File("CompressedFile.txt");
                    long compressedFileLength = compressedFile.length();
                    System.out.println("Compressed file size: " + compressedFileLength + " bytes\n");
                    double compressionRatio = (double) originalFileSize / compressedFileLength;
                    System.out.println("Compression ratio: " + compressionRatio + "\n");
                    break;

                case 2:
                    startTime = System.nanoTime();
                    decompress(filename);
                    long decompressionTime = System.nanoTime() - startTime;
                    System.out.println("Total execution time: " + decompressionTime / 1000000 + "ms\n");
                    System.out.println("Original file size: " + new File(defaultInputFileName).length() + " bytes");
                    System.out.println("Decompressed file size: " + new File("DecompressedFile.txt").length() + " bytes");
                    break;

                case 3:
                    System.exit(0);
                    break;

                default:
                    System.err.println("Invalid input");
            }
        }
    }
}
