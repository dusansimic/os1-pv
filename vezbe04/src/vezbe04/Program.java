package vezbe04;

interface Pripremac {
	void pripremi(Kuhinja skladiste) throws InterruptedException;
}

class Const {
	public static int SEC = 1000;
}

class PripremacHleba implements Pripremac {

	@Override
	public void pripremi(Kuhinja skladiste) throws InterruptedException {
		Thread.sleep(60 * Const.SEC);
		skladiste.dodajHleb();
	}
	
}

class PripremacPovrca implements Pripremac {

	@Override
	public void pripremi(Kuhinja skladiste) throws InterruptedException {
		Thread.sleep(90 * Const.SEC);
		skladiste.dodajPovrce();
	}
	
}

class PripremacPotaza implements Pripremac {

	@Override
	public void pripremi(Kuhinja skladiste) throws InterruptedException {
		Thread.sleep(4 * 60 * Const.SEC);
		skladiste.dodajPotaz();
	}
	
}

class PripremacTofe implements Pripremac {

	@Override
	public void pripremi(Kuhinja skladiste) throws InterruptedException {
		Thread.sleep(60 * Const.SEC);
		skladiste.dodajTofu();
	}
	
}

class Kuhinja {
	protected int tofa;
	protected double povrce;
	protected double potaz;
	protected int hleb;
	protected int dinara;
	
	private void stampajPoruku(String poruka) {
		System.out.printf("%4s %-30s Stanje u kuhinji: %5.1f %5.1f %2d %2d %5d%n",
				Thread.currentThread().getName(),
				poruka,
				povrce,
				potaz,
				hleb,
				tofa,
				dinara);
	}
	
	public synchronized void dodajTofu() {
		tofa++;
		notifyAll();
	}
	
	public synchronized void dodajHleb() {
		hleb += 6;
		notifyAll();
	}
	
	public synchronized void dodajPovrce() {
		povrce += 1.0;
		notifyAll();
	}
	
	public synchronized void dodajPotaz() {
		potaz += 10.0;
		notifyAll();
	}
	
	public synchronized int uzmiZaradu() {
		return dinara;
	}
	
	public synchronized void napraviSendvic() throws InterruptedException {
		while (hleb < 2 || tofa < 1 || povrce < 0.1) {
			stampajPoruku("ceka sastojke za sendvic");
			wait();
		}
		hleb -= 2;
		tofa--;
		povrce -= 0.1;
		dinara += 230;
		stampajPoruku("je napravila sendvic");
	}
	
	public synchronized void napraviPotaz() throws InterruptedException {
		while (potaz < 0.5 || hleb < 1) {
			stampajPoruku("ceka sastojke za potaz");
			wait();
		}
		potaz -= 0.5;
		hleb -= 1;
		dinara += 340;
		stampajPoruku("je napravila potaz");
	}
	
	public synchronized void napraviTofu() throws InterruptedException {
		while (tofa < 1 || povrce < 0.3) {
			stampajPoruku("ceka sastojke za tofu");
			wait();
		}
		tofa--;
		povrce -= 0.3;
		dinara += 520;
		stampajPoruku("je napravila tofu sa povrcem");
	}
}

class Konobar extends Thread {

	Kuhinja kuhinja;
	
	public Konobar(Kuhinja kuhinja) {
		this.kuhinja = kuhinja;
	}

	@Override
	public void run() {
		try {
			while (!interrupted()) {
				Thread.sleep(10 * Const.SEC);
				double porudzbina = Math.random();
				if (porudzbina < 0.4) {
					kuhinja.napraviPotaz();
				} else if (porudzbina < 0.7) {
					kuhinja.napraviSendvic();
				} else {
					kuhinja.napraviTofu();
				}
			}
		} catch (InterruptedException e) {}
	}
	
}

class Kuvar extends Thread {
	Pripremac pripremac;
	Kuhinja skladiste;
	
	public Kuvar(Pripremac pripremac, Kuhinja skladiste) {
		this.pripremac = pripremac;
		this.skladiste = skladiste;
	}

	@Override
	public void run() {
		try {
			while (!interrupted()) {
				pripremac.pripremi(skladiste);
			}
		} catch (InterruptedException e) {
			
		}
	}
	
}

public class Program {

	public static void main(String[] args) throws InterruptedException {
		Kuhinja kuhinja = new Kuhinja();
		
		Kuvar miki = new Kuvar(new PripremacPovrca(), kuhinja);
		miki.setName("Miki");
		Kuvar mica = new Kuvar(new PripremacPotaza(), kuhinja);
		mica.setName("Mica");
		Kuvar joki = new Kuvar(new PripremacHleba(), kuhinja);
		joki.setName("Joki");
		Kuvar vule = new Kuvar(new PripremacTofe(), kuhinja);
		vule.setName("Vule");
		Kuvar gule = new Kuvar(new PripremacTofe(), kuhinja);
		gule.setName("Gule");
		
		Konobar rada = new Konobar(kuhinja);
		rada.setName("Rada");
		Konobar dara = new Konobar(kuhinja);
		dara.setName("Dara");
		
		miki.start();
		mica.start();
		joki.start();
		vule.start();
		gule.start();
		
		rada.start();
		dara.start();
		
		Thread.sleep(5 * 60 * Const.SEC);
		
		miki.interrupt();
		mica.interrupt();
		joki.interrupt();
		vule.interrupt();
		gule.interrupt();
		
		rada.interrupt();
		dara.interrupt();
		
		miki.join();
		mica.join();
		joki.join();
		vule.join();
		gule.join();
		
		rada.join();
		dara.join();
		
		System.out.printf("Ukupna zarada: %d din%n", kuhinja.uzmiZaradu());
	}

}
