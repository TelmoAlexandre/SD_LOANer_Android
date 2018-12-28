/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alex.telmo.loaner.BankServices;

import android.annotation.TargetApi;
import android.os.Build;

import com.alex.telmo.loaner.AccountManager.AccountManager;
import com.alex.telmo.loaner.BlockChain.Block;
import com.alex.telmo.loaner.BlockChain.BlockChain;

import Utilities.SecurityUtils;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Classe abstrata que contem métodos de movimento de conta.
 *
 * @author Telmo
 */
public abstract class Service extends AccountManager
{
    protected byte[] signature;
    protected final double amount;

    public Service(Key publicKey, double amount)
    {
        super(publicKey);
        this.amount = amount;
    }

    public abstract boolean validate(BlockChain bc) throws Exception;
    public abstract double getTrueAmount();

    /**
     * Assina um movimento de conta.
     *
     * @param pvKey - Chave privada do cliente.
     * @throws Exception
     */
    @TargetApi(Build.VERSION_CODES.O)
    public void sign(PrivateKey pvKey) throws Exception
    {
        byte[] data
                = (Base64.getEncoder().encodeToString(publicKey.getEncoded())
                        + amount).getBytes();

        signature = SecurityUtils.signRSA(data, pvKey);
    }

    /**
     * Verifica a integridade da assinatura do movimento.
     *
     * @return
     * @throws Exception
     */
    @TargetApi(Build.VERSION_CODES.O)
    public boolean verifySignature() throws Exception
    {
        byte[] data
                = (Base64.getEncoder().encodeToString(publicKey.getEncoded())
                        + amount).getBytes();

        return SecurityUtils.verifyRSA(
                data,
                signature,
                (PublicKey) SecurityUtils.getPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()))
        );
    }

    /**
     * Percorre a blockchain para contar o dinheiro em movimentos de conta da
     * public key passada por paramatero.
     *
     * @param bc BlockChain a ser percorrida.
     * @param pbK Chave publica do cliente.
     * @return O montante atual que se encontra na conta.
     */
    public static double getMyMoney(BlockChain bc, Key pbK)
    {
        double money = 0.0;

        for ( Block block : bc.chain )
        {
            // Isola o conteudo do bloco
            AccountManager content = block.content;

            //Garante que se trata da conta do cliente em questão
            if ( content.comparePublicKeys(pbK) )
            {
                // Trata como serviço
                if ( content instanceof Service )
                {
                    // Cast para Service
                    Service serv = (Service) content;

                    money += serv.getTrueAmount();
                }
            }
        }

        return money;
    }
}
