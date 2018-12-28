package Utilities;

//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2018   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
//////////////////////////////////////////////////////////////////////////////


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created on 6/out/2018, 15:46:33
 *
 * @author zulu
 */
public class SecurityUtils {

    
    public static byte[] signRSA(byte[] data, PrivateKey key) throws Exception{
        Signature shaWithRsa = Signature.getInstance("SHA256withRSA");
        shaWithRsa.initSign(key);
        shaWithRsa.update(data);
        return shaWithRsa.sign();
    }
    
    public static boolean verifyRSA(byte[] data, byte[] signature, PublicKey key) throws Exception{
        Signature shaWithRsa = Signature.getInstance("SHA256withRSA");
        shaWithRsa.initVerify(key);
        shaWithRsa.update(data);
        return shaWithRsa.verify(signature);
    }
    
    
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::        ZIP /  UNZIP                                :::::::::::
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * calcula o hash de um conjunto de dados
     * @param data dados
     * @param algorithm algoritmo de hash
     * @return hash dos dados
     * @throws Exception 
     */
    public static byte[] hash(byte[] data, String algorithm) throws Exception {
        //objecto de integridade
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        //fornecer os dados ao objecto
        messageDigest.update(data);
        //calcular o valor da hash
        return messageDigest.digest();
    }
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::        ZIP /  UNZIP                                :::::::::::
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * Comprime um array de bytes utilizando o algoritmo GZIP
     *
     * @param data dados originais
     * @return dados comprimidos
     * @throws IOException
     */
    public static byte[] zip(byte[] data) throws IOException {
        //array de bytes em memória
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        // adaptador GZIP para comprimir bytes
        GZIPOutputStream zout = new GZIPOutputStream(bout);
        //escrever os dados no GZIP
        zout.write(data, 0, data.length);
        //terminar a escrita de dados
        zout.finish();
        //devolver os dados comprimidos
        return bout.toByteArray();
    }

