import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:h2:mem:testdb";
        String user = "sa";
        String pass = "";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {

            String sqlScript = new String(Files.readAllBytes(Paths.get("music-create.sql")));
            try (Statement stmt = conn.createStatement()) {
                for (String command : sqlScript.split(";")) {
                    if (!command.trim().isEmpty()) {
                        stmt.execute(command.trim());
                    }
                }
            }

            // Задание 1: Список музыкальных композиций
            System.out.println("Задание 1: Все композиции");
            printQuery(conn, "SELECT * FROM study.music");

            //Задание 2: Фильтрация композиций
            System.out.println("Задание 2: Композиции без 'm' и 't'");
            printQuery(conn, "SELECT * FROM study.music WHERE LOWER(name) NOT LIKE '%m%' AND LOWER(name) NOT LIKE '%t%'");

            // Задание 3: Добавление композиции
            System.out.println("Задание 3: Добавление своей композиции");
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO study.music (name) VALUES (?)")) {
                ps.setString(1, "My Favorite Song");
                ps.executeUpdate();
                System.out.println("Композиция 'My Favorite Song' добавлена.");
            }
            printQuery(conn, "SELECT * FROM study.music");

            //Задание 4: Загрузка данных из JSON
            System.out.println("Задание 4: Загрузка данных из JSON");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS visitors (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) UNIQUE)");
                stmt.execute("CREATE TABLE IF NOT EXISTS books (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) UNIQUE, publication_year INT)");
            }

            String jsonContent = new String(Files.readAllBytes(Paths.get("books.json")));
            JSONArray jsonArray = new JSONArray(jsonContent);

            Set<String> addedVisitors = new HashSet<>();
            Set<String> addedBooks = new HashSet<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String visitorName = obj.getString("visitor");

                if (!addedVisitors.contains(visitorName)) {
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO visitors (name) VALUES (?)")) {
                        ps.setString(1, visitorName);
                        ps.executeUpdate();
                    }
                    addedVisitors.add(visitorName);
                }

                JSONArray books = obj.getJSONArray("books");
                for (int j = 0; j < books.length(); j++) {
                    JSONObject book = books.getJSONObject(j);
                    String title = book.getString("title");
                    int year = book.getInt("year");
                    String bookKey = title + "_" + year; // Уникальный ключ для книги

                    if (!addedBooks.contains(bookKey)) {
                        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title, publication_year) VALUES (?, ?)")) {
                            ps.setString(1, title);
                            ps.setInt(2, year);
                            ps.executeUpdate();
                        }
                        addedBooks.add(bookKey);
                    }
                }
            }
            System.out.println("Данные из JSON успешно загружены без дубликатов.");

            //Задание 5: Сортировка книг
            System.out.println("Задание 5: Книги, отсортированные по году ===");
            printQuery(conn, "SELECT * FROM books ORDER BY publication_year");

            //Задание 6: Книги до 2000 года
            System.out.println("Задание 6: Книги до 2000 года");
            printQuery(conn, "SELECT * FROM books WHERE publication_year < 2000");

            //Задание 7: Добавление посетителя и книг
            System.out.println("Задание 7: Добавление себя и любимых книг");
            try (PreparedStatement psVisitor = conn.prepareStatement("INSERT INTO visitors (name) VALUES (?)");
                 PreparedStatement psBook = conn.prepareStatement("INSERT INTO books (title, publication_year) VALUES (?, ?)")) {

                psVisitor.setString(1, "Евгений Павлович");
                psVisitor.executeUpdate();

                psBook.setString(1, "Особые блюда");
                psBook.setInt(2, 2023);
                psBook.executeUpdate();
            }
            System.out.println("Добавлено. Читаем из базы:");
            printQuery(conn, "SELECT * FROM visitors");
            printQuery(conn, "SELECT * FROM books");

            // Задание 8: Удаление таблиц
            System.out.println(" Задание 8: Удаление таблиц");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS books");
                stmt.execute("DROP TABLE IF EXISTS visitors");
                System.out.println("Таблицы visitors и books успешно удалены.");
            }

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printQuery(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(metaData.getColumnName(i) + ": " + rs.getString(i) + " | ");
                }
                System.out.println();
            }
        }
    }
}