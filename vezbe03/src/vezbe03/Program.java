package vezbe03;

import java.util.LinkedList;
import java.util.Random;
import java.util.Arrays;

/* Uzeti implementaciju klasa 'Karta' i 'Spil' iz prvog zadatka i adaptirati ih
 * tako da se mogu koristiti od strane vise procesa istovremeno.
 * 
 * Napraviti i pokrenuti 12 niti koje predstavljaju igrace. Svaka nit uzima
 * jednu kartu sa vrha spila i smesta je u svoje privatno polje. Potom tu kartu
 * stavlja na talon (videti ispod) i ceka da to urade i svi ostali igraci.
 * 
 * Kada su svi igraci stavili svoje karte na talon, nastavljaju izvrsavanje.
 * Svako samostalno proverava da li je imao najjacu kartu i stampa prigodnu
 * poruku o tome. Moze biti vise igraca sa najjacom kartom, gleda se samo rang
 * karte kao i u prethodnim zadacima.
 * 
 * Implementirati klasu 'Talon' koja ima sledece metode i koristiti je za
 * sinhronizaciju igraca:
 * 
 *   void staviKartu(Karta)   - pomocu koje igrac stavlja kartu na talon
 *   void cekajOstale()       - blokira nit dok se na talon ne stavi 12 karata
 *                              ovaj metod baca InterruptedException ako neko
 *                              prekine nit u toku ovog cekanja
 *   boolean jeNajjaca(Karta) - utvrdjuje da li je prosledejna karta najjaca
 * 
 * Glavna nit kreira spil i talon, pokrece sve ostale niti, posle cega zavrsava
 * svoj rad.
 */

class Karta implements Comparable<Karta> {

	enum Boja {
		PIK, KARO, HERC, TREF, U_BOJI, BEZBOJAN
	}
	
	enum Rang {
		R2, R3, R4, R5, R6, R7, R8, R9, R10, ZANDAR, KRALJICA, KRALJ, KEC, DZOKER
	}

	private Boja boja;
	private Rang rang;

	public Karta(Boja boja, Rang rang) {
		this.boja = boja;
		this.rang = rang;
	}

	@Override
	public int compareTo(Karta karta) {
		return rang.compareTo(karta.rang);
	}

	@Override
	public String toString() {
		return rang == Rang.DZOKER ? "[" + boja + "]" : "[" + boja + ", " + rang + "]";
	}
}

class Spil {

	private static final Random random = new Random();

	public Karta uzmi() {
		int id = random.nextInt(54);
		if (id == 53) {
			return new Karta(Karta.Boja.BEZBOJAN, Karta.Rang.DZOKER);
		}
		if (id == 52) {
			return new Karta(Karta.Boja.U_BOJI, Karta.Rang.DZOKER);
		}
		
		Karta.Boja boja = Arrays.copyOf(Karta.Boja.values(), 4)[id / 13];
		Karta.Rang rang = Arrays.copyOf(Karta.Rang.values(), 13)[id % 13];
		return new Karta(boja, rang);
	}
}

class Igrac extends Thread {
	
	private String ime;
	private Spil spil;
	private Talon talon;
	private volatile Karta karta;
	
	public Igrac(String ime, Spil spil, Talon talon) {
		this.ime = ime;
		this.spil = spil;
		this.talon = talon;
	}

	@Override
	public void run() {
		this.karta = spil.uzmi();
		talon.staviKartu(karta);
		talon.cekajOstale();
		System.out.println(String.format("%s je uzeo %s i ona %s najjaca.", ime, karta, talon.jeNajjaca(karta) ? "jeste" : "nije"));
	}
	
}

class Talon extends Thread {
	
	private LinkedList<Karta> karte;
	private int br;
	
	public Talon(int br) {
		karte = new LinkedList<>();
		this.br = br;
	}
	
	public synchronized void staviKartu(Karta karta) {
		karte.add(karta);
	}
	
	private synchronized boolean sviStigli() {
		return karte.size() != br;
	}
	
	public void cekajOstale() {
		while (sviStigli());
	}
	
	public boolean jeNajjaca(Karta karta) {
		return karte
				.stream()
				.max(Karta::compareTo)
				.get()
				.compareTo(karta) == 0;
	}
	
}

public class Program {

	public static void main(String[] args) {
		Talon talon = new Talon(12);
		talon.start();

		Spil spil = new Spil();

		Igrac[] igraci = new Igrac[12];
		for (int i = 0; i < 12; i++) {
			igraci[i] = new Igrac(String.format("Igrac_%d", i+1), spil, talon);
			igraci[i].start();
		}
	}
}