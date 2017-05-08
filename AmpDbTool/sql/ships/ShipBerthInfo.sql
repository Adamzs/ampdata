CREATE OR REPLACE VIEW ShipBerthInfo AS SELECT 
    shipBerthTimes.ShipName,
    shipBerthTimes.Fleet,
    shipBerthTimes.Terminal,
    shipBerthTimes.BerthName,
    shipCargoUtilization.ShipID,
    shipCargoUtilization.DepartureDay
    FROM shipBerthTimes, shipCargoUtilization
    WHERE shipBerthTimes.ShipName = shipCargoUtilization.ShipName;