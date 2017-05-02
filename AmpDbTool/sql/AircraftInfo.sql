CREATE OR REPLACE VIEW Amp.AircraftInfo AS
   SELECT Amp.FleetAircraftType.*,Amp.CrewDuty.*
   FROM Amp.FleetAircraftType,Amp.CrewDuty
   WHERE Amp.FleetAircraftType.StagedCrewType=Amp.CrewDuty.Name;
