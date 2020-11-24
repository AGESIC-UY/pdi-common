package uy.gub.agesic.pdi.common.utiles;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtil {

	public LocalDate currentDate() {
		return LocalDate.now();
	}

	public LocalDateTime currentDateTime() {
		return LocalDateTime.now();
	}

	public String format(LocalDate date, String mask) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(mask);
		return date.format(formatter);
	}

	public String format(LocalDateTime date, String mask) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(mask);
		return date.format(formatter);
	}

}
