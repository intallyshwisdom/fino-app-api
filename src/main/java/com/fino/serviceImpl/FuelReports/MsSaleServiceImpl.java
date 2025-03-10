package com.fino.serviceImpl.FuelReports;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fino.configuration.FuelReportConfig.MsSaleInitialData;
import com.fino.dto.FuelReports.MsSaleDto;
import com.fino.entity.FuelReports.PetrolTankOne;
import com.fino.exception.BadRequest;
import com.fino.exception.InternalServerError;
import com.fino.exception.NotFoundException;
import com.fino.helpers.AppConstants;
import com.fino.repository.FuelReportsRepository.PetrolTankOneRepository;
import com.fino.service.FuelReports.MsSaleService;
import com.fino.utils.FuelReportUpdateUtil;
import com.fino.utils.FuelReportUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MsSaleServiceImpl implements MsSaleService {

	@Autowired
	private PetrolTankOneRepository petrolTankOneRepository;

	@Autowired
	private FuelReportUtils fuelReportUtils;

	@Autowired
	private MsSaleInitialData msSaleInitialData;

	@Autowired
	private FuelReportUpdateUtil fuelReportUpdateUtil;

	@Override
	public Map<Object, Object> insertMsSaleDetails(MsSaleDto msSaleDto) {
		Map<Object, Object> msSaleResponseMap = new HashMap<>();
		List<PetrolTankOne> msSaleList = this.petrolTankOneRepository.findAll();

		if (msSaleList.isEmpty()) {
			try {
				var msSaleResponse = this.petrolTankOneRepository.save(this.fuelReportUtils
						.msSaleDetailsIfNoDataAvailable(msSaleDto, msSaleInitialData.getMssaleinitialvalue()));
				if (msSaleResponse != null) {
					msSaleResponseMap.put(AppConstants.statusCode, AppConstants.ok);
					msSaleResponseMap.put(AppConstants.status, AppConstants.success);
					msSaleResponseMap.put(AppConstants.statusMessage, AppConstants.dataSubmitedsuccessfully);
				}
			} catch (Exception e) {
				throw new BadRequest(e.getLocalizedMessage());
			}
		} else {

			try {
				var previouseDayMsSale = Collections.max(msSaleList,
						Comparator.comparing(petrol -> petrol.getMsSaleDate()));
				log.info("previous day data:: " + previouseDayMsSale.getMsSaleDate());

				var msSaleResponseIfDataAvailable = this.petrolTankOneRepository.save(
						this.fuelReportUtils.msSaleDetailsIfPreviousDayDataAvailable(msSaleDto, previouseDayMsSale));
				if (msSaleResponseIfDataAvailable != null) {
					msSaleResponseMap.put(AppConstants.statusCode, AppConstants.ok);
					msSaleResponseMap.put(AppConstants.status, AppConstants.success);
					msSaleResponseMap.put(AppConstants.statusMessage, AppConstants.dataSubmitedsuccessfully);
				}
			} catch (Exception e) {
				throw new BadRequest(e.getLocalizedMessage());
			}
		}
		return msSaleResponseMap;
	}

	@Override
	public Map<Object, Object> deleteMsSaleDetails(Long msSaleId) {
		Map<Object, Object> msSaleResponseMap = new HashMap<>();
		try {
			if (this.petrolTankOneRepository.findById(msSaleId).isPresent()) {
				this.petrolTankOneRepository.deleteById(msSaleId);
				msSaleResponseMap.put(AppConstants.statusCode, AppConstants.ok);
				msSaleResponseMap.put(AppConstants.status, AppConstants.success);
				msSaleResponseMap.put(AppConstants.statusMessage, AppConstants.dataDeletedSuccesFully);
			} else {
				throw new NotFoundException(AppConstants.noRecordFound + msSaleId);
			}
			return msSaleResponseMap;

		} catch (Exception e) {
			throw new InternalServerError(e.getMessage());
		}
	}

	@Override
	public Map<Object, Object> updateMsSaleDetails(Long msSaleId, MsSaleDto msSaleDto) {

		Map<Object, Object> msSaleResponseMap = new HashMap<>();
		var msSaleRecord = this.petrolTankOneRepository.findById(msSaleId);
		var previousDayRecordOfMsSale = this.petrolTankOneRepository
				.findByMsSaleDate(msSaleDto.getMsSaleDate().minusDays(1));

		if (msSaleRecord.isPresent()) {

			if (previousDayRecordOfMsSale.isEmpty()) {
				try {

					var msSaleIfPrevDayUnavilable = this.fuelReportUtils.msSaleDetailsIfNoDataAvailable(msSaleDto,
							msSaleInitialData.getMssaleinitialvalue());
					this.petrolTankOneRepository.save(
							this.fuelReportUpdateUtil.getUpdatedMssale(msSaleRecord.get(), msSaleIfPrevDayUnavilable));

					msSaleResponseMap.put(AppConstants.statusCode, AppConstants.ok);
					msSaleResponseMap.put(AppConstants.status, AppConstants.success);
					msSaleResponseMap.put(AppConstants.statusMessage,
							AppConstants.recordUpdatedSuccessFully + msSaleId);

				} catch (Exception e) {
					throw new BadRequest(e.getMessage());
				}
			}

			else {

				try {
					var updatedMsSale = this.fuelReportUtils.msSaleDetailsIfPreviousDayDataAvailable(msSaleDto,
							previousDayRecordOfMsSale.get());

					this.petrolTankOneRepository
							.save(this.fuelReportUpdateUtil.getUpdatedMssale(msSaleRecord.get(), updatedMsSale));
					msSaleResponseMap.put(AppConstants.statusCode, AppConstants.ok);
					msSaleResponseMap.put(AppConstants.status, AppConstants.success);
					msSaleResponseMap.put(AppConstants.statusMessage,
							AppConstants.recordUpdatedSuccessFully + msSaleId);

				} catch (Exception e) {
					throw new BadRequest(e.getMessage());
				}
			}

		} else {
			throw new NotFoundException(AppConstants.noRecordFound + msSaleId);
		}

		return msSaleResponseMap;
	}

	@Override
	public Map<Object, Object> getAllMsSaleDetails() {
		try {
			Map<Object, Object> msSaleResponseMap = new HashMap<>();
			msSaleResponseMap.put(AppConstants.statusCode, AppConstants.ok);
			msSaleResponseMap.put(AppConstants.status, AppConstants.success);
			msSaleResponseMap.put(AppConstants.statusMessage, AppConstants.dataFetchedSuccesfully);
			msSaleResponseMap.put(AppConstants.response, this.petrolTankOneRepository.findAll());
			return msSaleResponseMap;

		} catch (Exception e) {
			throw new InternalServerError(e.getMessage());
		}
	}

	Predicate<PetrolTankOne> msSaleOfPreviousDay = (petrol) -> {
		Calendar calender = Calendar.getInstance();
		calender.add(Calendar.DATE, -1);
		return petrol.getMsSaleDate().isEqual(
				LocalDateTime.ofInstant(calender.toInstant(), calender.getTimeZone().toZoneId()).toLocalDate());
	};

}
