/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alex.telmo.loaner.BankServices;

import com.alex.telmo.loaner.BlockChain.Block;
import com.alex.telmo.loaner.BlockChain.BlockChain;

import java.security.Key;

/**
 * Informação do emprestimo.
 * <p>Tem informações sobre o cliente, o montante do emprestimo.
 * 
 * @author Telmo
 */
public class Loan extends Service
{
    private final double interest;
    private double amountWithInterest;
    private final String clientName;

    /**
     *
     * @param publicKey Chave pública do cliente
     * @param clientName Nome do cliente
     * @param amountRequested Valor do emprestimo
     */
    public Loan(Key publicKey, String clientName, double amountRequested)
    {
        super(publicKey, amountRequested);
        this.interest = 20.0;
        this.clientName = clientName;

        // Calcula o montante a pagar já com os interesses incluidos
        amountWithInterest = (amount * (100 + interest)) / 100;
    }

    /**
     * Retorna o montante que ainda falta pagar do emprestimo.
     *
     * @return O montante que falta pagar.
     */
    public double getAmountWithInterest()
    {
        return amountWithInterest;
    }
        
    /**
     * Retorna a informação do emprestimo.
     * 
     * @return 
     */
    @Override
    public String toString()
    {
        return "Client: " + clientName + "\nLoan amount: " + amount + "€\nTotal Amount: " + amountWithInterest + "€";
    }

    /**
     * Verifica se o utilizador já se encontra com um emprestimo activo.
     * 
     * @param blockChain
     * @return Verdade - Caso não exista emprestimo activo <p>Falso - Caso exista emprestimo activo
     * @throws Exception 
     */
    @Override
    public boolean validate(BlockChain blockChain) throws Exception
    {
        boolean hasLoan = false;

        for ( Block b : blockChain.chain )
        {
            // Caso se trate de um emprestimo
            if ( b.content instanceof Loan )
            {
                // Individualizar a instancia do Loan
                Loan loanInfo = (Loan) b.content;

                // Se se tratar de um empretimo do cliente
                if ( loanInfo.comparePublicKeys(publicKey) )
                {

                    // Recolhe o que falta pagar do emprestimo
                    double whatsLeftToPay = LoanPayment.whatsLeftToPayInThisLoan(
                            b.hashCode, // O Hash do bloco de emprestimo porque existe referencia a ele em todos os pagamentos do emprestimo
                            blockChain,
                            loanInfo.getAmountWithInterest() // Total a pagar do emprestimo
                    );

                    // Caso ainda não tenha pago tudo, coloca como activo (TRUE)
                    // Seão coloca como FALSE
                    hasLoan = (whatsLeftToPay != 0.0);
                }
            }
        }

        // Retorna falso caso o utilzador tenha um emprestimo activo
        return !hasLoan;
    }

    /**
     * Retorna o valor do emprestimo.
     * 
     * @return Valor do emprestimo
     */
    @Override
    public double getTrueAmount()
    {
         return amount;
    }
}
