package de.proben.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.proben.model.Probe;
import de.proben.model.Probe.Ergebnis;
import de.proben.util.Constants;

class ProbenVerwaltenDbTest {

	private static ProbenVerwalten db;

	private static LocalDateTime ldt = LocalDateTime
			.of(LocalDate.of(2001, 01, 01), LocalTime.of(0, 0));
	private static int mwNeg = Constants.MW_LOWER_BOUND_NEGATIV;
	private static int mwFrag = Constants.MW_LOWER_BOUND_FRAGLICH + 1;
	private static int mwPos = Constants.MW_UPPER_BOUND_POSITIV;
	private static int mwExc1 = Constants.MW_LOWER_BOUND - 1;
	private static int mwExc2 = Constants.MW_UPPER_BOUND + 1;

	private static Probe p1 = new Probe(ldt.plusDays(1), mwNeg);
	private static Probe p2 = new Probe(ldt, mwFrag);
	private static Probe p3 = new Probe(ldt.plusDays(2), mwPos);

	private static Probe p4 = new Probe(ldt.plusDays(7)); // ohne Messwert

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		db = ProbenVerwaltenFactory.getInstance(ProbenVerwaltenFactory.Instance.DB);
	}

	@BeforeEach
	void setUp() throws Exception {
		removeAllProben();
		db.addProbe(p1);
		db.addProbe(p2);
		db.addProbe(p3);
		db.addProbe(p4);
	}

	@Test
	void getAllRichtig() {
		List<Probe> proben = db.getAll();
		assertTrue(proben.contains(p1));
		assertTrue(proben.contains(p2));
		assertTrue(proben.contains(p3));
		assertTrue(proben.contains(p4));
		assertEquals(4, proben.size());
	}

	@Test
	void emptyProben() {
		removeAllProben();
		List<Probe> proben = db.getAll();
		assertTrue(proben.isEmpty());

		proben = db.filtered(Ergebnis.POSITIV);
		assertTrue(proben.isEmpty());

		proben = db.timeSorted(true);
		assertTrue(proben.isEmpty());
	}

	@Test
	void timeSortedRichtig() {
		boolean isAeltesteZuerst = true;
		List<Probe> proben = db.timeSorted(isAeltesteZuerst);
//	p1=ldt.plusDays(1), p2=ldt, p3=ldt.plusDays(2), p4=ldt.plusDays(7)
		assertEquals(p2, proben.get(0));
		assertEquals(p1, proben.get(1));
		assertEquals(p3, proben.get(2));
		assertEquals(p4, proben.get(3));

		isAeltesteZuerst = false;
		proben = db.timeSorted(isAeltesteZuerst);
		assertEquals(p4, proben.get(0));
		assertEquals(p3, proben.get(1));
		assertEquals(p1, proben.get(2));
		assertEquals(p2, proben.get(3));
	}

	@Test
	void filteredRichtig() {
//	p1=mwNeg, p2=mwFraglich, p3=mwPos
		List<Probe> proben = db.filtered(Ergebnis.NEGATIV);
		assertEquals(p1, proben.get(0));

		proben = db.filtered(Ergebnis.FRAGLICH);
		assertEquals(p2, proben.get(0));

		proben = db.filtered(Ergebnis.POSITIV);
		assertEquals(p3, proben.get(0));
	}

	@Test
	void removeProbeRichtig() {
		assertTrue(db.removeProbe(1)); // p1
		assertFalse(db.removeProbe(1)); // p1 schon entfernt
		assertFalse(db.getAll()
				.contains(p1));
	}

	@Test
	void addProbeRichtig() {
//	aus setUp()
		assertTrue(db.getAll()
				.contains(p1));

//	Probe p4 ohne Messwert
		assertTrue(db.getAll()
				.contains(p4));
	}

	@Test
	void addProbe_LocalDateTimeIntRichtig() {
		removeAllProben();
		db.addProbe(ldt, mwPos);
		assertTrue(ldt.equals(db.getAll()
				.get(0)
				.getTime()));
		assertTrue(mwPos == db.getAll()
				.get(0)
				.getMw());
	}

	@Test
	void addProbe_NurLocalDateTimeRichtig() {
		removeAllProben();
		db.addProbe(ldt);
		assertTrue(ldt.equals(db.getAll()
				.get(0)
				.getTime()));
		assertTrue(null == db.getAll()
				.get(0)
				.getMw());
		assertTrue(null == db.getAll()
				.get(0)
				.getErg());
	}

	@Test
	void addMesswertRichtig() {
		Integer newMesswert = mwFrag;
		Ergebnis newErgebnis = Ergebnis.FRAGLICH;

//	Messwert noch nicht vorhanden
		assertTrue(db.addMesswert(4, newMesswert));
		assertEquals(newMesswert, db.getAll()
				.get(3)
				.getMw());
		assertEquals(newErgebnis, db.getAll()
				.get(3)
				.getErg());

//	Probe nicht vorhanden
		assertFalse(db.addMesswert(0, newMesswert));
//	Messwert schon vorhanden
		assertFalse(db.addMesswert(1, newMesswert));
//p1 hat mwNeg
		assertFalse(newMesswert.equals(db.getAll()
				.get(0)
				.getMw()));
	}

	@Test
	void addProbeExc() {
		assertThrows(IllegalArgumentException.class,
				() -> db.addProbe(new Probe(ldt, mwExc1)));
		assertThrows(IllegalArgumentException.class,
				() -> db.addProbe(ldt, mwExc2));
	}

//	#######################################
//	######### Helper Meths #################
	private void removeAllProben() {
		List<Probe> list = db.getAll();
		list.stream()
				.mapToLong(p -> p.getId())
				.forEach(db::removeProbe);
	}
}
