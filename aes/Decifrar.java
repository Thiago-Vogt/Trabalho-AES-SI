package aes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Decifrar {

    public static int[] decifrarTexto(int[] textoCifrado, String key) {
        List<MatrizEstado> roundKeys = BlockCipher.expandirChaves(MatrizEstado.deChave(key));
        List<MatrizEstado> textBlocks = MatrizEstado.deTextoSimples(textoCifrado);
        return decifrar(roundKeys, textBlocks);
    }

    private static int[] decifrar(List<MatrizEstado> roundKeys, List<MatrizEstado> textBlocks) {
        Stream<MatrizEstado> blockStream = textBlocks.stream()
                .map(b -> b.addRoundKey(roundKeys.get(10)));

        // Rodadas de 9 até 1
        for (int i = 9; i >= 1; i--) {
            MatrizEstado roundKey = roundKeys.get(i);
            blockStream = blockStream
                    .map(MatrizEstado::invMixColumns)
                    .map(MatrizEstado::invShiftRows)
                    .map(MatrizEstado::invSubBytes)
                    .map(b -> b.addRoundKey(roundKey));
        }

        return blockStream
                .map(MatrizEstado::invShiftRows)
                .map(MatrizEstado::invSubBytes)
                .map(b -> b.addRoundKey(roundKeys.get(0)))
                .map(MatrizEstado::toIntArray)
                .flatMapToInt(Arrays::stream).toArray();
    }
}
