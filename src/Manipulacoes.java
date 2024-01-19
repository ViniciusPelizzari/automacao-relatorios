import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A classe Manipulacoes representa um conjunto de operações em dados e suas manipulações.
 * Implementa Comparable para permitir ordenação de instâncias com base em um critério.
 */
public class Manipulacoes implements Comparable<Manipulacoes> {

    // campos/colunas do DB
    private int codigo_pdc;
    private int item_item_saldo;
    private int empresa_item_saldo;
    private String descricao_item;
    private float saldoatual_item_saldo;
    private float estoqueminimo_item_saldo;
    private float qtdeaberta_pdcitem;
    private Date dtpreventrega_pdc;

    // listas estáticas compartilhadas entre instâncias da classe
    public static List<Manipulacoes> diasSuficientes = new ArrayList<>();
    public static List<Manipulacoes> quaseNoFim = new ArrayList<>();
    public static List<Manipulacoes> acabou = new ArrayList<>();
    public static List<Manipulacoes> verificar = new ArrayList<>();

    // métodos da classe

    /**
     * Realiza uma consulta no banco de dados e categoriza os resultados em listas estáticas.
     *
     * @param connection Uma conexão estabelecida com o banco de dados.
     * @throws SQLException Exceção lançada em caso de erros no acesso ao banco de dados.
     */
    public static void select(Connection connection) throws SQLException {

        try (Statement statement = connection.createStatement()) {
            // script de consulta
            String selectTable = "SELECT\n" +
                    "    p.codigo_pdc,\n" +
                    "    s.item_item_saldo,\n" +
                    "    s.empresa_item_saldo,\n" +
                    "    i.descricao_item AS descricao_item,\n" +
                    "    s.saldoatual_item_saldo AS saldoatual_item_saldo,\n" +
                    "    s.estoqueminimo_item_saldo AS estoqueminimo_item_saldo,\n" +
                    "    c.qtdeaberta_pdcitem AS qtdeaberta_pdcitem,\n" +
                    "    p.dtpreventrega_pdc AS dtpreventrega_pdc\n" +
                    "FROM\n" +
                    "    item_saldo s\n" +
                    "JOIN\n" +
                    "    item i ON s.item_item_saldo = i.codigo_item\n" +
                    "JOIN\n" +
                    "    pedido_compra_item c ON s.item_item_saldo = c.item_pdcitem\n" +
                    "JOIN\n" +
                    "    pedido_compra p ON c.autoincpedido_pdcitem = p.codigo_pdc\n" +
                    "WHERE\n" +
                    "    i.arquivo_item = 3\n" +
                    "    AND s.qualificacao_item_saldo = 1\n" +
                    "    AND s.empresa_item_saldo in(30,14)\n" +
                    "    AND p.empresa_pdc in(30,14)\n" +
                    "    AND p.unidfabril_pdc = 3\n" +
                    "    AND p.tipo_pdc = 1\n" +
                    "    AND c.qtdeaberta_pdcitem <> 0\n" +
                    "    AND c.qtdeaberta_pdcitem IS NOT NULL;";
            try (ResultSet resultSet = statement.executeQuery(selectTable)) {
                while (resultSet.next()) {
                    Manipulacoes manipulacao = new Manipulacoes();

                    manipulacao.codigo_pdc = resultSet.getInt("codigo_pdc");
                    manipulacao.item_item_saldo = resultSet.getInt("item_item_saldo");
                    manipulacao.empresa_item_saldo = resultSet.getInt("empresa_item_saldo");
                    manipulacao.descricao_item = resultSet.getString("descricao_item");
                    manipulacao.saldoatual_item_saldo = resultSet.getFloat("saldoatual_item_saldo");
                    manipulacao.estoqueminimo_item_saldo = resultSet.getFloat("estoqueminimo_item_saldo");
                    manipulacao.qtdeaberta_pdcitem = resultSet.getFloat("qtdeaberta_pdcitem");
                    manipulacao.dtpreventrega_pdc = resultSet.getDate("dtpreventrega_pdc");

                    // cálculo de dias do estoque
                    float dias = manipulacao.saldoatual_item_saldo / manipulacao.estoqueminimo_item_saldo;
                    if (Float.isFinite(dias)) {
                        DecimalFormat df = new DecimalFormat("#.##");
                        String diasFormatado = df.format(dias).replace(',', '.');
                        float dias_estoque = Float.parseFloat(diasFormatado);

                        // verificação de prioridades
                        if (dias_estoque >= 15) {
                            diasSuficientes.add(manipulacao);
                        } else if (dias_estoque <= 14 && dias_estoque >= 7) {
                            quaseNoFim.add(manipulacao);
                        } else if (dias_estoque <= 14 && dias_estoque >= 0) {
                            verificar.add(manipulacao);
                        } else {
                            acabou.add(manipulacao);
                        }
                        }
                }
            }

            // ordenação das listas
            Collections.sort(diasSuficientes);
            Collections.sort(quaseNoFim);
            Collections.sort(acabou);
            Collections.sort(verificar);
        }
    }

