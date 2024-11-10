import aes.BlockCipher;
import aes.Decifrar;
import aes.ExpansaoChave;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.IntStream;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Escolha uma opção:");
        System.out.println("1: Criptografar um arquivo");
        System.out.println("2: Descriptografar um arquivo");
        int option = scanner.nextInt();
        scanner.nextLine(); // Consumir a nova linha

        if (option == 1) {
            // Criptografar
            System.out.println("Digite o caminho completo para o arquivo a ser criptografado:");
            String inputPath = scanner.nextLine();
            File fileToEncrypt = new File(inputPath);

            // pegar chave de cripto
            System.out.println("Digite a chave (bytes separados por ','):");
            String password = scanner.nextLine();

            // pegar diretório de saída
            System.out.println("Digite o caminho completo de saída para o arquivo criptografado:");
            String outputPath = scanner.nextLine();
            File outputFile = new File(outputPath);

            byte[] bytes;
            try (FileInputStream fis = new FileInputStream(fileToEncrypt.getPath())) {
                bytes = fis.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("Erro ao ler o arquivo de entrada");
            }

            String conteudo = new String(bytes);
            System.out.println(conteudo);

            int[] encriptado = encriptar(byteToIntArray(bytes), password);

            try (FileOutputStream fos = new FileOutputStream(outputFile.getPath())) {
                fos.write(intToByteArray(encriptado));
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar o arquivo de saida");
            }

            System.out.println("Arquivo criptografado com sucesso!");

        } else if (option == 2) {
            // Descriptografar
            System.out.println("Digite o caminho completo para o arquivo a ser descriptografado:");
            String inputPath = scanner.nextLine();
            File fileToDecrypt = new File(inputPath);

            // pegar chave de cripto
            System.out.println("Digite a chave (bytes separados por ','):");
            String password = scanner.nextLine();

            // pegar diretório de saída
            System.out.println("Digite o caminho completo de saída para o arquivo descriptografado:");
            String outputPath = scanner.nextLine();
            File outputFile = new File(outputPath);

            byte[] bytes;
            try (FileInputStream fis = new FileInputStream(fileToDecrypt.getPath())) {
                bytes = fis.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("Erro ao ler o arquivo de entrada");
            }

            String conteudo = new String(bytes);
            System.out.println(conteudo);

            int[] decifrado = decifrar(byteToIntArray(bytes), password);

            try (FileOutputStream fos = new FileOutputStream(outputFile.getPath())) {
                fos.write(intToByteArray(decifrado));
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar o arquivo de saida");
            }

            System.out.println("Arquivo descriptografado com sucesso!");
        } else {
            System.out.println("Opção inválida.");
        }

        scanner.close();
    }

    public static int[] encriptar(int[] textoSimples, String key) {
        int[] pkcsText = ExpansaoChave.pkcs7(textoSimples, 16);
        return BlockCipher.encriptarTexto(pkcsText, key);
    }

    public static int[] decifrar(int[] textoCifrado, String key) {
        return Decifrar.decifrarTexto(textoCifrado, key);
    }
    
    

    static int[] byteToIntArray(byte[] array) {
        return IntStream.range(0, array.length).map(i -> array[i]).toArray();
    }


    static byte[] intToByteArray(int[] array) {
        byte[] newArray = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = (byte) array[i];
        }
        return newArray;
    }

}