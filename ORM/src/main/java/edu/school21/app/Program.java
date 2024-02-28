package edu.school21.app;

import edu.school21.database.DBConect;
import edu.school21.database.OrmManager;
import edu.school21.model.User;

import java.sql.SQLException;

public class Program {
    public static void main(String[] args) {
        try {
            OrmManager ormManager = new OrmManager(DBConect.getInstance().createDatasource());
            User user = new User(1L, "Azat", "Malikov", 23);
            ormManager.save(user);
            user.setFirstName("Azatos");
            ormManager.update(user);
            System.out.println(ormManager.findById(1L, User.class).toString());
        } catch (SQLException | IllegalArgumentException | IllegalAccessException e) {
            System.out.println(e.getMessage());
        }
    }
}