    /**
     * Imprime os resultados de uma lista de manipulações no console.
     *
     * @param resultados Lista de manipulações a serem impressas.
     */
    public void imprimirResultados(List<Manipulacoes> resultados) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (Manipulacoes manipulacao : resultados) {
            float dias_estoque = manipulacao.saldoatual_item_saldo / manipulacao.estoqueminimo_item_saldo;

            //faz a impressão da consulta do DB no console da aplicação
            System.out.println("DIAS: " + (Float.isNaN(dias_estoque) ? "sem_info" : dias_estoque) +
                    " | ITEM: " + manipulacao.item_item_saldo +
                    " | DESCRIÇÃO: " + manipulacao.descricao_item +
                    " | SALDO: " + manipulacao.saldoatual_item_saldo +
                    " | CONSUMO: " + manipulacao.estoqueminimo_item_saldo +
                    " | OC: " + manipulacao.codigo_pdc +
                    " | QTDE OC: " + manipulacao.qtdeaberta_pdcitem +
                    " | DATA PREVISTA: " + dateFormat.format(manipulacao.dtpreventrega_pdc));
        }
    }

    /**
     * Exporta os resultados de uma lista de manipulações para um arquivo HTML.
     *
     * @param nomeArquivo Nome do arquivo HTML de saída.
     * @param resultados Lista de manipulações a serem exportadas.
     */
    public void exportarParaHTML(String nomeArquivo, List<Manipulacoes> resultados) {
        LocalDateTime agora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // geração do HTML
        try (PrintWriter writer = new PrintWriter(nomeArquivo)) {
            writer.println("<html><head><title>Resultados " + agora.format(formatter) +"</title>");

            // estilização do HTML
            writer.println("<style>");
            writer.println("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            writer.println("th, td { padding: 10px; text-align: left; border: 1px solid #ddd; }");
            writer.println("th { background-color: #f2f2f2; }");

            writer.println(".baixo { background-color: #ff6a6a; }");
            writer.println(".medio { background-color: #f9f94f; }");
            writer.println(".alto { background-color: #5ad65a; }");

            writer.println("</style>");

            writer.println("</head><body>");
            writer.println("<table border=\"1\">");

            writer.println("<tr>");
            String[] headers = {"DIAS", "ITEM", "DESCRIÇÃO", "SALDO", "CONSUMO", "OC", "QTDE OC", "DATA PREVISTA"};
            for (String header : headers) {
                writer.println("<th>" + header + "</th>");
            }
            writer.println("</tr>");

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            for (Manipulacoes manipulacao : resultados) {
                float dias_estoque = manipulacao.saldoatual_item_saldo / manipulacao.estoqueminimo_item_saldo;
                String categoriaEstoque;

                // verificação e seleção - inserção de cor na linha
                if (Float.isNaN(dias_estoque)) {
                    categoriaEstoque = "sem_info";
                } else if (dias_estoque >= 15) {
                    categoriaEstoque = "alto";
                } else if (dias_estoque <= 14 && dias_estoque >= 7) {
                    categoriaEstoque = "medio";
                } else {
                    categoriaEstoque = "baixo";
                }

                writer.println("<tr class='" + categoriaEstoque + "'>");
                writer.println("<td>" + (Float.isNaN(dias_estoque) ? "sem_info" : dias_estoque) + "</td>");
                writer.println("<td>" + manipulacao.item_item_saldo + "</td>");
                writer.println("<td>" + manipulacao.descricao_item + "</td>");
                writer.println("<td>" + manipulacao.saldoatual_item_saldo + "</td>");
                writer.println("<td>" + manipulacao.estoqueminimo_item_saldo + "</td>");
                writer.println("<td>" + manipulacao.codigo_pdc + "</td>");
                writer.println("<td>" + manipulacao.qtdeaberta_pdcitem + "</td>");
                writer.println("<td>" + dateFormat.format(manipulacao.dtpreventrega_pdc) + "</td>");
                writer.println("</tr>");
            }

            writer.println("</table></body></html>");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método de comparação utilizado para a ordenação de instâncias com base no campo item_item_saldo.
     *
     * @param outraManipulacao Outra instância de Manipulacoes a ser comparada.
     * @return Um valor negativo se esta instância for menor, zero se igual, e um valor positivo se maior.
     */
    @Override
    public int compareTo(Manipulacoes outraManipulacao) {
        // fazendo a comparação e ordenação pelos códigos dos itens
        return Integer.compare(this.item_item_saldo, outraManipulacao.item_item_saldo);
    }
}