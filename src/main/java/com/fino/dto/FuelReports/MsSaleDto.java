package com.fino.dto.FuelReports;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MsSaleDto {
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate MsSaleDate;
	private Double inwardOfMSSale;
	private Double dipStockOfMSSaleInLtrs;
	private Double dipStockOfMSSaleInCentimeters;
	private Double testing;
	private Double density;
	private Double waterDip;
	private String remarks;
	private Double closingMeterOfMSSaleNozzleOne;
	private Double closingMeterOfMSSaleNozzleTwo;

}
