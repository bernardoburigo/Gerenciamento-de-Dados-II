package backupPostgre;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/nome_do_seu_banco"; // Substitua pelo seu banco de dados
    private static final String USER = "seu_usuario"; // Seu usuário do banco
    private static final String PASSWORD = "sua_senha"; // Sua senha do banco

    public static Connection conectar() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão estabelecida com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
        }
        return connection;
    }
}
