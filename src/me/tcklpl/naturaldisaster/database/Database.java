package me.tcklpl.naturaldisaster.database;

import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;

public class Database {

    private Connection connection;

    public Database(String url) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + url);
            if (connection != null)
                Bukkit.getLogger().info("Conexão aberta com o banco de dados");
            else Bukkit.getLogger().warning("Falha ao abrir conexão com o banco de dados");
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Falha ao abrir conexão com o banco de dados");
            e.printStackTrace();
        }
    }

    public void assertDefaults() {
        String passwords = "CREATE TABLE IF NOT EXISTS passwords (\n" +
                "uuid text PRIMARY KEY,\n" +
                "pass text NOT NULL );";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(passwords);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Falha ao criar tabelas padrões: " + e.getMessage());
        }
    }

    public void insert(String table, String[] fields, Object[] values) {

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(table);
        sql.append("(");
        for (String field : fields) {
            sql.append(field);
            sql.append(",");
        }
        // remove last ,
        sql.deleteCharAt(sql.length() - 1);
        // by this point sql looks like this:
        // INSERT INTO <table>(<params>
        sql.append(") VALUES (");
        for (Object o : values)
            sql.append("?,");
        // remove last , again
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        // by this point sql looks like this:
        // INSERT INTO <table>(<params>) VALUES (<values>)

        try {
            PreparedStatement stmt = connection.prepareStatement(sql.toString());
            for (int i = 0; i < values.length; i++) {
                // parameter index is i + 1 because preparedstatement's counting starts on 1
                stmt.getClass().getDeclaredMethod("set" + values[i].getClass().getName(), int.class, values[i].getClass()).invoke(stmt, i + 1, values[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }

}
