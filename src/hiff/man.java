package hiff;
import java.util.*;
import java.io.*;

public class man {
    static class Node implements Comparable<Node> {
        char ch;
        int freq;
        Node left, right;
        Node(char ch, int freq) { this.ch = ch; this.freq = freq; }
        Node(int freq, Node left, Node right) {
            this.freq = freq; this.left = left; this.right = right;
        }
        @Override
        public int compareTo(Node other) { return this.freq - other.freq; }
        boolean isLeaf() { return left == null && right == null; }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("========================================================");
        System.out.println("   HUFFMAN ANALYZER: SINGLE BINARY FILE TRACE           ");
        System.out.println("========================================================");
       
        System.out.print("Enter input file path: ");
        String path = sc.nextLine();
        File inputFile = new File(path);
        if (!inputFile.exists()) {
            System.out.println("Error: File not found!");
            return;
        }

        // --- PHASE 1: COMPRESSION ---
        String content = readFile(path);
        System.out.println("\n[STEP 1] FREQUENCY ANALYSIS (HASH TABLE)");
        System.out.println("----------------------------------------");
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : content.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }
        freqMap.forEach((k, v) -> System.out.println("  '" + formatChar(k) + "' -> " + v));

        System.out.println("\n[STEP 2] BUILDING HUFFMAN TREE (MIN-HEAP / PQ)");
        System.out.println("----------------------------------------");
        PriorityQueue<Node> heap = new PriorityQueue<>();
        for (var entry : freqMap.entrySet()) {
            heap.add(new Node(entry.getKey(), entry.getValue()));
        }

        int mergeStep = 1;
        while (heap.size() > 1) {
            Node l = heap.poll();
            Node r = heap.poll();
            System.out.println("  Merge #" + (mergeStep++) + ": L[" + formatNode(l) + "] + R[" + formatNode(r) + "]");
            heap.add(new Node(l.freq + r.freq, l, r));
        }
        Node root = heap.poll();

        System.out.println("\n[STEP 3] GENERATING CODES (TREE DFS)");
        System.out.println("----------------------------------------");
        Map<Character, String> huffmanCodes = new HashMap<>();
        generateCodes(root, "", huffmanCodes);
        huffmanCodes.forEach((k, v) -> System.out.println("  '" + formatChar(k) + "' : " + v));

        System.out.println("\n[STEP 4] BITSTREAM PACKING");
        System.out.println("----------------------------------------");
        StringBuilder bitString = new StringBuilder();
        for (char c : content.toCharArray()) {
            bitString.append(huffmanCodes.get(c));
        }
        byte[] compressedBytes = packBits(bitString.toString());
       
        File compFile = new File(inputFile.getParent(), "compressed.huf");
        saveToFile(compFile, compressedBytes, freqMap, bitString.length());
        System.out.println("  -> SUCCESS: Compressed file created at: " + compFile.getAbsolutePath());

        // --- NEW: SIZE & PERCENTAGE ANALYSIS ---
        long originalSize = inputFile.length();
        long compressedSize = compFile.length();
        double ratio = (1.0 - (double) compressedSize / originalSize) * 100; 

        System.out.println("\n[STEP 5] COMPRESSION STATISTICS");
        System.out.println("----------------------------------------");
        System.out.println("  Original Size:   " + originalSize + " bytes");
        System.out.println("  Compressed Size: " + compressedSize + " bytes (includes header)");
        System.out.printf("  Space Saved:     %.2f%%\n", ratio);

        // --- PHASE 2: DECOMPRESSION ---
        System.out.println("\n[STEP 6] DECOMPRESSION & DS RECONSTRUCTION");
        System.out.println("----------------------------------------");
        String recoveredText = decompressFromFile(compFile);
       
        System.out.println("\n[STEP 7] FINAL VERIFICATION");
        System.out.println("----------------------------------------");
        System.out.println("  Match Original?  " + (content.equals(recoveredText) ? "YES (LOSSLESS)" : "NO"));
        System.out.println("\n[FULL DECOMPRESSED CONTENT]:");
        System.out.println("----------------------------------------");
        System.out.println(recoveredText);
        System.out.println("----------------------------------------");
    }

    private static String decompressFromFile(File file) throws Exception {
        Map<Character, Integer> recoveredFreqMap = new HashMap<>();
        byte[] hufData;
        int totalBits;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            totalBits = dis.readInt();
            int mapSize = dis.readInt();
            
            System.out.println("  [6.1] Restoring Frequency Map from Header:");
            for (int i = 0; i < mapSize; i++) {
                char key = dis.readChar();
                int val = dis.readInt();
                recoveredFreqMap.put(key, val);
                System.out.println("        Parsed Metadata: '" + formatChar(key) + "' -> " + val);
            }
            hufData = dis.readAllBytes();
        }

        System.out.println("\n  [6.2] In-Depth Repopulation of Min-Heap (PQ):");
        PriorityQueue<Node> heap = new PriorityQueue<>();
        for (var entry : recoveredFreqMap.entrySet()) {
            Node newNode = new Node(entry.getKey(), entry.getValue());
            heap.add(newNode);
            System.out.println("        Heap add -> [" + formatNode(newNode) + "]. Current Min: [" + formatNode(heap.peek()) + "]");
        }

        System.out.println("\n  [6.3] Re-merging Nodes to Restore Huffman Tree:");
        int mergeStep = 1;
        while (heap.size() > 1) {
            Node l = heap.poll(); Node r = heap.poll();
            System.out.println("        Re-merge #" + (mergeStep++) + ": L[" + formatNode(l) + "] + R[" + formatNode(r) + "]");
            heap.add(new Node(l.freq + r.freq, l, r));
        }
        Node root = heap.poll();
        System.out.println("  [6.4] Binary Tree Fully Restored.");

        StringBuilder bits = new StringBuilder();
        for (byte b : hufData) {
            for (int i = 7; i >= 0; i--) bits.append((b >> i & 1));
        }
       
        StringBuilder result = new StringBuilder();
        Node current = root;
        for (int i = 0; i < totalBits; i++) {
            current = (bits.charAt(i) == '0') ? current.left : current.right;
            if (current.isLeaf()) {
                result.append(current.ch);
                current = root;
            }
        }
        return result.toString();
    }

    private static String formatChar(char c) {
        if (c == '\n') return "\\n";
        if (c == '\r') return "\\r";
        if (c == ' ') return "[Space]";
        return String.valueOf(c);
    }

    private static String formatNode(Node n) {
        return n.isLeaf() ? "'" + formatChar(n.ch) + "':" + n.freq : "Internal:" + n.freq;
    }

    private static void generateCodes(Node node, String code, Map<Character, String> map) {
        if (node == null) return;
        if (node.isLeaf()) { map.put(node.ch, code); return; }
        generateCodes(node.left, code + "0", map);
        generateCodes(node.right, code + "1", map);
    }

    private static byte[] packBits(String bitStr) {
        byte[] out = new byte[(bitStr.length() + 7) / 8];
        for (int i = 0; i < bitStr.length(); i++) {
            if (bitStr.charAt(i) == '1') out[i / 8] |= (1 << (7 - (i % 8)));
        }
        return out;
    }

    private static void saveToFile(File file, byte[] data, Map<Character, Integer> map, int bitCount) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(bitCount);
            dos.writeInt(map.size());
            for (var entry : map.entrySet()) {
                dos.writeChar(entry.getKey());
                dos.writeInt(entry.getValue());
            }
            dos.write(data);
        }
    }

    private static String readFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            int c;
            while ((c = br.read()) != -1) sb.append((char) c);
        }
        return sb.toString();
    }
}