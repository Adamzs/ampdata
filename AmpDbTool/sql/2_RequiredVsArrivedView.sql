/*
 * Joins air and sea manifests with the movement requests to provide information for lateness calculations.
 * Keeps only those rows measured in STONS and arriving to unload cargo.
 */
CREATE OR REPLACE VIEW `RequiredVsArrivedView` AS SELECT DISTINCT 
  AirSeaManifest.CargoId AS CargoId,
  AirSeaManifest.RLN AS RLN,
  MoveRequests.CCC AS CCC,
  AirSeaManifest.FedTime AS FedTime,
  MoveRequests.RequiredTime AS RequiredTime,
  AirSeaManifest.ArrivalTime AS ArrivalTime,
  AirSeaManifest.CargoType AS CargoType,
  AirSeaManifest.NumItems AS NumItems,
  AirSeaManifest.Amount AS Amount,
  MoveRequests.Units AS Units,
  AirSeaManifest.OnloadGeoloc AS OnloadGeoloc,
  AirSeaManifest.OffloadGeoloc AS OffloadGeoloc,
  MoveRequests.RequestedMode AS RequestedMode
FROM AirSeaManifest
  INNER JOIN MoveRequests ON (AirSeaManifest.RLN = MoveRequests.RLN)
  WHERE Units = 'STONS' AND ToPurpose = 'UNLOAD'
  ORDER BY CargoID ASC, ArrivalTime ASC, RequiredTime ASC;