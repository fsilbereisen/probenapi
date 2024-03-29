package de.proben.model;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import de.proben.util.Constants;

public class Probe {

	private static long idCounter = 1;
	private long probeId;
	private LocalDateTime zeitpunkt;
	private Integer messwert;
	private Ergebnis ergebnis;

	public Probe(LocalDateTime time) {
		probeId = idCounter++;
		this.zeitpunkt = time;
	}

	public Probe(LocalDateTime time, Integer messwert) {
		testMesswert(messwert);

		probeId = idCounter++;
		this.zeitpunkt = time;
		this.messwert = messwert;
		berechneErgebnis();
	}

	public Probe(long id, LocalDateTime time) {
		this.probeId = id;
		idCounter = id;
		this.zeitpunkt = time;
	}

	public Probe(long id, LocalDateTime time, Integer mw, Ergebnis erg) {
		this.probeId = id;
		idCounter = id;
		this.zeitpunkt = time;
		this.messwert = mw;
		this.ergebnis = erg;
	}

	@Override
	public String toString() {
//		Java12 Feature
		String formatKilosStr;
		if (messwert == null) {
			formatKilosStr = null;
		} else {
			NumberFormat formatKilos = NumberFormat.getCompactNumberInstance(
					new Locale("en", "US"), NumberFormat.Style.SHORT);
			formatKilos.setMaximumFractionDigits(1);
			formatKilosStr = formatKilos.format(messwert);
		}
		return String.format("[id=%3d,  zeit=%17s,  messwert=%5s,  ergebnis=%9s",
				probeId, zeitpunkt.truncatedTo(ChronoUnit.MINUTES), formatKilosStr,
				ergebnis + "]");
	}

	@Override
	public int hashCode() {
		return Long.valueOf(this.getId())
				.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Probe)) {
			return false;
		}

		Probe other = (Probe) object;
		if (this.getId() == other.getId()) {
			return true;
		} else {
			return false;
		}

	}

//	Getter
	public long getId() {
		return probeId;
	}

	public LocalDateTime getTime() {
		return zeitpunkt;
	}

	public Integer getMw() {
		return messwert;
	}

	public Ergebnis getErg() {
		return ergebnis;
	}

//	Setter
	public void setMesswert(Integer messwert) {
		testMesswert(messwert);
		this.messwert = messwert;
		berechneErgebnis();
	}

//	Enum
	public static enum Ergebnis {
		POSITIV, NEGATIV, FRAGLICH
	}

//	##################### Helper Meths ##################
	private void berechneErgebnis() {
		if (messwert > Constants.MW_UPPER_BOUND_FRAGLICH) {
			ergebnis = Ergebnis.POSITIV;
		} else if (messwert >= Constants.MW_LOWER_BOUND_FRAGLICH
				&& messwert <= Constants.MW_UPPER_BOUND_FRAGLICH) {
			ergebnis = Ergebnis.FRAGLICH;
		} else {
			ergebnis = Ergebnis.NEGATIV;
		}
	}

	private void testMesswert(Integer messwert) {
		if (messwert < Constants.MW_LOWER_BOUND
				|| messwert > Constants.MW_UPPER_BOUND) {
			throw new IllegalArgumentException("invalid messwert:" + messwert);
		}
	}

}
