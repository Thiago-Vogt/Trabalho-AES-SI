package aes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatrizEstado {
    private final int[][] words = new int[4][4];

    private MatrizEstado(int[] text) {
        if (text.length != 16) {
            throw new IllegalArgumentException("Chaves devem ter 128 bits");
        }

        int index = 0;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                words[j][i] = text[index++];
            }
        }
    }

    private MatrizEstado(int[] w0, int[] w1, int[] w2, int[] w3) {
        if (w0.length != 4 || w1.length != 4 || w2.length != 4 || w3.length != 4) {
            throw new IllegalArgumentException("");
        }

        for (int i = 0; i < 4; i++) {
            words[i][0] = w0[i];
            words[i][1] = w1[i];
            words[i][2] = w2[i];
            words[i][3] = w3[i];
        }
    }

    public static MatrizEstado deChave(String password) {
        return new MatrizEstado(Arrays.stream(password.split(",")).mapToInt(Integer::parseInt).toArray());
    }

    public static MatrizEstado deChave(int[] array) {
        return new MatrizEstado(array);
    }

    public static List<MatrizEstado> deTextoSimples(int[] simpleText) {
        List<MatrizEstado> result = new ArrayList<>();

        for (int i = 0; i < simpleText.length; i += 16) {
            result.add(new MatrizEstado(Arrays.copyOfRange(simpleText, i, i + 16)));
        }

        return result;
    }

    public static MatrizEstado deRoundKeys(int[] w0, int[] w1, int[] w2, int[] w3) {
        return new MatrizEstado(w0, w1, w2, w3);
    }

    private static int[] rotateRow(int[] row, int index) {
        for (int i = 0; i < index; i++) {
            row = ExpansaoChave.rotWord(row);
        }
        return row;
    }


    public MatrizEstado subBytes() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                words[i][j] = Tabelas.PegarValorTabelaSbox(words[i][j]);
            }
        }
        return this;
    }

    public MatrizEstado shiftRows() {
        IntStream.range(0, 4).forEach(i -> words[i] = rotateRow(words[i], i));
        return this;
    }

    public MatrizEstado mixColumns() {
        List<int[]> wordsList = getPalavras();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                words[i][j] = calcularMixColumns(wordsList.get(j), ExpansaoChave.getMultiMatrixRow(i));
            }
        }

        return this;
    }

    public MatrizEstado addRoundKey(MatrizEstado roundKey) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                words[i][j] ^= roundKey.words[i][j];
            }
        }
        return this;
    }

    public List<int[]> getLinhas() {
        return Arrays.stream(words, 0, 4).collect(Collectors.toList());
    }

    public List<int[]> getPalavras() {
        return IntStream.range(0, 4).mapToObj(i -> IntStream.range(0, 4).map(j -> words[j][i]).toArray()).collect(Collectors.toList());
    }

    public int[] toIntArray() {
        return getPalavras().stream().flatMapToInt(Arrays::stream).toArray();
    }

    private String formatHex(String hex) {
        return (hex.length() == 1 ? "0x0" : "0x").concat(hex);
    }

    @Override
    public String toString() {
        StringBuilder[] lines = new StringBuilder[4];
        IntStream.range(0, 4).forEach(i -> lines[i] = new StringBuilder());

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                lines[j].append(formatHex(Integer.toHexString(words[j][i]))).append(" | ");
            }
        }

        return String.join("\n", lines);
    }

    public MatrizEstado invSubBytes() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                words[i][j] = Tabelas.pegarValorTabelaS_inv(words[i][j]);
            }
        }
        return this;
    }

    public MatrizEstado invShiftRows() {
        for (int i = 0; i < 4; i++) {
            words[i] = rotateRow(words[i], 4 - i);
        }
        return this;
    }

    public MatrizEstado invMixColumns() {
        List<int[]> wordsList = getPalavras();
        int[][] invMixColumnsMatrix = {
            {0x0e, 0x0b, 0x0d, 0x09},
            {0x09, 0x0e, 0x0b, 0x0d},
            {0x0d, 0x09, 0x0e, 0x0b},
            {0x0b, 0x0d, 0x09, 0x0e}
        };

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                words[i][j] = calcularMixColumns(wordsList.get(j), invMixColumnsMatrix[i]);
            }
        }

        return this;
    }
    
    private static int calcularMixColumns(int[] column, int[] row) {
        int result = calcularValorGalois(column[0], row[0]);

        for (int i = 1; i < 4; i++) {
            result ^= calcularValorGalois(column[i], row[i]);
        }

        return result;
    }

    private static int calcularValorGalois(int valueOne, int valueTwo) {
        if (valueOne == 0 || valueTwo == 0) return 0;
        if (valueOne == 1) return valueTwo;
        if (valueTwo == 1) return valueOne;

        int TabelaLResult = Tabelas.pegarValorTabelaL(valueOne) + Tabelas.pegarValorTabelaL(valueTwo);

        if (TabelaLResult > 0xFF) TabelaLResult -= 0xFF;

        return Tabelas.pegarValorTabelaE(TabelaLResult);
    }
}
