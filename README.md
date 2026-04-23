SY CSE A2

Group - 11

Problem Statement - 21

# Zip File Compression Analyzer

A Java-based implementation of the **Huffman Coding Algorithm** for lossless text compression and decompression. This project was developed as part of the 2nd Year Data Structures course to demonstrate the practical application of Heaps, Binary Trees, and Hash Maps.

## 🚀 Overview
This tool analyzes the character frequency of a given text file, constructs an optimal prefix-free binary tree, and packs the resulting bitstream into a custom `.huf` binary file. It also includes a decompression engine to restore the original file with 100% integrity.

### Key Features
* **Real-time Trace:** Displays the step-by-step merging of nodes in the Min-Heap.
* **Binary Packing:** Efficiently packs bit-strings into `byte[]` arrays to ensure actual file size reduction.
* **Lossless Verification:** Automatically compares original and decompressed content to verify integrity.
* **Detailed Stats:** Reports original size, compressed size, and space-saving percentage.

## 🛠️ Data Structures Used
* **Min-Heap (`PriorityQueue`):** To store and merge nodes with the lowest frequencies during tree construction.
* **Huffman Tree (`Binary Tree`):** A custom tree structure used to generate variable-length prefix codes.
* **HashMap:**
    * `Map<Character, Integer>` for frequency analysis.
    * `Map<Character, String>` for quick encoding lookups.
* **Recursion (DFS):** To traverse the tree and assign binary paths (0 for left, 1 for right).

## 📊 How It Works
1. **Frequency Analysis:** Scans the input file to count occurrences of each character.
2. **Tree Construction:** Uses a greedy approach to build a Huffman Tree from the bottom up.
3. **Code Generation:** Generates unique binary codes; more frequent characters get shorter codes.
4. **Bit Packing:** Converts the text into a binary stream and writes it to a file along with the metadata (header) required to rebuild the tree.

## 💻 Usage

1. **Compile the program:**
   ```bash
   javac man.java

2. **Run the analyzer:**
   ```bash
   java man

3. **Follow the prompts:** Enter the full path of your `.txt` file when requested.

## 📈 Sample Output
```Plaintext
========================================================
   HUFFMAN ANALYZER: SINGLE BINARY FILE TRACE           
========================================================

[STEP 5] COMPRESSION STATISTICS
----------------------------------------
  Original Size:   1240 bytes
  Compressed Size: 748 bytes (includes header)
  Space Saved:     39.68%

[STEP 7] FINAL VERIFICATION
----------------------------------------
  Match Original?  YES (LOSSLESS)
