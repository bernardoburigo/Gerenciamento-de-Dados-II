package backupPostgre;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.conectar();

            if (conn != null) {
                Funcoes funcoes = new Funcoes();

                funcoes.criarLog("Iniciando o processo de backup...");
                funcoes.limpezaBanco(true, true, true);
                funcoes.processoIniciar();
                funcoes.backupCriptografar();
                funcoes.backupCompactar();
                funcoes.antigosExcluir();
                funcoes.backupCopiar();
                funcoes.processoFinalizar();

                funcoes.criarLog("Processo de backup finalizado com sucesso.");
            } else {
                Funcoes funcoes = new Funcoes();
                funcoes.criarLog("Falha na conexão ao banco de dados.");
            }
        } catch (Exception e) {
            Funcoes funcoes = new Funcoes();
            funcoes.emailErroEnviar(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    Funcoes funcoes = new Funcoes();
                    funcoes.emailErroEnviar(e);
                }
            }
        }
    }
}