    /**
     * Expande um array de dados comprimidos pelo algoritmo GZIP
     *
     * @param data dados comprimidos
     * @return dados originais
     * @throws IOException
     */
    public static byte[] unzip(byte[] data) throws IOException {
        //Stream com Array de bytes em memória
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        //Adaptador GZIP para descomprimir a stream
        GZIPInputStream zin = new GZIPInputStream(bin);
        //Array de bytes expandidos
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        //buffer de leitura
        byte[] buffer = new byte[1024];
        int len = 0;
        //ler os dados da stream GZIP
        while ((len = zin.read(buffer)) > 0) {
            //escrever os dados na Stream expandida
            bout.write(buffer, 0, len);
        }
        //retornar os bytes originais
        return bout.toByteArray();
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::        SIMETRIC KEYS                               :::::::::::
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Guarda uma chave num ficheiro
     *
     * @param key chave
     * @param fileName nome do ficheiro
     * @throws IOException
     */
    public static void saveKey(Key key, String fileName) throws IOException {
        //System.out.println("Saving key to file " + fileName + " ...");
        //System.out.println("Key: " + Base64.getEncoder().encodeToString(key.getEncoded()));
        Files.write(Paths.get(fileName), key.getEncoded());
    }

    /**
     * Le uma chave de um ficheiro
     *
     * @param fileName nome do ficheiro
     * @param algorithm algoritmo da cheve
     * @return
     * @throws IOException
     */
    public static Key loadKey(String fileName, String algorithm) throws IOException {
        //System.out.println("Loading key from file " + fileName + " ...");
        byte[] encoded = Files.readAllBytes(Paths.get(fileName));
        //gerar a chave com os bytes e com as especificações do algoritmo
        Key key = new SecretKeySpec(encoded, algorithm);
        //System.out.println("Key: " + Base64.getEncoder().encodeToString(key.getEncoded()));
        return key;
    }

    /**
     * Le uma chave de um ficheiro
     *
     * @param data
     * @param algorithm algoritmo da cheve
     * @return
     * @throws IOException
     */
    public static Key loadB64Key(String data, String algorithm) throws Exception {
        //System.out.println("Loading key from Base64 ...");
        byte[] encoded = Base64.getDecoder().decode(data);
        Key key = new SecretKeySpec(encoded, algorithm);
        return key;
    }

    /**
     *
     *
     * /**
     * Gera uma chave de criptogradia simetrica
     *
     * @param algorithm algoritmo
     * @param keySize tamanho da chave
     * @return chave
     * @throws Exception
     */
    public static Key generateKey(String algorithm, int keySize) throws Exception {
        //System.out.println("Generating " + algorithm + " - " + keySize + " key ...");
        // gerador de chaves
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        //tamanho da chave
        keyGen.init(keySize);
        //gerar a chave
        Key key = keyGen.generateKey();
        //System.out.println("Key :" + Base64.getEncoder().encodeToString(key.getEncoded()));
        return key;
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::        PASSWORD BASED ENCRYPTATION                 :::::::::::
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * Cria um objecto de cifragem com PBE SHA1 e triple DES
     *
     * @param mode Cipher.DECRYPT_MODE ou Cipher.ENCRYPT_MODE
     * @param password password de da cifra
     * @return Objecto de cifragem
     * @throws Exception
     */
    private static Cipher createCipherPBE(int mode, String password) throws Exception {
        //:::::::::   1 - gerar uma chave secreta  :::::::::::::::::::::::::::::::
        //transformar a password nos parametros na chave
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        //algoritmo de cifragem
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithSHA1AndDESede");
        //gerar a chave
        SecretKey key = keyFactory.generateSecret(keySpec);
        //::::::::: 2 -  adicionar sal á chave  :::::::::::::::::::::::::::::::
        // usae o SHA1 para gerar um conjunto de  bytes a partir da passaord
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(password.getBytes());
        //usar os primeiros 8 bytes
        byte[] digest = Arrays.copyOf(md.digest(), 8);
        //fazer 1000 iterações com o sal
        PBEParameterSpec paramSpec = new PBEParameterSpec(digest, 1000);
        //3 - Gerar o objecto de cifragem
        Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        //:::::::::   4 - iniciar a cifra ::::::::: ::::::::: ::::::::: ::::::::: 
        // iniciar o objeto de cifragem com os parâmetros
        cipher.init(mode, key, paramSpec);
        return cipher;
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::                    R S A                           :::::::::::
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * gera um par de chave RSA
     *
     * @param size tamanho da chave
     * @return par de chaves
     * @throws Exception
     */
    public static KeyPair generateKeyPair(int size) throws Exception {
        //System.out.println("Generating RSA " + size + " keys ...");
        // gerador de chaves RSA
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        //tamanho da chave
        keyGen.initialize(size);
        return keyGen.generateKeyPair();
    }

    /**
     * gera uma chave publica atraves de array de bytes
     *
     * @param pubData dados da chave publica
     * @return chave publica
     * @throws Exception
     */
    public static Key getPublicKey(byte[] pubData) throws Exception {
        //especifacção do encoding da chave publica X509
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubData);
        //objecto para grerar a chave RSA
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //Gerar a chave pública
        return keyFactory.generatePublic(pubSpec);
    }

    /**
     * gera uma chave privada atraves de array de bytes
     *
     * @return chave publica
     * @throws Exception
     */
    public static Key getPrivateKey(byte[] privData) throws Exception {
        //especificações da chave privada PKCS8
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privData);
        //objecto para grerar a chave RSA
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //Gerar a chave privada
        return keyFactory.generatePrivate(privSpec);
    }

    /**
     * conver uma string base64 para chave pública
     *
     * @param b64 base64 da chave
     * @return chave publica
     * @throws Exception
     */
    public static Key getPublicKey(String b64) throws Exception {
        return getPublicKey(Base64.getDecoder().decode(b64));
    }

    /**
     * converte uma string base64 para chave privada
     *
     * @param b64 base64 da chave
     * @return chave privada
     * @throws Exception
     */
    public static Key getPrivateKey(String b64) throws Exception {
        return getPrivateKey(Base64.getDecoder().decode(b64));
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //:::::::::::               ENCRYPT /  DECRYPT                   :::::::::::
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /**
     * encripta dados usando uma password de texto
     *
     * @param data dados para encriptar
     * @param password password de encriptação
     * @return dados encriptados
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, String password) throws Exception {
        //criar um objecto de cifragem da chave
        Cipher cipher = createCipherPBE(Cipher.ENCRYPT_MODE, password);
        //cifrar os dados
        return cipher.doFinal(data);
    }

    /**
     * desencripta dados usando uma password de texto
     *
     * @param data dados para desencriptar
     * @param password password de desencriptação
     * @return dados desencriptados
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, String password) throws Exception {
        //criar um objecto de cifragem da chave
        Cipher cipher = createCipherPBE(Cipher.DECRYPT_MODE, password);
        //cifrar os dados
        return cipher.doFinal(data);
    }

    /**
     * encrypta dados utilizado uma chave binária
     *
     * @param data dados para encriptar
     * @param key chave de encriptação
     * @return dados encriptados
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, Key key) throws Exception {
        //criar um objecto de cifragem da chave
        Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        //configurar o objecto para cifrar
        cipher.init(Cipher.ENCRYPT_MODE, key);
        //cifrar os dados
        return cipher.doFinal(data);
    }

    /**
     * desencripta dados utilizado uma chave binária
     *
     * @param data dados para desencriptar
     * @param key chave de desencriptacao
     * @return dados desencriptados
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, Key key) throws Exception {
        //criar um objecto de cifragem da chave
        Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        //configurar o objecto para cifrar
        cipher.init(Cipher.DECRYPT_MODE, key);
        //decifrar os dados
        return cipher.doFinal(data);
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 201810061546L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2018  :::::::::::::::::::
    ///////////////////////////////////////////////////////////////////////////
}
