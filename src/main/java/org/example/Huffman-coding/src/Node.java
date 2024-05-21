public class Node {
    Node leftChild;
    Node rightChild;
    char character;
    int frequency;
    public static boolean isLeaf(Node root) {
        return root.leftChild == null && root.rightChild == null;
    }
}
