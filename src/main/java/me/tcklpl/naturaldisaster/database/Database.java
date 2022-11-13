package me.tcklpl.naturaldisaster.database;

import me.tcklpl.naturaldisaster.NaturalDisaster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

    private Connection connection;
    private boolean ok;

    public Database(String ip, int port, String databaseName, String user, String pass) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + databaseName + "?useSSL=false", user, pass);
            if (connection != null) {
                NaturalDisaster.getMainReference().getLogger().info("Conexão aberta com o banco de dados");
                ok = true;
            }
            else {
                NaturalDisaster.getMainReference().getLogger().warning("Falha ao abrir conexão com o banco de dados");
                ok = false;
            }
        } catch (SQLException | ClassNotFoundException e) {
            NaturalDisaster.getMainReference().getLogger().warning("Falha ao abrir conexão com o banco de dados");
            ok = false;
        }
    }

    public boolean isOk() {
        return ok;
    }

    public void insert(String table, String[] fields, Object[] values) throws SQLException {

        if (!ok) return;

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

        PreparedStatement stmt = connection.prepareStatement(sql.toString());
        for (int i = 0; i < values.length; i++) {
            stmt.setObject(i + 1, values[i]);
        }
        stmt.executeUpdate();
    }

    public void update(String table, String[] fields, Object[] values, String[] discriminatorFields, Object[] discriminatorKeys) throws SQLException {

        if (!ok) return;

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(table);
        sql.append(" SET ");
        for (String field : fields) {
            sql.append(field);
            sql.append("=?,");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ");

        for (String field : discriminatorFields) {
            sql.append(field);
            sql.append("=? AND ");
        }

        for (int i = 0; i < 4; i++)
            sql.deleteCharAt(sql.length() - 1);

        PreparedStatement stmt = connection.prepareStatement(sql.toString());

        int i = 0;
        for (; i < values.length; i++) {
            stmt.setObject(i + 1, values[i]);
        }

        for (int j = 0; j < discriminatorKeys.length; j++) {
            stmt.setObject(i++ + 1, values[j]);
        }

        stmt.executeUpdate();
    }

}
