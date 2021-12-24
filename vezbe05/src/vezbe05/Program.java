package vezbe05;

import os.simulation.Application;
import os.simulation.AutoCreate;
import os.simulation.Container;
import os.simulation.Item;
import os.simulation.Operation;
import os.simulation.Thread;

/*
 * U okviru maturske ekskurzije, za djake iz tri evropske drzave - Engleske,
 * Nemacke i Italije - je organizovan obilazak muzeja Louvre u Parizu. Sve tri
 * grupe djaka borave neko vreme ispred muzeja, nakon cega ulaze u muzej i uzi-
 * vaju u izlozenim umetnickim delima. Medjutim, u jednom momentu samo djaci
 * jedne drzave mogu boraviti u muzeju, jer bi se u suprotnom njihovi vodici
 * morali nadvikivati i niko nista ne bi cuo.
 * 
 * Sinhronizovati boravak djaka u muzeju tako da u jednom momentu samo jedna
 * grupa bude unutar muzeja. Svaki djak je predstavljen jednom niti cija klasa
 * odredjuje drzavu iz koje on dolazi.
 */

class Pristup {
	private enum Drzava {
		ENGLESKA, NEMACKA, ITALIJA
	}
	private Drzava zauzimac = null;
	private int brDjaka = 0;
	
	public synchronized void zauzmiEng() throws InterruptedException {
		while (brDjaka > 0 && zauzimac != Drzava.ENGLESKA) {
			wait();
		}
		
		zauzimac = Drzava.ENGLESKA;
		brDjaka ++;
	}
	
	public synchronized void oslobodiEng() {
		brDjaka --;
		if (brDjaka == 0)
			notifyAll();
	}
	
	public synchronized void zauzmiNem() throws InterruptedException {
		while (brDjaka > 0 && zauzimac != Drzava.NEMACKA) {
			wait();
		}
		
		zauzimac = Drzava.NEMACKA;
		brDjaka ++;
	}
	
	public synchronized void oslobodiNem() {
		brDjaka --;
		if (brDjaka == 0)
			notifyAll();
	}
	
	public synchronized void zauzmiIta() throws InterruptedException {
		while (brDjaka > 0 && zauzimac != Drzava.ITALIJA) {
			wait();
		}
		
		zauzimac = Drzava.ITALIJA;
		brDjaka ++;
	}
	
	public synchronized void oslobodiIta() {
		brDjaka --;
		if (brDjaka == 0)
			notifyAll();
	}
}

public class Program extends Application {
	
	Pristup pristup = new Pristup();

	@AutoCreate(8)
	protected class Englez extends Thread {

		@Override
		protected void step() {
			odmara();

			try {
				pristup.zauzmiEng();
				
				try {
					obilazi();
				} finally {
					pristup.oslobodiEng();
				}
			} catch (InterruptedException e) {}
		}
	}

	@AutoCreate(8)
	protected class Nemac extends Thread {

		@Override
		protected void step() {
			odmara();
			
			try {
				pristup.zauzmiNem();
				
				try {
					obilazi();
				} finally {
					pristup.oslobodiNem();
				}
			} catch (InterruptedException e) {}
		}
	}

	@AutoCreate(8)
	protected class Italijan extends Thread {

		@Override
		protected void step() {
			odmara();
			
			try {
				pristup.zauzmiIta();
				
				try {
					obilazi();
				} finally {
					pristup.oslobodiIta();
				}
			} catch (InterruptedException e) {}
		}
	}

	// ------------------- //
	//    Sistemski deo    //
	// ------------------- //
	// Ne dirati kod ispod //
	// ------------------- //

	protected final Container englezi   = box("Енглези").color(MAROON);
	protected final Container nemci     = box("Немци").color(ROYAL);
	protected final Container italijani = box("Италијани").color(ARMY);
	protected final Container muzej     = box("Музеј").color(NAVY);
	protected final Container main      = column(row(englezi, nemci, italijani), muzej);
	protected final Operation englez    = init().container(englezi).name("Енглез %d").color(RED);
	protected final Operation nemac     = init().container(nemci).name("Немац %d").color(PURPLE);
	protected final Operation italijan  = init().container(italijani).name("Италијан %d").color(GREEN);

	protected final Operation odmaranje = duration("7±2").text("Одмара").textAfter("Чека");
	protected final Operation obilazak  = duration("5±2").text("Обилази").container(muzej).textAfter("Обишао").update(this::azuriraj);

	protected void odmara() {
		odmaranje.performUninterruptibly();
	}

	protected void obilazi() {
		obilazak.performUninterruptibly();
	}

	protected void azuriraj(Item item) {
		long brE = muzej.stream(Englez.class).count();
		long brN = muzej.stream(Nemac.class).count();
		long brI = muzej.stream(Italijan.class).count();
		muzej.setText(String.format("%d / %d / %d", brE, brN, brI));
		if (brE == 0 && brN == 0 && brI == 0) {
			muzej.setColor(NAVY);
		} else if (brE > 0 && brN == 0 && brI == 0) {
			muzej.setColor(MAROON);
		} else if (brE == 0 && brN > 0 && brI == 0) {
			muzej.setColor(ROYAL);
		} else if (brE == 0 && brN == 0 && brI > 0) {
			muzej.setColor(ARMY);
		} else {
			muzej.setColor(CARBON);
		}
	}

	@Override
	protected void initialize() {
		azuriraj(null);
	}

	public static void main(String[] a) {
		launch("Музеј");
	}
}