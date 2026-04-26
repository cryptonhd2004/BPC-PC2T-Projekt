package projekt;

import java.util.Map;

public class DatovyAnalytik extends Zamestnanec {

    private static final long serialVersionUID = 1L;

    public DatovyAnalytik(int id, String jmeno, String prijmeni, int rokNarozeni) {
        super(id, jmeno, prijmeni, rokNarozeni);
    }

    @Override
    public void spustDovednost(Map vsiZamestnanci) {
        System.out.println("--- Dovednost: Datovy Analytik (" + jmeno + ") ---");

        int maxSpolecnych = -1;
        int idNejKolegy = -1;

        for (Object mojeVazbaIdObj : seznamSpolupracovniku.keySet()) {
            Integer mojeVazbaId = (Integer) mojeVazbaIdObj;
            Zamestnanec kolega = (Zamestnanec) vsiZamestnanci.get(mojeVazbaId);

            if (kolega != null) {
                int spolecnych = 0;

                for (Object idJehoVazbyObj : kolega.getSeznamSpolupracovniku().keySet()) {
                    Integer idJehoVazby = (Integer) idJehoVazbyObj;
                    if (this.seznamSpolupracovniku.containsKey(idJehoVazby)) {
                        spolecnych++;
                    }
                }

                if (spolecnych > maxSpolecnych) {
                    maxSpolecnych = spolecnych;
                    idNejKolegy = mojeVazbaId;
                }
            }
        }

        if (idNejKolegy != -1 && maxSpolecnych > 0) {
            Zamestnanec top = (Zamestnanec) vsiZamestnanci.get(idNejKolegy);
            System.out.println("Nejvice spolecnych spolupracovniku mate s: "
                    + top.getJmeno() + " " + top.getPrijmeni());
        } else {
            System.out.println("Nemate zadne spolecne spolupracovniky.");
        }
    }
}