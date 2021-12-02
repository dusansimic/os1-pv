package vezbe02;

/*
 * Data je implementacija beskonacnog spila karata (klase 'Spil' i 'Karta').
 * 
 * Napraviti nit koja uzima jednu po jednu kartu iz spila i deli ih drugim
 * nitima.
 * 
 * Napraviti 12 niti koje predstavljaju igrace. One cekaju da dobiju kartu od
 * dilera, koju potom ispisuju na ekranu i zavrsavaju svoj rad.
 * 
 * Glavna nit kreira i pokrece sve ostale niti posle cega zavrsava svoj rad.
 */

import java.util.Random;

class Karta {

	private String vrednost;

	public Karta(String vrednost) {
		this.vrednost = vrednost;
	}

	@Override
	public String toString() {
		return vrednost;
	}
}

class Spil {

	private static final String[] BOJE = "\u2660,\u2665,\u2666,\u2663".split(",");
	private static final String[] RANGOVI = "2,3,4,5,6,7,8,9,10,J,Q,K,A".split(",");
	private static final String[] DZOKERI = "\u2605,\u2606".split(",");
	private static final Random random = new Random();

	public Karta uzmi() {
		int id = random.nextInt(54);
		if (id == 53) {
			return new Karta(DZOKERI[0]);
		}
		if (id == 52) {
			return new Karta(DZOKERI[1]);
		}
		String boja = BOJE[id / 13];
		String rang = RANGOVI[id % 13];
		return new Karta(rang + boja);
	}
}

class Igrac extends Thread {
	
	private String ime;
	private volatile Karta karta;
	
	public Igrac(String ime) {
		this.ime = ime;
	}

	@Override
	public void run() {
		while (karta == null);
		System.out.println(karta);
	}
	
	public void preuzmiKartu(Karta karta) {
		this.karta = karta;
	}
	
}

class Diler extends Thread {
	
	private Igrac[] igraci;
	private Spil spil;
	
	public Diler(Igrac[] igraci, Spil spil) {
		this.igraci = igraci;
		this.spil = spil;
	}

	@Override
	public void run() {
		for (Igrac igrac : igraci) {
			igrac.preuzmiKartu(spil.uzmi());
		}
	}
	
}

public class Program {

	public static void main(String[] args) {
		Igrac[] igraci = new Igrac[12];
		for (int i = 0; i < 12; i++) {
			igraci[i] = new Igrac(String.format("Igrac_%d", i+1));
			igraci[i].start();
		}
		
		Spil spil = new Spil();
		
		Diler diler = new Diler(igraci, spil);
		
		diler.start();
	}

}
