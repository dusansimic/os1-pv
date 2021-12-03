package zadatak04;

import java.util.ArrayList;
import java.util.List;

/*
 * Napisati program koji kreira jednu Deda Mrazovu radionicu poklona i cetiri
 * vilenjaka. Vilenjaci koji rade u radionici se zovu Maglor, Mrazdalf,
 * Vetromir i Snegolas i definisani su kao zasebni procesi. Prva dva vilenjaka
 * su definisana pomocu klase Thread kao korisnicki procesi, a druga dva pomocu
 * interfejsa Runnable kao pozadinski procesi. (5 poena)
 * 
 * Radni dan prva dva vilenjaka se sastoji samo od proizvodnje igracaka, pri
 * cemu svako od njih moze u toku dana da napravi po 50 igracaka, posle cega
 * zavrsavaju svoj radni dan. (5 poena)
 * 
 * Druga dva vilenjaka po ceo dan pakuju poklone u koje stavljaju po jednu
 * igracku (pozivajuci metod Igracka::upakuj). (5 poena)
 * 
 * Sinhronizovati klasu Radionica tako da se ni u kom slucaju ne izgube
 * igracke. Takodje, blokirati vilenjaka koji pokusa da upakuje poklon bez
 * igracke ili napravi novu igracku ako je radionica puna. Na stolu u radionici
 * moze da stane najvise 10 igracaka. Odblokirati vilenjake cim se stvore
 * uslovi za nastavak njihovog rada. (10 poena)
 * 
 * Obratiti paznju na elegantnost i objektnu orijentisanost realizacije i stil
 * resenja. Za program koji se ne kompajlira, automatski se dobija 0 poena bez
 * daljeg pregledanja.
 */

public class Program {

	public static void main(String[] args) {
		Radionica radionica = new Radionica();

		Proizvodjac maglor = new Proizvodjac(radionica);
		maglor.setName("Maglor");
		Proizvodjac mrazdalf = new Proizvodjac(radionica);
		mrazdalf.setName("Mrazdalf");
		
		Thread vetromir = new Thread(new Paker(radionica));
		vetromir.setName("Vetromir");
		vetromir.setDaemon(true);
		Thread snegolas = new Thread(new Paker(radionica));
		snegolas.setName("Vetromir");
		snegolas.setDaemon(true);
		
		maglor.start();
		mrazdalf.start();
		vetromir.start();
		snegolas.start();
	}

}

class Proizvodjac extends Thread {
	
	Radionica radionica;
	
	public Proizvodjac(Radionica radionica) {
		this.radionica = radionica;
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < 50; i++) {
					radionica.staviNaSto(new Igracka());
			}
		} catch (InterruptedException e) {}
	}
	
}

class Paker implements Runnable {
	
	Radionica radionica;
	
	public Paker(Radionica radionica) {
		this.radionica = radionica;
	}

	@Override
	public void run() {
		try {
			while (true) {
				radionica.uzmiSaStola().upakuj();
			}
		} catch (InterruptedException e) {}
	}
	
}

class Radionica {
	
	List<Igracka> igracke = new ArrayList<Igracka>();
	
	public synchronized void staviNaSto(Igracka igracka) throws InterruptedException {
		while (igracke.size() + 1 > 10) {
			System.out.println(Thread.currentThread().getName() + " ceka da se oslobodi mesta na stolu.");
			wait();
		}
		
		igracke.add(igracka);
		notifyAll();
	}
	
	public synchronized Igracka uzmiSaStola() throws InterruptedException {
		while (igracke.size() - 1 < 0) {
			System.out.println(Thread.currentThread().getName() + " ceka da se napravi jos igracaka.");
			wait();
		}
		
		Igracka igracka = igracke.remove(0);
		notifyAll();
		return igracka;
	}
}

class Igracka {

	public Igracka() {
		String boja = BOJE[(int) (BOJE.length * Math.random())];
		String zivotinja = ZIVOTINJE[(int) (ZIVOTINJE.length * Math.random())];
		this.opis = boja + " " + zivotinja;
		System.out.println(Thread.currentThread().getName() + " pravi " + opis + ".");
		try {
			Thread.sleep((long) (500 + 500 * Math.random()));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void upakuj() {
		System.out.println(Thread.currentThread().getName() + " pakuje " + opis + ".");
		try {
			Thread.sleep((long) (500 + 500 * Math.random()));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static final String[] BOJE = { "Plavog", "Crvenog", "Zelenog", "Belog", "Zlatnog", };

	private static final String[] ZIVOTINJE = { "medu", "zeku", "papagaja", "irvasa", "lava", };

	private final String opis;
}
