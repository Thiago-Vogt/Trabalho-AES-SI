package aes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ExpansaoChave {
    public static int[] rotWord(int[] array) {
        return new int[]{array[1], array[2], array[3], array[0]};
    }

    public static int[] subWord(int[] words) {
    	return Arrays.stream(words)
                 .map(word -> {
                     int valorSubstituido = Tabelas.PegarValorTabelaSbox(word);
                     return valorSubstituido != -1 ? valorSubstituido : word;
                 })
                 .toArray();
    }
    
    private static final Map<Integer, Integer> VALORES = Map.of(
        1, 0x01,
        2, 0x02,
        3, 0x04,
        4, 0x08,
        5, 0x10,
        6, 0x20,
        7, 0x40,
        8, 0x80,
        9, 0x1B,
        10, 0x36
    );

    public static int[] roundConstant(int index) {
        return new int[]{VALORES.get(index), 0, 0, 0};
    }

        public static int[] aplicarXor(int[] first, int[] second) {
        return IntStream.range(0, 4).map(i -> first[i] ^ second[i]).toArray();
    }

     public static final List<int[]> matrix_linhas_multiplicar = MatrizEstado.deChave("2,1,1,3,3,2,1,1,1,3,2,1,1,1,3,2").getLinhas();

    
    public static int[] getMultiMatrixRow(int index) {
        return matrix_linhas_multiplicar.get(index);
    }
    
    public static int[] pkcs7(int[] simpleText, int blockSize) {
        int textLength = simpleText.length;
        int qdtCharAppend = blockSize - textLength % blockSize;

        int[] result = new int[textLength + qdtCharAppend];

        System.arraycopy(simpleText, 0, result, 0, textLength);

        for (int i = 0; i < qdtCharAppend; i++)
        	result[i + textLength] = qdtCharAppend;

        return result;
    }

}
