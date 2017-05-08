/*
 * Calculates Days Late and DayTons Late by FedDay and FedWeek.
 */
CREATE OR REPLACE VIEW `DayTonsLate` AS 
SELECT DISTINCT 
  CargoId,
  RLN,
  RequiredTime,
  ArrivalTime,
  FLOOR(FedTime/7) as FedWeek,
  FLOOR(RequiredTime) as RequiredDay,
  FLOOR(ArrivalTime) as ArrivalDay,
  Amount,
  Units,
  OnloadGeoloc,
  OffloadGeoloc,
  (FLOOR(ArrivalTime-RequiredTime)) as DaysLate,
  (FLOOR(ArrivalTime-RequiredTime)*Amount) as DayTonsLate
FROM `RequiredVsArrivedView`;