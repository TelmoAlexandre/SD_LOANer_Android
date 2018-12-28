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
 * Pagamento sobre o emprestimo.
 *
 * @author Telmo
 */
public class LoanPayment extends Service
{
    private final String loanBlockHash;
    
    public LoanPayment(Key publicKey, double amount, String loanBlockHash)
    {
        super(publicKey, amount);
        this.loanBlockHash = loanBlockHash;
    }

    /**
     * Valida a integridade do pagamento de emprestimo.
     *
     * @param bc
     * @return
     * @throws java.lang.Exception
     */
    @Override
    public boolean validate(BlockChain bc) throws Exception
    {
        return (amount <= getMyMoney(bc, publicKey) && verifySignature());
    }

    /**
     * Retorna o valor do movimento como negativo, porque esse dinheiro foi
     * retirado da conta para pagagamento de emprestimo.
     *
     * @return
     */
    @Override
    public double getTrueAmount()
    {
        return amount * -1.0;
    }

    /**
     * Compara se este pagamento é pertence a um devido emprestimo.
     *
     * @param loanHash Hash do bloco do emprestimo a ser comparado.
     * @return
     */
    public boolean belongsToThisLoan(String loanHash)
    {
        return (loanBlockHash.equals(loanHash));
    }
    
    /**
     * Retorna o que falta pagar sobre o emprestimo em questão.
     * 
     * @param loanHash Hash do bloco de emprestimo em questão.
     * @param bc BlockChain a ser percorrida.
     * @param loanTotal O total a pagar do emprestimo.
     * @return 
     */
    public static double whatsLeftToPayInThisLoan(String loanHash, BlockChain bc, double loanTotal)
    {        
        for ( Block b : bc.chain )
        {
            // Caso seja um pagamento de emprestimo
            if ( b.content instanceof LoanPayment )
            {
                // Individualiza a instancia de pagamento de emprestimo
                LoanPayment loanPayment = (LoanPayment) b.content;
                
                // Caso este pagamento de emprestimo seja sobre o emprestimo em questão
                if ( loanPayment.belongsToThisLoan(loanHash) )
                {
                    // getTrueAmount retorna como negativo porque é saida de dinheiro da conta
                    loanTotal += loanPayment.getTrueAmount();
                }
            }
        }
        
        return loanTotal;
    }

    /**
     * Retorna os atributos do Loan Payment em forma de String.
     *
     * @return type + amount.
     */
    @Override
    public String toString()
    {
        return " Type: Loan Payment" + "\n Amount: " + amount + "€";
    }
}
