package projekt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DatabazeUloziste {

    private static final String URL_DATABAZE = "jdbc:sqlite:firemni_databaze.db";

    public static void ulozDoSouboru(SpravceZamestnancu spravce, String nazevSouboru) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nazevSouboru))) {
            oos.writeObject(spravce);
            System.out.println("Data uspesne ulozena do souboru: " + nazevSouboru);
        } catch (IOException e) {
            System.out.println("Chyba pri ukladani do souboru: " + e.getMessage());
        }
    }

    public static SpravceZamestnancu nactiZeSouboru(String nazevSouboru) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(nazevSouboru))) {
            SpravceZamestnancu spravce = (SpravceZamestnancu) ois.readObject();
            System.out.println("Data uspesne nactena ze souboru.");
            return spravce;
        } catch (FileNotFoundException e) {
            System.out.println("Soubor nenalezen. Bude vytvorena nova databaze.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Chyba pri nacitani ze souboru: " + e.getMessage());
        }
        return new SpravceZamestnancu();
    }

    public static void ulozDoSql(SpravceZamestnancu spravce) {
        try (Connection conn = DriverManager.getConnection(URL_DATABAZE)) {
            Statement stmt = conn.createStatement();

            stmt.execute("CREATE TABLE IF NOT EXISTS zamestnanci (id INTEGER PRIMARY KEY, typ INTEGER, jmeno TEXT, prijmeni TEXT, rok_narozeni INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS spoluprace (id_zamestnance INTEGER, id_kolegy INTEGER, uroven TEXT)");

            stmt.execute("DELETE FROM zamestnanci");
            stmt.execute("DELETE FROM spoluprace");

            PreparedStatement insertZam = conn.prepareStatement(
                    "INSERT INTO zamestnanci (id, typ, jmeno, prijmeni, rok_narozeni) VALUES (?, ?, ?, ?, ?)");
            PreparedStatement insertSpol = conn.prepareStatement(
                    "INSERT INTO spoluprace (id_zamestnance, id_kolegy, uroven) VALUES (?, ?, ?)");

            Map<Integer, Zamestnanec> databaze = spravce.getDatabaze();
            Set<String> ulozeneVazby = new HashSet<>();

            for (Zamestnanec z : databaze.values()) {
                insertZam.setInt(1, z.getId());
                insertZam.setInt(2, (z instanceof DatovyAnalytik) ? 1 : 2);
                insertZam.setString(3, z.getJmeno());
                insertZam.setString(4, z.getPrijmeni());
                insertZam.setInt(5, z.getRokNarozeni());
                insertZam.executeUpdate();

                for (Map.Entry<Integer, UrovenSpoluprace> vazba : z.getSeznamSpolupracovniku().entrySet()) {
                    int id1 = z.getId();
                    int id2 = vazba.getKey();

                    int mensi = Math.min(id1, id2);
                    int vetsi = Math.max(id1, id2);
                    String klic = mensi + "-" + vetsi;

                    if (!ulozeneVazby.contains(klic)) {
                        insertSpol.setInt(1, mensi);
                        insertSpol.setInt(2, vetsi);
                        insertSpol.setString(3, vazba.getValue().name());
                        insertSpol.executeUpdate();
                        ulozeneVazby.add(klic);
                    }
                }
            }

            System.out.println("Data byla uspesne zalohovana do SQL databaze.");
        } catch (SQLException e) {
            System.out.println("Chyba SQL ukladani: " + e.getMessage());
        }
    }

    public static SpravceZamestnancu nactiZSql() {
        SpravceZamestnancu spravce = new SpravceZamestnancu();
        int maxId = 0;

        try (Connection conn = DriverManager.getConnection(URL_DATABAZE)) {
            Statement stmt = conn.createStatement();

            stmt.execute("CREATE TABLE IF NOT EXISTS zamestnanci (id INTEGER PRIMARY KEY, typ INTEGER, jmeno TEXT, prijmeni TEXT, rok_narozeni INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS spoluprace (id_zamestnance INTEGER, id_kolegy INTEGER, uroven TEXT)");

            ResultSet rsZam = stmt.executeQuery("SELECT * FROM zamestnanci");
            while (rsZam.next()) {
                int id = rsZam.getInt("id");
                int typ = rsZam.getInt("typ");
                String jmeno = rsZam.getString("jmeno");
                String prijmeni = rsZam.getString("prijmeni");
                int rok = rsZam.getInt("rok_narozeni");

                Zamestnanec novy = (typ == 1)
                        ? new DatovyAnalytik(id, jmeno, prijmeni, rok)
                        : new BezpecnostniSpecialista(id, jmeno, prijmeni, rok);

                spravce.getDatabaze().put(id, novy);
                if (id > maxId) {
                    maxId = id;
                }
            }

            ResultSet rsSpol = stmt.executeQuery("SELECT * FROM spoluprace");
            while (rsSpol.next()) {
                int idZamestnance = rsSpol.getInt("id_zamestnance");
                int idKolegy = rsSpol.getInt("id_kolegy");
                UrovenSpoluprace uroven = UrovenSpoluprace.valueOf(rsSpol.getString("uroven"));

                Zamestnanec z1 = spravce.getDatabaze().get(idZamestnance);
                Zamestnanec z2 = spravce.getDatabaze().get(idKolegy);

                if (z1 != null && z2 != null) {
                    z1.pridejSpolupracovnika(idKolegy, uroven);
                    z2.pridejSpolupracovnika(idZamestnance, uroven);
                }
            }

            spravce.setDatabaze(spravce.getDatabaze(), maxId + 1);
            System.out.println("Data uspesne obnovena z SQL databaze.");
        } catch (SQLException e) {
            System.out.println("SQL databaze zatim neexistuje nebo je chybna. Zacina se s cistym stitem.");
        }

        return spravce;
    }
}