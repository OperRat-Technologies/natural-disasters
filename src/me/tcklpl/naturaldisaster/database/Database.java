package me.tcklpl.naturaldisaster.database;

import me.tcklpl.naturaldisaster.NaturalDisaster;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private Connection connection;

    public Database(String url) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + url);
            if (connection != null)
                NaturalDisaster.getMainReference().getLogger().info("Conexão aberta com o banco de dados");
            else NaturalDisaster.getMainReference().getLogger().warning("Falha ao abrir conexão com o banco de dados");
        } catch (SQLException e) {
            NaturalDisaster.getMainReference().getLogger().warning("Falha ao abrir conexão com o banco de dados");
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
            NaturalDisaster.getMainReference().getLogger().warning("Falha ao criar tabelas padrões: " + e.getMessage());
        }
    }

    public void insert(String table, String[] fields, Object[] values) throws SQLException {

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
                stmt.getClass().getMethod("set" + values[i].getClass().getSimpleName(), int.class, values[i].getClass()).invoke(stmt, i + 1, values[i]);
            }
            stmt.executeUpdate();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public List<Object> executeSelect(String query) throws SQLException {
        List<Object> res = new ArrayList<>();
        PreparedStatement stmt = connection.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            res.add(rs.getObject(0));
        return res;
    }

}
