import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A classe FirebirdTest contém métodos para conectar e interagir com um banco de dados Firebird.
 */
public class FirebirdTest {
    /**
     * Estabelece uma conexão com o banco de dados Firebird e executa operações relacionadas.
     *
     * @return A conexão estabelecida ou null em caso de falha.
     */
    public static Connection conexao(){
        // configuração da conexão com o banco de dados Firebird
        String url = "jdbc:firebirdsql://xxx.xxx.x.xxx:3055/C:\\xxx-xxxxxx\\DadosTek-Teste\\EXC_DADOSMC_SFERA\\HOMOLOGACAO(CUSTO ESTOQUE).fdb?encoding=ISO8859_1";
        String user = "SYSDBA";
        String password = "masterkey";

        try {
            // carrega o driver JDBC do Firebird
            Class.forName("org.firebirdsql.jdbc.FBDriver");

            // estabelece a conexão com o banco de dados
            Connection connection = DriverManager.getConnection(url, user, password);

            System.out.println("OK - EM EXECUÇÃO!");

            // cria uma instância de Manipulacoes para realizar operações no banco de dados
            Manipulacoes manipulacoes = new Manipulacoes();
            try {
                manipulacoes.select(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // gera a data atual
            LocalDateTime agora = LocalDateTime.now();

            // faz a formatação da data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy---HH-mm-ss");

            // exporta os resultados para um arquivo HTML - com o nome e a data passado na URL
            manipulacoes.exportarParaHTML("\\\\192.168.0.145\\Arquivos\\controle-estoque\\compras\\arq-posicao\\verificar-" + agora.format(formatter) + ".html", manipulacoes.verificar);

            // fecha a conexão com o banco de dados
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            // caso haja algum erro de conexão, imprime uma mensagem no console da aplicação
            System.out.println("ERRO- FALHA DB");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * conecta-se ao banco de dados chamando o método conexao().
     *
     * @return true se a conexão for bem-sucedida, false caso contrário.
     */
    public static boolean connectToDatabase() {
        // cria uma instância de FirebirdTest e chama o método conexao()
        FirebirdTest fdb = new FirebirdTest();
        fdb.conexao();
        return true;
    }
}