package backupPostgre;

public class Main {
    public static void main(String[] args) {
        try {
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
        } catch (Exception e) {
            Funcoes funcoes = new Funcoes();
            funcoes.emailErroEnviar(e);
        }
    }
}