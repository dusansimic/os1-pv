package grupa01;

import java.util.ArrayList;
import java.util.List;

/*
 * Napisati program koji simulira rad jedne picerije.
 * 
 * Picerija ima dva radnika koji prave pice i deset radnika koji dostavljaju
 * porucene pice. Svaki radnik je predstavljen zasebnom niti.
 *
 * Data je klasa Porudzbina koja predstavlja jednu porudzbinu spremnu za
 * dostavu. Svaka porudzbina ima jedinstveni identifikator, cenu u dinarima i
 * udaljenost od picerije. Ovu klasu nije potrebno menjati.
 * 
 * Data je klasa Picerija koja predstavlja celu piceriju i koju je potrebno
 * dopuniti funkcionalnostima izlozenim u nastavku.
 * 
 * Dostavljaci su definisani pomocu klase Thread kao korisnicke niti. Oni ceo
 * svoj radni dan uzimaju porucbine iz picerije (pozivaju metod preuzmi()
 * picerije) i realizuju ih (pozivaju metod dostavi() preuzete porudzbine).
 * Ove niti se ne obaziru na prekide i svoj rad zavrsavaju kada odrade
 * 25 dostava. (5 poena)
 * 
 * Radnici koji prave pice su definisani pomocu interfejsa Runnable kao
 * pozadinske niti. Oni svo vreme pripremaju porucene pice za dostavu (pozivaju
 * konstruktor da naprave novu porudzbinu) i ovako napravljene porudzbine cine
 * dostupnim radnicima za dostavu (pozivaju metod spremno() picerije). Izmedju
 * svaka dva pripremanja porudzbina, ovi radnici dremnu sekundu-dve da se malo
 * odmore. Ove niti se ne obaziru na prekide i svo vreme pripremaju nove
 * porudzbine. (5 poena)
 * 
 * Glavna nit na pocetku kreira sve radnike i potrebne pomocne objekte. Takodje,
 * svim radnicima dodaljuje unapred smisljena imena. (5 poena)
 * 
 * Realizovati potrebnu sinhronizaciju oko primopredaje porudzbina tako da se
 * svaka od prvih 250 porudzbina realizuje tacno jednom, a sve ostale porudzbine
 * ne realizuju. (5 poena)
 * 
 * Pre zavrsetka svog rada, svaki dostavljac ispisuje koliko je ukupno novca
 * dobio za realizovane dostave, kao i koliko od toga je baksis. Metod koji
 * realizuje dostavu porudzbine, vraca ukupnu kolicinu novca koju je dostavljac
 * dobio za tu porudzbinu. Baksis je razlika u odnosu na cenu te porudzbine.
 * Glavna nit ne ispisuje nista, vec zavrsava svoj rad odmah po kreiranju
 * svih radnika. (5 poena)
 * 
 * Obratiti paznju na elegantnost i objektnu orijentisanost realizacije i stil
 * resenja. Za program koji se ne kompajlira, automatski se dobija 0 poena bez
 * daljeg pregledanja.
 */

public class Program {

	public static void main(String[] args) {
		Picerija picerija = new Picerija();
		
		Thread mario = new Thread(new Kuvar(picerija));
		mario.setDaemon(true);
		mario.setName("Mario");
		Thread luidji = new Thread(new Kuvar(picerija));
		luidji.setDaemon(true);
		luidji.setName("Luidji");
		
		mario.start();
		luidji.start();
		
		Dostavljac[] dostavljaci = new Dostavljac[10];
		for (int i = 0; i < dostavljaci.length; i++) {
			dostavljaci[i] = new Dostavljac(picerija);
			dostavljaci[i].setName(String.format("Dostavljac_%d", i+1));
			dostavljaci[i].start();
		}
	}
}

class Dostavljac extends Thread {
	
	private Picerija picerija;
	private int zarada;
	private int baksis;
	
	public Dostavljac(Picerija picerija) {
		this.picerija = picerija;
		this.zarada = 0;
		this.baksis = 0;
	}

	@Override
	public void run() {
		for (int i = 0; i < 25; i++) {
			try {
				Porudzbina porudzbina = picerija.preuzmi();
				int zarada = porudzbina.dostavi();
				this.zarada += zarada;
				this.baksis += zarada - porudzbina.cena;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.printf("%s je dostavio pice za %d dinara i zaradio %d dinara baksisa.\n", getName(), zarada, baksis);
	}

}

class Kuvar implements Runnable {
	
	Picerija picerija;
	
	public Kuvar(Picerija picerija) {
		this.picerija = picerija;
	}

	@Override
	public void run() {
		while (true) {
			Porudzbina porudzbina = new Porudzbina();
			picerija.spremno(porudzbina);
			int sekundDve = (int) (2 * Math.random()) + 1;
			try {
				Thread.sleep(sekundDve * 1_0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

class Picerija {
	
	List<Porudzbina> porudzbine = new ArrayList<Porudzbina>();

	public synchronized Porudzbina preuzmi() throws InterruptedException {
		while (porudzbine.size() == 0) {
			System.out.println(Thread.currentThread().getName() + " ceka sledecu porudzbinu.");
			wait();
		}

		return porudzbine.remove(0);
	}

	public synchronized void spremno(Porudzbina porudzbina) {
		porudzbine.add(porudzbina);
		notifyAll();
	}
}

class Porudzbina {

	public final String id;
	public final int cena;
	public final long udaljenost;

	public Porudzbina() {
		System.out.printf("%s sprema porudzbinu...%n", Thread.currentThread().getName());
		this.id = String.format("#%H", System.identityHashCode(this));
		this.cena = (int) (40 + 160 * Math.random()) * 10;
		this.udaljenost = (long) (200 + 2800 * Math.random());
		System.out.printf("%s je spremio porudzbinu %s%n", Thread.currentThread().getName(), this.id);
	}

	public int dostavi() {
		System.out.printf("%s dostavlja porudzbinu %s%n", Thread.currentThread().getName(), this.id);
		boolean interrupted = Thread.interrupted();
		long timeout = 5 * udaljenost;
		long endTime = System.currentTimeMillis() + timeout;
		while ((timeout = endTime - System.currentTimeMillis()) > 0) {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
		System.out.printf("%s je dostavio porudzbinu %s%n", Thread.currentThread().getName(), this.id);
		return cena + 5 * (int) (0.04 * cena * Math.random());
	}
}
