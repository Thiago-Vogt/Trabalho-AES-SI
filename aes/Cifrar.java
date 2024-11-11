package aes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public class Cifrar {

    private static int[] criptografar(List<MatrizEstado> roundKeys, List<MatrizEstado> textBlocks) {
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
                .map(MatrizEstado::converteArrayParaInt)
                .flatMapToInt(Arrays::stream).toArray();
    }

    public static int[] criptografarTexto(int[] textoSimples, String chave) {
        List<MatrizEstado> roundKeys = gerarChavesExpandidas(MatrizEstado.deChave(chave));
        List<MatrizEstado> textBlocks = MatrizEstado.deTextoSimples(textoSimples);
        return criptografar(roundKeys, textBlocks);
    }

     public static List<MatrizEstado> gerarChavesExpandidas(MatrizEstado matrizInicial) {
        List<MatrizEstado> roundKeys = new ArrayList<>();
        roundKeys.add(matrizInicial);

        for (int i = 1; i < 11; i++) {
            MatrizEstado matrizAnterior = roundKeys.get(i - 1);
            List<int[]> words = matrizAnterior.getPalavras();

            int[] firstKey = expandirPrimeiraChave(words, i);
            int[] secondKey = ExpansaoChave.aplicarXor(firstKey, words.get(1));
            int[] thirdKey = ExpansaoChave.aplicarXor(secondKey, words.get(2));
            int[] fourthKey = ExpansaoChave.aplicarXor(thirdKey, words.get(3));

            roundKeys.add(MatrizEstado.deRoundKeys(firstKey, secondKey, thirdKey, fourthKey));
        }

        return roundKeys;
    }

    private static int[] expandirPrimeiraChave(List<int[]> MatrizAnteriorWords, int index) {
        return Optional.of(MatrizAnteriorWords.get(3))                            
                .map(ExpansaoChave::rotWord)                                          
                .map(ExpansaoChave::subWord)                                       
                .map(w -> ExpansaoChave.aplicarXor(w, ExpansaoChave.roundConstant(index)))      
                .map(w -> ExpansaoChave.aplicarXor(w, MatrizAnteriorWords.get(0)))          
                .orElseThrow(() -> new RuntimeException("Erro ao tentar expandir a primeira chave do bloco"));
    }

}
