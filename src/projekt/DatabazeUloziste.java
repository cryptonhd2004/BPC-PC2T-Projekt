package projekt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DatabazeUloziste {

    private static final String URL_DATABAZE = "jdbc:sqlite:firemni_databaze.db";

    public static void ulozDoSouboru(SpravceZamestnancu spravce, String nazevSouboru) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nazevSouboru))) {

            for (Zamestnanec z : spravce.getDatabaze().values()) {
                String typ = "SPECIALISTA";
                if (z instanceof DatovyAnalytik) {
                    typ = "ANALYTIK";
                }

                writer.write("ZAMESTNANEC;" + z.getId() + ";" + typ + ";" + z.getJmeno() + ";" + z.getPrijmeni() + ";" + z.getRokNarozeni());
                writer.newLine();

                for (Map.Entry<Integer, UrovenSpoluprace> vazba : z.getSeznamSpolupracovniku().entrySet()) {
                    writer.write("SPOLUPRACE;" + z.getId() + ";" + vazba.getKey() + ";" + vazba.getValue());
                    writer.newLine();
                }
            }

            System.out.println("Data uspesne ulozena do textoveho souboru.");
        } catch (Exception e) {
            System.out.println("Chyba pri ukladani do souboru: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static SpravceZamestnancu nactiZeSouboru(String nazevSouboru) {
        SpravceZamestnancu spravce = new SpravceZamestnancu();
        Map<Integer, Zamestnanec> docasnaDatabaze = new TreeMap<>();
        int maxId = 0;
        int cisloRadku = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(nazevSouboru))) {
            String radek;

            while ((radek = reader.readLine()) != null) {
                cisloRadku++;

                if (radek.trim().isEmpty()) {
                    continue;
                }

                String[] casti = radek.split(";");

                try {
                    if (casti.length == 0) {
                        continue;
                    }

                    if (casti[0].equals("ZAMESTNANEC")) {
                        if (casti.length != 6) {
                            System.out.println("Preskakuji neplatny radek " + cisloRadku + ": spatny pocet polozek pro ZAMESTNANEC.");
                            continue;
                        }

                        int id = Integer.parseInt(casti[1]);
                        String typ = casti[2];
                        String jmeno = casti[3];
                        String prijmeni = casti[4];
                        int rokNarozeni = Integer.parseInt(casti[5]);

                        if (!typ.equals("ANALYTIK") && !typ.equals("SPECIALISTA")) {
                            System.out.println("Preskakuji neplatny radek " + cisloRadku + ": neznamy typ zamestnance.");
                            continue;
                        }

                        if (docasnaDatabaze.containsKey(id)) {
                            System.out.println("Preskakuji neplatny radek " + cisloRadku + ": duplicitni ID zamestnance.");
                            continue;
                        }

                        Zamestnanec novy;
                        if (typ.equals("ANALYTIK")) {
                            novy = new DatovyAnalytik(id, jmeno, prijmeni, rokNarozeni);
                        } else {
                            novy = new BezpecnostniSpecialista(id, jmeno, prijmeni, rokNarozeni);
                        }

                        docasnaDatabaze.put(id, novy);

                        if (id > maxId) {
                            maxId = id;
                        }
                    } else if (casti[0].equals("SPOLUPRACE")) {
                        if (casti.length != 4) {
                            System.out.println("Preskakuji neplatny radek " + cisloRadku + ": spatny pocet polozek pro SPOLUPRACE.");
                            continue;
                        }

                        int idZamestnance = Integer.parseInt(casti[1]);
                        int idKolegy = Integer.parseInt(casti[2]);
                        UrovenSpoluprace uroven = UrovenSpoluprace.valueOf(casti[3]);

                        Zamestnanec z = docasnaDatabaze.get(idZamestnance);
                        if (z == null) {
                            System.out.println("Preskakuji neplatny radek " + cisloRadku + ": zamestnanec pro spolupraci neexistuje.");
                            continue;
                        }

                        z.pridejSpolupracovnika(idKolegy, uroven);
                    } else {
                        System.out.println("Preskakuji neplatny radek " + cisloRadku + ": neznamy typ zaznamu.");
                    }
                } catch (Exception e) {
                    System.out.println("Preskakuji neplatny radek " + cisloRadku + ": " + e.getMessage());
                }
            }

            spravce.setDatabaze(docasnaDatabaze);
            spravce.setDalsiId(maxId + 1);
            System.out.println("Data uspesne nactena z textoveho souboru.");
        } catch (Exception e) {
            System.out.println("Chyba pri nacitani ze souboru: " + e.getMessage());
            e.printStackTrace();
        }

        return spravce;
    }

    public static void ulozDoSql(SpravceZamestnancu spravce) {
        try {
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = DriverManager.getConnection(URL_DATABAZE);
                 Statement stmt = conn.createStatement()) {

                stmt.execute("CREATE TABLE IF NOT EXISTS zamestnanci (id INTEGER PRIMARY KEY, typ INTEGER, jmeno TEXT, prijmeni TEXT, rok_narozeni INTEGER)");
                stmt.execute("CREATE TABLE IF NOT EXISTS spoluprace (id_zamestnance INTEGER, id_kolegy INTEGER, uroven TEXT)");

                stmt.execute("DELETE FROM zamestnanci");
                stmt.execute("DELETE FROM spoluprace");

                try (PreparedStatement insertZam = conn.prepareStatement(
                        "INSERT INTO zamestnanci (id, typ, jmeno, prijmeni, rok_narozeni) VALUES (?, ?, ?, ?, ?)");
                     PreparedStatement insertSpol = conn.prepareStatement(
                        "INSERT INTO spoluprace (id_zamestnance, id_kolegy, uroven) VALUES (?, ?, ?)")) {

                    for (Zamestnanec z : spravce.getDatabaze().values()) {
                        insertZam.setInt(1, z.getId());
                        insertZam.setInt(2, (z instanceof DatovyAnalytik) ? 1 : 2);
                        insertZam.setString(3, z.getJmeno());
                        insertZam.setString(4, z.getPrijmeni());
                        insertZam.setInt(5, z.getRokNarozeni());
                        insertZam.executeUpdate();
                    }

                    Set<String> ulozeneVazby = new HashSet<>();

                    for (Zamestnanec z : spravce.getDatabaze().values()) {
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
                }

                System.out.println("Data byla uspesne zalohovana do SQL databaze.");
            }
        } catch (Exception e) {
            System.out.println("Chyba SQL ukladani: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static SpravceZamestnancu nactiZSql() {
        SpravceZamestnancu spravce = new SpravceZamestnancu();
        int maxId = 0;

        try {
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = DriverManager.getConnection(URL_DATABAZE);
                 Statement stmt = conn.createStatement()) {

                stmt.execute("CREATE TABLE IF NOT EXISTS zamestnanci (id INTEGER PRIMARY KEY, typ INTEGER, jmeno TEXT, prijmeni TEXT, rok_narozeni INTEGER)");
                stmt.execute("CREATE TABLE IF NOT EXISTS spoluprace (id_zamestnance INTEGER, id_kolegy INTEGER, uroven TEXT)");

                try (ResultSet rsZam = stmt.executeQuery("SELECT * FROM zamestnanci")) {
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
                }

                try (ResultSet rsSpol = stmt.executeQuery("SELECT * FROM spoluprace")) {
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
                }

                spravce.setDalsiId(maxId + 1);
                System.out.println("Data uspesne obnovena z SQL databaze.");
            }
        } catch (Exception e) {
            System.out.println("SQL databaze zatim neexistuje nebo se nepodarilo nacist zalohu.");
            e.printStackTrace();
        }

        return spravce;
    }
}