package projekt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

public class DatabazeUloziste {

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
        }
    }

    public static SpravceZamestnancu nactiZeSouboru(String nazevSouboru) {
        SpravceZamestnancu spravce = new SpravceZamestnancu();
        Map<Integer, Zamestnanec> docasnaDatabaze = new TreeMap<>();
        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(nazevSouboru))) {
            String radek;

            while ((radek = reader.readLine()) != null) {
                String[] casti = radek.split(";");

                if (casti[0].equals("ZAMESTNANEC")) {
                    int id = Integer.parseInt(casti[1]);
                    String typ = casti[2];
                    String jmeno = casti[3];
                    String prijmeni = casti[4];
                    int rokNarozeni = Integer.parseInt(casti[5]);

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
                    int idZamestnance = Integer.parseInt(casti[1]);
                    int idKolegy = Integer.parseInt(casti[2]);
                    UrovenSpoluprace uroven = UrovenSpoluprace.valueOf(casti[3]);

                    Zamestnanec z = docasnaDatabaze.get(idZamestnance);
                    if (z != null) {
                        z.pridejSpolupracovnika(idKolegy, uroven);
                    }
                }
            }

            spravce.setDatabaze(docasnaDatabaze);
            spravce.setDalsiId(maxId + 1);
            System.out.println("Data uspesne nactena z textoveho souboru.");
        } catch (Exception e) {
            System.out.println("Chyba pri nacitani ze souboru: " + e.getMessage());
        }

        return spravce;
    }
}