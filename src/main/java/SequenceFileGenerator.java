import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class SequenceFileGenerator {

    private static final String GENES = "ACGT";
    private static final int DNA_LENGTH = 20;

    private static String generateRandomDNASequence() {
        Random rand = new Random();
        StringBuilder dna = new StringBuilder();
        for (int i = 0; i < DNA_LENGTH; i++) {
            dna.append(GENES.charAt(rand.nextInt(GENES.length())));
        }
        return dna.toString();
    }

    public static void generateFile(String fileName, int sizeInGB) throws IOException {
        File file = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            long totalSize = (long) sizeInGB * 1024 * 1024 * 1024;
            long written = 0;

            while (written < totalSize) {
                String sequence = generateRandomDNASequence();
                writer.write(sequence);
                writer.newLine();
                written += sequence.length() + 1;
            }
        }
    }

    // Exemplo de uso
    public static void main(String[] args) throws IOException {
        generateFile("sequencias.txt", 1); // gera 1GB
    }
}

