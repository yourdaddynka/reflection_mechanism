package edu.school21.database;

import edu.school21.service.ImplManager;
import edu.school21.service.OrmColumn;
import edu.school21.service.OrmColumnId;
import edu.school21.service.OrmEntity;
import org.reflections.Reflections;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class OrmManager implements ImplManager {
    private static final String dropTable = "DROP TABLE if exists ";
    private final DataSource dataSource;

    public OrmManager(DataSource dataSource) {
        this.dataSource = dataSource;
        init();
    }

    private String getSQLFormat(String type) {
        switch (type.toLowerCase()) {
            case "string":
                return "VARCHAR";
            case "double":
            case "float":
                return "NUMERIC";
            case "int":
            case "integer":
            case "long":
                return "INT";
            default:
                throw new RuntimeException("ошибочный тип загружаемых в базу данных: " + type.toLowerCase() + " неудачно конвертирован");
        }
    }


    private String getTable(String tableName, Class<?> classes) {
        Field[] fields = classes.getDeclaredFields();
        StringBuilder resTableString = new StringBuilder().append("CREATE TABLE ").append(tableName).append("(\n"); //CREATE TABLE Users(
        for (int i = 0; i < fields.length; i++) {
            resTableString.append('\t').append(fields[i].getName()).append('\t');//название переменной
            if (fields[i].isAnnotationPresent(OrmColumnId.class)) {
                resTableString.append("BIGSERIAL PRIMARY KEY");
            } else if (fields[i].isAnnotationPresent(OrmColumn.class)) {
                resTableString.append(getSQLFormat(fields[i].getType().getSimpleName())).append(" NOT NULL");
            }
            if (i != fields.length - 1) {
                resTableString.append(",\n");
            }
        }
        return resTableString.append("\n);").toString();
    }

    private void createTable(String tableName, Class<?> classes) throws SQLException {
        System.out.println(getTable(tableName, classes));
        dataSource.getConnection().createStatement().execute(getTable(tableName, classes));
    }

    private void dropsTable(String tableName) throws SQLException {
        System.out.println(dropTable + tableName + ';');
        dataSource.getConnection().createStatement().execute(dropTable + tableName + ';');
    }

    private void init() {
        Reflections reflections = new Reflections("edu.school21.model");
        Set<Class<?>> annotated = reflections.get(SubTypes.of(TypesAnnotated.with(OrmEntity.class)).asClass());
        for (Class<?> thisClas : annotated) {
            String tableName = thisClas.getAnnotation(OrmEntity.class).table();
            try {
                dropsTable(tableName);
                createTable(tableName, thisClas);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String getStringValue(Field field, Object entity) throws IllegalAccessException {
        StringBuilder result = new StringBuilder();
        if (field.isAnnotationPresent(OrmColumnId.class)) {
            result.append(field.get(entity));
        } else if (field.isAnnotationPresent(OrmColumn.class)) {
            if (getSQLFormat(field.getType().getSimpleName()).equals("VARCHAR")) {
                result.append("'").append(field.get(entity)).append("'");
            } else result.append(field.get(entity));
        }
        return result.toString();
    }

    @Override
    public void save(Object entity) throws SQLException, IllegalAccessException {
        String tableName = entity.getClass().getAnnotation(OrmEntity.class).table();
        StringBuilder request = new StringBuilder().append("INSERT INTO ").append(tableName).append(" VALUES (");
        Field[] fields = entity.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            request.append(getStringValue(fields[i], entity));
            if (i != fields.length - 1) {
                request.append(",\n");
            }
        }
        request.append(");");
        System.out.println(request);
        dataSource.getConnection().createStatement().executeUpdate(request.toString());
    }

    @Override
    public void update(Object entity) throws IllegalAccessException, SQLException {
        String tableName = entity.getClass().getAnnotation(OrmEntity.class).table();
        StringBuilder request = new StringBuilder().append("UPDATE ").append(tableName).append(" SET ");
        Field[] fields = entity.getClass().getDeclaredFields();
        int idIt = 0;
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            if (fields[i].isAnnotationPresent(OrmColumnId.class)) {
                idIt = i;
            } else {
                request.append(fields[i].getName()).append(" = ").append(getStringValue(fields[i], entity));
                if (i != fields.length - 1) {
                    request.append(",\n");
                }
            }
        }
        request.append("\nWHERE ").append(fields[idIt].getName()).append(" = ").append(fields[idIt].get(entity)).append(';');
        dataSource.getConnection().createStatement().executeUpdate(request.toString());
    }

    private Object getClassObject(ResultSet res, Field field) throws SQLException {
        if (field.getType().equals(String.class)) return res.getString(field.getName());
        else if (field.getType().equals(Double.class)) return res.getDouble(field.getName());
        else if (field.getType().equals(Long.class)) return res.getLong(field.getName());
        else if (field.getType().equals(Integer.class)) return res.getInt(field.getName());
        throw new RuntimeException("Ошибка: тип данных " + field.getType() + " из запроса не найден внутри класса " + field.getClass());
    }

    @Override
    public <T> T findById(Long id, Class<T> aClass) throws SQLException, IllegalAccessException {
        if (!aClass.isAnnotationPresent(OrmEntity.class)) {
            return null;
        }
        String request = "SELECT * FROM " + aClass.getAnnotation(OrmEntity.class).table() + " WHERE id = " + id + ";";
        ResultSet res = dataSource.getConnection().createStatement().executeQuery(request);
        if (res.next()) {
            if (res.getRow() != 1) {
                throw new RuntimeException("Ошибка: в БД текущий ID " + id + " не уникальный.Ошибка в БД");
            }
            Field[] fields = aClass.getDeclaredFields();
            Class<?>[] constructorParams = new Class[fields.length];
            Object[] constructorValues = new Object[fields.length];
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                constructorParams[i] = fields[i].getType();
                constructorValues[i] = getClassObject(res, fields[i]);
            }
            try {
                Constructor<T> constructor = aClass.getDeclaredConstructor(constructorParams);
                constructor.setAccessible(true);
                return constructor.newInstance(constructorValues);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                System.out.println(e.getMessage());
            }
        }
        throw new RuntimeException("ID не существует в БД");
    }

}
