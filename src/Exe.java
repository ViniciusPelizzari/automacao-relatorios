import javax.swing.*;


/**
 * @author Vinícius Pelizzari
 * Classe Exe - responsável pela inicialização do aplicativo
 */
public class Exe {

    /**
     * Método principal da aplicação
     *
     * @param args
     */
    public static void main(String[] args) {
        // utiliza o método invokeLater para garantir que o código seja executado na thread de despacho de eventos (Event Dispatch Thread - EDT)
        SwingUtilities.invokeLater(() -> {
            // tenta se conectar ao DB utilizando o método connectToDatabase
            if (FirebirdTest.connectToDatabase()) {
                // aplicação se conectou ao DB
                System.out.println("OK - CONEXÃO BEM-SUCEDIDA!");
            } else {
                // erro na conexão - imprime tela com mensagem de erro
                JOptionPane.showMessageDialog(null, "Erro ao conectar ao banco de dados. Verifique as configurações.");
            }
        });
    }
}