package backupPostgre;

import java.io.*;
import java.nio.file.*;
import java.util.logging.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import java.sql.Connection;

public class Funcoes {
    private static final String caminhoDestino = "C:/Temp/Backup/"; //deve existir antes de rodar o código
    private static final String zipPassword = "senha123";
    private static final Logger logger = Logger.getLogger("BackupLog");

    public Funcoes() {
        try {
            FileHandler fileHandler = new FileHandler(caminhoDestino + "backup_log.log", true);
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void criarLog(String message) {
        logger.info(message);
    }

    public void limpezaBanco(boolean vacuum, boolean full, boolean reindex) throws Exception {
        criarLog("Executando limpeza do banco de dados...");
        if (vacuum) {
            criarLog("Executando VACUUM...");
        }
        if (full) {
            criarLog("Executando FULL...");
        }
        if (reindex) {
            criarLog("Executando REINDEX...");
        }
    }

    public void processoIniciar() throws Exception {
        Connection conn = DatabaseConnection.conectar();

        if (conn != null) {
            criarLog("Iniciando processo de backup...");

            String usuario = "seu_usuario"; // defina seu usuário do postgre aqui
            String senha = "sua_senha"; // defina sua senha do postgre aqui

            String[] comandoBackup = {
                "pg_dump", 
                "-h", "localhost", 
                "-U", usuario, 
                "-F", "c", 
                "-b", 
                "-v", 
                "-f", caminhoDestino + "backup.sql"
            };

            Map<String, String> env = System.getenv();
            ProcessBuilder processBuilder = new ProcessBuilder(comandoBackup);
            processBuilder.environment().put("PGPASSWORD", senha);
            processBuilder.directory(new File(caminhoDestino));
            processBuilder.redirectErrorStream(true);

            Process processo = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(processo.getInputStream()))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    criarLog(linha);
                }
            }

            int exitCode = processo.waitFor();
            if (exitCode == 0) {
                criarLog("Backup concluído com sucesso.");
            } else {
                throw new Exception("Erro ao executar o backup. Código de saída: " + exitCode);
            }

            conn.close();
        } else {
            criarLog("Falha na conexão ao banco de dados.");
        }
    }


    public void backupCriptografar() throws Exception {
        criarLog("Criptografando backup...");
        String arquivoBackup = caminhoDestino + "backup.sql";
        String arquivoCriptografado = caminhoDestino + "backup_encrypted.aes";

        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec key = new SecretKeySpec("senha123456789012".getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        try (FileInputStream fis = new FileInputStream(arquivoBackup);
             FileOutputStream fos = new FileOutputStream(arquivoCriptografado);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, len);
            }
        }

        criarLog("Criptografia concluída.");
    }

    public void backupCompactar() throws IOException {
        criarLog("Compactando backup com senha...");

        String arquivoCriptografado = caminhoDestino + "backup_encrypted.aes";
        String arquivoZip = caminhoDestino + "backup.zip";

        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);

        try (ZipFile zipFile = new ZipFile(arquivoZip, zipPassword.toCharArray())) {
            zipFile.addFile(new File(arquivoCriptografado), zipParameters);
        }
        criarLog("Compactação concluída.");
    }

    public void antigosExcluir() {
        criarLog("Excluindo backups antigos...");
    }

    public void backupCopiar() throws IOException {
        criarLog("Copiando backup para outro local...");
        Path source = Paths.get(caminhoDestino + "backup.zip");
        Path destination = Paths.get("D:/Backup/Copia/backup.zip");

        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        criarLog("Cópia concluída.");
    }

    public void emailErroEnviar(Exception e) {
        criarLog("Enviando e-mail de erro...");

        String to = "destinatario@exemplo.com";
        String from = "remetente@exemplo.com";
        String host = "smtp.exemplo.com";

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);

        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Erro no processo de backup");
            message.setText("Ocorreu um erro no processo de backup:\n\n" + e.getMessage());
            Transport.send(message);
            criarLog("E-mail de erro enviado.");
        } catch (MessagingException mex) {
            criarLog("Erro ao enviar e-mail: " + mex.getMessage());
        }
    }

    public void processoFinalizar() {
        criarLog("Finalizando processo de backup...");
    }
}
