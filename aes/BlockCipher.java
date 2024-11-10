package aes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public class BlockCipher {

    public static int[] encriptarTexto(int[] simpleText, String key) {
        List<MatrizEstado> roundKeys = expandirChaves(MatrizEstado.deChave(key));
        List<MatrizEstado> textBlocks = MatrizEstado.deTextoSimples(simpleText);
        return encriptar(roundKeys, textBlocks);
    }

    private static int[] encriptar(List<MatrizEstado> roundKeys, List<MatrizEstado> textBlocks) {
        Stream<MatrizEstado> blockStream = textBlocks.stream().map(b -> b.addRoundKey(roundKeys.get(0)));

        for (int i = 1; i < 10; i++) {
            MatrizEstado roundKey = roundKeys.get(i);
            blockStream = blockStream
                    .map(MatrizEstado::subBytes)
                    .map(MatrizEstado::shiftRows)
                    .map(MatrizEstado::mixColumns)
                    .map(b -> b.addRoundKey(roundKey));
        }

        return blockStream
                .map(MatrizEstado::subBytes)
                .map(MatrizEstado::shiftRows)
                .map(b -> b.addRoundKey(roundKeys.get(10)))
                .map(MatrizEstado::toIntArray)
                .flatMapToInt(Arrays::stream).toArray();
    }

     public static List<MatrizEstado> expandirChaves(MatrizEstado initialMatrix) {
        List<MatrizEstado> roundKeys = new ArrayList<>();
        roundKeys.add(initialMatrix);

        for (int i = 1; i < 11; i++) {
            MatrizEstado previousMatrix = roundKeys.get(i - 1);
            List<int[]> words = previousMatrix.getPalavras();

            int[] firstKey = expandirPrimeiraChave(words, i);
            int[] secondKey = ExpansaoChave.aplicarXor(firstKey, words.get(1));
            int[] thirdKey = ExpansaoChave.aplicarXor(secondKey, words.get(2));
            int[] fourthKey = ExpansaoChave.aplicarXor(thirdKey, words.get(3));

            roundKeys.add(MatrizEstado.deRoundKeys(firstKey, secondKey, thirdKey, fourthKey));
        }

        return roundKeys;
    }

    private static int[] expandirPrimeiraChave(List<int[]> previousMatrixWords, int index) {
        return Optional.of(previousMatrixWords.get(3))                            
                .map(ExpansaoChave::rotWord)                                          
                .map(ExpansaoChave::subWord)                                       
                .map(w -> ExpansaoChave.aplicarXor(w, ExpansaoChave.roundConstant(index)))      
                .map(w -> ExpansaoChave.aplicarXor(w, previousMatrixWords.get(0)))          
                .orElseThrow(() -> new RuntimeException("Error when trying to expand the first key of the block"));
    }

}
