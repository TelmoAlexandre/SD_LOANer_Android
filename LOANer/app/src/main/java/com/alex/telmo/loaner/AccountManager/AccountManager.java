/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alex.telmo.loaner.AccountManager;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.Serializable;
import java.security.Key;
import java.util.Base64;

/**
 * Classe abstrata que irá ser herdada por todos os objetos que irão ser colocados dentro dos blocos das BlockChains.
 * 
 * @author Telmo
 */
public abstract class AccountManager implements Serializable
{

    /**
     * Contem a chave publica do cliente.
     */
    protected Key publicKey;
    
    protected AccountManager (Key publicKey){
        this.publicKey = publicKey;
    }
    
    /**
     * Retorna a chave pública do cliente.
     * 
     * @return 
     */
    public Key getPublicKey(){
        return publicKey;
    }
    
    /**
     * Compara uma public key recebida por parâmetro com a do cliente.
     * 
     * @param pbK
     * @return 
     */
    @TargetApi(Build.VERSION_CODES.O)
    public boolean comparePublicKeys(Key pbK)
    {
        return (Base64.getEncoder().encodeToString(pbK.getEncoded()).equals(
                Base64.getEncoder().encodeToString(publicKey.getEncoded())));
    }
}
