import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/* Implementirati klasu 'Karta' sa osobinama 'boja' i 'rang' koje predstavljaju
 * standardne osobine karata klasicnog spila od 52+2 karte.
 * 
 * Potrebno je predstaviti sledece boje: pik, karo, herc i tref, dok su dozvo-
 * ljene vrednosti za rang poredjane po velicini: brojevi od 2 do 10, zandar,
 * kraljica, kralj i kec. Takodje je potrebno predstaviti i dva dzokera, jedan
 * u boji, jedan ne.
 * 
 * Implementirati klasu 'Spil' ciji konstruktor kreira nov spil koji sadrzi sve
 * 54 razlicite karte. Takodje, implementirati sledece operacije:
 * 
 *   int velicina()            - vraca broj karata trenutno u spilu
 *   Karta uzmiOdGore()        - ukljanja gornju kartu i vraca je kao rezultat
 *   Karta uzmiOdDole()        - ukljanja donju kartu i vraca je kao rezultat
 *   Karta uzmiIzSredine()     - ukljanja nasumicno izabranu kartu i vraca je
 *   void staviGore(Karta)     - dodaje kartu na vrh spila
 *   void staviDole(Karta)     - dodaje kartu na dno spila
 *   void staviUSredinu(Karta) - dodaje kartu na nasumicno izabrao mesto u spilu
 *   void promesaj()           - nasumicno rasporedjuje karte trenutno u spilu
 * 
 * Napisati program koji implementira sledecu igru za 12 igraca. Igraci redom
 * vuku po jednu kartu sa vrha spila i okrecu je. Program ispisuje koji igrac
 * je izvukao koju kartu. Pobednik je onaj igrac (ili igraci) cija je karta
 * najjaca, pri cemu se ne gleda boja karte a dzokeri su jaci od svih ostalih
 * karata. Ako je bilo vise pobednika igra se ponavlja samo sa pobednicima dok
 * ne ostane samo jedan. Program ispisuje ime konacnog pobednika.
 * 
 * Unapred smisliti imena za igrace, kreirati jedan spil i promesati ga pre
 * igre. Pretpostaviti da u toku igre nece nestati karata u spilu.
 */

class Karta {
	
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

	public Rang getRang() {
		return rang;
	}

	@Override
	public String toString() {
		return rang == Rang.DZOKER ? "[" + boja + "]" : "[" + boja + ", " + rang + "]";
	}
	
}

class Spil {
	
	private LinkedList<Karta> karte;
	
	public Spil() {
		karte = new LinkedList<>();
		
		for (Karta.Boja boja : Arrays.copyOf(Karta.Boja.values(), 4)) {
			for (Karta.Rang rang : Arrays.copyOf(Karta.Rang.values(), 13)) {
				karte.add(new Karta(boja, rang));
			}
		}
		
		karte.add(new Karta(Karta.Boja.U_BOJI, Karta.Rang.DZOKER));
		karte.add(new Karta(Karta.Boja.BEZBOJAN, Karta.Rang.DZOKER));
	}
	
	public int velicina() {
		return karte.size();
	}
	
	public Karta uzmiOdGore() {
		return karte.pollFirst();
	}
	
	public Karta uzmiOdDole() {
		return karte.pollLast();
	}
	
	public Karta uzmiIzSredine() {
		return karte.get(karte.size() / 2);
	}
	
	public void staviGore(Karta karta) {
		karte.addFirst(karta);
	}
	
	public void staviDole(Karta karta) {
		karte.addLast(karta);
	}
	
	public void staviUSredinu(Karta karta) {
		karte.add(karte.size() / 2, karta);
	}
	
	public void promesaj() {
		Collections.shuffle(karte);
	}
	
	@Override
	public String toString() {
		String out = "";
		for (Karta karta : karte) {
			out += karta.toString() + " ";
		}
		return out;
	}
	
}

class Igrac {
	
	private String ime;
	private Karta karta;
	
	public Igrac(String ime) {
		this.ime = ime;
	}
	
	public static List<Igrac> napravi(int n) {
		LinkedList<Igrac> igraci = new LinkedList<>();
				
		for (int i = 0; i < n; i++) {
			igraci.add(new Igrac(String.format("Igrac_%d", i+1)));
		}
		
		return igraci;
	}
	
	public void vuci(Spil spil) {
		karta = spil.uzmiOdGore();
	}
	
	public Karta getKarta() {
		return karta;
	}

	@Override
	public String toString() {
		return "Igrac [ime=" + ime + ", karta=" + karta + "]";
	}
	
}

public class Program {
	
	public static void main(String[] args) {
		Spil spil = new Spil();
		spil.promesaj();
		
		List<Igrac> igraci = Igrac.napravi(12); 
		
		boolean hasWinner = false;
		
		do {
			System.out.println("Rezultati izvlacenja:");

			igraci.get(igraci.size() - 1).vuci(spil);
			System.out.println(igraci.get(igraci.size() - 1));
			Karta.Rang max = igraci.get(igraci.size() - 1).getKarta().getRang();

			for (int i = igraci.size() - 2; i >= 0; i--) {
				igraci.get(i).vuci(spil);
				System.out.println(igraci.get(i));

				if (igraci.get(i).getKarta().getRang().compareTo(max) > 0) {
					max = igraci.get(i).getKarta().getRang();

					for (int j = igraci.size() - 1; j > i; j--) {
						igraci.remove(j);
					}
				} else if (igraci.get(i).getKarta().getRang().compareTo(max) < 0) {
					igraci.remove(i);
				}
			}
			System.out.println();
			
			if (igraci.size() == 1) {
				System.out.print("Pobednik: ");
				System.out.println(igraci.get(0));
				hasWinner = true;
			}
		} while (!hasWinner);
	}
	
}
